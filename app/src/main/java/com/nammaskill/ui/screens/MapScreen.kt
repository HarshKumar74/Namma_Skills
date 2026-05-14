package com.nammaskill.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nammaskill.domain.model.AdminModel
import com.nammaskill.ui.viewmodels.EnrollmentViewModel
import com.nammaskill.util.LocationPermissionHandler
import com.nammaskill.util.hasLocationPermission
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapboxMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: EnrollmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isPermissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    var showPermissionRequest by remember { mutableStateOf(!isPermissionGranted) }

    val centers by viewModel.skillCenters.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchSkillCenters()
    }

    if (showPermissionRequest) {
        LocationPermissionHandler(
            onPermissionGranted = {
                isPermissionGranted = true
                showPermissionRequest = false
            },
            onPermissionDenied = {
                isPermissionGranted = false
                showPermissionRequest = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Skill Centers Map") })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (centers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (isPermissionGranted) {
                    MapLibreView(centers = centers)
                } else if (!showPermissionRequest) {
                    CenterListView(centers = centers)
                }
            }
        }
    }
}

@Composable
fun MapLibreView(centers: List<AdminModel>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Initialize Mapbox/MapLibre
    remember {
        Mapbox.getInstance(context)
    }

    // Create MapView and manage its lifecycle
    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            view.getMapAsync { map ->
                if (map.style == null) {
                    map.setStyle("https://demotiles.maplibre.org/style.json") { style ->
                        setupMap(context, map, style, centers, view)
                    }
                } else {
                    setupMap(context, map, map.style!!, centers, view)
                }
            }
        }
    )
}

@SuppressLint("MissingPermission")
private fun setupMap(
    context: android.content.Context, 
    map: MapboxMap, 
    style: Style, 
    centers: List<AdminModel>, 
    mapView: MapView
) {
    // Enable the Location Component (shows the blue dot)
    if (hasLocationPermission(context)) {
        val locationComponent = map.locationComponent
        if (!locationComponent.isLocationComponentActivated) {
            val activationOptions = LocationComponentActivationOptions.builder(context, style)
                .useDefaultLocationEngine(true)
                .build()
            locationComponent.activateLocationComponent(activationOptions)
        }
        locationComponent.isLocationComponentEnabled = true
        locationComponent.renderMode = RenderMode.COMPASS
    }

    // Clear existing markers and rebuild the list of points to show
    map.removeAnnotations()

    val boundsBuilder = LatLngBounds.Builder()
    var validPointCount = 0

    // 1. Add Marker for Present Location (User)
    if (hasLocationPermission(context)) {
        map.locationComponent.lastKnownLocation?.let {
            val userLatLng = LatLng(it.latitude, it.longitude)
            map.addMarker(
                MarkerOptions()
                    .position(userLatLng)
                    .title("Your Location")
            )
            boundsBuilder.include(userLatLng)
            validPointCount++
        }
    }

    // 2. Add Markers for Skill Centers
    centers.forEach { center ->
        if (center.latitude != 0.0 && center.longitude != 0.0) {
            val position = LatLng(center.latitude, center.longitude)
            map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(center.skillCenterName)
                    .snippet(center.address)
            )
            boundsBuilder.include(position)
            validPointCount++
        }
    }

    // 3. Zoom Camera to show all relevant points
    if (validPointCount > 0) {
        try {
            if (validPointCount == 1) {
                // If only one point (either user or one center), center on it with zoom
                val pos = if (map.markers.isNotEmpty()) map.markers.first().position else LatLng(12.9716, 77.5946)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13.0))
            } else {
                // Fit all pins (user + centers) in view
                val bounds = boundsBuilder.build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
            }
        } catch (e: Exception) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(12.9716, 77.5946), 10.0))
        }
    } else {
        // Default view: Bengaluru
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(12.9716, 77.5946), 10.0))
    }
    
    map.setOnMarkerClickListener { marker ->
        marker.showInfoWindow(map, mapView)
        true
    }
}

@Composable
fun CenterListView(centers: List<AdminModel>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Location permission denied. Showing registered centers.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(centers) { center ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = center.skillCenterName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = center.address, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
