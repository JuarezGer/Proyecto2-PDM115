package ues.fia.proyecto2_pdm115.mantenimiento;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;
import ues.fia.proyecto2_pdm115.utils.PermissionHelper;

public class ModificarMantenimientoActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private SpeechRecognizer speechRecognizer;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // Vistas de navegación
    private ViewSwitcher viewSwitcher;
    private RecyclerView rvMantenimientos;
    private TextView tvEmpty, tvHeaderTitle, tvHeaderSubtitle;

    // Vistas del Formulario
    private Spinner spEquipo, spIncidencia, spTecnico, spTipo, spEstado;
    private TextInputEditText editDiagnostico, editSolucion;
    private EditText editFechaInicio, editFechaFin;
    private ImageButton btnVoz;
    private Button btnGuardar, btnCancelar;

    // Datos de apoyo
    private final List<Integer> idsEquipos = new ArrayList<>();
    private final List<Integer> idsIncidencias = new ArrayList<>();
    private final List<Integer> idsTecnicos = new ArrayList<>();
    private int idMantenimientoSeleccionado = -1;
    private int idUsuarioCreaOriginal = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_mantenimiento);

        helper = new controlDBLabCare(this);

        vincularVistas();
        configurarRecyclerView();
        configurarSpinners();
        configurarFechas();
        configurarVoz();
        configurarBotones();
        configurarManejoAtras();

        cargarMantenimientos();
    }

    private void vincularVistas() {
        viewSwitcher = findViewById(R.id.viewSwitcher);
        rvMantenimientos = findViewById(R.id.rvMantenimientos);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle);

        spEquipo = findViewById(R.id.spinnerEquipo);
        spIncidencia = findViewById(R.id.spinnerIncidencia);
        spTecnico = findViewById(R.id.spinnerTecnico);
        spTipo = findViewById(R.id.spinnerTipoMantenimiento);
        spEstado = findViewById(R.id.spinnerEstadoMantenimiento);
        editDiagnostico = findViewById(R.id.editDiagnostico);
        editSolucion = findViewById(R.id.editSolucion);
        editFechaInicio = findViewById(R.id.editFechaInicio);
        editFechaFin = findViewById(R.id.editFechaFin);
        btnVoz = findViewById(R.id.btnVozDiagnostico);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void configurarRecyclerView() {
        rvMantenimientos.setLayoutManager(new LinearLayoutManager(this));
    }

    private void configurarSpinners() {
        helper.abrir();

        // 1. Equipos
        Cursor cEquipos = helper.consultarEquiposCursor();
        List<String> listaEquipos = new ArrayList<>();
        listaEquipos.add("Selecciona un equipo");
        idsEquipos.clear();
        idsEquipos.add(-1);
        if (cEquipos != null) {
            while (cEquipos.moveToNext()) {
                idsEquipos.add(cEquipos.getInt(cEquipos.getColumnIndexOrThrow("id_equipo")));
                listaEquipos.add(cEquipos.getString(cEquipos.getColumnIndexOrThrow("codigo_inventario")) + " - " + 
                                 cEquipos.getString(cEquipos.getColumnIndexOrThrow("nombre")));
            }
            cEquipos.close();
        }
        spEquipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaEquipos));

        // 2. Incidencias
        Cursor cIncidencias = helper.consultarIncidenciasCursor();
        List<String> listaIncidencias = new ArrayList<>();
        listaIncidencias.add("Ninguna (Opcional)");
        idsIncidencias.clear();
        idsIncidencias.add(null);
        if (cIncidencias != null) {
            while (cIncidencias.moveToNext()) {
                idsIncidencias.add(cIncidencias.getInt(cIncidencias.getColumnIndexOrThrow("id_incidencia")));
                listaIncidencias.add(cIncidencias.getString(cIncidencias.getColumnIndexOrThrow("titulo")));
            }
            cIncidencias.close();
        }
        spIncidencia.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaIncidencias));

        // 3. Técnicos
        Cursor cTecnicos = helper.consultarTecnicosCursor();
        List<String> listaTecnicos = new ArrayList<>();
        listaTecnicos.add("Selecciona un técnico");
        idsTecnicos.clear();
        idsTecnicos.add(-1);
        if (cTecnicos != null) {
            while (cTecnicos.moveToNext()) {
                idsTecnicos.add(cTecnicos.getInt(0));
                listaTecnicos.add(cTecnicos.getString(1) + " " + cTecnicos.getString(2));
            }
            cTecnicos.close();
        }
        spTecnico.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaTecnicos));

        // 4. Tipos
        String[] tipos = {"Selecciona un tipo", "preventivo", "correctivo"};
        spTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tipos));

        // 5. Estados
        String[] estados = {"Selecciona un estado", "pendiente", "en_proceso", "finalizado", "cancelado"};
        spEstado.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, estados));

        helper.cerrar();
    }

    private void configurarFechas() {
        editFechaInicio.setOnClickListener(v -> mostrarDatePicker(editFechaInicio));
        editFechaFin.setOnClickListener(v -> mostrarDatePicker(editFechaFin));
    }

    private void mostrarDatePicker(final EditText field) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, (month + 1), dayOfMonth);
            field.setText(selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void configurarVoz() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { Toast.makeText(ModificarMantenimientoActivity.this, "Escuchando...", Toast.LENGTH_SHORT).show(); }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) { Toast.makeText(ModificarMantenimientoActivity.this, "Error de voz", Toast.LENGTH_SHORT).show(); }
            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String currentText = editDiagnostico.getText() != null ? editDiagnostico.getText().toString() : "";
                    String newText = (currentText + " " + matches.get(0)).trim();
                    editDiagnostico.setText(newText);
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
        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnCancelar.setOnClickListener(v -> volverALista());
    }

    private void configurarManejoAtras() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewSwitcher.getDisplayedChild() == 1) {
                    volverALista();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void cargarMantenimientos() {
        helper.abrir();
        Cursor cursor = helper.consultarMantenimientosCursor();
        if (cursor != null && cursor.getCount() > 0) {
            tvEmpty.setVisibility(View.GONE);
            rvMantenimientos.setVisibility(View.VISIBLE);
            MantenimientoEditAdapter adapter = new MantenimientoEditAdapter(cursor, this::cargarFormularioEdicion);
            rvMantenimientos.setAdapter(adapter);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
            rvMantenimientos.setVisibility(View.GONE);
            if (cursor != null) cursor.close();
        }
        helper.cerrar();
    }

    private void cargarFormularioEdicion(int idMantenimiento) {
        idMantenimientoSeleccionado = idMantenimiento;
        helper.abrir();
        HashMap<String, String> mant = helper.consultarMantenimiento(idMantenimiento);
        helper.cerrar();

        if (mant != null) {
            String idUserCrea = mant.get("id_usuario_crea");
            idUsuarioCreaOriginal = (idUserCrea != null && !idUserCrea.isEmpty()) ? Integer.parseInt(idUserCrea) : -1;
            
            String idEq = mant.get("id_equipo");
            if (idEq != null && !idEq.isEmpty()) {
                seleccionarEnSpinner(spEquipo, idsEquipos, Integer.parseInt(idEq));
            }
            
            String idInc = mant.get("id_incidencia");
            seleccionarEnSpinner(spIncidencia, idsIncidencias, (idInc == null || idInc.isEmpty()) ? null : Integer.parseInt(idInc));
            
            String idTec = mant.get("id_usuario_tecnico");
            if (idTec != null && !idTec.isEmpty()) {
                seleccionarEnSpinner(spTecnico, idsTecnicos, Integer.parseInt(idTec));
            }

            seleccionarEnSpinnerPorTexto(spTipo, mant.get("tipo_mantenimiento"));
            seleccionarEnSpinnerPorTexto(spEstado, mant.get("estado_mantenimiento"));

            editDiagnostico.setText(mant.get("diagnostico"));
            editSolucion.setText(mant.get("solucion_aplicada"));
            editFechaInicio.setText(mant.get("fecha_inicio"));
            editFechaFin.setText(mant.get("fecha_fin"));

            String title = "Editar Mantenimiento #" + idMantenimiento;
            tvHeaderTitle.setText(title);
            tvHeaderSubtitle.setText("Actualiza la información del registro");
            viewSwitcher.showNext();
        }
    }

    private void seleccionarEnSpinner(Spinner spinner, List<Integer> ids, Integer idBuscar) {
        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);
            if ((id == null && idBuscar == null) || (id != null && id.equals(idBuscar))) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void seleccionarEnSpinnerPorTexto(Spinner spinner, String texto) {
        if (texto == null) return;
        ArrayAdapter<?> adapterSpinner = (ArrayAdapter<?>) spinner.getAdapter();
        for (int i = 0; i < adapterSpinner.getCount(); i++) {
            Object item = adapterSpinner.getItem(i);
            if (item != null && item.toString().equalsIgnoreCase(texto)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void guardarCambios() {
        if (!validarCampos()) return;

        int idEquipo = idsEquipos.get(spEquipo.getSelectedItemPosition());
        Integer idIncidencia = idsIncidencias.get(spIncidencia.getSelectedItemPosition());
        int idTecnico = idsTecnicos.get(spTecnico.getSelectedItemPosition());
        String tipo = spTipo.getSelectedItem().toString();
        String estado = spEstado.getSelectedItem().toString();
        String diagnostico = editDiagnostico.getText() != null ? editDiagnostico.getText().toString().trim() : "";
        String solucion = editSolucion.getText() != null ? editSolucion.getText().toString().trim() : "";
        String fechaInicio = editFechaInicio.getText().toString().trim();
        String fechaFin = editFechaFin.getText().toString().trim();

        helper.abrir();
        String resultado = helper.actualizarMantenimiento(
                idMantenimientoSeleccionado, idEquipo, idIncidencia, idUsuarioCreaOriginal,
                idTecnico, tipo, estado, diagnostico, solucion,
                fechaInicio.isEmpty() ? null : fechaInicio,
                fechaFin.isEmpty() ? null : fechaFin
        );
        helper.cerrar();

        if (resultado.contains("correctamente")) {
            Toast.makeText(this, "Mantenimiento actualizado con éxito", Toast.LENGTH_SHORT).show();
            volverALista();
            cargarMantenimientos();
        } else {
            Toast.makeText(this, "Error: " + resultado, Toast.LENGTH_LONG).show();
        }
    }

    private boolean validarCampos() {
        boolean valido = true;
        if (spEquipo.getSelectedItemPosition() == 0) { Toast.makeText(this, "Seleccione un equipo", Toast.LENGTH_SHORT).show(); valido = false; }
        if (spTecnico.getSelectedItemPosition() == 0) { Toast.makeText(this, "Seleccione un técnico", Toast.LENGTH_SHORT).show(); valido = false; }
        if (spTipo.getSelectedItemPosition() == 0) { Toast.makeText(this, "Seleccione un tipo", Toast.LENGTH_SHORT).show(); valido = false; }
        if (spEstado.getSelectedItemPosition() == 0) { Toast.makeText(this, "Seleccione un estado", Toast.LENGTH_SHORT).show(); valido = false; }
        
        if (editDiagnostico.getText() == null || editDiagnostico.getText().toString().trim().isEmpty()) {
            editDiagnostico.setError("El diagnóstico es obligatorio");
            valido = false;
        }
        return valido;
    }

    private void volverALista() {
        tvHeaderTitle.setText("Modificar Mantenimiento");
        tvHeaderSubtitle.setText("Selecciona un registro para editar");
        viewSwitcher.setDisplayedChild(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            iniciarEscucha();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (helper != null) helper.cerrar();
    }
}