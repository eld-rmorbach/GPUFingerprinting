package br.org.eldorado.gpufingerprint

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class GPUFingerprintApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Fabric.with(this, Crashlytics())
    }
}