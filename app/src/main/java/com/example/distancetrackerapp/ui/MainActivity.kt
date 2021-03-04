package com.example.distancetrackerapp.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.distancetrackerapp.R
import com.example.distancetrackerapp.utils.Constant.MAPS_FRAGMENT_URI
import com.example.distancetrackerapp.utils.Permission.hasLocationPermission
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment)

        if (hasLocationPermission(this)) {
            val uri = Uri.parse(MAPS_FRAGMENT_URI)
            navController.navigate(uri)
        }
    }
}