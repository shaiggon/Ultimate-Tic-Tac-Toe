package com.digiprog.uttt;

/**
 * Created by lauri on 20.3.2014.
 */
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

//TODO: maek dis yo dawg
public class GameRenderer implements GLSurfaceView.Renderer{

    public Logic logic;

    private static final String TAG = "GameRenderer";
    public float col[];
    public boolean up;

    private float mvp[];

    public float pointerX;
    public float pointerY;
    public boolean pointerDown;
    public boolean pointerPressed;


    public Button but;
    public Button but2;
    public BoardRend brend;

    private float but1Mat[];
    private float but2Mat[];
    private float but1Trans[];
    private float boardMat[];

    private float winningZoom;
    private float winningTwist;

    //tells where is the line between button and game board
    private float mapButtonRelation = 0.8f;
    private boolean zoomOn;
    private double startOfWin;

    public GameRenderer(Logic logic) {
        col = new float[4];
        for(int i = 0; i < 4; i++) {
            col[i] = 1.0f;
        }
        up = false;

        but = new Button();
        but2 = new Button();
        brend = new BoardRend();

        but2.color[0] = 1.0f*0.95f;//{1.0f, 1.0f, 0.8f, 1.0f};
        but2.color[1] = 1.0f*0.95f;
        but2.color[2] = 0.8f*0.95f;

        this.logic = logic;
        brend.logic = logic;

        mvp = new float[16];
        but1Mat = new float[16];
        but2Mat = new float[16];
        but1Trans = new float[16];
        boardMat = new float[16];
        Matrix.setIdentityM(mvp, 0);
        Matrix.setIdentityM(but1Mat, 0);
        Matrix.setIdentityM(but1Trans, 0);
        pointerDown = false;
        pointerPressed = false;
        zoomOn = false;

        winningZoom = 1.0f;
        winningTwist = 0.0f;
        startOfWin = -1.0;
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
        //col[2] = (float)(Math.sin(SystemClock.uptimeMillis()/400.0)*0.3+0.6);


        GLES20.glClearColor(col[0], col[1], col[2], col[3]);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(but1Mat, 0);
        Matrix.setIdentityM(but1Trans, 0);
        Matrix.setIdentityM(but2Mat, 0);

        //BUTTON 1 MATRIX
        Matrix.scaleM(but1Mat, 0, (1.0f-mapButtonRelation), 0.5f, 1.0f);
        Matrix.translateM(but1Trans, 0, mapButtonRelation, 0.5f, 0.0f);
        Matrix.multiplyMM(but1Mat, 0, but1Trans, 0, but1Mat, 0);

        //BUTTON 2 MATRIX
        Matrix.translateM(but2Mat, 0, mapButtonRelation, -0.5f, 0.0f);
        Matrix.scaleM(but2Mat, 0, (1.0f-mapButtonRelation), 0.5f, 1.0f);

        //BOARD MATRIX
        Matrix.setIdentityM(boardMat, 0);
        Matrix.translateM(boardMat, 0, mapButtonRelation-1.0f, 0.0f, 0.0f);
        Matrix.scaleM(boardMat, 0, mapButtonRelation, 1.0f, 1.0f);


        // twist effect when game won
        if(logic.game != Logic.EMPTY) {
            if(startOfWin < 0.0)
                startOfWin = SystemClock.uptimeMillis()/170.0;
            Matrix.scaleM(boardMat, 0, winningZoom, winningZoom, 1.0f);
            Matrix.rotateM(boardMat, 0, winningTwist, 0.0f, 0.0f, 1.0f);
            winningTwist += 0.5f;
            winningZoom = ((float)Math.sin(SystemClock.uptimeMillis()/170.0-startOfWin)+1.2f)/1.2f;
        }

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

            if(pointerX < mapButtonRelation) {

                int chosenX = (int) (3.0f * pointerX / mapButtonRelation);
                int chosenY = (int) (3.0f * (1.0f - pointerY));

                if(logic.game == Logic.EMPTY) {

                    if (logic.canChooseSubBoardFreely() && !zoomOn) {
                        logic.chooseSubBoard(chosenX, chosenY);

                        brend.nextZoomX = logic.currentRow;
                        brend.nextZoomY = logic.currentCol;
                    } else if (zoomOn) {

                        logic.updateGame(chosenX, chosenY);
                        brend.nextZoomX = logic.currentRow;
                        brend.nextZoomY = logic.currentCol;

                        if (logic.canChooseSubBoardFreely()) {
                            zoomOn = false;
                            brend.zoomOn = zoomOn;
                        }

                        if(logic.game != Logic.EMPTY) {
                            zoomOn = false;
                            brend.zoomOn = zoomOn;
                            brend.nextZoomX = 1;
                            brend.nextZoomY = 1;
                        }
                    }
                } else {
                    zoomOn = false;
                    brend.zoomOn = zoomOn;
                    logic.init();
                    winningZoom = 1.0f;
                    winningTwist = 0.0f;
                }

            }
        }

        brend.draw(boardMat);
        but.draw(but1Mat);
        brend.renderZoom(but1Mat, zoomOn);
        but2.draw(but2Mat);

        Matrix.scaleM(but2Mat, 0, 0.5f/(mapButtonRelation), 1.0f, 1.0f);
        if(logic.getNextPlayer() == Logic.CIRCLE)
            brend.renderCircleOutside(but2Mat);
        else
            brend.renderCrossOutside(but2Mat);
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
