// Generated by view binder compiler. Do not edit!
package com.geinzz.geinzwork.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.geinzz.geinzwork.R;
import com.google.android.material.button.MaterialButton;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityConfirmacionDeCompraTiendaBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final TextView codigoPedido;

  @NonNull
  public final MaterialButton confirmar;

  @NonNull
  public final TextView deliveryEscojido;

  @NonNull
  public final TextView direccion;

  @NonNull
  public final TextView fecha;

  @NonNull
  public final TextView hora;

  @NonNull
  public final LinearLayout layoutDescripcion;

  @NonNull
  public final LinearLayout layoutDireccion;

  @NonNull
  public final LinearLayout linealDriverPrecio;

  @NonNull
  public final LinearLayout linealMetodoEntrega;

  @NonNull
  public final LinearLayout linealMetodoEntrega1;

  @NonNull
  public final TextView localidaUSer;

  @NonNull
  public final TextView localidadTienda;

  @NonNull
  public final RelativeLayout main;

  @NonNull
  public final TextView metodoEntrega;

  @NonNull
  public final TextView nombreTienda;

  @NonNull
  public final TextView nombreUser;

  @NonNull
  public final TextView numero;

  @NonNull
  public final TextView precioDriver;

  @NonNull
  public final ImageButton productos;

  @NonNull
  public final TextView referenciaEntrega;

  @NonNull
  public final TextView tipoTienda;

  @NonNull
  public final TextView tipop;

  @NonNull
  public final TextView totalPagar;

  @NonNull
  public final TextView totalProductos;

  private ActivityConfirmacionDeCompraTiendaBinding(@NonNull RelativeLayout rootView,
      @NonNull TextView codigoPedido, @NonNull MaterialButton confirmar,
      @NonNull TextView deliveryEscojido, @NonNull TextView direccion, @NonNull TextView fecha,
      @NonNull TextView hora, @NonNull LinearLayout layoutDescripcion,
      @NonNull LinearLayout layoutDireccion, @NonNull LinearLayout linealDriverPrecio,
      @NonNull LinearLayout linealMetodoEntrega, @NonNull LinearLayout linealMetodoEntrega1,
      @NonNull TextView localidaUSer, @NonNull TextView localidadTienda,
      @NonNull RelativeLayout main, @NonNull TextView metodoEntrega, @NonNull TextView nombreTienda,
      @NonNull TextView nombreUser, @NonNull TextView numero, @NonNull TextView precioDriver,
      @NonNull ImageButton productos, @NonNull TextView referenciaEntrega,
      @NonNull TextView tipoTienda, @NonNull TextView tipop, @NonNull TextView totalPagar,
      @NonNull TextView totalProductos) {
    this.rootView = rootView;
    this.codigoPedido = codigoPedido;
    this.confirmar = confirmar;
    this.deliveryEscojido = deliveryEscojido;
    this.direccion = direccion;
    this.fecha = fecha;
    this.hora = hora;
    this.layoutDescripcion = layoutDescripcion;
    this.layoutDireccion = layoutDireccion;
    this.linealDriverPrecio = linealDriverPrecio;
    this.linealMetodoEntrega = linealMetodoEntrega;
    this.linealMetodoEntrega1 = linealMetodoEntrega1;
    this.localidaUSer = localidaUSer;
    this.localidadTienda = localidadTienda;
    this.main = main;
    this.metodoEntrega = metodoEntrega;
    this.nombreTienda = nombreTienda;
    this.nombreUser = nombreUser;
    this.numero = numero;
    this.precioDriver = precioDriver;
    this.productos = productos;
    this.referenciaEntrega = referenciaEntrega;
    this.tipoTienda = tipoTienda;
    this.tipop = tipop;
    this.totalPagar = totalPagar;
    this.totalProductos = totalProductos;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityConfirmacionDeCompraTiendaBinding inflate(
      @NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityConfirmacionDeCompraTiendaBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_confirmacion_de_compra_tienda, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityConfirmacionDeCompraTiendaBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.codigoPedido;
      TextView codigoPedido = ViewBindings.findChildViewById(rootView, id);
      if (codigoPedido == null) {
        break missingId;
      }

      id = R.id.confirmar;
      MaterialButton confirmar = ViewBindings.findChildViewById(rootView, id);
      if (confirmar == null) {
        break missingId;
      }

      id = R.id.deliveryEscojido;
      TextView deliveryEscojido = ViewBindings.findChildViewById(rootView, id);
      if (deliveryEscojido == null) {
        break missingId;
      }

      id = R.id.direccion;
      TextView direccion = ViewBindings.findChildViewById(rootView, id);
      if (direccion == null) {
        break missingId;
      }

      id = R.id.fecha;
      TextView fecha = ViewBindings.findChildViewById(rootView, id);
      if (fecha == null) {
        break missingId;
      }

      id = R.id.hora;
      TextView hora = ViewBindings.findChildViewById(rootView, id);
      if (hora == null) {
        break missingId;
      }

      id = R.id.layoutDescripcion;
      LinearLayout layoutDescripcion = ViewBindings.findChildViewById(rootView, id);
      if (layoutDescripcion == null) {
        break missingId;
      }

      id = R.id.layoutDireccion;
      LinearLayout layoutDireccion = ViewBindings.findChildViewById(rootView, id);
      if (layoutDireccion == null) {
        break missingId;
      }

      id = R.id.lineal_driver_precio;
      LinearLayout linealDriverPrecio = ViewBindings.findChildViewById(rootView, id);
      if (linealDriverPrecio == null) {
        break missingId;
      }

      id = R.id.linealMetodoEntrega;
      LinearLayout linealMetodoEntrega = ViewBindings.findChildViewById(rootView, id);
      if (linealMetodoEntrega == null) {
        break missingId;
      }

      id = R.id.lineal_metodo_entrega;
      LinearLayout linealMetodoEntrega1 = ViewBindings.findChildViewById(rootView, id);
      if (linealMetodoEntrega1 == null) {
        break missingId;
      }

      id = R.id.localidaUSer;
      TextView localidaUSer = ViewBindings.findChildViewById(rootView, id);
      if (localidaUSer == null) {
        break missingId;
      }

      id = R.id.localidadTienda;
      TextView localidadTienda = ViewBindings.findChildViewById(rootView, id);
      if (localidadTienda == null) {
        break missingId;
      }

      RelativeLayout main = (RelativeLayout) rootView;

      id = R.id.metodoEntrega;
      TextView metodoEntrega = ViewBindings.findChildViewById(rootView, id);
      if (metodoEntrega == null) {
        break missingId;
      }

      id = R.id.nombreTienda;
      TextView nombreTienda = ViewBindings.findChildViewById(rootView, id);
      if (nombreTienda == null) {
        break missingId;
      }

      id = R.id.nombreUser;
      TextView nombreUser = ViewBindings.findChildViewById(rootView, id);
      if (nombreUser == null) {
        break missingId;
      }

      id = R.id.numero;
      TextView numero = ViewBindings.findChildViewById(rootView, id);
      if (numero == null) {
        break missingId;
      }

      id = R.id.precioDriver;
      TextView precioDriver = ViewBindings.findChildViewById(rootView, id);
      if (precioDriver == null) {
        break missingId;
      }

      id = R.id.productos;
      ImageButton productos = ViewBindings.findChildViewById(rootView, id);
      if (productos == null) {
        break missingId;
      }

      id = R.id.referenciaEntrega;
      TextView referenciaEntrega = ViewBindings.findChildViewById(rootView, id);
      if (referenciaEntrega == null) {
        break missingId;
      }

      id = R.id.tipoTienda;
      TextView tipoTienda = ViewBindings.findChildViewById(rootView, id);
      if (tipoTienda == null) {
        break missingId;
      }

      id = R.id.tipop;
      TextView tipop = ViewBindings.findChildViewById(rootView, id);
      if (tipop == null) {
        break missingId;
      }

      id = R.id.totalPagar;
      TextView totalPagar = ViewBindings.findChildViewById(rootView, id);
      if (totalPagar == null) {
        break missingId;
      }

      id = R.id.totalProductos;
      TextView totalProductos = ViewBindings.findChildViewById(rootView, id);
      if (totalProductos == null) {
        break missingId;
      }

      return new ActivityConfirmacionDeCompraTiendaBinding((RelativeLayout) rootView, codigoPedido,
          confirmar, deliveryEscojido, direccion, fecha, hora, layoutDescripcion, layoutDireccion,
          linealDriverPrecio, linealMetodoEntrega, linealMetodoEntrega1, localidaUSer,
          localidadTienda, main, metodoEntrega, nombreTienda, nombreUser, numero, precioDriver,
          productos, referenciaEntrega, tipoTienda, tipop, totalPagar, totalProductos);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
