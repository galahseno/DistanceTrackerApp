package com.example.distancetrackerapp.ui.permission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.distancetrackerapp.databinding.FragmentPermissionBinding
import com.example.distancetrackerapp.utils.Permission.hasLocationPermission
import com.example.distancetrackerapp.utils.Permission.requestLocationPermission
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentPermissionBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPermissionBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.continueButton?.setOnClickListener {
            if (hasLocationPermission(requireContext())) {
                val action = PermissionFragmentDirections.actionPermissionFragmentToMapsFragment()
                findNavController().navigate(action)
            } else {
                requestLocationPermission(this)
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
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms[0])){
            SettingsDialog.Builder(requireContext()).build().show()
        } else {
            requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        val action = PermissionFragmentDirections.actionPermissionFragmentToMapsFragment()
        findNavController().navigate(action)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}