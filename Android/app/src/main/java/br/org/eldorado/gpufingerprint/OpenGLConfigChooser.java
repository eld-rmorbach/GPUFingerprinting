package br.org.eldorado.gpufingerprint;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * OpenGL configuration chooser, used to pick an OpenGL configuration to draw the image intended to
 * identify the mobile device.
 */
final class OpenGLConfigChooser implements GLSurfaceView.EGLConfigChooser {
    /**
     * The minimum color channel size, in bits, when listing all OpenGL configurations.
     */
    private static final int MINIMUM_COLOR_CHANNEL_SIZE = 4;

    /**
     * OpenGL ES 2 config selection.
     */
    private static final int EGL_OPEN_GL_ES2_BIT = 0x0004;

    /**
     * The anti-aliasing levels to be tried when choosing an OpenGL configuration.
     */
    private static final int[] ANTI_ALIASING_LEVELS = {4, 2};

    /**
     * The desired size of the red component of the color buffer, in bits.
     */
    private final int mRedSize;

    /**
     * The desired size of the green component of the color buffer, in bits.
     */
    private final int mGreenSize;

    /**
     * The desired size of the blue component of the color buffer, in bits.
     */
    private final int mBlueSize;

    /**
     * The desired size of the alpha component of the color buffer, in bits.
     */
    private final int mAlphaSize;

    /**
     * The desired color depth size, in bits.
     */
    private final int mDepthSize;

    /**
     * The desired stencil size, in bits.
     */
    private final int mStencilSize;

    /**
     * Constructor.
     *
     * @param redSize     the desired size of the red component of the color buffer, in bits.
     * @param greenSize   the desired size of the green component of the color buffer, in bits.
     * @param blueSize    the desired size of the blue component of the color buffer, in bits.
     * @param alphaSize   the desired size of the alpha component of the color buffer, in bits.
     * @param depthSize   the desired color depth size, in bits.
     * @param stencilSize the desired stencil size, in bits.
     */
    OpenGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize,
                        int stencilSize) {
        this.mRedSize = redSize;
        this.mGreenSize = greenSize;
        this.mBlueSize = blueSize;
        this.mAlphaSize = alphaSize;
        this.mDepthSize = depthSize;
        this.mStencilSize = stencilSize;
    }

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        EGLConfig config = null;
        int[] numConfigs = new int[1];
        int[] attributes = null;

        for (int i = 0; (i < ANTI_ALIASING_LEVELS.length) && (numConfigs[0] == 0); i++) {
            attributes = new int[]{EGL10.EGL_RED_SIZE, MINIMUM_COLOR_CHANNEL_SIZE,
                    EGL10.EGL_GREEN_SIZE, MINIMUM_COLOR_CHANNEL_SIZE, EGL10.EGL_BLUE_SIZE,
                    MINIMUM_COLOR_CHANNEL_SIZE, EGL10.EGL_RENDERABLE_TYPE, EGL_OPEN_GL_ES2_BIT,
                    EGL10.EGL_SAMPLES, ANTI_ALIASING_LEVELS[i], EGL10.EGL_SAMPLE_BUFFERS, 1,
                    EGL10.EGL_NONE};

            egl.eglChooseConfig(display, attributes, null, 0, numConfigs);
        }

        if (numConfigs[0] == 0) {
            attributes = new int[]{EGL10.EGL_RED_SIZE, MINIMUM_COLOR_CHANNEL_SIZE,
                    EGL10.EGL_GREEN_SIZE, MINIMUM_COLOR_CHANNEL_SIZE, EGL10.EGL_BLUE_SIZE,
                    MINIMUM_COLOR_CHANNEL_SIZE, EGL10.EGL_RENDERABLE_TYPE, EGL_OPEN_GL_ES2_BIT,
                    EGL10.EGL_NONE};

            egl.eglChooseConfig(display, attributes, null, 0, numConfigs);
        }

        if (numConfigs[0] > 0) {
            EGLConfig[] allConfigs = new EGLConfig[numConfigs[0]];

            egl.eglChooseConfig(display, attributes, allConfigs, numConfigs[0], numConfigs);

            int[] depthSize = new int[1];
            int[] stencilSize = new int[1];
            int[] redSize = new int[1];
            int[] greenSize = new int[1];
            int[] blueSize = new int[1];
            int[] alphaSize = new int[1];

            for (int i = 0; (i < allConfigs.length) && (config == null); i++) {
                EGLConfig currentConfig = allConfigs[i];

                egl.eglGetConfigAttrib(display, currentConfig, EGL10.EGL_DEPTH_SIZE, depthSize);
                egl.eglGetConfigAttrib(display, currentConfig, EGL10.EGL_STENCIL_SIZE, stencilSize);

                if ((depthSize[0] >= mDepthSize) && (stencilSize[0] >= mStencilSize)) {
                    egl.eglGetConfigAttrib(display, currentConfig, EGL10.EGL_RED_SIZE, redSize);
                    egl.eglGetConfigAttrib(display, currentConfig, EGL10.EGL_GREEN_SIZE, greenSize);
                    egl.eglGetConfigAttrib(display, currentConfig, EGL10.EGL_BLUE_SIZE, blueSize);
                    egl.eglGetConfigAttrib(display, currentConfig, EGL10.EGL_ALPHA_SIZE, alphaSize);

                    if ((redSize[0] == mRedSize) && (greenSize[0] == mGreenSize)
                            && (blueSize[0] == mBlueSize) && (alphaSize[0] == mAlphaSize)) {
                        config = currentConfig;
                    }
                }
            }
        }

        return config;
    }
}
