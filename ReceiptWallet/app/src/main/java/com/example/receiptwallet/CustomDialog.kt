package com.example.receiptwallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.google.android.material.textview.MaterialTextView

class CustomDialog : DialogFragment() {

    private var title: String? = null
    private var positiveButtonText: String? = null
    private var negativeButtonText: String? = null
    private var positiveButtonClickListener: (() -> Unit)? = null
    private var negativeButtonClickListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialog()
        setTitle()
        setPositiveButton()
        setNegativeButton()
    }

    private fun setupDialog() {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setTitle() {
        view?.findViewById<MaterialTextView>(R.id.tvDialogTitle)?.text = title
    }

    private fun setPositiveButton() {
        val btnPositive = view?.findViewById<AppCompatButton>(R.id.btnPositiveAnswer)
        btnPositive?.text = positiveButtonText
        btnPositive?.setOnClickListener {
            positiveButtonClickListener?.invoke()
            dismiss()
        }
    }

    private fun setNegativeButton() {
        val btnNegative = view?.findViewById<AppCompatButton>(R.id.btnNegativeAnswer)
        btnNegative?.text = negativeButtonText
        btnNegative?.setOnClickListener {
            negativeButtonClickListener?.invoke()
            dismiss()
        }
    }

    companion object {
        fun newInstance(
            title: String,
            positiveButtonText: String,
            negativeButtonText: String,
            positiveButtonClickListener: () -> Unit,
            negativeButtonClickListener: () -> Unit
        ): CustomDialog {
            return CustomDialog().apply {
                this.title = title
                this.positiveButtonText = positiveButtonText
                this.negativeButtonText = negativeButtonText
                this.positiveButtonClickListener = positiveButtonClickListener
                this.negativeButtonClickListener = negativeButtonClickListener
            }
        }
    }
}
