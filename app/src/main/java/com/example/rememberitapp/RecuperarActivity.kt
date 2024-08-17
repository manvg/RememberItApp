package com.example.rememberitapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class RecuperarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_recuperar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //----------Envio de correo----------//
        val emailInput: TextInputEditText = findViewById(R.id.email_input)
        val btnEnviar: ImageView = findViewById(R.id.btn_enviar)
        val loginText: TextView = findViewById(R.id.login_text) // Añadir este TextView para redirigir al login

        btnEnviar.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isNotEmpty()) {
                Toast.makeText(this, "Se ha enviado un correo para recuperar la contraseña a $email", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Por favor, ingresa un correo electrónico válido.", Toast.LENGTH_SHORT).show()
            }
        }

        //----------Redireccionar al Login----------//
        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
