package com.example.distancetrackerapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.distancetrackerapp.R
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
            navController.navigate(R.id.action_permissionFragment_to_mapsFragment)
        }
    }
}