package com.example.eventplanner.ui.auth

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
import com.example.eventplanner.utils.EmailAuthUtils
import com.example.eventplanner.GoogleSignInUtils

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle Google Sign-In result if needed
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
                EmailAuthUtils.loginUser(requireContext(), email, password)
            } else {
                Toast.makeText(requireContext(), "Enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.googleSignInButton.setOnClickListener {
            GoogleSignInUtils.doGoogleSignIn(
                requireContext(),
                lifecycleScope,
                googleSignInLauncher
            ) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
