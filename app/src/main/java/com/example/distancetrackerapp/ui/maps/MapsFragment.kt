package com.example.distancetrackerapp.ui.maps

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.distancetrackerapp.R
import com.example.distancetrackerapp.databinding.FragmentMapsBinding
import com.example.distancetrackerapp.model.ResultDistance
import com.example.distancetrackerapp.service.TrackerService
import com.example.distancetrackerapp.ui.maps.MapsUtils.calculateDistance
import com.example.distancetrackerapp.ui.maps.MapsUtils.calculateElapseTime
import com.example.distancetrackerapp.ui.maps.MapsUtils.cameraPosition
import com.example.distancetrackerapp.utils.Constant.ACTION_START_SERVICE
import com.example.distancetrackerapp.utils.Constant.ACTION_STOP_SERVICE
import com.example.distancetrackerapp.utils.Constant.COUNT_DOWN_INTERVAL
import com.example.distancetrackerapp.utils.Constant.DELAY
import com.example.distancetrackerapp.utils.Constant.LOCATION_UPDATE_INTERVAL
import com.example.distancetrackerapp.utils.Constant.ONE_HUNDRED
import com.example.distancetrackerapp.utils.Constant.ONE_SECOND
import com.example.distancetrackerapp.utils.Constant.TEN_FLOAT
import com.example.distancetrackerapp.utils.Constant.TWO_SECOND
import com.example.distancetrackerapp.utils.Constant.ZERO
import com.example.distancetrackerapp.utils.ExtensionsFunction.animateGone
import com.example.distancetrackerapp.utils.ExtensionsFunction.disable
import com.example.distancetrackerapp.utils.ExtensionsFunction.enable
import com.example.distancetrackerapp.utils.ExtensionsFunction.hide
import com.example.distancetrackerapp.utils.ExtensionsFunction.showAnimate
import com.example.distancetrackerapp.utils.Permission.hasBackgroundLocationPermission
import com.example.distancetrackerapp.utils.Permission.requestBackgroundLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding

    private lateinit var map: GoogleMap

    private var locationList = mutableListOf<LatLng>()
    private var polylineList = mutableListOf<Polyline>()
    private var markerList = mutableListOf<Marker>()
    val started = MutableLiveData<Boolean>()

    private var startTime = ZERO
    private var stopTime = ZERO

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding?.apply {
            lifecycleOwner = this@MapsFragment
            tracking = this@MapsFragment
            startButton.setOnClickListener { onStartClick() }
            stopButton.setOnClickListener { onStopClick() }
            resetButton.setOnClickListener { onResetClick() }
        }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        observeLocationList()
    }

    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            map = googleMap
        }
        map.isMyLocationEnabled = true
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isRotateGesturesEnabled = false
            isCompassEnabled = false
            isTiltGesturesEnabled = false
            isScrollGesturesEnabled = false
            isZoomGesturesEnabled = false
        }

        map.setOnMyLocationButtonClickListener {
            binding?.tapMylocationText?.animateGone()
            binding?.startButton?.showAnimate()
            false
        }
        map.setOnMarkerClickListener {
            true
        }
    }

    private fun observeLocationList() {
        TrackerService.locationList.observe(viewLifecycleOwner, {
            if (it != null) {
                locationList = it
                drawPolyline()
                followPolyline()
                if (it.size > 1) {
                    binding?.stopButton?.enable()
                }
            }
        })
        TrackerService.initialState.observe(viewLifecycleOwner, {
            started.value = it
        })
        TrackerService.startTime.observe(viewLifecycleOwner, {
            startTime = it
        })
        TrackerService.stopTime.observe(viewLifecycleOwner, {
            stopTime = it
            if (stopTime != ZERO) {
                showBiggerPicture()
                displayResult()
            }
        })
    }

    private fun drawPolyline() {
        val polyline = map.addPolyline(
            PolylineOptions().apply {
                width(TEN_FLOAT)
                color(Color.RED)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(locationList)
            }
        )
        polylineList.add(polyline)
    }

    private fun followPolyline() {
        if (locationList.isNotEmpty()) {
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    cameraPosition(locationList.last())
                ), ONE_SECOND, null
            )
        }
    }

    private fun onStartClick() {
        if (hasBackgroundLocationPermission(requireContext())) {
            onStartTimer()
            binding?.apply {
                startButton.disable()
                startButton.hide()
                stopButton.showAnimate()
            }
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    private fun onStopClick() {
        binding?.apply {
            stopButton.hide()
            stopButton.disable()
            startButton.showAnimate()
            startButton.disable()
        }
        sendCommandToService(ACTION_STOP_SERVICE)
    }

    @SuppressLint("MissingPermission")
    private fun onResetClick() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastKnowLocation = LatLng(
                it.result.latitude,
                it.result.longitude
            )
            for (polyline in polylineList) {
                polyline.remove()
            }
            polylineList.clear()
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    cameraPosition(lastKnowLocation)
                )
            )
            locationList.clear()
            for (marker in markerList) {
                marker.remove()
            }
            markerList.clear()
            binding?.apply {
                resetButton.hide()
                startButton.showAnimate()
            }
        }
    }

    private fun onStartTimer() {
        binding?.apply {
            timerText.showAnimate()
            stopButton.disable()
        }
        val timer = object : CountDownTimer(LOCATION_UPDATE_INTERVAL, COUNT_DOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                val currentSecond = millisUntilFinished / ONE_SECOND
                if (currentSecond.toString() == "0") {
                    binding?.apply {
                        timerText.text = getString(R.string.go)
                        timerText.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.black
                            )
                        )
                    }
                } else {
                    binding?.apply {
                        timerText.text = currentSecond.toString()
                    }
                }
            }

            override fun onFinish() {
                sendCommandToService(ACTION_START_SERVICE)
                binding?.timerText?.animateGone()
            }
        }
        timer.start()
    }

    private fun sendCommandToService(action: String) {
        Intent(
            requireContext(),
            TrackerService::class.java,
        ).apply {
            this.action = action
            requireContext().startService(this)
        }
    }

    private fun showBiggerPicture() {
        val bounds = LatLngBounds.Builder()
        for (location in locationList) {
            bounds.include(location)
        }
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds.build(), ONE_HUNDRED), TWO_SECOND, null
        )
        addMarker(locationList.first())
        addMarker(locationList.last())
    }

    private fun addMarker(position: LatLng) {
        val marker = map.addMarker(MarkerOptions().position(position))
        markerList.add(marker)
    }

    private fun displayResult() {
        val result = ResultDistance(
            calculateDistance(locationList),
            calculateElapseTime(startTime, stopTime)
        )
        lifecycleScope.launch {
            delay(DELAY)
            val action = MapsFragmentDirections.actionMapsFragmentToResultFragment(result)
            findNavController().navigate(action)
            binding?.apply {
                startButton.apply {
                    hide()
                    enable()
                }
                stopButton.hide()
                resetButton.showAnimate()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms[0])) {
            SettingsDialog.Builder(requireContext()).build().show()
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}