package com.digiprog.uttt;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by lauri on 23.3.2014.
 */

//TODO: make it render the board (now just copies the button)
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

    //TODO: coder colors -> designer colorz!
    private float color[] = {0.0f, 0.1f, 0.1f, 1.0f};
    private float subBoardColor[] = {1.0f, 1.0f, 0.8f, 1.0f};

    private static final int COORDS_IN_VERT = 2;

    private float mvp[] = new float[16];

    public boolean zoomOn = false;
    private float zoom = 0.0f;

    static float coords[] = {
            //upper right triangle starting from up and right
            1.0f, 1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            //down left starting from down left
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f
    };

    //static float coords[] = new float[(squareCoords.length/2)*4];

    private static final int vCount = coords.length/COORDS_IN_VERT;

    //call this before any rendering starts!
    public static void init() {

        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        int vShader = GameRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fShader = GameRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);
        GLES20.glLinkProgram(mProgram);
    }

    //TODO: add zooming
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

        for(int i = 0; i < 16; i++) {
            mvp[i] = mvp_[i];
        }

        float time = (float)SystemClock.uptimeMillis()/100.0f;
        if(zoomOn) {
            zoom = zoom*0.95f + 2.0f*0.05f;
        } else {
            zoom = zoom*0.95f; //+0.0f*0.04f;
        }
        //zoom = (float)Math.sin(time) + 2.0f;
        Matrix.translateM(mvp, 0, zoom, zoom, 0.0f);
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
                renderSubBoard(submvp, mvpHandle, colHandle);
            }
        }
        GLES20.glDisableVertexAttribArray(posHandle);
    }

    void renderSubBoard(float[] mvp, int mvpHandle, int colHandle) {

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
            }
        }
    }
}
