package com.gomdev.shader.occlusionQuery;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.gomdev.gles.*;
import com.gomdev.gles.GLESConfig.Version;
import com.gomdev.gles.GLESObject.PrimitiveMode;
import com.gomdev.gles.GLESObject.RenderType;
import com.gomdev.shader.EffectRenderer;
import com.gomdev.shader.EffectUtils;
import com.gomdev.shader.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class OQRenderer extends EffectRenderer implements Renderer {
    private static final String CLASS = "OQRenderer";
    private static final String TAG = OQConfig.TAG + " " + CLASS;
    private static final boolean DEBUG = OQConfig.DEBUG;

    private final static int NUM_OF_INSTANCE = 1000;
    private final static int NUM_OF_ELEMENT = 3;
    private static final int MAX_NUM_OF_FRAMES = 5;

    private Version mVersion;
    private GLESObject mObject;
    private GLESShader mShader;

    private boolean mIsTouchDown = false;

    private float mDownX = 0f;
    private float mDownY = 0f;

    private float mMoveX = 0f;
    private float mMoveY = 0f;

    private float mScreenRatio = 0f;

    private float[] mTrans = new float[NUM_OF_ELEMENT * NUM_OF_INSTANCE];
    private float[] mScales = new float[NUM_OF_ELEMENT * NUM_OF_INSTANCE];
    private Random mRandom = new Random();

    private int mQueryID[] = new int[NUM_OF_INSTANCE];
    private int mNumOfPass[] = new int[NUM_OF_INSTANCE];

    private int mNumOfFrames = 0;
    private boolean mIsVisibilityChecked = false;

    public OQRenderer(Context context) {
        super(context);

        mVersion = GLESContext.getInstance().getVersion();

        mObject = GLESSceneManager.createObject();
        mObject.setTransform(new GLESTransform());
        mObject.setPrimitiveMode(PrimitiveMode.TRIANGLES);

        mObject.setRenderType(RenderType.DRAW_ELEMENTS);

        GLESGLState state = new GLESGLState();
        state.setCullFaceState(true);
        state.setCullFace(GLES20.GL_BACK);
        state.setDepthState(true);
        state.setDepthFunc(GLES20.GL_LEQUAL);
        mObject.setGLState(state);

        mRenderer.addObject(mObject);
    }

    public void destroy() {
        mObject = null;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (DEBUG)
            Log.d(TAG, "onDrawFrame()");

        super.updateFPS();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mVersion == Version.GLES_30) {
            if (MAX_NUM_OF_FRAMES > mNumOfFrames) {
                mNumOfFrames++;
                GLES30.glGenQueries(NUM_OF_INSTANCE, mQueryID, 0);

                for (int i = 0; i < NUM_OF_INSTANCE; i++) {
                    GLES30.glBeginQuery(GLES30.GL_ANY_SAMPLES_PASSED,
                            mQueryID[i]);

                    drawObjects(i);

                    GLES30.glEndQuery(GLES30.GL_ANY_SAMPLES_PASSED);
                }
            } else {
                for (int i = 0; i < NUM_OF_INSTANCE; i++) {
                    if (mIsVisibilityChecked == false) {
                        GLES30.glGetQueryObjectuiv(mQueryID[i],
                                GLES30.GL_QUERY_RESULT, mNumOfPass, i);

                        if (mNumOfPass[i] == 0) {
                            Log.d(TAG, "onDrawFrame() i=" + i + " skipped");
                        }
                    }

                    if (mNumOfPass[i] > 0) {
                        drawObjects(i);
                    }
                }

                mIsVisibilityChecked = true;
            }
        } else {
            for (int i = 0; i < NUM_OF_INSTANCE; i++) {
                drawObjects(i);
            }
        }
    }

    private void drawObjects(int index) {
        GLESTransform transform = mObject.getTransform();
        transform.setIdentity();
        transform.translate(
                mTrans[index * NUM_OF_ELEMENT + 0],
                mTrans[index * NUM_OF_ELEMENT + 1],
                mTrans[index * NUM_OF_ELEMENT + 2]);
        transform.rotate(mMoveX * 0.2f, 0f, 1f, 0f);
        transform.rotate(mMoveY * 0.2f, 1f, 0f, 0f);
        transform.scale(
                mScales[index * NUM_OF_ELEMENT + 0],
                mScales[index * NUM_OF_ELEMENT + 1],
                mScales[index * NUM_OF_ELEMENT + 2]);
        mRenderer.updateObjects();

        mRenderer.drawObjects();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (DEBUG)
            Log.d(TAG, "onSurfaceChanged()");

        mScreenRatio = (float) width / height;

        mRenderer.reset();

        GLES20.glViewport(0, 0, width, height);

        GLESCamera camera = setupCamera(width, height);

        mObject.setCamera(camera);

        makeTransformInfo();

        GLESVertexInfo vertexInfo = GLESMeshUtils.createCube(0.1f,
                false, true, false);
        mObject.setVertexInfo(vertexInfo, true, true);
    }

    private GLESCamera setupCamera(int width, int height) {
        GLESCamera camera = new GLESCamera();

        float fovy = 30f;
        float eyeZ = 1f / (float) Math.tan(Math.toRadians(fovy * 0.5));

        camera.setLookAt(0f, 0f, eyeZ, 0f, 0f, 0f, 0f, 1f, 0f);

        camera.setFrustum(fovy, mScreenRatio, 1f, 400f);

        String uniformName = GLESShaderConstant.UNIFORM_PROJ_MATRIX;
        int handle = mShader.getUniformLocation(uniformName);
        GLES20.glUniformMatrix4fv(handle, 1, false,
                camera.getProjectionMatrix(), 0);

        uniformName = GLESShaderConstant.UNIFORM_VIEW_MATRIX;
        handle = mShader.getUniformLocation(uniformName);
        GLES20.glUniformMatrix4fv(handle, 1, false, camera.getViewMatrix(), 0);

        return camera;
    }

    private void makeTransformInfo() {
        float scale = 0f;
        for (int i = 0; i < NUM_OF_INSTANCE; i++) {
            mTrans[i * NUM_OF_ELEMENT + 0] = (mRandom.nextFloat() - 0.5f)
                    * mScreenRatio * 2f;
            mTrans[i * NUM_OF_ELEMENT + 1] = (mRandom.nextFloat() - 0.5f) * 2f;
            mTrans[i * NUM_OF_ELEMENT + 2] = (mRandom.nextFloat() - 0.5f) * 4f;

            scale = (mRandom.nextFloat() + 0.1f) * 6f;
            mScales[i * NUM_OF_ELEMENT + 0] = scale;
            mScales[i * NUM_OF_ELEMENT + 1] = scale;
            mScales[i * NUM_OF_ELEMENT + 2] = scale;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.7f, 0.7f, 0.7f, 0.0f);

        createShader();

        mObject.setShader(mShader);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.gradient);
        GLESTexture texture = new GLESTexture(bitmap);
        mObject.setTexture(texture);

        mIsVisibilityChecked = false;
        mNumOfFrames = 0;
    }

    public void touchDown(float x, float y) {
        if (DEBUG)
            Log.d(TAG, "touchDown() x=" + x + " y=" + y);

        mIsTouchDown = true;

        mDownX = x;
        mDownY = y;

        mView.requestRender();
    }

    public void touchUp(float x, float y) {
        if (mIsTouchDown == false) {
            return;
        }

        mView.requestRender();

        mIsTouchDown = false;
    }

    public void touchMove(float x, float y) {
        if (mIsTouchDown == false) {
            return;
        }

        mMoveX = x - mDownX;
        mMoveY = y - mDownY;

        mView.requestRender();
    }

    public void touchCancel(float x, float y) {
    }

    private boolean createShader() {
        Log.d(TAG, "createShader()");
        mShader = new GLESShader(mContext);

        String vsSource = EffectUtils.getShaderSource(mContext, 0);
        String fsSource = EffectUtils.getShaderSource(mContext, 1);

        mShader.setShaderSource(vsSource, fsSource);
        if (mShader.load() == false) {
            mHandler.sendEmptyMessage(EffectRenderer.COMPILE_OR_LINK_ERROR);
            return false;
        }

        if (mVersion == Version.GLES_20) {
            String attribName = GLESShaderConstant.ATTRIB_POSITION;
            mShader.setVertexAttribIndex(attribName);

            attribName = GLESShaderConstant.ATTRIB_TEXCOORD;
            mShader.setTexCoordAttribIndex(attribName);
        }

        return true;
    }
}