package ues.fia.proyecto2_pdm115.serviciosnube;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ues.fia.proyecto2_pdm115.R;

public class ServiciosNubeMenuActivity extends AppCompatActivity {

    private ServiciosNubeHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios_nube_menu);

        helper = new ServiciosNubeHelper(this);

        findViewById(R.id.cardDatosNube).setOnClickListener(v -> abrir(DatosNubeActivity.class));
        findViewById(R.id.cardFusionarDatos).setOnClickListener(v -> abrir(FusionarDatosActivity.class));
        findViewById(R.id.cardModificarRolesUsuarios).setOnClickListener(v -> abrir(ModificarRolesUsuariosActivity.class));
        findViewById(R.id.btnVerificarConexionNube).setOnClickListener(v -> verificarConexion());
        findViewById(R.id.btnVolverServiciosNube).setOnClickListener(v -> finish());
    }

    private void abrir(Class<?> destino) {
        startActivity(new Intent(this, destino));
    }

    private void verificarConexion() {
        Toast.makeText(this, "Verificando conexión...", Toast.LENGTH_SHORT).show();
        helper.probarConexion(new ServiciosNubeHelper.SyncCallback() {
            @Override
            public void onSuccess(String mensaje) {
                Toast.makeText(ServiciosNubeMenuActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ServiciosNubeMenuActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
