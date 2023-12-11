package edu.iu.kevschoo.final_project.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import edu.iu.kevschoo.final_project.AuthenticationState
import edu.iu.kevschoo.final_project.R
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentImageViewBinding


class ImageViewFragment : Fragment() {

    private var _binding: FragmentImageViewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentImageViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri -> Glide.with(this).load(uri).into(binding.imageViewFull) }

        val callback = object : OnBackPressedCallback(true)
        {override fun handleOnBackPressed() {findNavController().navigate(R.id.loginFragment)}}
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.confirmPhoto.setOnClickListener {
            if (viewModel.authenticationState.value == AuthenticationState.AUTHENTICATED)
            { findNavController().navigate(R.id.profileFragment) }
            else
            { findNavController().navigate(R.id.loginFragment) }
        }

        binding.cancelPhoto.setOnClickListener { findNavController().navigate(R.id.cameraFragment) }

        binding.cancelPhoto.setOnClickListener { findNavController().navigate(R.id.cameraFragment) }
    }


    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}