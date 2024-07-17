package com.example.receiptwallet

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.receiptwallet.databinding.ActivityMainBinding
import com.example.receiptwallet.util.Constants
import com.example.receiptwallet.viewModel.ReceiptViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedViewModel: ReceiptViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(this)[ReceiptViewModel::class.java]
        getPermissions()
        viewBindingSetUp()
        settingSupportBarUp()
        settingNavControllerUp()
        handleDestinationRequiredSettings()
    }

    private fun handleDestinationRequiredSettings() {
        findNavController(R.id.fragmentContainerView).addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    supportActionBar?.show()
                    binding.bottomNavigationView.makeVisible(true)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    binding.deleteWarrantyBtn.makeVisible(false)
                }

                R.id.detailsFragment -> {
                    supportActionBar?.show()
                    binding.bottomNavigationView.makeVisible(false)
                    supportActionBar?.show()
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    binding.deleteWarrantyBtn.makeVisible(true)
                }

                R.id.expensesFragment -> {
                    supportActionBar?.show()
                    binding.bottomNavigationView.makeVisible(true)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    binding.deleteWarrantyBtn.makeVisible(false)
                }

                R.id.addReceiptFragment -> {
                    binding.bottomNavigationView.makeVisible(false)
                    supportActionBar?.show()
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    binding.deleteWarrantyBtn.makeVisible(false)
                }

                R.id.detailsFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    binding.deleteWarrantyBtn.makeVisible(false)
                }

                R.id.cameraFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    supportActionBar?.hide()
                    binding.deleteWarrantyBtn.makeVisible(false)
                }

                R.id.galleryFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    supportActionBar?.hide()
                    binding.deleteWarrantyBtn.makeVisible(false)
                }

                else -> binding.bottomNavigationView.makeVisible(true)
            }
        }
    }

    private fun settingNavControllerUp() {
        val navController = findNavController(R.id.fragmentContainerView)
        binding.bottomNavigationView.setupWithNavController(navController)

        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.homeFragment, R.id.expensesFragment))

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun settingSupportBarUp() {
        setSupportActionBar(binding.actionNameBar)
    }

    fun deleteWarrantyBtn() = binding.deleteWarrantyBtn

    private fun viewBindingSetUp() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun getPermissions() {
        val permissionsList = mutableListOf<String>()
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(android.Manifest.permission.CAMERA)
        if (permissionsList.size > Constants.ZERO)
            requestPermissions(permissionsList.toTypedArray(), Constants.REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!Constants.notificationShown)
            if (!gratedAllPermissions())
                Constants.notificationShown = true
    }

    private fun gratedAllPermissions(): Boolean {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            return false
        return true
    }
}

private fun ImageButton.makeVisible(visible: Boolean) {
    if (visible)
        this.visibility = View.VISIBLE
    else
        this.visibility = View.GONE
}

private fun BottomNavigationView.makeVisible(visible: Boolean) {
    if (visible)
        this.visibility = View.VISIBLE
    else
        this.visibility = View.GONE
}