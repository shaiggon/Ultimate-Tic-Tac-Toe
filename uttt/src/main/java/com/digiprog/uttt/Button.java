package com.digiprog.uttt;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by lauri on 21.3.2014.
 */
public class Button {
    private static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec2 vPosition;" +
            //"attribute vec2 vTexPos"+
            "void main() {" +
            "  gl_Position = uMVPMatrix*vec4(vPosition, 0.0, 1.0);" +
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
    private float color[] = {1.0f, 0.1f, 0.5f, 1.0f};

    private static final int COORDS_IN_VERT = 2;
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
    static float texCoords[] = {
        //upper right triangle starting from up and right
        1.0f, 1.0f,
        .0f, 1.0f,
        1.0f, .0f,
        //down left starting from down left
        .0f, .0f,
        1.0f, .0f,
        .0f, 1.0f
    };
    private static final int vCount = coords.length/COORDS_IN_VERT;

    //call this before any rendering starts!
    //TODO: add texturing
    public static void init() {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        ByteBuffer tb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tb.order(ByteOrder.nativeOrder());
        texBuffer = tb.asFloatBuffer();
        texBuffer.put(texCoords);
        texBuffer.position(0);

        int vShader = GameRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fShader = GameRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);
        GLES20.glLinkProgram(mProgram);
    }

    //TODO: add texturing
    public void draw(float[] mvp) {
        GLES20.glUseProgram(mProgram);
        int posHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GameRenderer.checkGlError("glGetAttribLocation");
        GLES20.glEnableVertexAttribArray(posHandle);
        GLES20.glVertexAttribPointer(posHandle, COORDS_IN_VERT, GLES20.GL_FLOAT, false, 4*COORDS_IN_VERT, vertexBuffer);

        int colHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GameRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniform4fv(colHandle, 1, color, 0);

        int mvpHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GameRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0);
        GameRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
        GLES20.glDisableVertexAttribArray(posHandle);
    }

    public void buttonHover() {
        color[0] = 0.0f; color[1] = 0.0f; color[2] = 1.0f;
    }
    public void buttonIdle() {
        color[0] = 1.0f; color[1] = 0.1f; color[2] = 0.5f;
    }
}
