package br.org.eldorado.gpufingerprint;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

/**
 * Class intended to replace a {@link GLSurfaceView} to capture an image drawn by a
 * {@link GLSurfaceView.Renderer}.
 */
final class OpenGLPixelBuffer {
    /**
     * The image width.
     */
    private final int mWidth;

    /**
     * The image height.
     */
    private final int mHeight;

    /**
     * The Khronos rendering API interface instance.
     */
    private final EGL10 mEGL;

    /**
     * The OpenGL display instance.
     */
    private final EGLDisplay mEGLDisplay;

    /**
     * The OpenGL configuration used to draw the image.
     */
    private final EGLConfig mEGLConfig;

    /**
     * The OpenGL rendering context.
     */
    private final EGLContext mEGLContext;

    /**
     * The OpenGL surface instance where the image will be drawn.
     */
    private final EGLSurface mEGLSurface;

    /**
     * The OpenGL interface instance.
     */
    private final GL10 mGL;

    /**
     * The renderer instance used to drawn the image used to identify a mobile device.
     */
    private GLSurfaceView.Renderer mRenderer;

    /**
     * Constructor.
     *
     * @param width          the image width.
     * @param height         the image height.
     * @param contextFactory the OpenGL context factory instance.
     * @param configChooser  the OpenGL configuration chooser.
     */
    OpenGLPixelBuffer(int width, int height, GLSurfaceView.EGLContextFactory contextFactory,
                      GLSurfaceView.EGLConfigChooser configChooser) {
        this.mWidth = width;
        this.mHeight = height;

        this.mEGL = (EGL10) EGLContext.getEGL();
        this.mEGLDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        mEGL.eglInitialize(mEGLDisplay, new int[2]);

        this.mEGLConfig = configChooser.chooseConfig(mEGL, mEGLDisplay);

        if (!isInvalid()) {
            this.mEGLContext = contextFactory.createContext(mEGL, mEGLDisplay, mEGLConfig);
            this.mGL = (GL10) mEGLContext.getGL();

            int[] attributesList = new int[]{EGL10.EGL_WIDTH, mWidth, EGL10.EGL_HEIGHT, mHeight,
                    EGL10.EGL_NONE};

            this.mEGLSurface = mEGL.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig,
                    attributesList);

            mEGL.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        } else {
            mEGLContext = null;
            mEGLSurface = null;
            mGL = null;
        }
    }

    /**
     * Sets the renderer instance used to drawn the image used to identify a mobile device.
     *
     * @param renderer the renderer instance used to drawn the image used to identify a mobile device.
     */
    void setRenderer(GLSurfaceView.Renderer renderer) {
        mRenderer = renderer;

        if (mRenderer != null) {
            mRenderer.onSurfaceCreated(mGL, mEGLConfig);
            mRenderer.onSurfaceChanged(mGL, mWidth, mHeight);
        }
    }

    /**
     * Extracts the bitmap from the pixel buffer. The bitmap contains the image drawn by the given
     * renderer.
     *
     * @return the bitmap from the pixel buffer
     */
    Bitmap getBitmap() {
        Bitmap bitmap = null;

        if (mRenderer != null) {
            mRenderer.onDrawFrame(mGL);

            IntBuffer originalBuffer = IntBuffer.allocate(mWidth * mHeight);

            mGL.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE,
                    originalBuffer);

            int[] originalBufferCopy = originalBuffer.array();
            int[] regularImageBuffer = new int[mWidth * mHeight];

            for (int y = 0; y < mHeight; y++) {
                for (int x = 0; x < mWidth; x++) {
                    regularImageBuffer[(mHeight - y - 1) * mWidth
                            + x] = originalBufferCopy[y * mWidth + x];
                }
            }

            bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(IntBuffer.wrap(regularImageBuffer));
        }

        return bitmap;
    }

    /**
     * Destroys the pixel buffer.
     */
    void destroy() {
        if (mRenderer != null) {
            mRenderer.onDrawFrame(mGL);
        }

        mEGL.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT);
        mEGL.eglDestroySurface(mEGLDisplay, mEGLSurface);
        mEGL.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEGL.eglTerminate(mEGLDisplay);
    }

    /**
     * Returns whether the pixel buffer is invalid. The pixel buffer is invalid an OpenGL
     * configuration could not be found.
     *
     * @return true if the pixel buffer is invalid or false otherwise.
     */
    private boolean isInvalid() {
        return mEGLConfig == null;
    }
}
