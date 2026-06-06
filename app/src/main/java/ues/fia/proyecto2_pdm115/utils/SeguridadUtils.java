package ues.fia.proyecto2_pdm115.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class SeguridadUtils {

    private SeguridadUtils() {
    }

    public static String sha256(String texto) {
        if (texto == null) {
            texto = "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No se pudo cifrar la clave", e);
        }
    }

    public static String sha256SiNecesario(String texto) {
        if (texto == null) {
            return sha256("");
        }

        String limpio = texto.trim().toLowerCase(Locale.ROOT);

        if (limpio.matches("^[a-f0-9]{64}$")) {
            return limpio;
        }

        return sha256(texto);
    }
}
