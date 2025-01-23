package com.initbase.reliefone.features.sos.presentation.send_sos

import android.Manifest
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.initbase.reliefone.core.data.CallState
import com.initbase.reliefone.features.sos.domain.use_cases.SendSosResponse
import com.initbase.reliefone.ui.theme.BackgroundDark

@Composable
fun SosContactsBar(onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(60.dp)
        .background(color = Color.BackgroundDark, shape = MaterialTheme.shapes.medium)
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(imageVector = Icons.Default.Contacts, contentDescription = "Sos Contacts")
    }
}

@Composable
fun SendSosButton(modifier: Modifier = Modifier,state:CallState<SendSosResponse> = CallState.Initial, onClick: () -> Unit = {}) {
    val stateColorAndText:Pair<Color,String>  = when(state){
        is CallState.Error -> Color(0xffe84037) to "SOS Not Sent"
        CallState.Initial -> MaterialTheme.colors.primary to "Send SOS"
        CallState.Loading -> MaterialTheme.colors.primaryVariant to "Sending SOS"
        is CallState.Success -> Color(0xff188251) to "SOS Sent"
    }
    val stateColor by animateColorAsState(stateColorAndText.first)

    Box(modifier = modifier
        .fillMaxSize(0.5f)
        .aspectRatio(1f)
        .background(brush = Brush.verticalGradient(colors = listOf(
            Color(
                ColorUtils.blendARGB(stateColor.toArgb(), Color.White.toArgb(), 0.2f),
            ),
            stateColor
        )),
            shape = CircleShape)
        .clip(CircleShape)
        .clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(stateColorAndText.second, style = MaterialTheme.typography.h5, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContactsBottomSheet(viewModel: SosViewModel): @Composable() (ColumnScope.() -> Unit) =
    {
        val textFieldValue by viewModel.contactTextFieldState.collectAsState()
        val state by viewModel.screenState.collectAsState()
        val items = state.contacts
        val focusManager = LocalFocusManager.current
        val onAddContact = {
            if(textFieldValue.isNotEmpty()){
                viewModel.onEvent(SendSosEvent.AddNumber(textFieldValue))
                focusManager.clearFocus()
            }
        }
        Row(modifier = Modifier
            .padding(16.dp)
            .height(50.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(modifier = Modifier
                .weight(4f)
                .fillMaxHeight(),
                value = textFieldValue,
                onValueChange = {
                    viewModel.onEvent(SendSosEvent.ContactTextFieldChanged(it))
                },
                singleLine = true,
                placeholder={Text("Enter phone number")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                keyboardActions = KeyboardActions(onDone = { onAddContact() }))
            Button(modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = { onAddContact() }) {
                Text("Add", color = MaterialTheme.colors.onBackground)
            }
        }
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp), verticalArrangement = Arrangement.spacedBy(8.dp)){
            items(items.size){index->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically){
                    Text(text = items[index],modifier=Modifier.weight(4f), style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.W600))
                    IconButton(onClick = {
                        viewModel.onEvent(SendSosEvent.RemoveNumber(index))
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }