package com.example.aeps_sdk.error

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.example.aeps_sdk.R
import com.example.aeps_sdk.databinding.FragmentErrorBinding

class ErrorFragment : Fragment() {
    private var _binding: FragmentErrorBinding? = null
    private val binding get() = _binding!!

    override

    fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentErrorBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
        Log.d("matmdata", requireArguments().getInt("errorResponse", 0).toString())
        when (requireArguments().getInt("errorResponse", 0)) {
            4046 -> {
                binding.errorTxt.text = resources.getString(R.string.CardExpire)
            }
            4001 -> {
                binding.errorTxt.text = resources.getString(R.string.BlockCard)
            }
            4003 -> {
                binding.errorTxt.text = resources.getString(R.string.BlockApplication)
            }
            4006 -> {
                binding.errorTxt.text = resources.getString(R.string.UnknownAID)
            }
            4011 -> {
                binding.errorTxt.text = resources.getString(R.string.TransactionIsDenied)
            }
            4007 -> {
                binding.errorTxt.text = resources.getString(R.string.TransactionIsCancelled)
            }
        }
        binding.closeView.setOnClickListener {
            requireActivity().finish()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}