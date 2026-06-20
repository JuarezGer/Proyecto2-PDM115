package ues.fia.proyecto2_pdm115.serviciosnube;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import ues.fia.proyecto2_pdm115.R;

public class ServiciosNubeUiHelper {

    public static void agregarCard(Context context, LinearLayout contenedor, String titulo, String subtitulo, String icono, View.OnClickListener listener) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        card.setPadding(dp(context, 16), dp(context, 12), dp(context, 16), dp(context, 12));
        card.setBackgroundResource(R.drawable.bg_card);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(listener);
        card.setElevation(dp(context, 3));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, dp(context, 10), 0, 0);
        card.setLayoutParams(cardParams);

        TextView icon = new TextView(context);
        icon.setText(icono);
        icon.setTextSize(24);
        icon.setGravity(android.view.Gravity.CENTER);
        icon.setBackgroundResource(R.drawable.bg_icon_container);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(context, 50), dp(context, 50));
        card.addView(icon, iconParams);

        LinearLayout textos = new LinearLayout(context);
        textos.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textosParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textosParams.setMargins(dp(context, 16), 0, 0, 0);

        TextView tvTitulo = new TextView(context);
        tvTitulo.setText(titulo);
        tvTitulo.setTextColor(ContextCompat.getColor(context, R.color.blue_gray_900));
        tvTitulo.setTextSize(16);
        tvTitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView tvSub = new TextView(context);
        tvSub.setText(subtitulo);
        tvSub.setTextColor(ContextCompat.getColor(context, R.color.blue_gray_400));
        tvSub.setTextSize(12);
        tvSub.setPadding(0, dp(context, 4), 0, 0);

        textos.addView(tvTitulo);
        textos.addView(tvSub);
        card.addView(textos, textosParams);
        contenedor.addView(card);
    }

    public static void mostrarDetalleJson(Context context, String titulo, JSONObject obj) {
        new AlertDialog.Builder(context)
                .setTitle(titulo)
                .setMessage(jsonBonito(obj))
                .setPositiveButton("Cerrar", null)
                .show();
    }

    public static String jsonBonito(JSONObject obj) {
        StringBuilder sb = new StringBuilder();
        try {
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String valor = obj.isNull(key) ? "—" : obj.optString(key, "—");
                sb.append(formatearCampo(key)).append(": ").append(valor).append("\n\n");
            }
        } catch (Exception e) {
            return obj.toString();
        }
        return sb.toString().trim();
    }

    public static String formatearCampo(String key) {
        String texto = key.replace("_", " ");
        if (texto.length() == 0) return key;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
