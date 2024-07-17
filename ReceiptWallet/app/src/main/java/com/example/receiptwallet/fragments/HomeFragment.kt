package com.example.receiptwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.receiptwallet.R
import com.example.receiptwallet.adapters.ReceiptsAdapter
import com.example.receiptwallet.databinding.FragmentHomeBinding
import com.example.receiptwallet.dto.ReceiptDto
import com.example.receiptwallet.models.Receipt
import com.example.receiptwallet.util.Constants.MINUS_ONE
import com.example.receiptwallet.util.Constants.ZERO
import com.example.receiptwallet.viewModel.ReceiptViewModel
import java.util.Locale

class HomeFragment : BaseFragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: ReceiptViewModel
    private lateinit var receiptsAdapter: ReceiptsAdapter
    private var allReceipts: ArrayList<ReceiptDto>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ReceiptViewModel::class.java]
        FragmentHomeBinding.inflate(inflater, container, false).apply {
            binding = this
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiptsAdapter = ReceiptsAdapter(requireContext())
        prepareAdapter()

        viewModel.getAllReceipts()
        val inputManager = requireActivity().getSystemService(InputMethodManager::class.java)
        viewModel.receiptDtoLiveData.observe(viewLifecycleOwner) { receipts ->
            if (receipts.isNotEmpty()) {
                receiptsAdapter.setReceipts(ArrayList(receipts))
            }
            allReceipts = ArrayList(receipts)
        }

        searchReceipts(inputManager)

        receiptsAdapter.onItemClick = {
            val receiptToSend = Receipt(
                it.id,
                it.pictureUUID,
                it.name,
                it.buyingDate.toString(),
                it.guarantee,
                it.category,
                it.cost,
                it.currency
            )
            val action =
                HomeFragmentDirections.actionHomeFragmentToDetailsFragment(receiptToSend)
            Navigation.findNavController(binding.root).navigate(action)
        }
        navigateToNewReceiptScreen(binding)
    }

    private fun navigateToNewReceiptScreen(binding: FragmentHomeBinding) {
        binding.newReceiptButton.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_homeFragment_to_addReceiptFragment)
        }
    }

    private fun prepareAdapter() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = receiptsAdapter
        }
    }

    private fun searchReceipts(inputManager: InputMethodManager) {
        binding.iconImageView.setOnClickListener {
            if (binding.etSearch.visibility == View.INVISIBLE) {
                binding.etSearch.visibility = View.VISIBLE
                binding.etSearch.requestFocus()
                inputManager.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)

                binding.etSearch.addTextChangedListener { editable ->
                    editable?.let {
                        if (editable.toString().isNotEmpty()) {
                            val filteredList = ArrayList<ReceiptDto>()
                            if (allReceipts != null && allReceipts?.isNotEmpty() == true) {
                                allReceipts?.let { listOfDto ->
                                    for (receipt in listOfDto) {
                                        if (receipt.name.lowercase(Locale.ROOT)
                                                .contains(
                                                    editable.toString().lowercase(Locale.ROOT)
                                                )
                                        )
                                            filteredList.add(receipt)
                                    }
                                }
                                receiptsAdapter.setReceipts(filteredList)
                                updateVisibility(filteredList.size, isSearchEmpty = false)
                            } else {
                                updateVisibility(0, isSearchEmpty = false)
                            }
                        } else {
                            if (allReceipts != null && allReceipts?.isNotEmpty() == true) {
                                allReceipts?.let { listOfDto ->
                                    receiptsAdapter.setReceipts(listOfDto)
                                    updateVisibility(listOfDto.size, isSearchEmpty = true)
                                }
                            } else {
                                updateVisibility(0, isSearchEmpty = true)
                            }
                        }
                    }
                }
            } else {
                inputManager.hideSoftInputFromWindow(view?.windowToken, ZERO)
                binding.etSearch.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateVisibility(itemCount: Int, isSearchEmpty: Boolean) {
        if (itemCount == ZERO && !isSearchEmpty) {
            binding.tvNoResultsFound.text = getString(R.string.no_results_found)
            binding.tvNoResultsFound.setCompoundDrawablesRelativeWithIntrinsicBounds(
                ZERO,
                ZERO,
                R.drawable.no_result_found,
                ZERO
            )
        } else {
            binding.tvNoResultsFound.text = ""
            binding.tvNoResultsFound.setCompoundDrawablesRelativeWithIntrinsicBounds(ZERO, ZERO, ZERO, ZERO)
        }
    }
}