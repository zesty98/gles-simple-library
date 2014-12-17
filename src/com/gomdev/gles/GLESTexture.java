package com.gomdev.gles;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

public abstract class GLESTexture {
    static final String CLASS = "GLESTexture";
    static final String TAG = GLESConfig.TAG + "_" + CLASS;
    static final boolean DEBUG = GLESConfig.DEBUG;

    protected int mTextureID;
    protected int mWidth;
    protected int mHeight;

    protected int mTarget = GLES20.GL_TEXTURE_2D;
    protected int mWrapMode = GLES20.GL_CLAMP_TO_EDGE;// 33071;
    protected int mMinFilter = GLES20.GL_LINEAR;
    protected int mMagFilter = GLES20.GL_LINEAR;

    protected GLESTexture() {
    }

    public void load() {
        makeTexture();
    }

    public int getTarget() {
        return mTarget;
    }

    public void setWrapMode(int wrapMode) {
        mWrapMode = wrapMode;
    }

    public void setFilter(int minFilter, int magFilter) {
        mMinFilter = minFilter;
        mMagFilter = magFilter;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getTextureID() {
        if (!GLES20.glIsTexture(mTextureID)) {
            Log.e(TAG, "mTextureID is invalid");
            return -1;
        }
        return mTextureID;
    }

    public abstract void destroy();

    protected abstract void makeTexture();

    public abstract void makeSubTexture(int offsetX, int offsetY, Bitmap bitmap);

    public abstract void makeSubTexture(int offsetX, int offsetY, Bitmap[] bitmap);

    public abstract void changeTexture(Bitmap bitmap);

    public abstract void changeTexture(Bitmap[] bitmap);

    public static class Builder {
        private int mTarget = -1;
        private int mWidth = 0;
        private int mHeight = 0;
        private int mWrapMode = GLES20.GL_REPEAT;
        private int mMinFilter = GLES20.GL_LINEAR;
        private int mMagFilter = GLES20.GL_LINEAR;

        public Builder(int target, int width, int height) {
            mTarget = target;
            mWidth = width;
            mHeight = height;
        }

        public Builder setWrapMode(int wrapMode) {
            mWrapMode = wrapMode;
            return this;
        }

        public Builder setFilter(int minFilter, int magFilter) {
            mMinFilter = minFilter;
            mMagFilter = magFilter;
            return this;
        }

        public GLESTexture load() {
            GLESTexture texture = null;

            if (GLES20.GL_TEXTURE_CUBE_MAP == mTarget) {
                texture = new GLESTextureCubemap(mWidth, mHeight);
            } else {
                texture = new GLESTexture2D(mWidth, mHeight);
            }

            setTextureInfo(texture);

            return texture;
        }

        private void setTextureInfo(GLESTexture texture) {
            texture.setFilter(mMinFilter, mMagFilter);
            texture.setWrapMode(mWrapMode);
            texture.load();
        }

        public GLESTexture load(Bitmap[] bitmaps) {
            GLESTexture texture = null;

            if (GLES20.GL_TEXTURE_CUBE_MAP == mTarget) {
                texture = new GLESTextureCubemap(mWidth, mHeight, bitmaps);
            } else {
                throw new IllegalArgumentException(
                        "Should use load(Bitmap bitmap) or use GL_TEXTURE_CUBE_MAP as target");
            }

            setTextureInfo(texture);

            return texture;
        }

        public GLESTexture load(Bitmap bitmap) {
            GLESTexture texture = null;

            if (GLES20.GL_TEXTURE_2D == mTarget) {
                texture = new GLESTexture2D(mWidth, mHeight, bitmap);
            } else {
                throw new IllegalArgumentException(
                        "Should use load(Bitmap[] bitmaps) or use GL_TEXTURE_2D as target");
            }

            setTextureInfo(texture);

            return texture;
        }
    }

}