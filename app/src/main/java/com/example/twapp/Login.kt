package com.example.twapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var tvRedirectSignUp: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var buLogin: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        tvRedirectSignUp = findViewById(R.id.tvRedirectSignUp)
        buLogin = findViewById(R.id.buLogin)
        etEmail = findViewById(R.id.editTextTextEmailAddress)
        etPass = findViewById(R.id.editTextTextPassword)

        buLogin.setOnClickListener {
            login()
        }

        tvRedirectSignUp.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
        }

    }

        override fun onStart() {
            super.onStart()
            loadTweets()
        }

        private fun loadTweets() {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("email", currentUser.email)
                intent.putExtra("uid", currentUser.uid)
                startActivity(intent)
            }
        }

        private fun login() {
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()
            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                    loadTweets()
                } else
                    Toast.makeText(this, "Giriş Başarısız!", Toast.LENGTH_SHORT).show()
            }
        }
    }