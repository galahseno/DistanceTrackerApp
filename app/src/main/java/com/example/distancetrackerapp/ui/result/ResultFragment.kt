package com.example.distancetrackerapp.ui.result

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.distancetrackerapp.R
import com.example.distancetrackerapp.databinding.FragmentResultBinding
import com.example.distancetrackerapp.utils.Constant.INTENT_TYPE
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding

    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            contentDistanceText.text = getString(R.string.result, args.result.distance)
            contentTimeText.text = args.result.time
            shareButton.setOnClickListener { shareResult() }
        }
    }

    private fun shareResult() {
        val distance = args.result.distance
        val time = args.result.time
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = INTENT_TYPE
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_result, distance, time))
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}