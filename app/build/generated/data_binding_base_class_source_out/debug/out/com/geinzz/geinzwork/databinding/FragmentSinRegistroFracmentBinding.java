// Generated by view binder compiler. Do not edit!
package com.geinzz.geinzwork.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.airbnb.lottie.LottieAnimationView;
import com.geinzz.geinzwork.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentSinRegistroFracmentBinding implements ViewBinding {
  @NonNull
  private final FrameLayout rootView;

  @NonNull
  public final AppCompatButton Registrarme;

  @NonNull
  public final LottieAnimationView imgnoCuenta;

  @NonNull
  public final AppCompatButton inicarSeccion;

  private FragmentSinRegistroFracmentBinding(@NonNull FrameLayout rootView,
      @NonNull AppCompatButton Registrarme, @NonNull LottieAnimationView imgnoCuenta,
      @NonNull AppCompatButton inicarSeccion) {
    this.rootView = rootView;
    this.Registrarme = Registrarme;
    this.imgnoCuenta = imgnoCuenta;
    this.inicarSeccion = inicarSeccion;
  }

  @Override
  @NonNull
  public FrameLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentSinRegistroFracmentBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentSinRegistroFracmentBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_sin_registro_fracment, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentSinRegistroFracmentBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.Registrarme;
      AppCompatButton Registrarme = ViewBindings.findChildViewById(rootView, id);
      if (Registrarme == null) {
        break missingId;
      }

      id = R.id.imgnoCuenta;
      LottieAnimationView imgnoCuenta = ViewBindings.findChildViewById(rootView, id);
      if (imgnoCuenta == null) {
        break missingId;
      }

      id = R.id.inicarSeccion;
      AppCompatButton inicarSeccion = ViewBindings.findChildViewById(rootView, id);
      if (inicarSeccion == null) {
        break missingId;
      }

      return new FragmentSinRegistroFracmentBinding((FrameLayout) rootView, Registrarme,
          imgnoCuenta, inicarSeccion);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
