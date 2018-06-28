package com.avaboy.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by iengpho on 6/28/18.
 */

public class Stage extends GLSurfaceView {

    int screenWidth;
    int screenHeight;
    int w;
    int h;

    public Stage(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        setRenderer(new MyRenderer());
    }


    private final class MyRenderer implements GLSurfaceView.Renderer {

        public final void onDrawFrame(GL10 gl) {

        }

        @Override
        public final void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glClearColor(0, 0, 0, 1.0f);
            if(width > height) {
                h = 600;
                w = width * h / height;
            } else {
                w = 600;
                h = height * w / width;
            }
            screenWidth = width;
            screenHeight = height;
            gl.glViewport(0, 0, screenWidth, screenHeight);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glOrthof(0, w, h, 0, -1, 1);
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
        }

        @Override
        public final void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // Set up alpha blending
            gl.glEnable(GL10.GL_ALPHA_TEST);
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
            // We are in 2D, so no need depth
            gl.glDisable(GL10.GL_DEPTH_TEST);
            // Enable vertex arrays (we'll use them to draw primitives).
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        }

    }

}
