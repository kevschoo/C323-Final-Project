package edu.iu.kevschoo.final_project.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import edu.iu.kevschoo.final_project.AuthenticationState
import edu.iu.kevschoo.final_project.R
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraFragment : Fragment()
{

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var isCameraInitialized = false
    private val cameraPermissionRequestCode = 1001


    /**
     * Inflates the layout for this fragment
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return Return the View for the fragment's UI, or null
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
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
        viewModel.authenticationState.observe(viewLifecycleOwner) { authState ->
            val callback = object : OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed()
                {
                    if (authState == AuthenticationState.AUTHENTICATED)
                    { findNavController().navigate(R.id.profileFragment) }
                    else { findNavController().navigate(R.id.loginFragment) }
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {requestPermissions(arrayOf(android.Manifest.permission.CAMERA), cameraPermissionRequestCode)}
        else
        {startCamera()}

        binding.toggleCamera.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {CameraSelector.LENS_FACING_FRONT}
            else {CameraSelector.LENS_FACING_BACK}
            startCamera()
        }
        startCamera()

    }

    /**
     * Initializes and starts the camera with the required use cases
     */
    private fun startCamera()
    {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)}

            val imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try
            {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                isCameraInitialized = true
            }
            catch (exc: Exception)
            {
                isCameraInitialized = false
                Log.e("CameraFragment", "Use case binding failed", exc)
            }

            binding.buttonTakePhoto.setOnClickListener {
                if (!isCameraInitialized)
                {
                    Toast.makeText(context, "Camera not ready. Try again.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val photoFile = createImageFile(requireContext())
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults)
                    {
                        val savedUri = Uri.fromFile(photoFile)
                        viewModel.selectImageUri(savedUri)
                        findNavController().navigate(R.id.imageViewFragment)
                    }

                    override fun onError(exception: ImageCaptureException)
                    { Toast.makeText(context, "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show() } })
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Creates a temporary image file in the app's private storage directory
     * @param context The context used for accessing the file system
     * @return A File object representing the created image file
     */
    private fun createImageFile(context: Context): File
    {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    /**
     * Callback for the result from requesting permissions. Handles the case of the camera permission request
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[], int)
     * @param permissions The requested permissions. Never null
     * @param grantResults The grant results for the corresponding permissions which is either PackageManager.PERMISSION_GRANTED or PackageManager.PERMISSION_DENIED. Never null
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        when (requestCode)
        {
            cameraPermissionRequestCode ->
            {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {startCamera()}
                else {Toast.makeText(context, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show()}
                return
            }
        }
    }

    /**
     * Called when the view hierarchy associated with the fragment is being destroyed
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}