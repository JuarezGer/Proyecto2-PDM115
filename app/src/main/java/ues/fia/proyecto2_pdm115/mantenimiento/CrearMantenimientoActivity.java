package ues.fia.proyecto2_pdm115.mantenimiento;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;
import ues.fia.proyecto2_pdm115.utils.PermissionHelper;
import ues.fia.proyecto2_pdm115.utils.SessionManager;

public class CrearMantenimientoActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private SessionManager sessionManager;
    private SpeechRecognizer speechRecognizer;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private Spinner spEquipo, spIncidencia, spTecnico, spTipo, spEstado;
    private TextInputEditText editDiagnostico, editSolucion;
    private TextInputLayout tilDiagnostico;
    private EditText editFechaInicio, editFechaFin;
    private ImageButton btnVoz;
    private Button btnGuardar, btnCancelar;

    private List<Integer> idsEquipos = new ArrayList<>();
    private List<Integer> idsIncidencias = new ArrayList<>();
    private List<Integer> idsTecnicos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_mantenimiento);

        helper = new controlDBLabCare(this);
        sessionManager = new SessionManager(this);

        vincularVistas();
        configurarSpinners();
        configurarFechas();
        configurarVoz();
        configurarBotones();
    }

    private void vincularVistas() {
        spEquipo = findViewById(R.id.spinnerEquipo);
        spIncidencia = findViewById(R.id.spinnerIncidencia);
        spTecnico = findViewById(R.id.spinnerTecnico);
        spTipo = findViewById(R.id.spinnerTipoMantenimiento);
        spEstado = findViewById(R.id.spinnerEstadoMantenimiento);
        editDiagnostico = findViewById(R.id.editDiagnostico);
        tilDiagnostico = findViewById(R.id.tilDiagnostico);
        editSolucion = findViewById(R.id.editSolucion);
        editFechaInicio = findViewById(R.id.editFechaInicio);
        editFechaFin = findViewById(R.id.editFechaFin);
        btnVoz = findViewById(R.id.btnVozDiagnostico);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void configurarSpinners() {
        helper.abrir();

        // 1. Equipos
        Cursor cEquipos = helper.consultarEquiposActivosCursor();
        List<String> listaEquipos = new ArrayList<>();
        listaEquipos.add("Selecciona un equipo");
        idsEquipos.add(-1);
        if (cEquipos != null) {
            if (cEquipos.moveToFirst()) {
                do {
                    idsEquipos.add(cEquipos.getInt(0));
                    listaEquipos.add(cEquipos.getString(1) + " - " + cEquipos.getString(2));
                } while (cEquipos.moveToNext());
            }
            cEquipos.close();
        }
        ArrayAdapter<String> adapterEquipos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaEquipos);
        spEquipo.setAdapter(adapterEquipos);

        // 2. Incidencias (Opcional)
        Cursor cIncidencias = helper.consultarIncidenciasPendientesCursor();
        List<String> listaIncidencias = new ArrayList<>();
        listaIncidencias.add("Ninguna (Opcional)");
        idsIncidencias.add(null);
        if (cIncidencias != null) {
            if (cIncidencias.moveToFirst()) {
                do {
                    idsIncidencias.add(cIncidencias.getInt(0));
                    listaIncidencias.add(cIncidencias.getString(1) + " (" + cIncidencias.getString(5) + ")");
                } while (cIncidencias.moveToNext());
            }
            cIncidencias.close();
        }
        ArrayAdapter<String> adapterIncidencias = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaIncidencias);
        spIncidencia.setAdapter(adapterIncidencias);

        // 3. Técnicos
        Cursor cTecnicos = helper.consultarTecnicosCursor();
        List<String> listaTecnicos = new ArrayList<>();
        listaTecnicos.add("Selecciona un técnico");
        idsTecnicos.add(-1);
        if (cTecnicos != null) {
            if (cTecnicos.moveToFirst()) {
                do {
                    idsTecnicos.add(cTecnicos.getInt(0));
                    listaTecnicos.add(cTecnicos.getString(1) + " " + cTecnicos.getString(2));
                } while (cTecnicos.moveToNext());
            }
            cTecnicos.close();
        }
        ArrayAdapter<String> adapterTecnicos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaTecnicos);
        spTecnico.setAdapter(adapterTecnicos);

        // 4. Tipos
        String[] tipos = {"Selecciona un tipo", "preventivo", "correctivo"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tipos);
        spTipo.setAdapter(adapterTipo);

        // 5. Estados
        String[] estados = {"Selecciona un estado", "pendiente", "en_proceso", "finalizado", "cancelado"};
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, estados);
        spEstado.setAdapter(adapterEstado);

        helper.cerrar();
    }

    private void configurarFechas() {
        editFechaInicio.setOnClickListener(v -> mostrarDatePicker(editFechaInicio));
        editFechaFin.setOnClickListener(v -> mostrarDatePicker(editFechaFin));
    }

    private void mostrarDatePicker(final EditText field) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, (monthOfYear + 1), dayOfMonth);
            field.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void configurarVoz() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { Toast.makeText(CrearMantenimientoActivity.this, "Escuchando...", Toast.LENGTH_SHORT).show(); }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) {
                String message;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO: message = "Error de audio"; break;
                    case SpeechRecognizer.ERROR_CLIENT: message = "Error del cliente"; break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: message = "Permiso denegado"; break;
                    case SpeechRecognizer.ERROR_NETWORK: message = "Error de red"; break;
                    case SpeechRecognizer.ERROR_NO_MATCH: message = "No se encontró coincidencia"; break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: message = "Reconocedor ocupado"; break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: message = "Silencio detectado"; break;
                    default: message = "Error de voz desconocido"; break;
                }
                Toast.makeText(CrearMantenimientoActivity.this, message, Toast.LENGTH_SHORT).show();
            }
            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String currentText = editDiagnostico.getText() != null ? editDiagnostico.getText().toString() : "";
                    if (!currentText.isEmpty()) currentText += " ";
                    editDiagnostico.setText(String.format("%s%s", currentText, matches.get(0)));
                }
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        btnVoz.setOnClickListener(v -> {
            if (PermissionHelper.hasPermission(this, Manifest.permission.RECORD_AUDIO)) {
                iniciarEscucha();
            } else {
                PermissionHelper.requestPermission(this, Manifest.permission.RECORD_AUDIO, REQUEST_RECORD_AUDIO_PERMISSION);
            }
        });
    }

    private void iniciarEscucha() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.startListening(intent);
    }

    private void configurarBotones() {
        btnGuardar.setOnClickListener(v -> guardarMantenimiento());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void guardarMantenimiento() {
        if (!validarCampos()) return;

        int idEquipo = idsEquipos.get(spEquipo.getSelectedItemPosition());
        Integer idIncidencia = idsIncidencias.get(spIncidencia.getSelectedItemPosition());
        int idTecnico = idsTecnicos.get(spTecnico.getSelectedItemPosition());
        int idUsuarioCrea = sessionManager.getIdUsuario();
        String tipo = spTipo.getSelectedItem().toString();
        String estado = spEstado.getSelectedItem().toString();
        String diagnostico = editDiagnostico.getText() != null ? editDiagnostico.getText().toString().trim() : "";
        String solucion = editSolucion.getText() != null ? editSolucion.getText().toString().trim() : "";
        String fechaInicio = editFechaInicio.getText().toString().trim();
        String fechaFin = editFechaFin.getText().toString().trim();

        if (fechaInicio.isEmpty()) fechaInicio = null;
        if (fechaFin.isEmpty()) fechaFin = null;

        helper.abrir();
        String resultado = helper.insertarMantenimiento(
                idEquipo, idIncidencia, idUsuarioCrea, idTecnico,
                tipo, estado, diagnostico, solucion, fechaInicio, fechaFin
        );
        helper.cerrar();

        if (resultado.contains("correctamente")) {
            Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Error: " + resultado, Toast.LENGTH_LONG).show();
        }
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (spEquipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Seleccione un equipo", Toast.LENGTH_SHORT).show();
            valido = false;
        }
        if (spTecnico.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Seleccione un técnico", Toast.LENGTH_SHORT).show();
            valido = false;
        }
        if (spTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Seleccione un tipo de mantenimiento", Toast.LENGTH_SHORT).show();
            valido = false;
        }
        if (spEstado.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Seleccione un estado", Toast.LENGTH_SHORT).show();
            valido = false;
        }
        
        // La validación inline según la instrucción "setError()"
        if (editDiagnostico.getText() != null && editDiagnostico.getText().toString().trim().isEmpty()) {
            editDiagnostico.setError("El diagnóstico es obligatorio");
            valido = false;
        }

        return valido;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                iniciarEscucha();
            } else {
                Toast.makeText(this, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
