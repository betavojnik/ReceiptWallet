package com.example.receiptwallet.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptwallet.CategoryAdapter
import com.example.receiptwallet.CustomDialog
import com.example.receiptwallet.MainActivity
import com.example.receiptwallet.R
import com.example.receiptwallet.converters.CategoryConverter
import com.example.receiptwallet.databinding.FragmentDetailsBinding
import com.example.receiptwallet.models.Receipt
import com.example.receiptwallet.util.Constants
import com.example.receiptwallet.viewModel.ReceiptViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Suppress("NAME_SHADOWING")
class DetailsFragment : BaseFragment(R.layout.fragment_details),
    CategoryAdapter.OnItemClickListener {

    private lateinit var binding: FragmentDetailsBinding
    private lateinit var viewModel: ReceiptViewModel
    private val args: DetailsFragmentArgs by navArgs()
    private var isRsdSelected: Boolean? = null
    private lateinit var categoryDialog: BottomSheetDialog
    private lateinit var categoryAdapter: CategoryAdapter
    private var listOfCategories = ArrayList<String>()
    private lateinit var recyclerViewCategories: RecyclerView
    private var validationStatus = mutableMapOf<String, Boolean>()
    private var isDialogShown = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ReceiptViewModel::class.java]
        FragmentDetailsBinding.inflate(inflater, container, false).apply {
            binding = this
            updateButtonState()
            updateListOfCategories()
            (activity as AppCompatActivity).supportActionBar?.title = args.currentReceipt.name
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val formatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_PATTERN_DETAILS)
        val date = LocalDate.parse(args.currentReceipt.purchaseDate, formatter)
        val correctDateString = "${date.dayOfMonth}/${date.monthValue}/${date.year}"
        val today = Calendar.getInstance()
        val year = today.get(Calendar.YEAR)
        val month = today.get(Calendar.MONTH)
        val day = today.get(Calendar.DAY_OF_MONTH)

        binding.etName.setText(args.currentReceipt.name)
        binding.etPurchaseDate.setText(correctDateString)
        binding.etWarrantyLength.setText(args.currentReceipt.warrantyLength.toString())
        binding.etCategories.setText(convertCategoryToString(args.currentReceipt.category))

        val cost: Double = args.currentReceipt.cost
        val formattedCost: String = if (cost % 1.0 == 0.0) {
            String.format("%.2f", cost)
        } else {
            cost.toString()
        }

        binding.etCost.setText(formattedCost)

        val selectedReceipt: Receipt = args.currentReceipt

        if (args.currentReceipt.currency == Constants.Currency.EURO) {
            isRsdSelected = false
            binding.rbRsd.setBackgroundResource(R.drawable.radio_button_left_unchecked)
            binding.rbEuro.setBackgroundResource(R.drawable.radio_button_right_checked)
        } else {
            isRsdSelected = true
            binding.rbRsd.setBackgroundResource(R.drawable.radio_button_left_checked)
            binding.rbEuro.setBackgroundResource(R.drawable.radio_button_right_unchecked)
        }
        replaceDefaultImage()

        binding.etCategories.setOnClickListener {
            if (!::categoryDialog.isInitialized || !categoryDialog.isShowing) {
                showCategories()
            }
        }

        binding.etPurchaseDate.setOnClickListener {
            datePickerSetUp(year, month, day, today)
        }

        binding.rbRsd.setOnClickListener {
            isRsdSelected = true
            updateButtonState()
        }

        binding.rbEuro.setOnClickListener {
            isRsdSelected = false
            updateButtonState()
        }

        (activity as MainActivity).deleteWarrantyBtn().setOnClickListener {
            showDeleteDialog()
        }

        binding.btnDiscard.setOnClickListener {
            discardChanges(selectedReceipt)
        }

        binding.showFullScreenImage.setOnClickListener {
            if (!isDialogShown) {
                showFullImageDialog()
            }
        }
        binding.btnSaveChanges.setOnClickListener {
            val name = binding.etName.text.toString()
            val purchaseDate = binding.etPurchaseDate.text.toString()
            val warrantyLength = binding.etWarrantyLength.text.toString()
            val category = binding.etCategories.text.toString()
            val cost = binding.etCost.text.toString()
            val imageTag = getString(R.string.image_added_tag)

            validationStatus = viewModel.validateFields(
                name,
                purchaseDate,
                warrantyLength,
                category,
                cost,
                imageTag
            ).toMutableMap()

            fieldValidation()

            if (binding.tvInvalidFieldsLabel.text == "") {
                updateReceipt()
            }
        }

        binding.linearLayoutContainerForNewReceipt.setOnClickListener {
            hideKeyboard()
            binding.linearLayoutContainerForNewReceipt.clearFocus()
        }

        binding.nestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            hideKeyboard()
            binding.nestedScrollView.clearFocus()
        }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun datePickerSetUp(
        year: Int,
        month: Int,
        day: Int,
        today: Calendar
    ) {
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
        val nightModeFlags = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        } else {
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        }
        datePickerDialog.datePicker.maxDate = today.timeInMillis
    }

    private fun convertCategoryToString(category: Constants.Category): String {
        return category.name.lowercase().replace("_", " ").split(" ").joinToString(" ") {
            it.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }.replace("It", "IT").replace("Tv", "TV")
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

    private fun replaceDefaultImage() {
        if (args.currentReceipt.pictureUUID != Constants.NO_IMAGE && args.currentReceipt.pictureUUID != Constants.NULL) {
            binding.ivAddImage.setImageURI(args.currentReceipt.pictureUUID.toUri())
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

    private fun updateButtonState() {
        if (isRsdSelected == true) {
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

    override fun onItemClick(item: String) {
        binding.etCategories.text = Editable.Factory.getInstance().newEditable(item)
        categoryDialog.dismiss()
    }

    private fun navigateToHomeScreen() {
        Navigation.findNavController(binding.root)
            .navigate(R.id.action_detailsFragment_to_homeFragment)
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

    private fun updateReceipt() {
        var updatedField = false

        val name = binding.etName.text.toString()
        val purchaseDate = binding.etPurchaseDate.text.toString()
        val warrantyLength = binding.etWarrantyLength.text.toString().toInt()
        val category = CategoryConverter().toCategory(
            binding.etCategories.text.toString().replace(' ', '_').uppercase()
        )
        val cost = binding.etCost.text.toString().toDouble()
        val currency =
            if (isRsdSelected == true) Constants.Currency.RSD else Constants.Currency.EURO

        val currentReceipt = args.currentReceipt

        val formatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_PATTERN_DETAILS)
        val date = LocalDate.parse(currentReceipt.purchaseDate, formatter)
        val correctDate = "${date.dayOfMonth}/${date.monthValue}/${date.year}"

        if (name != currentReceipt.name || purchaseDate != correctDate || warrantyLength != currentReceipt.warrantyLength ||
            category != currentReceipt.category || cost != currentReceipt.cost || currency != currentReceipt.currency
        ) {
            updatedField = true
        }

        if (updatedField) {
            val updatedReceipt = Receipt(
                id = args.currentReceipt.id,
                pictureUUID = args.currentReceipt.pictureUUID,
                name = name,
                purchaseDate = purchaseDate,
                warrantyLength = warrantyLength,
                category = category,
                cost = cost,
                currency = currency
            )

            viewModel.updateReceipt(updatedReceipt)
            Toast.makeText(context, Constants.WARRANTY_UPDATED, Toast.LENGTH_SHORT).show()
        }
        navigateToHomeScreen()
    }

    private fun showDeleteDialog() {
        val dialog = CustomDialog.newInstance(
            getString(R.string.dialog_delete_title),
            getString(R.string.dialog_positive),
            getString(R.string.dialog_negative),
            {
                viewModel.removeReceiptFromDatabase(args.currentReceipt)
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_detailsFragment_to_homeFragment)
            },
            {
            }
        )
        dialog.show(requireActivity().supportFragmentManager, "CustomDialog")
    }

    private fun showFullImageDialog() {
        val customDialog = Dialog(requireContext())
        configureCustomDialog(customDialog)
        showImage(customDialog)
        customDialog.findViewById<ImageButton>(R.id.btnCloseFullScreenImage).setOnClickListener {
            customDialog.dismiss()
            isDialogShown = false
        }
        customDialog.show()
        isDialogShown = true
    }

    private fun configureCustomDialog(customDialog: Dialog) {
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customDialog.setCanceledOnTouchOutside(false)
        customDialog.setContentView(R.layout.full_screen_image_layout)

        val displayMetrics = resources.displayMetrics
        val customDialogWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val toolbarHeight = getToolbarHeight()
        val bottomNavigationView: BottomNavigationView =
            requireActivity().findViewById(R.id.bottomNavigationView)
        val bottomNavHeight = bottomNavigationView.height
        val customDialogHeight = screenHeight - toolbarHeight - bottomNavHeight

        val layoutParams: ViewGroup.LayoutParams? = customDialog.window?.attributes
        layoutParams?.width = customDialogWidth
        layoutParams?.height = customDialogHeight
        customDialog.window?.attributes = layoutParams as WindowManager.LayoutParams
    }

    private fun getToolbarHeight(): Int {
        val tv = TypedValue()
        if (requireContext().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }
        return 0
    }

    private fun showImage(customDialog: Dialog) {
        val imageView: ImageView = customDialog.findViewById(R.id.ivFullScreenImage)

        imageView.setImageURI(args.currentReceipt.pictureUUID.toUri())

        val bitmap = (imageView.drawable as? BitmapDrawable)?.bitmap
        if (bitmap != null) {
            if (bitmap.width > bitmap.height) {
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            } else {
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                val layoutParams = imageView.layoutParams
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                imageView.layoutParams = layoutParams
            }
        }
    }

    private fun showDiscardDialog() {
        val dialog = CustomDialog.newInstance(
            getString(R.string.dialog_discard_title),
            getString(R.string.dialog_positive),
            getString(R.string.dialog_negative),
            {
                navigateToHomeScreen()
            },
            {
            }
        )
        dialog.show(requireActivity().supportFragmentManager, "CustomDialog")
    }

    private fun discardChanges(selectedReceipt: Receipt) {
        val categoryString = convertCategoryToString(selectedReceipt.category)
        val formatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_PATTERN_DETAILS)
        val date = LocalDate.parse(args.currentReceipt.purchaseDate, formatter)
        val correctDateString = "${date.dayOfMonth}/${date.monthValue}/${date.year}"

        val cost: Double = selectedReceipt.cost
        val formattedCost: String = if (cost % 1.0 == 0.0) {
            String.format("%.2f", cost)
        } else {
            cost.toString()
        }

        if (binding.etName.text.toString() == selectedReceipt.name && binding.etPurchaseDate.text.toString() == correctDateString
            && binding.etCategories.text.toString() == categoryString && selectedReceipt.currency == Constants.Currency.EURO
            && binding.etCost.text.toString() == formattedCost && binding.etWarrantyLength.text.toString() == selectedReceipt.warrantyLength.toString()
            && binding.rbRsd.isClickable
        ) navigateToHomeScreen()
        else if (binding.etName.text.toString() == selectedReceipt.name && binding.etPurchaseDate.text.toString() == correctDateString
            && binding.etCategories.text.toString() == categoryString && selectedReceipt.currency == Constants.Currency.RSD
            && binding.etCost.text.toString() == formattedCost && binding.etWarrantyLength.text.toString() == selectedReceipt.warrantyLength.toString()
            && binding.rbEuro.isClickable
        ) navigateToHomeScreen()
        else showDiscardDialog()
    }
}