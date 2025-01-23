package com.initbase.reliefone.features.sos.presentation.send_sos

import android.location.Location
import android.net.Uri
import androidx.camera.core.ImageCaptureException
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.initbase.reliefone.core.data.CallState
import com.initbase.reliefone.features.sos.domain.use_cases.SendSosResponse

data class SendSosScreenState @ExperimentalPermissionsApi constructor(
    val sendSosCallState: CallState<SendSosResponse> = CallState.Initial,
    val showBottomSheet:Boolean = false,
    val contacts: List<String> = emptyList(),
    val cameraPermissionStatus: PermissionStatus? = null,
    val locationPermissionsGranted: Boolean? = null,
    val capturedImage: Uri? =null,
    val imageCaptureError: ImageCaptureException? = null,
    val getLocationCallState: CallState<Location> = CallState.Initial,
    val location: Location? = null,
    val showCamera:Boolean = false
)