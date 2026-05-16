package com.plantdisease.detector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.material.textfield.TextInputEditText
import android.widget.CheckBox

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        // Zaten giriş yapılmışsa direkt ana sayfaya git
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        val etEmail    = findViewById<android.widget.EditText>(R.id.etEmail)
        val etPassword = findViewById<android.widget.EditText>(R.id.etPassword)
        val btnLogin   = findViewById<Button>(R.id.btnLogin)
        val btnGoogle  = findViewById<Button>(R.id.btnGoogleLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val cbKvkk     = findViewById<CheckBox>(R.id.cbKvkk)  // ← EKLE

        btnLogin.setOnClickListener {
            if (!cbKvkk.isChecked) {
                Toast.makeText(this, "Lütfen KVKK metnini onaylayın!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "E-posta ve şifre giriniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        goToMain()
                    } else {
                        Toast.makeText(this, "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Google ile giriş
        btnGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, gso)
            startActivityForResult(googleClient.signInIntent, RC_SIGN_IN)
        }

        // Kayıt ol sayfasına git
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            goToMain()
                        } else {
                            Toast.makeText(this, "Google girişi başarısız!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}