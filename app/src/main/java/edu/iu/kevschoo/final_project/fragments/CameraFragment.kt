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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}