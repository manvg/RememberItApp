package com.example.rememberitapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

data class UsuarioSesion(
    val nombre: String,
    val apellido: String,
    val fechaNacimiento: String,
    val email: String,
    var isActive: Boolean
)

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //----------INICIO -> GUARDAR REGISTRO USUARIO----------//
        val nombreInput: TextInputEditText = findViewById(R.id.nombre_input)
        val apellidoInput: TextInputEditText = findViewById(R.id.apellido_input)
        val fechaNacimientoInput: TextInputEditText = findViewById(R.id.fecha_nacimiento_input)
        val emailInput: TextInputEditText = findViewById(R.id.email_input)
        val passwordInput: TextInputEditText = findViewById(R.id.password_input)

        // Configurar el DatePickerDialog para la selección de la fecha de nacimiento
        fechaNacimientoInput.setOnClickListener {
            showDatePickerDialog(fechaNacimientoInput)
        }

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

        val btnArrow: ImageView = findViewById(R.id.btn_arrow)
        btnArrow.setOnClickListener {
            val nombre = nombreInput.text.toString()
            val apellido = apellidoInput.text.toString()
            val fechaNacimiento = fechaNacimientoInput.text.toString()
            val email = emailInput.text.toString()
            val contrasena = passwordInput.text.toString()

            //Crear un objeto UsuarioSesion
            val usuarioSesion = UsuarioSesion(
                nombre = nombre,
                apellido = apellido,
                fechaNacimiento = fechaNacimiento,
                email = email,
                isActive = true
            )

            //Recuperar la lista de usuarios almacenada (si existe)
            val jsonListaUsuarios = sharedPreferences.getString("listaUsuarios", null)
            val listaUsuariosTipo = object : TypeToken<MutableList<UsuarioSesion>>() {}.type

            val listaUsuarios: MutableList<UsuarioSesion> = if (jsonListaUsuarios != null) {
                gson.fromJson(jsonListaUsuarios, listaUsuariosTipo)
            } else {
                mutableListOf()
            }

            //Añadir el nuevo usuario a la lista
            listaUsuarios.add(usuarioSesion)

            //Guardar la lista de usuarios actualizada en memoria
            val updatedUserListJson = gson.toJson(listaUsuarios)
            val editor = sharedPreferences.edit()
            editor.putString("listaUsuarios", updatedUserListJson)

            // Guardar los datos de sesión del usuario actual por separado
            val usuarioSesionJson = gson.toJson(usuarioSesion)
            editor.putString("usuarioSesion", usuarioSesionJson)
            editor.putString("contrasena", contrasena)
            editor.apply()

            // Redirigir a la pantalla principal después del registro
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        //----------FIN GUARDAR REGISTRO USUARIO----------//

        //----------REDIRECCIÓN HACIA LOGIN----------//
        val loginText: TextView = findViewById(R.id.login_text)
        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Método para mostrar el DatePickerDialog
    private fun showDatePickerDialog(fechaNacimientoInput: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Formatear la fecha seleccionada y establecerla en el campo de texto
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                fechaNacimientoInput.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }
}
