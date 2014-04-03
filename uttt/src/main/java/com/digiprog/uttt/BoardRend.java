package com.digiprog.uttt;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by lauri on 23.3.2014.
 * Renders also the circle and the cross :D
 */

public class BoardRend {
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
    private static FloatBuffer texBuffer;

    private static FloatBuffer cVertBuf;

    //TODO: coder colors -> designer colorz!
    private float color[] = {0.0f, 0.1f, 0.1f, 1.0f};
    private float subBoardColor[] = {1.0f, 1.0f, 0.8f, 1.0f};
    private float circleColor[] = {1.0f, 0.3f, 0.0f, 1.0f};
    private float crossColor[] = {0.0f, 0.3f, 1.0f, 1.0f};

    private static final int COORDS_IN_VERT = 2;

    private float mvp[] = new float[16];
    private float crossMvp[] = new float[16];

    public boolean zoomOn = false;
    public int nextZoomX = 1;
    public int nextZoomY = 1;
    private float smoothNextX = 0.0f;
    private float smoothNextY = 0.0f;
    private float zoom = 0.0f;
    private float zoomSpeed = 0.07f;

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

    //static float coords[] = new float[(squareCoords.length/2)*4];

    private static final int vCount = coords.length/COORDS_IN_VERT;
    private static final int cvCount = circleCoords.length/COORDS_IN_VERT;

    private float time = 0.0f;

    //call this before any rendering starts!
    public static void init() {

        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        initCircle();

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

        //time = (float)SystemClock.uptimeMillis()/100.0f;

        float zspd = zoomSpeed;
        float izspd = 1.0f-zspd;

        if(zoomOn) {
            zoom = zoom*izspd + 2.0f*zspd;
        } else {
            zoom = zoom*izspd; //+0.0f*0.04f;
        }

        smoothNextX = smoothNextX*izspd + (1.0f-(float)nextZoomX)*zspd;
        smoothNextY = smoothNextY*izspd + (1.0f-(float)nextZoomY)*zspd;
        //zoom = (float)Math.sin(time) + 2.0f;
        Matrix.translateM(mvp, 0, zoom*smoothNextX, zoom*smoothNextY, 0.0f);
        Matrix.scaleM(mvp, 0, zoom+1.0f, zoom+1.0f, zoom);

        float submvp[] = new float[16];

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {

                GLES20.glUniform4fv(colHandle, 1, color, 0);

                Matrix.setIdentityM(submvp, 0);
                Matrix.translateM(submvp, 0, (((float) j) - 1.0f) / 1.5f, (((float) i) - 1.0f) / 1.5f, 0.0f);
                Matrix.scaleM(submvp, 0, 1.0f / 3.1f, 1.0f / 3.1f, 0.0f);
                Matrix.multiplyMM(submvp, 0, mvp, 0, submvp, 0);

                GLES20.glUniformMatrix4fv(mvpHandle, 1, false, submvp, 0);

                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
                renderSubBoard(submvp, mvpHandle, colHandle, posHandle);
            }
        }
        GLES20.glDisableVertexAttribArray(posHandle);
    }

    void renderSubBoard(float[] mvp, int mvpHandle, int colHandle, int posHandle) {

        float submvp[] = new float[16];
        GLES20.glUniform4fv(colHandle, 1, subBoardColor, 0);

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {

                Matrix.setIdentityM(submvp, 0);
                Matrix.translateM(submvp, 0, (((float) j) - 1.0f) / 1.5f, (((float) i) - 1.0f) / 1.5f, 0.0f);
                Matrix.scaleM(submvp, 0, 1.0f / 3.1f, 1.0f / 3.1f, 0.0f);
                Matrix.multiplyMM(submvp, 0, mvp, 0, submvp, 0);

                GLES20.glUniformMatrix4fv(mvpHandle, 1, false, submvp, 0);

                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

                if(j%3 == 0)
                    renderCircle(submvp, mvpHandle, colHandle, posHandle);
                else if(j%3 == 1)
                    renderCross(submvp, mvpHandle, colHandle);
            }
        }
    }

    void renderCircle(float[] mvp, int mvpHandle, int colHandle, int posHandle) {
        GLES20.glVertexAttribPointer(posHandle, COORDS_IN_VERT, GLES20.GL_FLOAT, false, 4*COORDS_IN_VERT, cVertBuf);

        GLES20.glUniform4fv(colHandle, 1, circleColor, 0);
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, cvCount);

        GLES20.glUniform4fv(colHandle, 1, subBoardColor, 0);
        GLES20.glVertexAttribPointer(posHandle, COORDS_IN_VERT, GLES20.GL_FLOAT, false, 4*COORDS_IN_VERT, vertexBuffer);
    }

    void renderCross(float[] mvp, int mvpHandle, int colHandle) {

        Matrix.setIdentityM(crossMvp, 0);
        Matrix.rotateM(crossMvp, 0, 45.0f, 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(crossMvp, 0, 0.2f, 1.0f, 1.0f);
        Matrix.multiplyMM(crossMvp, 0, mvp, 0, crossMvp, 0);

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, crossMvp, 0);
        GLES20.glUniform4fv(colHandle, 1, crossColor, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        Matrix.setIdentityM(crossMvp, 0);
        Matrix.rotateM(crossMvp, 0, -45.0f, 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(crossMvp, 0, 0.2f, 1.0f, 1.0f);
        Matrix.multiplyMM(crossMvp, 0, mvp, 0, crossMvp, 0);

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, crossMvp, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        GLES20.glUniform4fv(colHandle, 1, subBoardColor, 0);
    }
}
