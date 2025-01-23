package com.initbase.reliefone.features.sos.presentation.send_sos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Base64
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.android.gms.location.Priority
import com.initbase.reliefone.SosContacts
import com.initbase.reliefone.core.data.CallState
import com.initbase.reliefone.core.data.source.RemoteSource
import com.initbase.reliefone.core.util.findActivity
import com.initbase.reliefone.features.sos.data.repository.SosRepository
import com.initbase.reliefone.features.sos.data.repository.SosRepositoryImp
import com.initbase.reliefone.features.sos.data.source.local.SosLocalDataSource
import com.initbase.reliefone.features.sos.data.source.remote.SosRemoteDataSource
import com.initbase.reliefone.features.sos.domain.use_cases.LocationCoordinates
import com.initbase.reliefone.features.sos.domain.use_cases.SendSosBody
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
class SosViewModel(private val repository: SosRepository) : ViewModel() {
    private val _screenState = MutableStateFlow(SendSosScreenState())
    val screenState: StateFlow<SendSosScreenState> = _screenState

    private val _contactTextFieldState = MutableStateFlow("")
    val contactTextFieldState: StateFlow<String> = _contactTextFieldState

    private fun updateScreenState(value: SendSosScreenState) {
        _screenState.update { value }
    }

    init {
        onEvent(SendSosEvent.GetContacts)
    }

    fun onEvent(event: SendSosEvent) {
        when (event) {
            is SendSosEvent.AddNumber -> handleAddNumberEvent(event)
            SendSosEvent.SendSos -> handleSendSosEvent()
            is SendSosEvent.UpdateContactsSheetState -> handleUpdateSheetStateEvent(event)
            is SendSosEvent.ContactTextFieldChanged -> handleContactTextFieldChangedEvent(event)
            SendSosEvent.GetContacts -> handleGetContactsEvent()
            is SendSosEvent.RemoveNumber -> handleRemoveNumberEvent(event)
            is SendSosEvent.CheckCameraPermissionStatus -> handleCheckCameraPermissionStatusEvent(event)
            is SendSosEvent.CaptureImage -> handleCaptureImageEvent(event)
            is SendSosEvent.UpdateCameraVisibilityEvent -> handleUpdateCameraVisibilityEvent(event)
            is SendSosEvent.GetLocation -> handleGetLocationEvent(event)
            is SendSosEvent.CheckLocationPermissionStatus -> handleCheckLocationPermissionStatusEvent(event)
        }
    }

    private fun handleUpdateCameraVisibilityEvent(event: SendSosEvent.UpdateCameraVisibilityEvent) {
        updateScreenState(_screenState.value.copy(showCamera = event.showCamera))
    }

    private fun handleCheckCameraPermissionStatusEvent(event: SendSosEvent.CheckCameraPermissionStatus) {
        when (val status = event.permissionState.status) {
            is PermissionStatus.Denied -> updateScreenState(_screenState.value.copy(cameraPermissionStatus = status))
            PermissionStatus.Granted -> updateScreenState(_screenState.value.copy(cameraPermissionStatus = status))
        }
        viewModelScope.launch {
            delay(100)
            updateScreenState(_screenState.value.copy(cameraPermissionStatus = null))
        }
    }

    private fun handleCheckLocationPermissionStatusEvent(event: SendSosEvent.CheckLocationPermissionStatus) {
        updateScreenState(_screenState.value.copy(locationPermissionsGranted = event.permissionState.allPermissionsGranted))
        viewModelScope.launch {
            delay(100)
            updateScreenState(_screenState.value.copy(locationPermissionsGranted = null))
        }
    }

    private fun handleRemoveNumberEvent(event: SendSosEvent.RemoveNumber) {
        viewModelScope.launch { repository.removeContact(event.index) }
    }

    private fun handleGetContactsEvent() {
        viewModelScope.launch {
            repository.getContacts().collect {
                updateScreenState(_screenState.value.copy(contacts = it))
            }
        }
    }

    private fun handleContactTextFieldChangedEvent(event: SendSosEvent.ContactTextFieldChanged) =
        _contactTextFieldState.update { event.text }

    private fun handleUpdateSheetStateEvent(event: SendSosEvent.UpdateContactsSheetState) =
        updateScreenState(_screenState.value.copy(showBottomSheet = event.showBottomSheet))

    private fun handleAddNumberEvent(event: SendSosEvent.AddNumber) =
        viewModelScope.launch {
            repository.addContact(event.number)
            onEvent(SendSosEvent.ContactTextFieldChanged(""))
        }

    private fun handleCaptureImageEvent(event: SendSosEvent.CaptureImage) {
        val photoFile = event.outputFile
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        event.imageCapture.takePicture(outputOptions, event.executor, object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) =
                updateScreenState(_screenState.value.copy(imageCaptureError = exception))

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) =
                updateScreenState(_screenState.value.copy(capturedImage = Uri.fromFile(photoFile), showCamera = false))
        })
    }

    private fun handleGetLocationEvent(event: SendSosEvent.GetLocation) {
        val context = event.locationProviderClient.applicationContext
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            updateScreenState(_screenState.value.copy(getLocationCallState = CallState.Error(InterruptedException("Grant location permission"))))
            return
        }
        updateScreenState(_screenState.value.copy(getLocationCallState = CallState.Loading))
        event.locationProviderClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,null).addOnCompleteListener {task->
                val location =task.result
                if(location!=null)
                    updateScreenState(_screenState.value.copy(getLocationCallState = CallState.Success(location), location = location))
                else
                    updateScreenState(_screenState.value.copy(getLocationCallState = CallState.Error(InterruptedException("Location request canceled"))))
        }
    }

    private fun handleSendSosEvent() {
        viewModelScope.launch {
            with(screenState.value){
                repository.sendSOS(SendSosBody(phoneNumbers = contacts,
                    image = Base64.encodeToString(capturedImage!!.toFile().readBytes(), Base64.DEFAULT),
                    location = LocationCoordinates(longitude = location?.longitude.toString(), latitude = location?.latitude.toString()))).collect {
                    updateScreenState(_screenState.value.copy(sendSosCallState = it))
                }
            }
        }
    }
}

class SosViewModelFactory(private val dataStore: DataStore<SosContacts>,val context:Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SosViewModel(repository = SosRepositoryImp(
            remoteDataSource = SosRemoteDataSource(RemoteSource.sosService(context)),
            localDataSource = SosLocalDataSource(dataStore))) as T
    }
}