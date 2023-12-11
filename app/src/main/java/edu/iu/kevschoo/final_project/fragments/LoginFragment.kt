package edu.iu.kevschoo.final_project.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import edu.iu.kevschoo.final_project.AuthenticationState
import edu.iu.kevschoo.final_project.R
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})

    companion object {
        private const val GALLERY_REQUEST_CODE = 1001
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)


        binding.messageTextView.visibility = View.GONE
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState)
            {
                AuthenticationState.AUTHENTICATED -> { findNavController().navigate(R.id.homeFragment) }

                AuthenticationState.UNAUTHENTICATED -> {
                    binding.messageTextView.visibility = View.GONE
                    viewModel.signOut()
                }

                AuthenticationState.INVALID_AUTHENTICATION -> {
                    binding.messageTextView.visibility = View.VISIBLE
                    binding.messageTextView.text = "Invalid username or password"
                    viewModel.signOut()
                }
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null)
            {
                binding.messageTextView.visibility = View.VISIBLE
                binding.messageTextView.text = errorMessage
            }
            else
            { binding.messageTextView.visibility = View.GONE }
        })

        binding.login.setOnClickListener {
            val email = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.signIn(email, password)
        }
        binding.signup.setOnClickListener {
            val email = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val name = binding.nameEditText.text.toString()
            viewModel.signUp(name, email, password)
        }

        val callback = object : OnBackPressedCallback(true) { override fun handleOnBackPressed() { viewModel.signOut()} }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.imageProfileView.setOnClickListener { showDialogForImageSelection() }
        viewModel.selectedImageUri.observe(viewLifecycleOwner, Observer { uri -> if (uri != null) { Glide.with(this).load(uri).into(binding.imageProfileView) } })

    }

    private fun showDialogForImageSelection()
    {
        val options = arrayOf("Choose from Gallery", "Take Photo")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose your profile picture")
        builder.setItems(options) { dialog, which ->
            when (which)
            {
                0 -> openGalleryForImage()
                1 -> navigateToCameraFragment()
            }
        }
        builder.show()
    }

    private fun openGalleryForImage()
    {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            val selectedImageUri = data?.data
            if (selectedImageUri != null)
            {
                viewModel.selectImageUri(selectedImageUri)
                Glide.with(this).load(selectedImageUri).into(binding.imageProfileView)
            }
        }
    }

    private fun navigateToCameraFragment() { findNavController().navigate(R.id.cameraFragment) }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}
