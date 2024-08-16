package com.example.rememberitapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperar el nombre del usuario desde la sesión almacenada
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
        val usuarioSesionJson = sharedPreferences.getString("usuarioSesion", null)

        val bienvenidaUsuarioTextView: TextView = findViewById(R.id.bienvenida_usuario)

        if (usuarioSesionJson != null) {
            val usuarioSesion = gson.fromJson(usuarioSesionJson, UsuarioSesion::class.java)
            val nombreUsuario = usuarioSesion?.nombre ?: "Usuario"
            bienvenidaUsuarioTextView.text = "Hola, $nombreUsuario"
        } else {
            bienvenidaUsuarioTextView.text = "Bienvenido"
        }
    }
}
