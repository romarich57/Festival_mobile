package com.projetmobile.mobile.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.projetmobile.mobile.MainActivity
import com.projetmobile.mobile.databinding.ActivityLoginBinding
import com.projetmobile.mobile.network.ApiClient
import com.projetmobile.mobile.ui.ApiRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        val repository = ApiRepository(ApiClient.apiService)
        LoginViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val identifier = binding.identifierInput.text.toString()
            val password = binding.passwordInput.text.toString()
            viewModel.login(identifier, password)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LoginUiState.Idle -> {
                            binding.loadingIndicator.visibility = View.GONE
                            binding.loginButton.isEnabled = true
                            binding.errorText.visibility = View.GONE
                        }
                        is LoginUiState.Loading -> {
                            binding.loadingIndicator.visibility = View.VISIBLE
                            binding.loginButton.isEnabled = false
                            binding.errorText.visibility = View.GONE
                        }
                        is LoginUiState.Success -> {
                            binding.loadingIndicator.visibility = View.GONE
                            binding.loginButton.isEnabled = true
                            binding.errorText.visibility = View.GONE
                            
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                            
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        is LoginUiState.Error -> {
                            binding.loadingIndicator.visibility = View.GONE
                            binding.loginButton.isEnabled = true
                            binding.errorText.visibility = View.VISIBLE
                            binding.errorText.text = state.message
                        }
                    }
                }
            }
        }
    }
}
