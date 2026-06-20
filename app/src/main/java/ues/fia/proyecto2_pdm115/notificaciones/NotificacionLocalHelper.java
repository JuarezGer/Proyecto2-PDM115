package ues.fia.proyecto2_pdm115.notificaciones;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.mantenimiento.ConsultarMantenimientoActivity;

public class NotificacionLocalHelper {

    private static final String ID_CANAL_MANTENIMIENTOS = "canal_mantenimientos_labcare";
    private static final String NOMBRE_CANAL_MANTENIMIENTOS = "Mantenimientos";
    private static final String DESCRIPCION_CANAL_MANTENIMIENTOS = "Avisos locales de mantenimientos asignados";

    public static void crearCanalMantenimientos(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    ID_CANAL_MANTENIMIENTOS,
                    NOMBRE_CANAL_MANTENIMIENTOS,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription(DESCRIPCION_CANAL_MANTENIMIENTOS);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    public static void mostrarNotificacionMantenimientoAsignado(
            Context context,
            int idMantenimiento,
            String nombreTecnico,
            String nombreEquipo
    ) {
        crearCanalMantenimientos(context);

        Intent intent = new Intent(context, ConsultarMantenimientoActivity.class);
        intent.putExtra(ConsultarMantenimientoActivity.EXTRA_ABRIR_MODAL, true);
        intent.putExtra(ConsultarMantenimientoActivity.EXTRA_ID_MANTENIMIENTO, idMantenimiento);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                idMantenimiento,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String titulo = "Mantenimiento asignado";
        String contenido = "Se asignó un mantenimiento a " + limpiarTexto(nombreTecnico) + ".";

        if (nombreEquipo != null && !nombreEquipo.trim().isEmpty()) {
            contenido += " Equipo: " + limpiarTexto(nombreEquipo) + ".";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ID_CANAL_MANTENIMIENTOS)
                .setSmallIcon(R.drawable.ic_build)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contenido))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManagerCompat.from(context).notify(idMantenimiento, builder.build());
    }

    private static String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.replace("Selecciona un técnico", "").replace("Selecciona un equipo", "").trim();
    }
}
