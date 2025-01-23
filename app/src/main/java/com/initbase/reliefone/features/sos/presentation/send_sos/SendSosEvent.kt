package com.initbase.reliefone.features.sos.presentation.send_sos

import androidx.camera.core.ImageCapture
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import java.io.File
import java.util.concurrent.Executor

sealed interface SendSosEvent{
    object SendSos:SendSosEvent
    data class CheckCameraPermissionStatus @OptIn(ExperimentalPermissionsApi::class)
    constructor(val permissionState:PermissionState):SendSosEvent
    data class CheckLocationPermissionStatus @OptIn(ExperimentalPermissionsApi::class)
    constructor(val permissionState:MultiplePermissionsState):SendSosEvent
    data class UpdateContactsSheetState(val showBottomSheet:Boolean):SendSosEvent
    data class AddNumber(val number:String):SendSosEvent
    data class RemoveNumber(val index:Int):SendSosEvent
    object GetContacts:SendSosEvent
    data class CaptureImage(val imageCapture: ImageCapture, val outputFile: File, val executor: Executor):SendSosEvent
    data class UpdateCameraVisibilityEvent(val showCamera:Boolean):SendSosEvent
    data class GetLocation(val locationProviderClient: FusedLocationProviderClient):SendSosEvent
    data class ContactTextFieldChanged(val text:String):SendSosEvent
}