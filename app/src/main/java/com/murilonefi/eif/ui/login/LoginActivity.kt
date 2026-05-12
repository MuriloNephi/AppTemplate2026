package com.murilonefi.eif.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.murilonefi.eif.MainActivity
import com.murilonefi.eif.R

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var googleSignInButton: SignInButton

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Launcher para resultado do Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Erro Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Se já estiver logado, vai direto para a tela principal
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Vincular views
        emailEditText      = findViewById(R.id.edit_text_email)
        passwordEditText   = findViewById(R.id.edit_text_password)
        loginButton        = findViewById(R.id.button_login)
        registerLink       = findViewById(R.id.registerLink)
        googleSignInButton = findViewById(R.id.button_google_sign_in)

        // Login com e-mail/senha
        loginButton.setOnClickListener {
            val email    = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { goToMain() }
                .addOnFailureListener {
                    loginButton.isEnabled = true
                    Toast.makeText(this, "Erro: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Cadastro com e-mail/senha
        registerLink.setOnClickListener {
            val email    = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha para cadastrar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show()
                    goToMain()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao cadastrar: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Login com Google
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { goToMain() }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao autenticar com Google: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
