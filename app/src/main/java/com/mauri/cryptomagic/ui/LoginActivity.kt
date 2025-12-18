package com.mauri.cryptomagic.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.mauri.cryptomagic.MainActivity
import com.mauri.cryptomagic.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var codeScanner: CodeScanner

    // State to distinguish if we are scanning for Registration or Login
    private var isRegistering = false
    private var tempEmail = ""
    private var tempPassword = ""
    private var isScanProcessing = false

    // Permission Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startScanning()
        } else {
            Toast.makeText(this, "Camera permission is required to scan QR codes.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Check current session
        val currentUser = auth.currentUser
        if (currentUser != null) {
             goToMainActivity()
        }
        
        setupListeners()
    }

    // Removed setupCodeScanner from onCreate to prevent stale surface references.
    // We initialize it right before scanning.

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                isRegistering = false
                loginStep1(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                isRegistering = true
                tempEmail = email
                tempPassword = password
                showScanPrompt("Scan ANY QR code to use as your Key")
            } else {
                Toast.makeText(this, "Enter email and password to create account", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnInfo.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        binding.btnScan2FA.setOnClickListener {
            checkCameraPermissionAndStart()
        }
        
        binding.btnCancel2FA.setOnClickListener {
            auth.signOut()
            showLoginForm()
        }
    }

    // --- REGISTRATION FLOW ---

    private fun completeRegistration(qrCode: String) {
        binding.progressBarLogin.visibility = View.VISIBLE
        // We don't call releaseResources here to avoid black screen issues if retried immediately,
        // but we rely on the view being hidden eventually or onPause.
        // However, to stop processing multiple times:
        if (!::codeScanner.isInitialized) return
        
        // 1. Create Auth User
        auth.createUserWithEmailAndPassword(tempEmail, tempPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 2. Save QR to Firestore
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userMap = hashMapOf(
                            "qrKey" to qrCode
                        )
                        db.collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                binding.progressBarLogin.visibility = View.GONE
                                Toast.makeText(this, "Account & Key Registered!", Toast.LENGTH_SHORT).show()
                                goToMainActivity()
                            }
                            .addOnFailureListener { e ->
                                binding.progressBarLogin.visibility = View.GONE
                                Toast.makeText(this, "Failed to save Key: ${e.message}", Toast.LENGTH_LONG).show()
                                showLoginForm()
                            }
                    }
                } else {
                    binding.progressBarLogin.visibility = View.GONE
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    showLoginForm()
                }
            }
    }

    // --- LOGIN FLOW ---

    private fun loginStep1(email: String, password: String) {
        binding.progressBarLogin.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBarLogin.visibility = View.GONE
                if (task.isSuccessful) {
                    // Password correct. Now ask for QR
                    showScanPrompt("Scan your personal Key QR")
                } else {
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun verifyLoginQR(scannedCode: String) {
        binding.progressBarLogin.visibility = View.VISIBLE
        
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                binding.progressBarLogin.visibility = View.GONE
                if (document != null && document.exists()) {
                    val savedQR = document.getString("qrKey")
                    if (savedQR == scannedCode) {
                        Toast.makeText(this, "Access Granted!", Toast.LENGTH_SHORT).show()
                        goToMainActivity()
                    } else {
                        Toast.makeText(this, "WRONG KEY! Access Denied.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                        showLoginForm()
                    }
                } else {
                    Toast.makeText(this, "No Key configured for this account.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                    showLoginForm()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBarLogin.visibility = View.GONE
                Toast.makeText(this, "Error checking Key: ${e.message}", Toast.LENGTH_SHORT).show()
                auth.signOut()
                showLoginForm()
            }
    }

    // --- UI HELPERS ---

    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startScanning()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScanning() {
        binding.btnScan2FA.visibility = View.GONE // Hide the button once scanning starts
        isScanProcessing = false // Reset flag

        // Re-initialize CodeScanner to ensure it attaches to the fresh Surface
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        
        codeScanner = CodeScanner(this, binding.scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                if (isScanProcessing) return@runOnUiThread
                isScanProcessing = true
                
                // Stop preview to freeze the image (optional) and prevent more callbacks
                codeScanner.releaseResources() 
                
                val scannedCode = it.text
                if (isRegistering) {
                    completeRegistration(scannedCode)
                } else {
                    verifyLoginQR(scannedCode)
                }
            }
        }
        
        codeScanner.errorCallback = com.budiyev.android.codescanner.ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
        
        codeScanner.startPreview()
    }

    private fun showScanPrompt(message: String) {
        binding.loginFormLayout.visibility = View.GONE
        binding.layout2FA.visibility = View.VISIBLE
        // Initially show the "Scan QR" button, user taps to start camera if needed
        binding.btnScan2FA.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showLoginForm() {
        binding.loginFormLayout.visibility = View.VISIBLE
        binding.layout2FA.visibility = View.GONE
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // If we are in scanning mode and scanner is initialized, ensure preview is running
        if (binding.layout2FA.visibility == View.VISIBLE && 
            binding.btnScan2FA.visibility == View.GONE && 
            ::codeScanner.isInitialized) {
             codeScanner.startPreview()
        }
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }
}
