package br.org.eldorado.gpufingerprint

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.opengl.GLES20
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.commons.codec.digest.DigestUtils
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null

    private var job: Job? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        create_images_button.setOnClickListener {
            launchGenerateImages()
        }
    }

    override fun onStop() {
        job?.apply {
            if (!isCancelled) {
                cancel()
            }
        }
        progressDialog?.dismiss()

        super.onStop()
    }

    private fun launchGenerateImages() {
        job?.apply {
            if (!isCancelled) {
                cancel()
            }
        }

        progressDialog = ProgressDialog.show(this, "Geração de imagens", "Aguarde, gerando imagens...", true, false)

        job = GlobalScope.launch {
            generateImageAndSaveData()

            progressDialog?.dismiss()
        }
    }

    private fun generateImageAndSaveData() {
        val pixelBuffer = OpenGLPixelBuffer(SAMPLE_IMAGE_WIDTH,
                SAMPLE_IMAGE_HEIGHT, OpenGLContextFactory(),
                OpenGLConfigChooser(COLOR_CHANNEL_BIT_SIZE, COLOR_CHANNEL_BIT_SIZE,
                        COLOR_CHANNEL_BIT_SIZE, COLOR_CHANNEL_BIT_SIZE, 0, 0))

        pixelBuffer.setRenderer(OpenGLRenderer())
        val bitmap = pixelBuffer.bitmap
        val bitmapBytes = getBitmapBytes(bitmap)
        val bitmapHash = getBitmapHash(bitmapBytes)
        val gpuName = GLES20.glGetString(GLES20.GL_RENDERER)
        val gpuVendor = GLES20.glGetString(GLES20.GL_VENDOR)
        val openGLVersion = GLES20.glGetString(GLES20.GL_VERSION)
        pixelBuffer.destroy()


        val deviceId = FirebaseInstanceId.getInstance().id
        val db = FirebaseDatabase.getInstance().reference
        val deviceReference = db.child("fingerprints").child(deviceId)

        deviceReference.child("androidVersion").setValue(Build.VERSION.RELEASE)
        deviceReference.child("apiLevel").setValue(Build.VERSION.SDK_INT)
        deviceReference.child("manufacturer").setValue(Build.MANUFACTURER)
        deviceReference.child("model").setValue(Build.MODEL)
        deviceReference.child("gpuName").setValue(gpuName)
        deviceReference.child("gpuVendor").setValue(gpuVendor)
        deviceReference.child("openGLVersion").setValue(openGLVersion)
        deviceReference.child("hash").setValue(bitmapHash)
    }

    private fun getBitmapBytes(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.PNG, SAMPLE_IMAGE_PNG_QUALITY, outputStream)

        return outputStream.toByteArray()
    }

    private fun getBitmapHash(bitmapBytes: ByteArray): String {
        val digest = DigestUtils.getMd5Digest()

        digest.update(bitmapBytes)

        val messageDigest = digest.digest()

        return Base64.encodeToString(messageDigest,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    companion object {
        private const val COLOR_CHANNEL_BIT_SIZE = 8

        private const val SAMPLE_IMAGE_WIDTH = 250

        private const val SAMPLE_IMAGE_HEIGHT = 250

        private const val SAMPLE_IMAGE_PNG_QUALITY = 100
    }
}