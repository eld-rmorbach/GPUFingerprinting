package br.org.eldorado.gpufingerprint;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Renderer responsible to draw the image used to identify the mobile device.
 */
final class OpenGLRenderer implements GLSurfaceView.Renderer {
    /**
     * Vertex position attribute name.
     */
    private static final String VERTEX_POSITION_ATTRIBUTE_NAME = "aVertexPosition";

    /**
     * Vertex color attribute name.
     */
    private static final String VERTEX_COLOR_ATTRIBUTE_NAME = "aVertexColor";

    /**
     * Projection matrix attribute name.
     */
    private static final String PROJECTION_MATRIX_ATTRIBUTE_NAME = "uPMatrix";

    /**
     * Model-view matrix attribute name.
     */
    private static final String MODEL_VIEW_MATRIX_ATTRIBUTE_NAME = "uMVMatrix";

    /**
     * Projection matrix used to draw the shapes on image.
     */
    private static final float[] SHAPES_PROJECTION_MATRIX = {2.41421365737915039063f, 0f, 0f, 0f,
            0f, 2.41421365737915039063f, 0f, 0f, 0f, 0f, -1.00200200080871582031f, -1f, 0f, 0f,
            -0.200200200080871582031f, 0f};

    /**
     * The triangle vertices matrix.
     */
    private static final float[] TRIANGLE_VERTICES_MATRIX = {0f, 1f, 0f, -1f, -1f, 0f, 1f, -1f,
            0f};

    /**
     * The triangle colors matrix.
     */
    private static final float[] TRIANGLE_COLORS_MATRIX = {1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 0f,
            1f, 1f};

    /**
     * The triangle model-view matrix.
     */
    private static final float[] TRIANGLE_MODEL_VIEW_MATRIX = {1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f,
            0f, 1f, 0f, -1.5f, 0f, -7f, 1f};

    /**
     * The cube vertices matrix.
     */
    private static final float[] CUBE_VERTICES_MATRIX = {-1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f,
            -1f, 1f, 1f, -1f, -1f, -1f, 1f, -1f, -1f, 1f, 1f, -1f, -1f, 1f, -1f};

    /**
     * The cube colors matrix.
     */
    private static final float[] CUBE_COLORS_MATRIX = {0.5f, 0.5f, 1f, 1f, 0.5f, 0.5f, 1f, 1f,
            0.5f, 0.5f, 1f, 1f, 0.5f, 0.5f, 1f, 1f, 0.5f, 0.5f, 1f, 1f, 0.5f, 0.5f, 1f, 1f, 0.5f,
            0.5f, 1f, 1f, 0.5f, 0.5f, 1f, 1f};

    /**
     * The cube indices matrix.
     */
    private static final short[] CUBE_INDICES_MATRIX = {0, 1, 2, 2, 3, 0, 1, 5, 6, 6, 2, 1, 7, 6,
            5, 5, 4, 7, 4, 0, 3, 3, 7, 4, 4, 5, 1, 1, 0, 4, 3, 2, 6, 6, 7, 3};

    /**
     * The cube view-model matrix.
     */
    private static final float[] CUBE_MODEL_VIEW_MATRIX = {1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f,
            1f, 0f, 0f, 0f, -9f, 1f};

    /**
     * The cube z-axis rotation angle.
     */
    private static final int CUBE_Z_AXIS_ROTATION_ANGLE = 60;

    /**
     * The cube y-axis rotation angle.
     */
    private static final int CUBE_Y_AXIS_ROTATION_ANGLE = 10;

    /**
     * Number of points to define a triangle.
     */
    private static final int TRIANGLE_POINTS = 3;

    /**
     * Number of coordinates to define a point (three-dimensional).
     */
    private static final int POINT_COORDINATES = 3;

    /**
     * Number of channels to define a color (red, green, blue and alpha).
     */
    private static final int COLOR_CHANNELS = 4;

    /**
     * The OpenGL program handle.
     */
    private int mProgramHandle;

    /**
     * The triangle vertices buffer handle.
     */
    private int mTriangleVerticesBufferHandle;

    /**
     * The triangle colors buffer handle.
     */
    private int mTriangleColorsBufferHandle;

    /**
     * The cube vertices buffer handle.
     */
    private int mCubeVerticesBufferHandle;

    /**
     * The cube colors buffer handle.
     */
    private int mCubeColorsBufferHandle;

    /**
     * Flag which indicates whether the method {@link #onSurfaceCreated(GL10, EGLConfig)} has been
     * called.
     */
    private boolean mCreatedCalled;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCreatedCalled = true;

        mProgramHandle = createProgram();

        if (mProgramHandle != GLES20.GL_FALSE) {
            mTriangleVerticesBufferHandle = createFloatBuffer(TRIANGLE_VERTICES_MATRIX);
            mTriangleColorsBufferHandle = createFloatBuffer(TRIANGLE_COLORS_MATRIX);

            mCubeVerticesBufferHandle = createFloatBuffer(CUBE_VERTICES_MATRIX);
            mCubeColorsBufferHandle = createFloatBuffer(CUBE_COLORS_MATRIX);

            GLES20.glClearColor(0, 0, 0, 0);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mProgramHandle != GLES20.GL_FALSE) {
            if (!mCreatedCalled) {
                onSurfaceCreated(gl, null);
            }

            GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            int vertexPositionAttributeLocation = GLES20.glGetAttribLocation(mProgramHandle,
                    VERTEX_POSITION_ATTRIBUTE_NAME);
            int vertexColorAttributeLocation = GLES20.glGetAttribLocation(mProgramHandle,
                    VERTEX_COLOR_ATTRIBUTE_NAME);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTriangleVerticesBufferHandle);
            GLES20.glVertexAttribPointer(vertexPositionAttributeLocation, POINT_COORDINATES,
                    GLES20.GL_FLOAT, false, 0, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTriangleColorsBufferHandle);
            GLES20.glVertexAttribPointer(vertexColorAttributeLocation, COLOR_CHANNELS,
                    GLES20.GL_FLOAT, false, 0, 0);

            int projectionMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle,
                    PROJECTION_MATRIX_ATTRIBUTE_NAME);
            int modelViewMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle,
                    MODEL_VIEW_MATRIX_ATTRIBUTE_NAME);

            GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false,
                    FloatBuffer.wrap(SHAPES_PROJECTION_MATRIX));
            GLES20.glUniformMatrix4fv(modelViewMatrixHandle, 1, false,
                    FloatBuffer.wrap(TRIANGLE_MODEL_VIEW_MATRIX));
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, TRIANGLE_POINTS);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeVerticesBufferHandle);
            GLES20.glVertexAttribPointer(vertexPositionAttributeLocation, POINT_COORDINATES,
                    GLES20.GL_FLOAT, false, 0, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeColorsBufferHandle);
            GLES20.glVertexAttribPointer(vertexColorAttributeLocation, COLOR_CHANNELS,
                    GLES20.GL_FLOAT, false, 0, 0);

            float[] modelViewMatrix = Arrays.copyOf(CUBE_MODEL_VIEW_MATRIX,
                    CUBE_MODEL_VIEW_MATRIX.length);

            Matrix.rotateM(modelViewMatrix, 0, CUBE_Z_AXIS_ROTATION_ANGLE, 0, 0, 1);
            Matrix.rotateM(modelViewMatrix, 0, CUBE_Y_AXIS_ROTATION_ANGLE, 0, 1, 0);

            GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false,
                    FloatBuffer.wrap(SHAPES_PROJECTION_MATRIX));
            GLES20.glUniformMatrix4fv(modelViewMatrixHandle, 1, false,
                    FloatBuffer.wrap(modelViewMatrix));

            ShortBuffer cubeIndices = ShortBuffer.wrap(CUBE_INDICES_MATRIX);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, cubeIndices.capacity(),
                    GLES20.GL_UNSIGNED_SHORT, cubeIndices);
        }
    }

    /**
     * Creates the OpenGL program to draw the image.
     *
     * @return the handle of the just create OpenGL program or {@link GLES20#GL_FALSE} if the
     * program couldn't be created.
     */
    private int createProgram() {
        int programHandle = GLES20.GL_FALSE;
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if ((vertexShaderHandle != GLES20.GL_FALSE) && (fragmentShaderHandle != GLES20.GL_FALSE)) {
            GLES20.glShaderSource(vertexShaderHandle,
                    "attribute vec3 aVertexPosition; attribute vec4 aVertexColor; uniform mat4 uMVMatrix; uniform mat4 uPMatrix; varying vec4 vColor; void main(void) { gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition, 1.0); vColor = aVertexColor; }");
            GLES20.glCompileShader(vertexShaderHandle);

            GLES20.glShaderSource(fragmentShaderHandle,
                    "precision mediump float; varying vec4 vColor; void main(void) { gl_FragColor = vColor; }");
            GLES20.glCompileShader(fragmentShaderHandle);

            programHandle = GLES20.glCreateProgram();

            GLES20.glAttachShader(programHandle, vertexShaderHandle);
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);
            GLES20.glLinkProgram(programHandle);

            int[] linkStatus = new int[1];

            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            if (linkStatus[0] == GLES20.GL_TRUE) {
                GLES20.glUseProgram(programHandle);

                int vertexPosition = GLES20.glGetAttribLocation(programHandle,
                        VERTEX_POSITION_ATTRIBUTE_NAME);
                GLES20.glEnableVertexAttribArray(vertexPosition);

                int vertexColor = GLES20.glGetAttribLocation(programHandle,
                        VERTEX_COLOR_ATTRIBUTE_NAME);
                GLES20.glEnableVertexAttribArray(vertexColor);
            } else {
                programHandle = GLES20.GL_FALSE;
            }
        }

        return programHandle;
    }

    /**
     * Creates an OpenGL float buffer.
     *
     * @param bufferData the float values to be added to the buffer.
     * @return the handle for the just created float buffer.
     */
    private int createFloatBuffer(float... bufferData) {
        int[] bufferHandle = new int[1];
        FloatBuffer buffer = FloatBuffer.wrap(bufferData);

        GLES20.glGenBuffers(1, bufferHandle, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandle[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer.capacity() * Float.SIZE / Byte.SIZE,
                buffer, GLES20.GL_STATIC_DRAW);

        return bufferHandle[0];
    }
}
