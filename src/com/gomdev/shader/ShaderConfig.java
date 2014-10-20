package com.gomdev.shader;

public class ShaderConfig {
    static final boolean DEBUG = false;
    static final String TAG = "gomdev";

    public static final String APP_DIRECTORY_NAME = "gomdev";

    public static final String PREF_NAME = "shader pref";
    public static final String PREF_SHOW_INFO = "show informations";
    public static final String PREF_USE_GLES_30 = "use gles 3.0";
    public static final String PREF_SHOW_FPS = "show fps";
    public static final String PREF_GLES_EXTENSION = "gles extension";

    public static final String STATE_EFFECT_NAME = "effectName";
    public static final String STATE_SHADER_INFO = "shaderInfo";
    public static final String STATE_NUM_OF_SHADER = "numOfShader";
    public static final String STATE_SAVED_SHADER_INFO = "savedShaderInfo";
    public static final String STATE_SHOW_INFO = "showInfo";
    public static final String STATE_SHOW_FPS = "showFPS";
    public static final String STATE_USE_GLES30 = "useGLES30";
    public static final String STATE_EXTENSIONS = "extensions";

    public static final boolean ENABLE_AD = true;

    public enum Options {
        SHOW_INFO(0, "Show informations"),
        SHOW_FPS(1, "Show FPS"),
        USE_GLES30(2, "Use GLES 3.0");

        private final int mIndex;
        private final String mOptionName;

        Options(int index, String optionName) {
            mIndex = index;
            mOptionName = optionName;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getOption() {
            return mOptionName;
        }
    }

    public static final Options[] EFFECT_OPTIONS = new Options[] {
            Options.SHOW_INFO,
            Options.USE_GLES30,
            Options.SHOW_FPS
    };
}
