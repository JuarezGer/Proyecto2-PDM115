package ues.fia.proyecto2_pdm115.utils;

import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

public class LottieHelper {

    private LottieHelper() {
        // Clase de utilidad, no se instancia.
    }

    public static void mostrarAnimacion(LottieAnimationView lottie, int rawRes, boolean repetir) {
        if (lottie == null) return;

        lottie.cancelAnimation();
        lottie.setVisibility(View.VISIBLE);
        lottie.setAnimation(rawRes);
        lottie.setRepeatCount(repetir ? LottieDrawable.INFINITE : 0);
        lottie.playAnimation();
    }

    public static void ocultarAnimacion(LottieAnimationView lottie) {
        if (lottie == null) return;

        lottie.cancelAnimation();
        lottie.setVisibility(View.GONE);
    }
}
