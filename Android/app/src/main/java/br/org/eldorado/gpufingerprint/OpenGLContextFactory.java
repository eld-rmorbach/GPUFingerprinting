package br.org.eldorado.gpufingerprint;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * OpenGL context factory which provides {@link EGLContext} instances to be used when drawing images
 * to identify mobile devices.
 */
final class OpenGLContextFactory implements GLSurfaceView.EGLContextFactory {
    /**
     * The version of the client API which the context supports, as specified at context creation
     * time.
     */
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    @Override
    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
        int[] attributes = new int[]{EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};

        return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attributes);
    }

    @Override
    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        egl.eglDestroyContext(display, context);
    }
}
