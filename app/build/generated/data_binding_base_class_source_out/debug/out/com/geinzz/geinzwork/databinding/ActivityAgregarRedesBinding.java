// Generated by view binder compiler. Do not edit!
package com.geinzz.geinzwork.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.geinzz.geinzwork.R;
import com.google.android.material.textfield.TextInputLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityAgregarRedesBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final AppCompatButton Enviar;

  @NonNull
  public final TextInputLayout fb;

  @NonNull
  public final EditText fbEd;

  @NonNull
  public final EditText igED;

  @NonNull
  public final ImageView limpiarFB;

  @NonNull
  public final ImageView limpiarIG;

  @NonNull
  public final ImageView limpiarTK;

  @NonNull
  public final ConstraintLayout main;

  @NonNull
  public final TextInputLayout tituloPublicaciontext;

  @NonNull
  public final TextInputLayout tk;

  @NonNull
  public final EditText tkED;

  private ActivityAgregarRedesBinding(@NonNull ConstraintLayout rootView,
      @NonNull AppCompatButton Enviar, @NonNull TextInputLayout fb, @NonNull EditText fbEd,
      @NonNull EditText igED, @NonNull ImageView limpiarFB, @NonNull ImageView limpiarIG,
      @NonNull ImageView limpiarTK, @NonNull ConstraintLayout main,
      @NonNull TextInputLayout tituloPublicaciontext, @NonNull TextInputLayout tk,
      @NonNull EditText tkED) {
    this.rootView = rootView;
    this.Enviar = Enviar;
    this.fb = fb;
    this.fbEd = fbEd;
    this.igED = igED;
    this.limpiarFB = limpiarFB;
    this.limpiarIG = limpiarIG;
    this.limpiarTK = limpiarTK;
    this.main = main;
    this.tituloPublicaciontext = tituloPublicaciontext;
    this.tk = tk;
    this.tkED = tkED;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityAgregarRedesBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityAgregarRedesBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_agregar_redes, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityAgregarRedesBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.Enviar;
      AppCompatButton Enviar = ViewBindings.findChildViewById(rootView, id);
      if (Enviar == null) {
        break missingId;
      }

      id = R.id.fb;
      TextInputLayout fb = ViewBindings.findChildViewById(rootView, id);
      if (fb == null) {
        break missingId;
      }

      id = R.id.fbEd;
      EditText fbEd = ViewBindings.findChildViewById(rootView, id);
      if (fbEd == null) {
        break missingId;
      }

      id = R.id.igED;
      EditText igED = ViewBindings.findChildViewById(rootView, id);
      if (igED == null) {
        break missingId;
      }

      id = R.id.limpiarFB;
      ImageView limpiarFB = ViewBindings.findChildViewById(rootView, id);
      if (limpiarFB == null) {
        break missingId;
      }

      id = R.id.limpiarIG;
      ImageView limpiarIG = ViewBindings.findChildViewById(rootView, id);
      if (limpiarIG == null) {
        break missingId;
      }

      id = R.id.limpiarTK;
      ImageView limpiarTK = ViewBindings.findChildViewById(rootView, id);
      if (limpiarTK == null) {
        break missingId;
      }

      ConstraintLayout main = (ConstraintLayout) rootView;

      id = R.id.titulo_publicaciontext;
      TextInputLayout tituloPublicaciontext = ViewBindings.findChildViewById(rootView, id);
      if (tituloPublicaciontext == null) {
        break missingId;
      }

      id = R.id.tk;
      TextInputLayout tk = ViewBindings.findChildViewById(rootView, id);
      if (tk == null) {
        break missingId;
      }

      id = R.id.tkED;
      EditText tkED = ViewBindings.findChildViewById(rootView, id);
      if (tkED == null) {
        break missingId;
      }

      return new ActivityAgregarRedesBinding((ConstraintLayout) rootView, Enviar, fb, fbEd, igED,
          limpiarFB, limpiarIG, limpiarTK, main, tituloPublicaciontext, tk, tkED);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
