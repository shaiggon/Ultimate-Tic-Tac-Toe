package com.digiprog.uttt;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by lauri on 23.3.2014.
 * Renders also the circle and the cross :D
 */

//everything would be better if everything used vertex array objects
public class BoardRend {

    public Logic logic;

    private static final String vertexShaderCode =
            "uniform mat4 mvp;" +
                    "attribute vec2 pos;" +
                    "void main() {" +
                    "  gl_Position = mvp*vec4(pos, 0.0, 1.0);" +
                    "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private static int mProgram;

    private static FloatBuffer vertexBuffer;
    //private static FloatBuffer texBuffer;

    private static FloatBuffer cVertBuf;

    //TODO: coder colors -> designer colorz!
    private float color[] = {1.0f, 1.0f, 0.8f, 1.0f};
    private float subBoardColor[] = {0.0f, 0.1f, 0.1f, 1.0f};
    private float hilightSubBoardColor[] = {0.1f, 0.3f, 0.3f, 1.0f};
    private float circleColor[] = {1.0f, 0.5f, 0.2f, 1.0f};
    private float crossColor[] = {0.2f, 0.5f, 1.0f, 1.0f};

    private static final int COORDS_IN_VERT = 2;

    private float mvp[] = new float[16];
    private float crossMvp[] = new float[16];
    private static float cMVP1[] = new float[16];
    private static float cMVP2[] = new float[16];

    public boolean zoomOn = false;
    public int nextZoomX = 1;
    public int nextZoomY = 1;
    private float smoothNextX = 0.0f;
    private float smoothNextY = 0.0f;
    private float zoom = 0.0f;
    private float zoomSpeed = 0.08f;

    private static float coords[] = {
            //upper right triangle starting from up and right
            1.0f, 1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            //down left starting from down left
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f
    };

    private static final int cResolution = 40;
    private static final float ciRadius = 0.7f;

    private static float circleCoords[] = new float[6*2*cResolution];

    private static final int vCount = coords.length/COORDS_IN_VERT;
    private static final int cvCount = circleCoords.length/COORDS_IN_VERT;

    //call this before any rendering starts!
    public static void init() {

        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        initCircle();
        initRCrMatrices();

        int vShader = GameRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fShader = GameRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);
        GLES20.glLinkProgram(mProgram);
    }

    public static void initCircle() {
        for(int i = 0; i < cResolution; i++) {
            int indx = i*2*6;
            float rd = ciRadius;

            float r1 = 2.0f * (float)Math.PI * ((float)(i+1)/(float)cResolution);
            float r2 = 2.0f * (float)Math.PI * ((float)i/(float)cResolution);

            float r1y = (float)Math.sin(r1);
            float r1x = (float)Math.cos(r1);
            float r2y = (float)Math.sin(r2);
            float r2x = (float)Math.cos(r2);

            circleCoords[indx] = r1x;
            circleCoords[indx+1] = r1y;
            circleCoords[indx+2] = rd*r1x;
            circleCoords[indx+3] = rd*r1y;
            circleCoords[indx+4] = r2x;
            circleCoords[indx+5] = r2y;

            circleCoords[indx+6] = rd*r2x;
            circleCoords[indx+7] = rd*r2y;
            circleCoords[indx+8] = r2x;
            circleCoords[indx+9] = r2y;
            circleCoords[indx+10] = rd*r1x;
            circleCoords[indx+11] = rd*r1y;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(circleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        cVertBuf = bb.asFloatBuffer();
        cVertBuf.put(circleCoords);
        cVertBuf.position(0);
    }


    public void draw(float[] mvp_) {
        GLES20.glUseProgram(mProgram);
        int posHandle = GLES20.glGetAttribLocation(mProgram, "pos");
        GameRenderer.checkGlError("glGetAttribLocation");
        GLES20.glEnableVertexAttribArray(posHandle);
        GLES20.glVertexAttribPointer(posHandle, COORDS_IN_VERT, GLES20.GL_FLOAT, false, 4*COORDS_IN_VERT, vertexBuffer);

        int colHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GameRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniform4fv(colHandle, 1, color, 0);

        int mvpHandle = GLES20.glGetUniformLocation(mProgram, "mvp");
        GameRenderer.checkGlError("glGetUniformLocation");

        System.arraycopy(mvp_, 0, mvp, 0, 16);

        float zspd = zoomSpeed;
        float izspd = 1.0f-zspd;

        if(zoomOn) {
            zoom = zoom*izspd + 2.0f*zspd;
        } else {
            zoom = zoom*izspd; //+0.0f*0.04f;
        }

        smoothNextX = smoothNextX*izspd + (1.0f-(float)nextZoomX)*zspd;
        smoothNextY = smoothNextY*izspd + (1.0f-(float)nextZoomY)*zspd;

        Matrix.translateM(mvp, 0, zoom*smoothNextX, zoom*smoothNextY, 0.0f);
        Matrix.scaleM(mvp, 0, zoom+1.0f, zoom+1.0f, zoom);

        float submvp[] = new float[16];

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {

                Matrix.setIdentityM(submvp, 0);
                Matrix.translateM(submvp, 0, (((float) j) - 1.0f) / 1.5f, (((float) i) - 1.0f) / 1.5f, 0.0f);
                Matrix.scaleM(submvp, 0, 1.0f / 3.1f, 1.0f / 3.1f, 0.0f);
                Matrix.multiplyMM(submvp, 0, mvp, 0, submvp, 0);

                renderSubBoard(submvp, mvpHandle, colHandle, posHandle, i, j);

                if(logic.subBoardWon(j, i) == Logic.CIRCLE) {
                    initRCi(colHandle, posHandle);
                    renderCircle(submvp, mvpHandle, colHandle, posHandle);
                    initBasic(colHandle, posHandle);
                } else if(logic.subBoardWon(j, i) == Logic.CROSS) {
                    initRCr(colHandle, posHandle);
                    renderCross(submvp, mvpHandle, colHandle);
                    initBasic(colHandle, posHandle);
                }
            }
        }

        //Draws the winner
        if(logic.game == Logic.CIRCLE) {
            initRCi(colHandle, posHandle);
            renderCircle(mvp, mvpHandle, colHandle, posHandle);
            initBasic(colHandle, posHandle);
        } else if(logic.game == Logic.CROSS) {
            initRCr(colHandle, posHandle);
            renderCross(mvp, mvpHandle, colHandle);
            initBasic(colHandle, posHandle);
        }

        GLES20.glDisableVertexAttribArray(posHandle);
    }

    boolean isCircle(int x, int y, int i, int j) {
        return logic.getGameMark(x, y, j, i) == Logic.CIRCLE;
    }

    boolean isCross(int x, int y, int i, int j) {
        return logic.getGameMark(x, y, j, i) == Logic.CROSS;
    }

    void renderSubBoard(float[] mvp, int mvpHandle, int colHandle, int posHandle, int y, int x) {

        float submvp[] = new float[16];
        if(logic.currentCol == y && logic.currentRow == x) {
            GLES20.glUniform4fv(colHandle, 1, hilightSubBoardColor, 0);
        } else {
            GLES20.glUniform4fv(colHandle, 1, subBoardColor, 0);
        }

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {

                Matrix.setIdentityM(submvp, 0);
                Matrix.translateM(submvp, 0, (((float) j) - 1.0f) / 1.5f, (((float) i) - 1.0f) / 1.5f, 0.0f);
                Matrix.scaleM(submvp, 0, 1.0f / 3.1f, 1.0f / 3.1f, 0.0f);
                Matrix.multiplyMM(submvp, 0, mvp, 0, submvp, 0);

                GLES20.glUniformMatrix4fv(mvpHandle, 1, false, submvp, 0);

                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

                if(isCircle(x, y, i, j)) {
                    initRCi(colHandle, posHandle);
                    renderCircle(submvp, mvpHandle, colHandle, posHandle);
                    initBasic(colHandle, posHandle);

                    if(logic.currentCol == y && logic.currentRow == x) {
                        GLES20.glUniform4fv(colHandle, 1, hilightSubBoardColor, 0);
                    } else {
                        GLES20.glUniform4fv(colHandle, 1, subBoardColor, 0);
                    }
                }
                if(isCross(x, y, i, j)) {
                    initRCr(colHandle, posHandle);
                    renderCross(submvp, mvpHandle, colHandle);
                    initBasic(colHandle, posHandle);

                    if(logic.currentCol == y && logic.currentRow == x) {
                        GLES20.glUniform4fv(colHandle, 1, hilightSubBoardColor, 0);
                    } else {
                        GLES20.glUniform4fv(colHandle, 1, subBoardColor, 0);
                    }
                }
            }
        }
    }

    //initializes the rendering of circles
    void initRCi(int colHandle, int posHandle) {
        GLES20.glVertexAttribPointer(posHandle, COORDS_IN_VERT, GLES20.GL_FLOAT, false, 4*COORDS_IN_VERT, cVertBuf);

        GLES20.glUniform4fv(colHandle, 1, circleColor, 0);
    }

    void initBasic(int colHandle, int posHandle) {
        GLES20.glUniform4fv(colHandle, 1, subBoardColor, 0);
        GLES20.glVertexAttribPointer(posHandle, COORDS_IN_VERT, GLES20.GL_FLOAT, false, 4*COORDS_IN_VERT, vertexBuffer);
    }

    void renderCircle(float[] mvp, int mvpHandle, int colHandle, int posHandle) {
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, cvCount);
    }

    //initializes the rendering of crosses
    void initRCr(int colHandle, int posHandle) {
        GLES20.glUniform4fv(colHandle, 1, crossColor, 0);
    }

    static void initRCrMatrices() {
        Matrix.setIdentityM(cMVP1, 0);
        Matrix.rotateM(cMVP1, 0, 45.0f, 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(cMVP1, 0, 0.2f, 1.0f, 1.0f);

        Matrix.setIdentityM(cMVP2, 0);
        Matrix.rotateM(cMVP2, 0, -45.0f, 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(cMVP2, 0, 0.2f, 1.0f, 1.0f);
    }

    void renderCross(float[] mvp, int mvpHandle, int colHandle) {
        Matrix.multiplyMM(crossMvp, 0, mvp, 0, cMVP1, 0);

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, crossMvp, 0);
        GLES20.glUniform4fv(colHandle, 1, crossColor, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        Matrix.multiplyMM(crossMvp, 0, mvp, 0, cMVP2, 0);

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, crossMvp, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        GLES20.glUniform4fv(colHandle, 1, subBoardColor, 0);
    }

    void renderCrossOutside(float[] mvp_) {
        GLES20.glUseProgram(mProgram);
        int posHandle = GLES20.glGetAttribLocation(mProgram, "pos");
        GLES20.glEnableVertexAttribArray(posHandle);
        GLES20.glVertexAttribPointer(posHandle, COORDS_IN_VERT, GLES20.GL_FLOAT, false, 4*COORDS_IN_VERT, vertexBuffer);

        int colHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        int mvpHandle = GLES20.glGetUniformLocation(mProgram, "mvp");

        renderCross(mvp_, mvpHandle, colHandle);
    }

    void renderCircleOutside(float[] mvp_) {
        GLES20.glUseProgram(mProgram);
        int posHandle = GLES20.glGetAttribLocation(mProgram, "pos");
        GLES20.glEnableVertexAttribArray(posHandle);

        int colHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        initRCi(colHandle, posHandle);

        int mvpHandle = GLES20.glGetUniformLocation(mProgram, "mvp");

        renderCircle(mvp_, mvpHandle, colHandle, posHandle);
    }

    void renderZoom(float[] mvp_, boolean zOn) {
        GLES20.glUseProgram(mProgram);
        int posHandle = GLES20.glGetAttribLocation(mProgram, "pos");
        GLES20.glEnableVertexAttribArray(posHandle);
        GLES20.glVertexAttribPointer(posHandle, COORDS_IN_VERT, GLES20.GL_FLOAT, false, 4*COORDS_IN_VERT, vertexBuffer);

        int colHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(colHandle, 1, color, 0);

        int mvpHandle = GLES20.glGetUniformLocation(mProgram, "mvp");

        Matrix.setIdentityM(crossMvp, 0);
        Matrix.scaleM(crossMvp, 0, 0.5f*0.6f, 0.5f*0.6f, 1.0f);

        if(!zOn) {
            Matrix.scaleM(crossMvp, 0, 0.2f, 1.0f, 1.0f);
            Matrix.multiplyMM(crossMvp, 0, mvp_, 0, crossMvp, 0);

            GLES20.glUniformMatrix4fv(mvpHandle, 1, false, crossMvp, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
        } else {
            Matrix.multiplyMM(crossMvp, 0, mvp_, 0, crossMvp, 0);
        }

        float zMult = 1.0f;
        if(!zOn)
            zMult = 1.0f/0.2f;

        Matrix.scaleM(crossMvp, 0, zMult, 1.0f, 1.0f);
        Matrix.scaleM(crossMvp, 0, 1.0f, 0.2f, 1.0f);

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, crossMvp, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        Matrix.scaleM(crossMvp, 0, 1.0f/0.5f, 1.0f/0.5f, 1.0f);

        GLES20.glVertexAttribPointer(posHandle, COORDS_IN_VERT, GLES20.GL_FLOAT, false, 4*COORDS_IN_VERT, cVertBuf);
        Matrix.scaleM(crossMvp, 0, 1.0f, 1.0f/0.2f, 1.0f);

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, crossMvp, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, cvCount);
    }
}
