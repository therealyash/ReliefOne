package com.initbase.reliefone.features.sos.presentation.send_sos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.initbase.reliefone.core.data.CallState
import com.initbase.reliefone.core.util.locationProvider
import com.initbase.reliefone.ui.theme.BackgroundDark
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService

@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun SendSosScreen(viewModel: SosViewModel,cameraExecutor: ExecutorService) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current
    val state by viewModel.screenState.collectAsState()
    val bottomSheetState = scaffoldState.bottomSheetState
    val cameraPermissionStatus = state.cameraPermissionStatus
    val locationPermissionsGranted = state.locationPermissionsGranted

    val onCameraPermissionGranted = {
        viewModel.onEvent(SendSosEvent.UpdateCameraVisibilityEvent(true))
    }
    val onLocationPermissionGranted = {
        viewModel.onEvent(SendSosEvent.GetLocation(context.locationProvider()))
    }
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA, onPermissionResult = {
        if(it)
            onCameraPermissionGranted()
    })
    val locationPermissionState = rememberMultiplePermissionsState(permissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION), onPermissionsResult = {
        if(it.values.all{value->value })
            onLocationPermissionGranted()
    })
    LaunchedEffect(state.showBottomSheet) {
        if (state.showBottomSheet)
            bottomSheetState.expand()
    }
    LaunchedEffect(bottomSheetState.currentValue){
        if(bottomSheetState.currentValue==BottomSheetValue.Collapsed)
            viewModel.onEvent(SendSosEvent.UpdateContactsSheetState(showBottomSheet = false))
    }
    LaunchedEffect(cameraPermissionStatus) {
        when (cameraPermissionStatus) {
            is PermissionStatus.Denied -> {
                if (cameraPermissionStatus.shouldShowRationale) {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar("Please grant camera permission", actionLabel = "Request").also {
                            if(it == SnackbarResult.ActionPerformed)
                                cameraPermissionState.launchPermissionRequest()
                        }
                    }
                } else
                    cameraPermissionState.launchPermissionRequest()
            }
            PermissionStatus.Granted -> onCameraPermissionGranted()
            null -> {}
        }
    }
    LaunchedEffect(locationPermissionsGranted){
        when(locationPermissionsGranted){
            true -> onLocationPermissionGranted()
            false -> {
                if (locationPermissionState.shouldShowRationale) {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar("Please grant location permission", actionLabel = "Request").also {
                            if(it == SnackbarResult.ActionPerformed)
                                locationPermissionState.launchMultiplePermissionRequest()
                        }
                    }
                } else
                    locationPermissionState.launchMultiplePermissionRequest()
            }
            null -> {}
        }
    }
    val capturedImage = state.capturedImage
    val getLocationState = state.getLocationCallState
    LaunchedEffect(capturedImage){
        capturedImage?.let {
            if(locationIsEnabled(context))
            viewModel.onEvent(SendSosEvent.CheckLocationPermissionStatus(locationPermissionState))
            else {
                scope.launch {
                    val result = scaffoldState.snackbarHostState.showSnackbar("Please enable location", actionLabel = "Enable")
                    if(result==SnackbarResult.ActionPerformed)
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).also {
                            context.startActivity(it)
                        }
                }
            }
        }
    }
    LaunchedEffect(getLocationState){
        scope.launch {
            when(getLocationState){
                is CallState.Error -> scaffoldState.snackbarHostState.showSnackbar(getLocationState.error.message?:"Unable to get location")
                CallState.Initial -> {}
                CallState.Loading -> scaffoldState.snackbarHostState.showSnackbar("Getting Location")
                is CallState.Success -> viewModel.onEvent(SendSosEvent.SendSos)
            }
        }
    }
    BackHandler(enabled = bottomSheetState.isExpanded) {
        scope.launch {
            bottomSheetState.collapse()
        }
    }
    BackHandler(enabled = state.showCamera) {
        scope.launch {
            viewModel.onEvent(SendSosEvent.UpdateCameraVisibilityEvent(false))
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = ContactsBottomSheet(viewModel),
        sheetPeekHeight = 0.dp,
    ) {
        Box(modifier=Modifier.fillMaxSize()){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SOS", style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.W600))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier
                        .fillMaxSize(0.9f)
                        .aspectRatio(1f)
                        .background(color = Color.BackgroundDark, shape = CircleShape))
                    SendSosButton(state=state.sendSosCallState) {
                        viewModel.onEvent(SendSosEvent.CheckCameraPermissionStatus(cameraPermissionState))
                    }
                }
                SosContactsBar {
                    viewModel.onEvent(SendSosEvent.UpdateContactsSheetState(showBottomSheet = true))
                }
            }

            if (state.showCamera)
                CameraView(
                    viewModel = viewModel,
                    executor = cameraExecutor,
                )
        }
    }
}

fun locationIsEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            ||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}
