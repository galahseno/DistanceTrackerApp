package com.example.distancetrackerapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.distancetrackerapp.ui.maps.MapsUtils.calculateDistance
import com.example.distancetrackerapp.utils.Constant.ACTION_START_SERVICE
import com.example.distancetrackerapp.utils.Constant.ACTION_STOP_SERVICE
import com.example.distancetrackerapp.utils.Constant.KM
import com.example.distancetrackerapp.utils.Constant.LOCATION_FASTEST_UPDATE_INTERVAL
import com.example.distancetrackerapp.utils.Constant.LOCATION_UPDATE_INTERVAL
import com.example.distancetrackerapp.utils.Constant.NOTIFICATION_CHANNEL_ID
import com.example.distancetrackerapp.utils.Constant.NOTIFICATION_CHANNEL_NAME
import com.example.distancetrackerapp.utils.Constant.NOTIFICATION__ID
import com.example.distancetrackerapp.utils.Constant.NOTIFICATION__TITLE
import com.example.distancetrackerapp.utils.Constant.ZERO
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService : LifecycleService() {

    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val initialState = MutableLiveData<Boolean>()
        val startTime = MutableLiveData<Long>()
        val stopTime = MutableLiveData<Long>()

        val locationList = MutableLiveData<MutableList<LatLng>>()
    }

    private fun setInitial() {
        initialState.postValue(false)
        locationList.postValue(mutableListOf())
        startTime.postValue(ZERO)
        stopTime.postValue(ZERO)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            result?.locations?.let { locations ->
                for (location in locations) {
                    updateLocationList(location)
                    updateNotification()
                }
            }
        }
    }

    private fun updateLocationList(location: Location) {
        val newLatLng = LatLng(location.latitude, location.longitude)
        locationList.value?.apply {
            add(newLatLng)
            locationList.postValue(this)
        }
    }

    override fun onCreate() {
        setInitial()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_SERVICE -> {
                    initialState.postValue(true)
                    startForegroundService()
                    startLocationUpdate()
                }
                ACTION_STOP_SERVICE -> {
                    initialState.postValue(false)
                    stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION__ID, notification.build())
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        val locationRequest = LocationRequest().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_FASTEST_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        startTime.postValue(System.currentTimeMillis())
    }

    private fun stopForegroundService() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION__ID
        )
        stopForeground(true)
        stopSelf()
        stopTime.postValue(System.currentTimeMillis())
    }

    private fun updateNotification() {
        notification.apply {
            setContentTitle(NOTIFICATION__TITLE)
            setContentText("${locationList.value?.let { calculateDistance(it) }} $KM")
        }
        notificationManager.notify(NOTIFICATION__ID, notification.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}