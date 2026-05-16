package com.plantdisease.detector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import android.widget.CheckBox

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Google giriş başarısız: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        // Google Sign-In yapılandırması
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etName            = findViewById<EditText>(R.id.etName)
        val etEmail           = findViewById<EditText>(R.id.etEmail)
        val etPassword        = findViewById<EditText>(R.id.etPassword)
        val etPasswordConfirm = findViewById<EditText>(R.id.etPasswordConfirm)
        val btnRegister       = findViewById<Button>(R.id.btnRegister)
        val btnGoogleRegister = findViewById<LinearLayout>(R.id.btnGoogleRegister)
        val tvLogin           = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val cbKvkk = findViewById<CheckBox>(R.id.cbKvkk)
            if (!cbKvkk.isChecked) {
                Toast.makeText(this, "Lütfen KVKK metnini onaylayın!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name            = etName.text.toString().trim()
            val email           = etEmail.text.toString().trim()
            val password        = etPassword.text.toString().trim()
            val passwordConfirm = etPasswordConfirm.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(this, "Tüm alanları doldurunuz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(this, "Şifreler eşleşmiyor!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        auth.currentUser?.updateProfile(profileUpdates)
                        Toast.makeText(this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        btnGoogleRegister.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google ile giriş başarılı!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Kimlik doğrulama hatası: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}