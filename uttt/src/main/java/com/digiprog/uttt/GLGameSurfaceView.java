package com.digiprog.uttt;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
/**
 * Created by lauri on 20.3.2014.
 */
public class GLGameSurfaceView extends GLSurfaceView{
    private final GameRenderer mRenderer;
    private float z = 0.0f;
    public GLGameSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        mRenderer = new GameRenderer();
        setRenderer(mRenderer);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    //TODO: check for input
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        mRenderer.pointerX = x/(float)getWidth();
        mRenderer.pointerY = y/(float)getHeight();

        switch(e.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                mRenderer.up = false;
                mRenderer.col[0] = x/(float)getWidth()*0.1f+mRenderer.col[0]*0.9f;
                mRenderer.col[1] = y/(float)getHeight()*0.1f+mRenderer.col[1]*0.9f;
                mRenderer.pointerDown = true;
                //if(z <= 1.0f) z += 0.01f;
                //mRenderer.col[2] = z;
                break;
            case MotionEvent.ACTION_UP:
                mRenderer.up = true;
                mRenderer.pointerPressed = true; //TODO: assign to false when detected
                mRenderer.pointerDown = false;
                break;
        }
        return true;
    }
}
