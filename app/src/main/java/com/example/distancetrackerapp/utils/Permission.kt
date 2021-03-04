package com.example.distancetrackerapp.utils

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import com.example.distancetrackerapp.utils.Constant.PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE
import com.example.distancetrackerapp.utils.Constant.PERMISSION_DENY_BACKGROUND_LOCATION_MESSAGE
import com.example.distancetrackerapp.utils.Constant.PERMISSION_DENY_LOCATION_MESSAGE
import com.example.distancetrackerapp.utils.Constant.PERMISSION_LOCATION_REQUEST_CODE
import com.vmadalin.easypermissions.EasyPermissions

object Permission {

    fun hasLocationPermission(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    fun requestLocationPermission(fragment: Fragment) {
        EasyPermissions.requestPermissions(
            fragment,
            PERMISSION_DENY_LOCATION_MESSAGE,
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun hasBackgroundLocationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
        return true
    }

    fun requestBackgroundLocationPermission(fragment: Fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                fragment,
                PERMISSION_DENY_BACKGROUND_LOCATION_MESSAGE,
                PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }
}