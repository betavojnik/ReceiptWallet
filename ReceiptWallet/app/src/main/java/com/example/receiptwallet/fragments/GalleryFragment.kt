package com.example.receiptwallet.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.receiptwallet.R
import com.example.receiptwallet.databinding.FragmentGalleryBinding
import com.example.receiptwallet.util.Constants
import com.example.receiptwallet.viewModel.ReceiptViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class GalleryFragment : BaseFragment(R.layout.fragment_gallery) {
    private lateinit var binding: FragmentGalleryBinding
    private var imageUri: Uri? = null
    private lateinit var viewModel: ReceiptViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity())[ReceiptViewModel::class.java]
        FragmentGalleryBinding.inflate(inflater, container, false).apply {
            binding = this
            val galleryImage = registerForActivityResult(
                ActivityResultContracts.GetContent(),
                ActivityResultCallback {
                    imageUri = it
                    val imagePath = getRealPathFromURI(imageUri!!, requireContext())
                    binding.ivGallery.setImageURI(it)
                    viewModel.validationStatus[Constants.imageTag] = true
                    val action =
                        GalleryFragmentDirections.actionGalleryFragmentToAddReceiptFragment(
                            imagePath.toString()
                        )
                    Navigation.findNavController(binding.root).navigate(action)
                })
            galleryImage.launch(Constants.ONLY_IMAGES)
            return root
        }
    }

    fun getRealPathFromURI(uri: Uri, context: Context): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        var file: File? = null
        try {
            file = File(context.filesDir, name)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = Constants.ZERO
            val maxBufferSize = Constants.MAX_BUFFER_SIZE
            val bytesAvailable: Int = inputStream?.available() ?: Constants.ZERO
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null) {
                        read = it
                    }
                } != Constants.MINUS_ONE) {
                outputStream.write(buffers, Constants.ZERO, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream?.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)
        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }

        return file?.path
    }
}