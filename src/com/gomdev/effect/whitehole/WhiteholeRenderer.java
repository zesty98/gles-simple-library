package com.gomdev.effect.whitehole;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.gomdev.effect.test.R;
import com.gomdev.gles.*;
import com.gomdev.gles.GLESConfig.MeshType;
import com.gomdev.gles.GLESConfig.ObjectType;
import com.gomdev.gles.GLESConfig.ProjectionType;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;

public class WhiteholeRenderer implements GLESRenderer {
    private static final String CLASS = "WhiteholeRenderer";
    private static final String TAG = WhiteholeConfig.TAG + " " + CLASS;
    private static final boolean DEBUG = WhiteholeConfig.DEBUG;
    private static final boolean DEBUG_PERF = WhiteholeConfig.DEBUG_PERF;

    private Context mContext;
    private GLESSurfaceView mView;

    private WhiteholeObject mWhiteholeObject;
    private GLESTexture mWhiteholeTexture;

    private int mWidth;
    private int mHeight;
    private GLESShader mShaderWhitehole;

    private boolean mIsTouchDown = false;
    private float mDownX = 0f;
    private float mDownY = 0f;

    private GLESAnimatorCallback mCallback = null;

    ArrayList<GLESAnimator> mAnimatorList = new ArrayList<GLESAnimator>();
    private GLESAnimator mAnimator = null;
    private float mRadius = 0f;
    private float mMinRingSize = 0f;
    private float mMaxRingSize = 0.0f;
    private float mBoundaryRingSize = 0f;

    public WhiteholeRenderer(Context context) {
        mContext = context;

        mWhiteholeObject = new WhiteholeObject(context, true, false,
                ObjectType.TRANPARENT);
    }

    public void destroy() {
        mWhiteholeObject = null;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (DEBUG)
            Log.d(TAG, "onDrawFrame()");

        if (DEBUG_PERF)
            GLESUtils.checkFPS();

        int count = 0;
        boolean needToRequestRender = false;

        for (GLESAnimator animator : mAnimatorList) {
            if (animator != null) {
                needToRequestRender = animator.doAnimation();

                if (needToRequestRender == true) {
                    count++;
                }
            }
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mWhiteholeObject.drawObject();

        if (count > 0) {
            mView.requestRender();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        if (DEBUG)
            Log.d(TAG, "onSurfaceChanged()");

        GLES20.glViewport(0, 0, width, height);

        mWhiteholeObject.setupSpace(ProjectionType.FRUSTUM, mWidth, mHeight);
        mWhiteholeObject.createMesh(MeshType.PLANE_MESH, 0f, 0f, mWidth,
                mHeight, WhiteholeConfig.MESH_RESOLUTION);
        mWhiteholeObject.show();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);

        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        createShader();

        createAnimation();

        mWhiteholeObject.setShader(mShaderWhitehole);
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.galaxy);
        // Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
        // R.drawable.moon);
        mWhiteholeTexture = new GLESTexture(bitmap, GLES20.GL_MIRRORED_REPEAT,
                true);
        mWhiteholeObject.setTexture(mWhiteholeTexture, false);

        mMinRingSize = GLESUtils.getPixelFromDpi(mContext,
                WhiteholeConfig.MIN_RING_SIZE);
        mMaxRingSize = (float) Math.hypot(GLESUtils.getWidthPixels(mContext),
                GLESUtils.getHeightPixels(mContext));
        mBoundaryRingSize = GLESUtils.getPixelFromDpi(mContext,
                WhiteholeConfig.BOUNDARY_RING_SIZE);
    }

    @Override
    public void initRenderer() {

    }

    @Override
    public void setSurfaceView(GLESSurfaceView surfaceView) {
        if (surfaceView == null) {
            Log.e(TAG, "setSurfaceView() surfaceView is null");
            return;
        }
        mView = surfaceView;
    }

    @Override
    public void touchDown(float x, float y, float userData) {
        if (DEBUG)
            Log.d(TAG, "touchDown() x=" + x + " y=" + y);

        mIsTouchDown = true;

        for (GLESAnimator animator : mAnimatorList) {
            if (animator != null) {
                animator.cancel();
            }
        }

        mDownX = x;
        mDownY = y;

        mWhiteholeObject.setPosition(x, y);
        mWhiteholeObject.setRadius(mMinRingSize);

        mView.requestRender();
    }

    @Override
    public void touchUp(float x, float y, float userData) {
        if (mIsTouchDown == false) {
            return;
        }

        mRadius = (float) Math.hypot((x - mDownX), (y - mDownY)) + mMinRingSize;

        if (mRadius > mBoundaryRingSize) {
            mAnimator.setDuration(0L, 500L);
            mAnimator.start(mRadius, mMaxRingSize);
        } else {
            mAnimator.setDuration(0L, 500L);
            mAnimator.start(mRadius, 0f);
        }

        mView.requestRender();

        mIsTouchDown = false;
    }

    @Override
    public void touchMove(float x, float y, float userData) {
        if (mIsTouchDown == false) {
            return;
        }

        mRadius = (float) Math.hypot((x - mDownX), (y - mDownY)) + mMinRingSize;
        mWhiteholeObject.setRadius(mRadius);

        mView.requestRender();
    }

    @Override
    public void touchCancel(float x, float y) {

    }

    @Override
    public void showAll() {
        if (mWhiteholeObject != null) {
            mWhiteholeObject.show();
        }
    }

    @Override
    public void hideAll() {
        if (mWhiteholeObject != null) {
            mWhiteholeObject.hide();
        }
    }

    public void setImage(Bitmap bitmap) {
        if (mWhiteholeObject != null) {
            mWhiteholeObject.setImage(bitmap);
        }
    }

    private void createShader() {
        mShaderWhitehole = new GLESShader(mContext);
        mShaderWhitehole.setShadersFromResource(R.raw.whitehole_vs,
                R.raw.whitehole_fs);
        mShaderWhitehole.load();
    }

    private void createAnimation() {
        mCallback = new GLESAnimatorCallback() {

            @Override
            public void onAnimation(GLESVector currentValue) {
                mRadius = currentValue.mX;
                mWhiteholeObject.setRadius(mRadius);
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onCancel() {
                // TODO Auto-generated method stub

            }
        };

        mAnimator = new GLESAnimator(mCallback);
        mAnimatorList.add(mAnimator);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }
}
