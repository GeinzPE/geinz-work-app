// Generated by view binder compiler. Do not edit!
package com.geinzz.geinzwork.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.geinzz.geinzwork.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivitySeleccionDeTrabajoBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final TextView EstasAdos;

  @NonNull
  public final TextView cat;

  @NonNull
  public final AutoCompleteTextView complete;

  @NonNull
  public final AppCompatButton enviar;

  @NonNull
  public final TextView freelancer;

  @NonNull
  public final TextView geinz;

  @NonNull
  public final LinearLayout lineaTipoT;

  @NonNull
  public final LinearLayout linealCategoriaT;

  @NonNull
  public final RelativeLayout main;

  @NonNull
  public final TextView seleccionTrabajo;

  @NonNull
  public final AutoCompleteTextView subcategoria;

  @NonNull
  public final TextView tipot;

  private ActivitySeleccionDeTrabajoBinding(@NonNull RelativeLayout rootView,
      @NonNull TextView EstasAdos, @NonNull TextView cat, @NonNull AutoCompleteTextView complete,
      @NonNull AppCompatButton enviar, @NonNull TextView freelancer, @NonNull TextView geinz,
      @NonNull LinearLayout lineaTipoT, @NonNull LinearLayout linealCategoriaT,
      @NonNull RelativeLayout main, @NonNull TextView seleccionTrabajo,
      @NonNull AutoCompleteTextView subcategoria, @NonNull TextView tipot) {
    this.rootView = rootView;
    this.EstasAdos = EstasAdos;
    this.cat = cat;
    this.complete = complete;
    this.enviar = enviar;
    this.freelancer = freelancer;
    this.geinz = geinz;
    this.lineaTipoT = lineaTipoT;
    this.linealCategoriaT = linealCategoriaT;
    this.main = main;
    this.seleccionTrabajo = seleccionTrabajo;
    this.subcategoria = subcategoria;
    this.tipot = tipot;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivitySeleccionDeTrabajoBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivitySeleccionDeTrabajoBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_seleccion_de_trabajo, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivitySeleccionDeTrabajoBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.EstasAdos;
      TextView EstasAdos = ViewBindings.findChildViewById(rootView, id);
      if (EstasAdos == null) {
        break missingId;
      }

      id = R.id.cat;
      TextView cat = ViewBindings.findChildViewById(rootView, id);
      if (cat == null) {
        break missingId;
      }

      id = R.id.complete;
      AutoCompleteTextView complete = ViewBindings.findChildViewById(rootView, id);
      if (complete == null) {
        break missingId;
      }

      id = R.id.enviar;
      AppCompatButton enviar = ViewBindings.findChildViewById(rootView, id);
      if (enviar == null) {
        break missingId;
      }

      id = R.id.freelancer;
      TextView freelancer = ViewBindings.findChildViewById(rootView, id);
      if (freelancer == null) {
        break missingId;
      }

      id = R.id.geinz;
      TextView geinz = ViewBindings.findChildViewById(rootView, id);
      if (geinz == null) {
        break missingId;
      }

      id = R.id.lineaTipoT;
      LinearLayout lineaTipoT = ViewBindings.findChildViewById(rootView, id);
      if (lineaTipoT == null) {
        break missingId;
      }

      id = R.id.linealCategoriaT;
      LinearLayout linealCategoriaT = ViewBindings.findChildViewById(rootView, id);
      if (linealCategoriaT == null) {
        break missingId;
      }

      RelativeLayout main = (RelativeLayout) rootView;

      id = R.id.seleccionTrabajo;
      TextView seleccionTrabajo = ViewBindings.findChildViewById(rootView, id);
      if (seleccionTrabajo == null) {
        break missingId;
      }

      id = R.id.subcategoria;
      AutoCompleteTextView subcategoria = ViewBindings.findChildViewById(rootView, id);
      if (subcategoria == null) {
        break missingId;
      }

      id = R.id.tipot;
      TextView tipot = ViewBindings.findChildViewById(rootView, id);
      if (tipot == null) {
        break missingId;
      }

      return new ActivitySeleccionDeTrabajoBinding((RelativeLayout) rootView, EstasAdos, cat,
          complete, enviar, freelancer, geinz, lineaTipoT, linealCategoriaT, main, seleccionTrabajo,
          subcategoria, tipot);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
