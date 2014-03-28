package com.digiprog.uttt;

/**
 * Created by lauri on 20.3.2014.
 */
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
//import android.os.SystemClock;
import android.util.Log;

//TODO: maek dis yo dawg
public class GameRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "GameRenderer";
    public float col[];
    public boolean up;

    private float mvp[];

    public float pointerX;
    public float pointerY;
    public boolean pointerDown;
    public boolean pointerPressed;


    public Button but;
    public BoardRend brend;
    private float but1Mat[];
    private float but1Trans[];
    private float boardMat[];

    //tells where is the line between button and game board
    private float mapButtonRelation = 0.8f;
    private boolean zoomOn;

    public GameRenderer(/*TODO: give this the game*/) {
        col = new float[4];
        for(int i = 0; i < 4; i++) {
            col[i] = 1.0f;
        }
        up = false;

        but = new Button();
        brend = new BoardRend();

        mvp = new float[16];
        but1Mat = new float[16];
        but1Trans = new float[16];
        boardMat = new float[16];
        Matrix.setIdentityM(mvp, 0);
        Matrix.setIdentityM(but1Mat, 0);
        Matrix.setIdentityM(but1Trans, 0);
        pointerDown = false;
        pointerPressed = false;
        zoomOn = false;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(col[0], col[1], col[2], col[3]);
        Button.init();
        BoardRend.init();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if(up) {
            if (col[2] < 1.0f)
                col[2] += 0.01f;
        } else {
            if(col[2] > 0.0f)
                col[2] -= 0.01f;
        }

        GLES20.glClearColor(col[0], col[1], col[2], col[3]);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(but1Mat, 0);
        Matrix.setIdentityM(but1Trans, 0);

        Matrix.scaleM(but1Mat, 0, (1.0f-mapButtonRelation), 0.5f, 1.0f);
        Matrix.translateM(but1Trans, 0, mapButtonRelation, 0.5f, 0.0f);
        Matrix.multiplyMM(but1Mat, 0, but1Trans, 0, but1Mat, 0);

        Matrix.setIdentityM(boardMat, 0);
        Matrix.translateM(boardMat, 0, mapButtonRelation-1.0f, 0.0f, 0.0f);
        Matrix.scaleM(boardMat, 0, mapButtonRelation, 1.0f, 1.0f);

        if(pointerDown && pointerX > mapButtonRelation && pointerY < 0.5f) {
            but.buttonHover();
        } else if(pointerDown && pointerX < mapButtonRelation) {
            //TODO: what happens when touched in the board
        } else {
            but.buttonIdle();
        }

        if(pointerPressed) {

            pointerPressed = false;

            if(pointerX > mapButtonRelation && pointerY < 0.5f) {
                zoomOn = !zoomOn;
                brend.zoomOn = zoomOn;
            }
        }

        brend.draw(boardMat);
        but.draw(but1Mat);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mapButtonRelation = (float)height/(float)width;
    }

    public static int loadShader(int shaderType, String shaderCode){
        int ret = GLES20.glCreateShader(shaderType);

        GLES20.glShaderSource(ret, shaderCode);
        GLES20.glCompileShader(ret);
        Log.e(TAG, GLES20.glGetShaderInfoLog(ret));
        return ret;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
