package com.example.eventplanner.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.eventplanner.R
import com.example.eventplanner.databinding.FragmentLoginBinding
import com.example.eventplanner.utils.AuthUtils
import com.example.eventplanner.GoogleSignInUtils

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize GoogleSignInClient
        GoogleSignInUtils.initGoogleSignInClient(requireContext())

        // Register the activity result launcher to handle Google sign-in result
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                GoogleSignInUtils.handleSignInResult(
                    requireContext(),
                    data,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to the home screen after successful sign-in
                        findNavController().navigate(R.id.action_loginFragment_to_homeScreen)
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(requireContext(), "Google Sign-In failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                AuthUtils.loginUser(
                    email,
                    password,
                    onSuccess = {
                        // Navigate to the home screen after successful login
                        findNavController().navigate(R.id.action_loginFragment_to_homeScreen)
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(requireContext(), "Login failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In button click listener
        binding.googleSignInButton.setOnClickListener {
            GoogleSignInUtils.launchGoogleSignIn(googleSignInLauncher)
        }

        // Navigate to Sign-Up screen
        binding.signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
