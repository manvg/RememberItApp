package com.example.rememberitapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
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
import java.text.SimpleDateFormat
import java.util.*

data class Recordatorio(
    val idRecordatorio: Int,
    val fechaRecordatorio: String, // Fecha y hora en formato "dd/MM/yyyy HH:mm"
    val titulo: String,
    val descripcion: String,
    val vigente: Boolean
)

class MainActivity : AppCompatActivity() {

    private val recordatorios = mutableListOf<Recordatorio>()
    private var recordatorioIdCounter = 1
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Recuperar el nombre del usuario desde la sesión almacenada
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
        //Datos en memoria
        cargarRecordatoriosDesdeMemoria()

        val gson = Gson()
        val usuarioSesionJson = sharedPreferences.getString("usuarioSesion", null)

        val bienvenidaUsuarioTextView: TextView = findViewById(id.bienvenida_usuario)
        val recordatoriosContainer: LinearLayout = findViewById(id.recordatorios_container)

        if (usuarioSesionJson != null) {
            val usuarioSesion = gson.fromJson(usuarioSesionJson, UsuarioSesion::class.java)
            if (usuarioSesion.isActive){
                val nombreUsuario = usuarioSesion?.nombre ?: "Usuario"
                bienvenidaUsuarioTextView.text = "Hola, $nombreUsuario"
            }else{
                bienvenidaUsuarioTextView.text = "Bienvenido"
            }
        } else {
            bienvenidaUsuarioTextView.text = "Bienvenido"
        }

        actualizarVistaRecordatorios()

        //Mostrar menú
        val btnAjustes: ImageView = findViewById(id.btn_ajustes)
        btnAjustes.setOnClickListener { view ->
            showPopupMenu(view)
        }

        //Mostrar formulario agregar recordatorio
        val btnAgregar: ImageView = findViewById(id.btn_agregar)
        btnAgregar.setOnClickListener {
            mostrarFormularioAgregarRecordatorio()
        }
    }

    private fun mostrarFormularioAgregarRecordatorio() {
        mostrarFormularioRecordatorio(null)
    }

    private fun mostrarFormularioEditarRecordatorio(recordatorio: Recordatorio) {
        mostrarFormularioRecordatorio(recordatorio)
    }

    private fun mostrarFormularioRecordatorio(recordatorio: Recordatorio?) {
        val dialogView = LayoutInflater.from(this).inflate(layout.df_agregar_recordatorio, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = dialogBuilder.create()

        val tituloInput: TextInputEditText = dialogView.findViewById(id.titulo_input)
        val descripcionInput: TextInputEditText = dialogView.findViewById(id.descripcion_input)
        val fechaInput: TextInputEditText = dialogView.findViewById(id.fecha_input)
        val btnGuardar: Button = dialogView.findViewById(id.btn_guardar)

        // Cargar campos a editar
        recordatorio?.let {
            tituloInput.setText(it.titulo)
            descripcionInput.setText(it.descripcion)
            fechaInput.setText(it.fechaRecordatorio)
        }
        // Cargar fecha
        fechaInput.apply {
            inputType = 0  // Ocultar el teclado
            isFocusable = false
            isFocusableInTouchMode = false
            setOnClickListener {
                mostrarDialogoFechaYHora(this)
            }
        }

        btnGuardar.setOnClickListener {
            val titulo = tituloInput.text.toString()
            val descripcion = descripcionInput.text.toString()
            val fecha = fechaInput.text.toString()

            if (titulo.isNotEmpty() && descripcion.isNotEmpty() && fecha.isNotEmpty()) {
                if (recordatorio == null) {
                    // Nuevo recordatorio
                    val nuevoRecordatorio = Recordatorio(
                        idRecordatorio = recordatorioIdCounter++,
                        fechaRecordatorio = fecha,
                        titulo = titulo,
                        descripcion = descripcion,
                        vigente = true
                    )
                    recordatorios.add(nuevoRecordatorio)
                } else {
                    // Editar recordatorio
                    val index = recordatorios.indexOfFirst { it.idRecordatorio == recordatorio.idRecordatorio }
                    if (index != -1) {
                        recordatorios[index] = recordatorio.copy(
                            titulo = titulo,
                            descripcion = descripcion,
                            fechaRecordatorio = fecha
                        )
                    }
                }

                // Guardar los recordatorios en SharedPreferences
                guardarRecordatoriosEnMemoria()

                actualizarVistaRecordatorios()
                dialog.dismiss()
            } else {
                var isValid = true

                if (titulo.isEmpty()) {
                    tituloInput.error = "El título no puede estar vacío"
                    isValid = false
                } else {
                    tituloInput.error = null
                }

                if (descripcion.isEmpty()) {
                    descripcionInput.error = "La descripción no puede estar vacía"
                    isValid = false
                } else {
                    descripcionInput.error = null
                }

                if (fecha.isEmpty()) {
                    fechaInput.error = "La fecha no puede estar vacía"
                    isValid = false
                } else {
                    fechaInput.error = null
                }

                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoFechaYHora(fechaInput: TextInputEditText) {
        val calendario = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                calendario.set(Calendar.YEAR, year)
                calendario.set(Calendar.MONTH, monthOfYear)
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendario.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendario.set(Calendar.MINUTE, minute)

                        // Formatear la fecha y hora seleccionadas y establecerla en el campo de texto
                        fechaInput.setText(dateFormatter.format(calendario.time))
                    },
                    calendario.get(Calendar.HOUR_OF_DAY),
                    calendario.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun actualizarVistaRecordatorios() {
        val recordatoriosContainer: LinearLayout = findViewById(id.recordatorios_container)
        recordatoriosContainer.removeAllViews()

        if (recordatorios.isEmpty()) {
            val defaultView = LayoutInflater.from(this).inflate(layout.item_recordatorio, recordatoriosContainer, false)
            val tituloTextView: TextView = defaultView.findViewById(id.recordatorio_titulo)
            val descripcionTextView: TextView = defaultView.findViewById(id.recordatorio_descripcion)
            val fechaTextView: TextView = defaultView.findViewById(id.recordatorio_fecha)
            val btnEditar: Button = defaultView.findViewById(id.btn_editar)
            val btnEliminar: Button = defaultView.findViewById(id.btn_eliminar)

            tituloTextView.text = "Agregar Recordatorios"
            descripcionTextView.text = "Aún no tienes recordatorios. Agrega uno nuevo."
            fechaTextView.text = ""
            btnEditar.visibility = View.GONE
            btnEliminar.visibility = View.GONE

            defaultView.setOnClickListener {
                mostrarFormularioAgregarRecordatorio()
            }

            recordatoriosContainer.addView(defaultView)
        } else {
            for (recordatorio in recordatorios) {
                val recordatorioView = LayoutInflater.from(this).inflate(layout.item_recordatorio, recordatoriosContainer, false)

                val tituloTextView: TextView = recordatorioView.findViewById(id.recordatorio_titulo)
                val descripcionTextView: TextView = recordatorioView.findViewById(id.recordatorio_descripcion)
                val fechaTextView: TextView = recordatorioView.findViewById(id.recordatorio_fecha)
                val btnEditar: Button = recordatorioView.findViewById(id.btn_editar)
                val btnEliminar: Button = recordatorioView.findViewById(id.btn_eliminar)

                tituloTextView.text = recordatorio.titulo
                descripcionTextView.text = recordatorio.descripcion
                fechaTextView.text = recordatorio.fechaRecordatorio

                // Asignar el idRecordatorio al tag del CardView
                recordatorioView.tag = recordatorio.idRecordatorio

                // Configurar el listener para el botón "Editar"
                btnEditar.setOnClickListener {
                    mostrarFormularioEditarRecordatorio(recordatorio)
                }

                // Configurar el listener para el botón "Eliminar"
                btnEliminar.setOnClickListener {
                    mostrarDialogoConfirmacionEliminar(recordatorio)
                }

                recordatoriosContainer.addView(recordatorioView)
            }
        }
    }

    private fun mostrarDialogoConfirmacionEliminar(recordatorio: Recordatorio) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Recordatorio")
            .setMessage("¿Estás seguro de que deseas eliminar este recordatorio?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarRecordatorio(recordatorio)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(menu.menu_ajustes, popup.menu)

        // Obtener la sesión de usuario desde SharedPreferences
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
        val usuarioSesion = if (usuarioSesionJson != null) {
            gson.fromJson(usuarioSesionJson, UsuarioSesion::class.java)
        } else {
            null
        }

        // Mostrar u ocultar opciones del menú en función de si la sesión está activa
        if (usuarioSesion != null && usuarioSesion.isActive) {
            // Si la sesión está activa, solo mostrar la opción "Cerrar sesión"
            popup.menu.findItem(id.menu_cerrar_sesion).isVisible = true
            popup.menu.findItem(id.menu_crear_cuenta).isVisible = false
            popup.menu.findItem(id.menu_iniciar_sesion).isVisible = false
        } else {
            // Si no hay sesión activa, mostrar "Crear cuenta" e "Iniciar sesión"
            popup.menu.findItem(id.menu_cerrar_sesion).isVisible = false
            popup.menu.findItem(id.menu_crear_cuenta).isVisible = true
            popup.menu.findItem(id.menu_iniciar_sesion).isVisible = true
        }

        popup.setOnMenuItemClickListener { item -> handleMenuItemClick(item) }
        popup.show()
    }


    private fun handleMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            id.menu_cerrar_sesion -> {
                cerrarSesion()
                true
            }
            id.menu_crear_cuenta -> {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                true
            }
            id.menu_iniciar_sesion -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }
    }


    private fun cerrarSesion() {
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

        // Borrar la información del usuario en SharedPreferences
        sharedPreferences.edit().remove("usuarioSesion").apply()

        // Cambiar el mensaje de bienvenida
        val bienvenidaUsuarioTextView: TextView = findViewById(id.bienvenida_usuario)
        bienvenidaUsuarioTextView.text = "Bienvenido"

        // Actualizar el menú desplegable
        showPopupMenu(findViewById(id.btn_ajustes))

        // Borrar recordatorios cargados previamente (opcional)
        recordatorios.clear()
        actualizarVistaRecordatorios()

        // Redirigir al LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    //----------Datos en memoria---------//
    private fun cargarRecordatoriosDesdeMemoria() {
        val sharedPreferences = getSharedPreferences("RecordatoriosPrefs", MODE_PRIVATE)
        val gson = Gson()
        val jsonRecordatorios = sharedPreferences.getString("recordatorios", null)
        if (jsonRecordatorios != null) {
            val recordatorioArray = gson.fromJson(jsonRecordatorios, Array<Recordatorio>::class.java)
            if (recordatorioArray != null) {
                recordatorios.addAll(recordatorioArray)
            }
        }
    }


    private fun guardarRecordatoriosEnMemoria() {
        val sharedPreferences = getSharedPreferences("RecordatoriosPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val jsonRecordatorios = gson.toJson(recordatorios)
        editor.putString("recordatorios", jsonRecordatorios)
        editor.apply()
    }

    private fun eliminarRecordatorio(recordatorio: Recordatorio) {
        recordatorios.remove(recordatorio)
        guardarRecordatoriosEnMemoria()
        actualizarVistaRecordatorios()
    }
}
