package com.gomdev.gles.gles30;

import com.gomdev.gles.GLESConfig;
import com.gomdev.gles.GLESContext;
import com.gomdev.gles.GLESObject;
import com.gomdev.gles.GLESVertexInfo;

public class GLES30Object extends GLESObject {
    static final String CLASS = "GLES30Object";
    static final String TAG = GLESConfig.TAG + " " + CLASS;
    static final boolean DEBUG = GLESConfig.DEBUG;
    
    private boolean mUseVAO = false;
    private int mNumOfInstance = 1;

    public GLES30Object() {
        super();
    }

    public GLES30Object(String name) {
        super(name);
    }

    @Override
    public void setVertexInfo(GLESVertexInfo vertexInfo, boolean useVBO,
            boolean useVAO) {
        super.setVertexInfo(vertexInfo, useVBO, useVAO);

        if (useVAO == true && useVBO == true) {
            mUseVAO = useVAO;
            GLES30Renderer renderer = (GLES30Renderer) GLESContext
                    .getInstance().getRenderer();
            renderer.setupVAO(this);
        } else if (useVAO == false) {
            mUseVAO = useVAO;
        }
    }

    @Override
    public boolean useVAO() {
        return mUseVAO;
    }

    @Override
    public void setNumOfInstance(int num) {
        mNumOfInstance = num;
    }

    @Override
    public int getNumOfInstance() {
        return mNumOfInstance;
    }
}
