package com.gomdev.gles;

public interface GLESRendererListener {
    public void setupVBO(GLESVertexInfo vertexInfo);
    public void setupVAO(GLESObject object);
    public void enableVertexAttribute(GLESObject object);
    public abstract void disableVertexAttribute(GLESObject object);
}