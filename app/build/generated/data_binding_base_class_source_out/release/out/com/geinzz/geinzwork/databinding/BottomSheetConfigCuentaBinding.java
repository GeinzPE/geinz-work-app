// Generated by view binder compiler. Do not edit!
package com.geinzz.geinzwork.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.geinzz.geinzwork.R;
import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class BottomSheetConfigCuentaBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final LinearLayout agregarImg;

  @NonNull
  public final BottomSheetDragHandleView cerrar;

  @NonNull
  public final LinearLayout containerCerrarSeccion;

  @NonNull
  public final LinearLayout containerEliminarCuenta;

  @NonNull
  public final LinearLayout containerGuardados;

  @NonNull
  public final LinearLayout containerLocalizacion;

  @NonNull
  public final LinearLayout containerPreview;

  @NonNull
  public final LinearLayout containerReview;

  @NonNull
  public final LinearLayout containerqrAgregarImg;

  @NonNull
  public final ImageView fb;

  @NonNull
  public final ImageView ig;

  @NonNull
  public final LinearLayout lineaReportes;

  @NonNull
  public final LinearLayout linealComoFuncionGeinz;

  @NonNull
  public final LinearLayout linealPublicacion;

  @NonNull
  public final LinearLayout linealServicios;

  @NonNull
  public final LinearLayout linealVerificado;

  @NonNull
  public final ImageView localizacion;

  @NonNull
  public final ImageView preview;

  @NonNull
  public final LinearLayout qrTrabajador;

  @NonNull
  public final ImageView tk;

  @NonNull
  public final ImageView web;

  private BottomSheetConfigCuentaBinding(@NonNull ConstraintLayout rootView,
      @NonNull LinearLayout agregarImg, @NonNull BottomSheetDragHandleView cerrar,
      @NonNull LinearLayout containerCerrarSeccion, @NonNull LinearLayout containerEliminarCuenta,
      @NonNull LinearLayout containerGuardados, @NonNull LinearLayout containerLocalizacion,
      @NonNull LinearLayout containerPreview, @NonNull LinearLayout containerReview,
      @NonNull LinearLayout containerqrAgregarImg, @NonNull ImageView fb, @NonNull ImageView ig,
      @NonNull LinearLayout lineaReportes, @NonNull LinearLayout linealComoFuncionGeinz,
      @NonNull LinearLayout linealPublicacion, @NonNull LinearLayout linealServicios,
      @NonNull LinearLayout linealVerificado, @NonNull ImageView localizacion,
      @NonNull ImageView preview, @NonNull LinearLayout qrTrabajador, @NonNull ImageView tk,
      @NonNull ImageView web) {
    this.rootView = rootView;
    this.agregarImg = agregarImg;
    this.cerrar = cerrar;
    this.containerCerrarSeccion = containerCerrarSeccion;
    this.containerEliminarCuenta = containerEliminarCuenta;
    this.containerGuardados = containerGuardados;
    this.containerLocalizacion = containerLocalizacion;
    this.containerPreview = containerPreview;
    this.containerReview = containerReview;
    this.containerqrAgregarImg = containerqrAgregarImg;
    this.fb = fb;
    this.ig = ig;
    this.lineaReportes = lineaReportes;
    this.linealComoFuncionGeinz = linealComoFuncionGeinz;
    this.linealPublicacion = linealPublicacion;
    this.linealServicios = linealServicios;
    this.linealVerificado = linealVerificado;
    this.localizacion = localizacion;
    this.preview = preview;
    this.qrTrabajador = qrTrabajador;
    this.tk = tk;
    this.web = web;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static BottomSheetConfigCuentaBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static BottomSheetConfigCuentaBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.bottom_sheet_config_cuenta, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static BottomSheetConfigCuentaBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.agregar_img;
      LinearLayout agregarImg = ViewBindings.findChildViewById(rootView, id);
      if (agregarImg == null) {
        break missingId;
      }

      id = R.id.cerrar;
      BottomSheetDragHandleView cerrar = ViewBindings.findChildViewById(rootView, id);
      if (cerrar == null) {
        break missingId;
      }

      id = R.id.container_cerrar_seccion;
      LinearLayout containerCerrarSeccion = ViewBindings.findChildViewById(rootView, id);
      if (containerCerrarSeccion == null) {
        break missingId;
      }

      id = R.id.container_eliminar_cuenta;
      LinearLayout containerEliminarCuenta = ViewBindings.findChildViewById(rootView, id);
      if (containerEliminarCuenta == null) {
        break missingId;
      }

      id = R.id.container_guardados;
      LinearLayout containerGuardados = ViewBindings.findChildViewById(rootView, id);
      if (containerGuardados == null) {
        break missingId;
      }

      id = R.id.containerLocalizacion;
      LinearLayout containerLocalizacion = ViewBindings.findChildViewById(rootView, id);
      if (containerLocalizacion == null) {
        break missingId;
      }

      id = R.id.container_preview;
      LinearLayout containerPreview = ViewBindings.findChildViewById(rootView, id);
      if (containerPreview == null) {
        break missingId;
      }

      id = R.id.container_review;
      LinearLayout containerReview = ViewBindings.findChildViewById(rootView, id);
      if (containerReview == null) {
        break missingId;
      }

      id = R.id.containerqr_agregar_img;
      LinearLayout containerqrAgregarImg = ViewBindings.findChildViewById(rootView, id);
      if (containerqrAgregarImg == null) {
        break missingId;
      }

      id = R.id.fb;
      ImageView fb = ViewBindings.findChildViewById(rootView, id);
      if (fb == null) {
        break missingId;
      }

      id = R.id.ig;
      ImageView ig = ViewBindings.findChildViewById(rootView, id);
      if (ig == null) {
        break missingId;
      }

      id = R.id.lineaReportes;
      LinearLayout lineaReportes = ViewBindings.findChildViewById(rootView, id);
      if (lineaReportes == null) {
        break missingId;
      }

      id = R.id.lineal_como_funcion_Geinz;
      LinearLayout linealComoFuncionGeinz = ViewBindings.findChildViewById(rootView, id);
      if (linealComoFuncionGeinz == null) {
        break missingId;
      }

      id = R.id.lineal_Publicacion;
      LinearLayout linealPublicacion = ViewBindings.findChildViewById(rootView, id);
      if (linealPublicacion == null) {
        break missingId;
      }

      id = R.id.linealServicios;
      LinearLayout linealServicios = ViewBindings.findChildViewById(rootView, id);
      if (linealServicios == null) {
        break missingId;
      }

      id = R.id.lineal_verificado;
      LinearLayout linealVerificado = ViewBindings.findChildViewById(rootView, id);
      if (linealVerificado == null) {
        break missingId;
      }

      id = R.id.localizacion;
      ImageView localizacion = ViewBindings.findChildViewById(rootView, id);
      if (localizacion == null) {
        break missingId;
      }

      id = R.id.preview;
      ImageView preview = ViewBindings.findChildViewById(rootView, id);
      if (preview == null) {
        break missingId;
      }

      id = R.id.qr_trabajador;
      LinearLayout qrTrabajador = ViewBindings.findChildViewById(rootView, id);
      if (qrTrabajador == null) {
        break missingId;
      }

      id = R.id.tk;
      ImageView tk = ViewBindings.findChildViewById(rootView, id);
      if (tk == null) {
        break missingId;
      }

      id = R.id.web;
      ImageView web = ViewBindings.findChildViewById(rootView, id);
      if (web == null) {
        break missingId;
      }

      return new BottomSheetConfigCuentaBinding((ConstraintLayout) rootView, agregarImg, cerrar,
          containerCerrarSeccion, containerEliminarCuenta, containerGuardados,
          containerLocalizacion, containerPreview, containerReview, containerqrAgregarImg, fb, ig,
          lineaReportes, linealComoFuncionGeinz, linealPublicacion, linealServicios,
          linealVerificado, localizacion, preview, qrTrabajador, tk, web);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
