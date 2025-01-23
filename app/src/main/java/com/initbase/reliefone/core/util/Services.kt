package com.initbase.reliefone.core.util

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Context.locationProvider() = LocationServices.getFusedLocationProviderClient(this.findActivity())

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}