import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rememberitapp.R
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AgregarRecordatorioActivity : AppCompatActivity() {

    private lateinit var fechaInput: TextInputEditText
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.df_agregar_recordatorio)

        fechaInput = findViewById(R.id.fecha_input)

        fechaInput.setOnClickListener {
            mostrarDialogoFechaYHora()
        }
    }

    private fun mostrarDialogoFechaYHora() {
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
}
