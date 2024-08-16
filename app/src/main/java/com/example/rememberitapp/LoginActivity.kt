package com.example.rememberitapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.rememberitapp.R.*
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.layout_login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuración de EncryptedSharedPreferences
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            this,
            "EncryptedUserPrefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val gson = Gson()

        // Verificar si ya existe una sesión activa
        val jsonUsuarioSesion = sharedPreferences.getString("usuarioSesion", null)
        if (jsonUsuarioSesion != null) {
            val usuarioSesion = gson.fromJson(jsonUsuarioSesion, UsuarioSesion::class.java)
            if (usuarioSesion.isActive) {
                // Si hay una sesión activa, redirigir al MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return
            }
        }

        val emailInput: TextInputEditText = findViewById(id.email_input)
        val passwordInput: TextInputEditText = findViewById(id.password_input)
        val loginButton: ImageView = findViewById(R.id.btn_login)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val contrasena = passwordInput.text.toString()

            // Recuperar la lista de usuarios almacenada
            val jsonListaUsuarios = sharedPreferences.getString("listaUsuarios", null)
            val listaUsuariosTipo = object : TypeToken<MutableList<UsuarioSesion>>() {}.type

            val listaUsuarios: MutableList<UsuarioSesion> = if (jsonListaUsuarios != null) {
                gson.fromJson(jsonListaUsuarios, listaUsuariosTipo)
            } else {
                mutableListOf()
            }

            // Buscar el usuario por email y validar contraseña
            val usuarioEncontrado = listaUsuarios.find { it.email == email }

            if (usuarioEncontrado != null) {
                if (sharedPreferences.getString("contrasena", null) == contrasena) {
                    // Establecer la sesión activa
                    val editor = sharedPreferences.edit()
                    usuarioEncontrado.isActive = true
                    val usuarioSesionJson = gson.toJson(usuarioEncontrado)
                    editor.putString("usuarioSesion", usuarioSesionJson)
                    editor.apply()

                    val nombreUsuario = usuarioEncontrado.nombre
                    findViewById<TextView>(R.id.bienvenida_usuario).text = "Hola, $nombreUsuario"
                    // Iniciar sesión y redirigir al MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Contraseña incorrecta
                    Toast.makeText(this, "Contraseña incorrecta. Inténtelo de nuevo.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Usuario no encontrado
                Toast.makeText(this, "Usuario no encontrado. Por favor, regístrese.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón "Saltar"
        val skipButton: Button = findViewById(id.skip_button)
        skipButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Link "Crear cuenta"
        val registerText: TextView = findViewById(id.register_text)
        registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
