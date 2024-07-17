package com.example.receiptwallet.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptwallet.CategoryAdapter
import com.example.receiptwallet.CustomDialog
import com.example.receiptwallet.R
import com.example.receiptwallet.databinding.FragmentAddReceiptBinding
import com.example.receiptwallet.models.Receipt
import com.example.receiptwallet.util.Constants
import com.example.receiptwallet.viewModel.ReceiptViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar

class AddReceiptFragment : BaseFragment(R.layout.fragment_add_receipt),
    CategoryAdapter.OnItemClickListener {

    private lateinit var binding: FragmentAddReceiptBinding
    private lateinit var viewModel: ReceiptViewModel
    private lateinit var categoryDialog: BottomSheetDialog
    private lateinit var categoryAdapter: CategoryAdapter
    private var listOfCategories = ArrayList<String>()
    private lateinit var recyclerViewCategories: RecyclerView
    private val args: AddReceiptFragmentArgs by navArgs()
    private var isRsdSelected = true
    private var validationStatus = mutableMapOf<String, Boolean>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ReceiptViewModel::class.java]
        FragmentAddReceiptBinding.inflate(inflater, container, false).apply {
            binding = this
            updateListOfCategories()
            resetErrorIndicator()
            replaceDefaultImage()
            updateButtonState()
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val today = Calendar.getInstance()
        val year = today.get(Calendar.YEAR)
        val month = today.get(Calendar.MONTH)
        val day = today.get(Calendar.DAY_OF_MONTH)

        retrieveData()

        binding.etPurchaseDate.setOnClickListener {
            hideKeyboard()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                R.style.CustomDatePickerDialog,
                { _, selectedYear, selectedMonth, selectedDay ->
                    binding.etPurchaseDate.text =
                        Editable.Factory.getInstance()
                            .newEditable("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
            val nightModeFlags = requireContext().resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
            if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.WHITE)
                datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.WHITE)
            }
            else{
                datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.BLACK)
                datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.BLACK)
            }
            datePickerDialog.datePicker.maxDate = today.timeInMillis
        }

        binding.linearLayoutContainerForNewReceipt.setOnClickListener {
            hideKeyboard()
            binding.linearLayoutContainerForNewReceipt.clearFocus()
        }

        binding.nestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            hideKeyboard()
            binding.nestedScrollView.clearFocus()
        }

        binding.rbRsd.setOnClickListener {
            isRsdSelected = true
            updateButtonState()
        }

        binding.rbEuro.setOnClickListener {
            isRsdSelected = false
            updateButtonState()
        }

        binding.etCategories.setOnClickListener {
            if (!::categoryDialog.isInitialized || !categoryDialog.isShowing) {
                showCategories()
            }
        }

        binding.btnCancel.setOnClickListener {
            if (areAllFieldsEmpty()) {
                emptyAllFields()
                navigateToHomeScreen()
            } else {
                showCancelDialog()
            }
        }

        binding.ivAddImage.setOnClickListener {
            if (args.photoLink == Constants.NO_IMAGE || args.photoLink == Constants.NULL) {
                saveData()
                showCameraDialogBox()
            }
        }

        binding.btnSaveNewReceipt.setOnClickListener {
            val name = binding.etName.text.toString()
            val purchaseDate = binding.etPurchaseDate.text.toString()
            val warrantyLength = binding.etWarrantyLength.text.toString()
            val category = binding.etCategories.text.toString()
            val cost = binding.etCost.text.toString()
            val imageTag = binding.ivAddImage.tag.toString()

            validationStatus = viewModel.validateFields(
                name,
                purchaseDate,
                warrantyLength,
                category,
                cost,
                imageTag
            ).toMutableMap()

            fieldValidation()

            saveNewReceiptIntoDataBase()
        }
    }

    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun saveNewReceiptIntoDataBase() {
        if (binding.tvInvalidFieldsLabel.text == "" && binding.ivAddImage.tag == getString(R.string.image_added_tag)) {
            viewModel.addNewReceiptToDatabase(
                Receipt(
                    Constants.ZERO,
                    args.photoLink,
                    binding.etName.text.toString(),
                    binding.etPurchaseDate.text.toString(),
                    binding.etWarrantyLength.text.toString().toInt(),
                    enumValueOf(
                        binding.etCategories.text.toString().replace(' ', '_').uppercase()
                    ),
                    binding.etCost.text.toString().toDouble(),
                    if (isRsdSelected) Constants.Currency.RSD else Constants.Currency.EURO
                )
            )
            emptyAllFields()
            Toast.makeText(requireContext(), Constants.WARRANTY_ADDED, Toast.LENGTH_LONG).show()
            emptyAllFields()
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_addReceiptFragment_to_homeFragment)
        }
    }

    private fun showCameraDialogBox() {
        val dialog = cameraDialog()
        val dialogCloseButton: ImageButton? = dialog.findViewById(R.id.btnXCameraDialog)
        val openCamera: TextView? = dialog.findViewById(R.id.tvOpenCamera)
        val uploadPicture: TextView? = dialog.findViewById(R.id.tvUploadPicture)
        dialogCloseButton?.setOnClickListener {
            dialog.dismiss()
        }
        cameraOption(openCamera, dialog)
        galleryOption(uploadPicture, dialog)
    }

    private fun replaceDefaultImage() {
        if (args.photoLink != Constants.NO_IMAGE && args.photoLink != Constants.NULL) {
            binding.ivAddImage.setImageURI(args.photoLink.toUri())
            binding.ivAddImage.tag = getString(R.string.image_added_tag)
        }
    }

    private fun galleryOption(uploadPicture: TextView?, dialog: Dialog) {
        uploadPicture?.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_addReceiptFragment_to_galleryFragment)
            dialog.dismiss()
        }
    }

    private fun cameraOption(openCamera: TextView?, dialog: Dialog) {
        openCamera?.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_addReceiptFragment_to_cameraFragment)
            dialog.dismiss()
        }
    }

    private fun cameraDialog(): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.camera_dialog_popup)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        return dialog
    }

    private fun updateButtonState() {
        if (isRsdSelected) {
            binding.rbRsd.isClickable = false
            binding.rbEuro.isClickable = true
            binding.rbRsd.setBackgroundResource(R.drawable.radio_button_left_checked)
            binding.rbEuro.setBackgroundResource(R.drawable.radio_button_right_unchecked)
        } else {
            binding.rbEuro.isClickable = false
            binding.rbRsd.isClickable = true
            binding.rbRsd.setBackgroundResource(R.drawable.radio_button_left_unchecked)
            binding.rbEuro.setBackgroundResource(R.drawable.radio_button_right_checked)
        }
    }

    private fun updateListOfCategories() {
        listOfCategories.addAll(Constants.Category.values().map { category ->
            category.name.lowercase().replace("_", " ").split(" ").joinToString(" ") {
                it.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase() else char.toString()
                }
            }.replace("It", "IT").replace("Tv", "TV")
        })
    }

    override fun onItemClick(item: String) {
        binding.etCategories.text = Editable.Factory.getInstance().newEditable(item)
        categoryDialog.dismiss()
    }

    private fun showCategories() {
        val dialogView = View.inflate(context, R.layout.category_dialog, null)
        categoryDialog = BottomSheetDialog(requireContext(), R.style.CategoryDialogTheme)
        categoryDialog.setContentView(dialogView)
        recyclerViewCategories = dialogView.findViewById(R.id.recyclerViewCategories)
        categoryAdapter = CategoryAdapter(listOfCategories, this)
        recyclerViewCategories.adapter = categoryAdapter
        categoryDialog.show()
    }

    private fun areAllFieldsEmpty(): Boolean {
        val isImageViewEmpty = binding.ivAddImage.tag == getString(R.string.image_icon_tag)
        val isNameEmpty = binding.etName.text.toString().isEmpty()
        val isPurchaseDateEmpty = binding.etPurchaseDate.text.toString().isEmpty()
        val isWarrantyLengthEmpty = binding.etWarrantyLength.text.toString().isEmpty()
        val isCategoryEmpty = binding.etCategories.text.toString().isEmpty()
        val isCostEmpty = binding.etCost.text.toString().isEmpty()

        return isRsdSelected && isImageViewEmpty && isNameEmpty && isPurchaseDateEmpty && isWarrantyLengthEmpty && isCategoryEmpty && isCostEmpty
    }

    private fun showCancelDialog() {
        val dialog = CustomDialog.newInstance(
            getString(R.string.dialog_title),
            getString(R.string.dialog_positive),
            getString(R.string.dialog_negative),
            {
                navigateToHomeScreen()
                emptyAllFields()
            },
            {

            }
        )
        dialog.show(requireActivity().supportFragmentManager, "CustomDialog")
    }

    private fun navigateToHomeScreen() {
        Navigation.findNavController(binding.root)
            .navigate(R.id.action_addReceiptFragment_to_homeFragment)
    }

    private fun resetErrorIndicator() {
        binding.tvInvalidFieldsLabel.text = ""
        binding.etName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        binding.etPurchaseDate.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        binding.etWarrantyLength.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        binding.etCategories.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        binding.etCost.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
    }

    private fun emptyAllFields() {
        viewModel.validationStatus.clear()
        viewModel.nameValue = ""
        viewModel.purchaseDateValue = ""
        viewModel.warrantyLengthValue = ""
        viewModel.categoryValue = ""
        viewModel.costValue = ""
        viewModel.currencyValue = getString(R.string.rsd)
        binding.ivAddImage.tag = getString(R.string.image_icon_tag)
    }

    private fun retrieveData() {
        validationStatus = viewModel.validationStatus
        binding.ivAddImage.tag = if (validationStatus[Constants.imageTag] == true) {
            getString(R.string.image_added_tag)
        } else {
            getString(R.string.image_icon_tag)
        }
        binding.etName.setText(viewModel.nameValue)
        binding.etPurchaseDate.setText(viewModel.purchaseDateValue)
        binding.etWarrantyLength.setText(viewModel.warrantyLengthValue)
        binding.etCategories.setText(viewModel.categoryValue)
        binding.etCost.setText(viewModel.costValue)
        if (viewModel.currencyValue == getString(R.string.rsd) || viewModel.currencyValue.isEmpty()) {
            isRsdSelected = true
            updateButtonState()
        } else {
            isRsdSelected = false
            updateButtonState()
        }

        fieldValidation()
    }

    private fun saveData() {
        viewModel.validationStatus = validationStatus
        viewModel.nameValue = binding.etName.text.toString()
        viewModel.purchaseDateValue = binding.etPurchaseDate.text.toString()
        viewModel.warrantyLengthValue = binding.etWarrantyLength.text.toString()
        viewModel.categoryValue = binding.etCategories.text.toString()
        viewModel.costValue = binding.etCost.text.toString()
        if (isRsdSelected) {
            viewModel.currencyValue = getString(R.string.rsd)
        } else {
            viewModel.currencyValue = getString(R.string.euro)
        }
    }

    private fun fieldValidation() {
        var hasInvalidInput = false

        if (validationStatus.values.any { !it }) {
            binding.tvInvalidFieldsLabel.text = getString(R.string.invalid_fields)
            hasInvalidInput = true
        }

        for ((fieldName, isValid) in validationStatus) {
            val editText = when (fieldName) {
                Constants.name -> binding.etName
                Constants.purchaseDate -> binding.etPurchaseDate
                Constants.warrantyLength -> binding.etWarrantyLength
                Constants.category -> binding.etCategories
                Constants.cost -> binding.etCost
                else -> null
            }

            editText?.let {
                if (!isValid) {
                    it.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.error_indicator,
                        0
                    )
                    hasInvalidInput = true
                } else {
                    it.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
        }

        if (hasInvalidInput) {
            binding.nestedScrollView.post {
                binding.nestedScrollView.smoothScrollTo(0, binding.tvInvalidFieldsLabel.bottom)
            }
        } else {
            binding.tvInvalidFieldsLabel.text = ""
        }
    }
}