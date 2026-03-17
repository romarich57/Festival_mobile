package com.projetmobile.mobile

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.projetmobile.mobile.databinding.ActivityMainBinding
import com.projetmobile.mobile.data.remote.ApiClient
import com.projetmobile.mobile.data.remote.ApiRepository
import com.projetmobile.mobile.ui.screens.MainUiState
import com.projetmobile.mobile.ui.screens.MainViewModel
import com.projetmobile.mobile.ui.screens.MainViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        val repository = ApiRepository(ApiClient.apiService)
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseUrlValue.text = BuildConfig.API_BASE_URL

        binding.checkApiButton.setOnClickListener {
            viewModel.checkHealth()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: MainUiState) {
        binding.loadingBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
        binding.checkApiButton.isEnabled = !state.isLoading
        binding.statusText.text = state.message

        binding.statusText.setTextColor(
            if (state.isError) {
                Color.parseColor("#C62828")
            } else {
                Color.parseColor("#1B5E20")
            },
        )
    }
}
