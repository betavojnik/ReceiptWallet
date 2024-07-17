package com.example.receiptwallet.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.receiptwallet.R
import com.example.receiptwallet.databinding.FragmentCameraBinding
import com.example.receiptwallet.util.Constants
import com.example.receiptwallet.viewModel.ReceiptViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraFragment : BaseFragment(R.layout.fragment_camera) {
    private lateinit var binding: FragmentCameraBinding
    private var imageCaputre: ImageCapture? = null
    private var photoFile: File? = null
    private var savedUri: Uri? = null
    private lateinit var outputDirectory: File
    private lateinit var viewModel: ReceiptViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity())[ReceiptViewModel::class.java]
        FragmentCameraBinding.inflate(inflater, container, false).apply {
            binding = this
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        outputDirectory = getOutputDirectory()
        startCamera()
        takeAPhoto()
        returnToNewReceiptFromCamera()
        saveImage()
        retakeImage()
        discardImage()
        checkForPermissions(view)
    }

    private fun checkForPermissions(view: View) {
        if (Constants.notificationShown)
            basicAlert(view)
    }

    private fun discardImage() {
        binding.btnDiscardImage.setOnClickListener {
            photoFile?.delete()
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_cameraFragment_to_addReceiptFragment)
        }
    }

    private fun retakeImage() {
        binding.btnRetakeImage.setOnClickListener {
            binding.btnTakeAPhoto.visibility = View.VISIBLE
            binding.pvCamera.visibility = View.VISIBLE
            binding.btnBackToCamera.visibility = View.VISIBLE
            binding.btnDiscardImage.visibility = View.GONE
            binding.ivPicturePreview.visibility = View.GONE
            binding.btnSaveImage.visibility = View.GONE
            binding.btnRetakeImage.visibility = View.GONE
            photoFile?.delete()
        }
    }

    private fun saveImage() {
        binding.btnSaveImage.setOnClickListener {
            viewModel.validationStatus[Constants.imageTag] = true
            val action =
                CameraFragmentDirections.actionCameraFragmentToAddReceiptFragment(savedUri.toString())
            Navigation.findNavController(binding.root).navigate(action)
        }
    }

    private fun returnToNewReceiptFromCamera() {
        binding.btnBackToCamera.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_cameraFragment_to_addReceiptFragment)
        }
    }

    private fun takeAPhoto() {
        binding.btnTakeAPhoto.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.pvCamera.surfaceProvider)
                }
            imageCaputre = ImageCapture.Builder().build()
            val cameraSelectior = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelectior, preview, imageCaputre)
            } catch (e: Exception) {
                Log.d(Constants.TAG_CAMERA, Constants.TAG_CAMERA_MESSAGE, e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir
        else
            requireActivity().filesDir
    }

    private fun takePhoto() {
        val imageCapture = imageCaputre ?: return
        photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                Constants.FILE_NAME_FORMAT,
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + Constants.JPG_EXT
        )

        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile!!).build()

        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    savedUri = Uri.fromFile(photoFile)
                    binding.btnTakeAPhoto.visibility = View.GONE
                    binding.btnBackToCamera.visibility = View.GONE
                    binding.pvCamera.visibility = View.GONE
                    binding.btnDiscardImage.visibility = View.VISIBLE
                    binding.ivPicturePreview.visibility = View.VISIBLE
                    binding.ivPicturePreview.setImageURI(savedUri)
                    binding.btnSaveImage.visibility = View.VISIBLE
                    binding.btnRetakeImage.visibility = View.VISIBLE
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d(Constants.TAG_CAMERA, Constants.TAG_CAMERA_IMAGE_SAVE_MESSAGE, exception)
                }
            }
        )
    }

    fun basicAlert(view: View) {
        val builder = AlertDialog.Builder(view.context,R.style.PermissionsDialogTheme)
        builder.setTitle(Constants.RECEIPT_WALLET)
        builder.setMessage(Constants.PERMISSIONS_NEEDED)
        builder.setIcon(R.drawable.app_icon_transparent)
        builder.setPositiveButton(Constants.OK){dialogInterface, which ->
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_cameraFragment_to_addReceiptFragment)
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
