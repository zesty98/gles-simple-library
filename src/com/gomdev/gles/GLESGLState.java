package com.gomdev.gles;

import android.opengl.GLES20;

public class GLESGLState {
    class BlendFunc {
        int mSrcColor;
        int mDstColor;
        int mSrcAlpha;
        int mDstAlpha;
    }

    private boolean mBlendState = false;
    private BlendFunc mBlendFunc = null;

    private boolean mDepthState = false;
    private int mDepthFunc = GLES20.GL_LEQUAL;

    private boolean mCullFaceState = false;
    private int mCullFace = GLES20.GL_BACK;

    public GLESGLState() {

    }

    public void setBlendState(boolean blendState) {
        mBlendState = blendState;
    }

    public boolean getBlendState() {
        return mBlendState;
    }

    public void setBlendFunc(int sfactor, int dfactor) {
        if (mBlendFunc == null) {
            mBlendFunc = new BlendFunc();
        }

        mBlendFunc.mSrcAlpha = sfactor;
        mBlendFunc.mSrcColor = sfactor;
        mBlendFunc.mDstAlpha = dfactor;
        mBlendFunc.mDstColor = dfactor;
    }

    public void setBlendFuncSeperate(int srcColor, int dstColor,
            int srcAlpha, int dstAlpha) {
        if (mBlendFunc == null) {
            mBlendFunc = new BlendFunc();
        }

        mBlendFunc.mSrcColor = srcColor;
        mBlendFunc.mDstColor = dstColor;
        mBlendFunc.mSrcAlpha = srcAlpha;
        mBlendFunc.mDstAlpha = dstAlpha;
    }

    public BlendFunc getBlendFunc() {
        return mBlendFunc;
    }

    public void setDepthState(boolean depthState) {
        mDepthState = depthState;
    }

    public boolean getDepthState() {
        return mDepthState;
    }

    public void setDepthFunc(int depthFunc) {
        mDepthFunc = depthFunc;
    }

    public int getDepthFunc() {
        return mDepthFunc;
    }

    public void setCullFaceState(boolean cullFaceState) {
        mCullFaceState = cullFaceState;
    }

    public boolean getCullFaceState() {
        return mCullFaceState;
    }

    public void setCullFace(int cullFace) {
        mCullFace = cullFace;
    }

    public int getCullFace() {
        return mCullFace;
    }
}
