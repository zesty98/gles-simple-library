/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_lge_gles_GLESShader */

#ifndef _Included_com_lge_gles_GLESShader
#define _Included_com_lge_gles_GLESShader
#ifdef __cplusplus
extern "C" {
#endif
#undef com_lge_gles_GLESShader_DEBUG
#define com_lge_gles_GLESShader_DEBUG 1L

/*
 * Class:     com_lge_gles_GLESShader
 * Method:    nGetShaderCompileLog
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_lge_gles_GLESShader_nGetShaderCompileLog
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_lge_gles_GLESShader
 * Method:    nRetrieveProgramBinary
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_lge_gles_GLESShader_nRetrieveProgramBinary
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     com_lge_gles_GLESShader
 * Method:    nLoadProgramBinary
 * Signature: (IILjava/lang/String;Landroid/content/res/AssetManager;)I
 */
JNIEXPORT jint JNICALL Java_com_lge_gles_GLESShader_nLoadProgramBinary
  (JNIEnv *, jobject, jint, jint, jstring);

JNIEXPORT jint JNICALL Java_com_lge_gles_GLESShader_nFreeBinary
  (JNIEnv * env, jobject obj);

#ifdef __cplusplus
}
#endif
#endif
