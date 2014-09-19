package com.gomdev.shader.instancedRendering;

import java.nio.FloatBuffer;
import java.util.Random;

import com.gomdev.gles.*;
import com.gomdev.gles.GLESConfig.Version;
import com.gomdev.gles.GLESObject.PrimitiveMode;
import com.gomdev.gles.GLESObject.RenderType;
import com.gomdev.shader.EffectRenderer;
import com.gomdev.shader.EffectUtils;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

public class IRRenderer extends EffectRenderer implements GLESRendererListener {
    private static final String CLASS = "IRRenderer";
    private static final String TAG = IRConfig.TAG + " " + CLASS;
    private static final boolean DEBUG = IRConfig.DEBUG;

    private final static int NUM_OF_INSTANCE = 1000;
    private final static int NUM_OF_ELEMENT = 3;

    private final static int USER_ATTRIB_LOCATION = 4;

    private GLESObject mObject;
    private GLESShader mShader;

    private Version mVersion;

    private boolean mIsTouchDown = false;

    private float mDownX = 0f;
    private float mDownY = 0f;

    private float mMoveX = 0f;
    private float mMoveY = 0f;

    private float mScreenRatio = 0f;

    private float[] mTrans = new float[NUM_OF_ELEMENT * NUM_OF_INSTANCE];
    private FloatBuffer mTransBuffer = null;
    private Random mRandom = new Random();
    private int mVBOID = -1;

    private int mNormalMatrixHandle = -1;
    private GLESVector4 mLightPos = new GLESVector4(1f, 1f, 1f, 0f);

    public IRRenderer(Context context) {
        super(context);

        mVersion = GLESContext.getInstance().getVersion();

        mObject = GLESSceneManager.createObject();
        mObject.setTransform(new GLESTransform());
        mObject.setPrimitiveMode(PrimitiveMode.TRIANGLES);

        if (mVersion == Version.GLES_20) {
            mObject.setRenderType(RenderType.DRAW_ELEMENTS);
        } else {
            mObject.setRenderType(RenderType.DRAW_ELEMENTS_INSTANCED);
            mObject.setNumOfInstance(NUM_OF_INSTANCE);
        }

        GLESGLState state = new GLESGLState();
        state.setCullFaceState(true);
        state.setCullFace(GLES20.GL_BACK);
        state.setDepthState(true);
        state.setDepthFunc(GLES20.GL_LEQUAL);
        mObject.setGLState(state);
        mObject.setListener(mObjListener);

        mRenderer.addObject(mObject);

        mRenderer.setListener(this);
    }

    public void destroy() {
        mObject = null;
    }

    @Override
    protected void onDrawFrame() {
        if (DEBUG)
            Log.d(TAG, "onDrawFrame()");

        super.updateFPS();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mVersion == Version.GLES_30) {
            GLESTransform transform = mObject.getTransform();
            transform.setIdentity();
            transform.rotate(mMoveX * 0.2f, 0f, 1f, 0f);
            transform.rotate(mMoveY * 0.2f, 1f, 0f, 0f);

            mRenderer.updateObjects();
            mRenderer.drawObjects();
        } else {
            for (int i = 0; i < NUM_OF_INSTANCE; i++) {
                GLESTransform transform = mObject.getTransform();
                transform.setIdentity();
                transform.translate(
                        mTrans[i * NUM_OF_ELEMENT + 0],
                        mTrans[i * NUM_OF_ELEMENT + 1],
                        mTrans[i * NUM_OF_ELEMENT + 2]);
                transform.rotate(mMoveX * 0.2f, 0f, 1f, 0f);
                transform.rotate(mMoveY * 0.2f, 1f, 0f, 0f);

                mRenderer.updateObjects();
                mRenderer.drawObjects();
            }
        }
    }

    @Override
    protected void onSurfaceChanged(int width, int height) {
        if (DEBUG)
            Log.d(TAG, "onSurfaceChanged()");

        mScreenRatio = (float) width / height;

        mRenderer.reset();

        GLES20.glViewport(0, 0, width, height);

        GLESCamera camera = setupCamera(width, height);

        mObject.setCamera(camera);

        makeTransBuffer();

        GLESVertexInfo vertexInfo = GLESMeshUtils.createCube(0.1f,
                true, false, true);
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

    private void makeTransBuffer() {
        for (int i = 0; i < NUM_OF_INSTANCE; i++) {
            mTrans[i * NUM_OF_ELEMENT + 0] = (mRandom.nextFloat() - 0.5f)
                    * mScreenRatio * 2f;
            mTrans[i * NUM_OF_ELEMENT + 1] = (mRandom.nextFloat() - 0.5f) * 2f;
            mTrans[i * NUM_OF_ELEMENT + 2] = (mRandom.nextFloat() - 0.5f);
        }

        mTransBuffer = GLESUtils.makeFloatBuffer(mTrans);
    }

    @Override
    protected void onSurfaceCreated() {
        GLES20.glClearColor(0.7f, 0.7f, 0.7f, 0.0f);

        mObject.setShader(mShader);

        int program = mShader.getProgram();
        mNormalMatrixHandle = GLES20.glGetUniformLocation(program,
                "uNormalMatrix");
        int location = GLES20.glGetUniformLocation(program, "uLightPos");
        GLES20.glUniform4f(location,
                mLightPos.mX,
                mLightPos.mY,
                mLightPos.mZ,
                mLightPos.mW);
    }

    @Override
    protected boolean createShader() {
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

            attribName = GLESShaderConstant.ATTRIB_NORMAL;
            mShader.setNormalAttribIndex(attribName);

            attribName = GLESShaderConstant.ATTRIB_COLOR;
            mShader.setColorAttribIndex(attribName);
        }

        return true;
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

    @Override
    public void setupVBO(GLESVertexInfo vertexInfo) {
        if (mVersion == Version.GLES_20) {
            return;
        }

        int[] vboIDs = new int[1];
        GLES30.glGenBuffers(1, vboIDs, 0);
        mVBOID = vboIDs[0];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboIDs[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mTransBuffer.capacity()
                * GLESConfig.FLOAT_SIZE_BYTES,
                mTransBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void setupVAO(GLESObject object) {
        if (mVersion == Version.GLES_20) {
            return;
        }

        GLES30.glEnableVertexAttribArray(USER_ATTRIB_LOCATION);

        boolean useVBO = object.useVBO();
        if (useVBO == true) {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,
                    mVBOID);
            GLES30.glVertexAttribPointer(USER_ATTRIB_LOCATION,
                    NUM_OF_ELEMENT, GLES30.GL_FLOAT, false,
                    NUM_OF_ELEMENT * GLESConfig.FLOAT_SIZE_BYTES,
                    0);
        } else {
            GLES30.glVertexAttribPointer(USER_ATTRIB_LOCATION,
                    NUM_OF_ELEMENT, GLES30.GL_FLOAT, false,
                    NUM_OF_ELEMENT * GLESConfig.FLOAT_SIZE_BYTES,
                    mTransBuffer);
        }

        GLES30.glVertexAttribDivisor(USER_ATTRIB_LOCATION, 1);
    }

    @Override
    public void enableVertexAttribute(GLESObject object) {
        if (mVersion == Version.GLES_20) {
            return;
        }

        boolean useVBO = object.useVBO();

        if (useVBO == true) {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBOID);

            GLES30.glVertexAttribPointer(USER_ATTRIB_LOCATION,
                    NUM_OF_ELEMENT, GLES30.GL_FLOAT, false,
                    NUM_OF_ELEMENT * GLESConfig.FLOAT_SIZE_BYTES,
                    0);
            GLES30.glEnableVertexAttribArray(USER_ATTRIB_LOCATION);
        } else {
            GLES30.glVertexAttribPointer(USER_ATTRIB_LOCATION,
                    NUM_OF_ELEMENT, GLES30.GL_FLOAT, false,
                    NUM_OF_ELEMENT * GLESConfig.FLOAT_SIZE_BYTES,
                    mTransBuffer);
            GLES30.glEnableVertexAttribArray(USER_ATTRIB_LOCATION);
        }
    }

    @Override
    public void disableVertexAttribute(GLESObject object) {
        if (mVersion == Version.GLES_20) {
            return;
        }

        if (object.useVAO() == true) {
            GLES30.glBindVertexArray(0);
            return;
        }

        GLES30.glDisableVertexAttribArray(USER_ATTRIB_LOCATION);
    }

    GLESObjectListener mObjListener = new GLESObjectListener() {

        @Override
        public void update(GLESObject object) {
            // TODO Auto-generated method stub

        }

        @Override
        public void apply(GLESObject object) {
            GLESShader shader = object.getShader();
            GLESTransform transform = object.getTransform();

            GLESCamera camera = object.getCamera();
            float[] vMatrix = camera.getViewMatrix();
            float[] mMatrix = transform.getMatrix();

            float[] vmMatrix = new float[16];
            Matrix.multiplyMM(vmMatrix, 0, vMatrix, 0, mMatrix, 0);
            float[] normalMatrix = new float[9];

            for (int i = 0; i < 3; i++) {
                normalMatrix[i * 3 + 0] = vmMatrix[i * 4 + 0];
                normalMatrix[i * 3 + 1] = vmMatrix[i * 4 + 1];
                normalMatrix[i * 3 + 2] = vmMatrix[i * 4 + 2];
            }

            shader.useProgram();

            GLES20.glUniformMatrix3fv(mNormalMatrixHandle, 1, false,
                    normalMatrix, 0);
        }
    };
}
