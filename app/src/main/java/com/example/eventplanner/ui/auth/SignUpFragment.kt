package com.example.eventplanner.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eventplanner.R
import com.example.eventplanner.databinding.FragmentSignupBinding
import com.example.eventplanner.utils.AuthUtils

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val name = binding.nameEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                // Call AuthUtils.signUpUser with the correct parameters
                AuthUtils.signUpUser(email, password, name,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Signup successful", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(requireContext(), "Signup failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Please enter all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
