package com.gomdev.gles.gles30;

import com.gomdev.gles.GLESContext;
import com.gomdev.gles.GLESObject;
import com.gomdev.gles.GLESVertexInfo;

public class GLES30Object extends GLESObject {
    private boolean mUseVAO = true;
    private int mNumOfInstance = 1;

    @Override
    public void setVertexInfo(GLESVertexInfo vertexInfo, boolean useVBO,
            boolean useVAO) {
        super.setVertexInfo(vertexInfo, useVBO, useVAO);

        if (useVAO == true && useVBO == true) {
            mUseVAO = useVAO;
            GLES30Renderer renderer = (GLES30Renderer) GLESContext
                    .getInstance().getRenderer();
            renderer.setupVAO(this);
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