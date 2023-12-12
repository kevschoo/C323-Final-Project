package edu.iu.kevschoo.final_project.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
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
import edu.iu.kevschoo.final_project.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})

    companion object {
        private const val GALLERY_REQUEST_CODE = 1001
    }

    /**
     * Inflates the layout for this fragment
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return Return the View for the fragment's UI, or null
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle)
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchUserData()
        binding.imageProfileView.setOnClickListener { showDialogForImageSelection() }
        viewModel.selectedImageUri.observe(viewLifecycleOwner, Observer { uri ->
            if (uri != null)
            { Glide.with(this).load(uri).into(binding.imageProfileView) }
        })

        viewModel.userProfilePictureUrl.observe(viewLifecycleOwner, Observer { url ->
            if (!url.isNullOrEmpty() && viewModel.selectedImageUri.value == null)
            { Glide.with(this).load(url).into(binding.imageProfileView) }
        })

        binding.save.setOnClickListener { viewModel.selectedImageUri.value?.let { uri -> viewModel.updateProfilePicture(uri) } }

        binding.btnTestNotify.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                    requestNotificationPermission()
                } else {
                    viewModel.sendTestNotification()
                }
            } else {
                viewModel.sendTestNotification()
            }
        }
    }

    /**
     * Requests notification permission from the user if not already granted
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Notification Permission")
            builder.setMessage("Please enable notifications for this app in your settings.")
            builder.setPositiveButton("Go to Settings") { dialog, _ ->
                dialog.dismiss()
                openAppSettings()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    /**
     * Opens the app's settings page in the system settings to allow the user to change notification permissions
     */
    private fun openAppSettings() {
        val intent = Intent().apply {
            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            data = uri
        }
        startActivity(intent)
    }

    /**
     * Shows a dialog for the user to select either to choose an image from the gallery or take a new photo
     */
    private fun showDialogForImageSelection()
    {
        val options = arrayOf("Choose from Gallery", "Take Photo")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose your profile picture")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openGalleryForImage()
                1 -> navigateToCameraFragment()
            }
        }
        builder.show()
    }

    /**
     * Opens the gallery for the user to select an image
     */
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    /**
     * Handles the result from the activity started by startActivityForResult
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from
     * @param resultCode The integer result code returned by the child activity through its setResult()
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras")
     */
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
    /**
     * Navigates to the camera fragment to allow the user to take a new photo
     */
    private fun navigateToCameraFragment() { findNavController().navigate(R.id.cameraFragment) }

    /**
     * Called when the view hierarchy associated with the fragment is being destroyed
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}
