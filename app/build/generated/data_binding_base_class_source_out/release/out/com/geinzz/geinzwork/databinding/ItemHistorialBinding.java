// Generated by view binder compiler. Do not edit!
package com.geinzz.geinzwork.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.geinzz.geinzwork.R;
import de.hdodenhof.circleimageview.CircleImageView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemHistorialBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final ImageView LlamadaDriver;

  @NonNull
  public final TextView TipoTienda;

  @NonNull
  public final LinearLayout TotalDriverLineal;

  @NonNull
  public final TextView Totalus;

  @NonNull
  public final Button cancelar;

  @NonNull
  public final TextView codigoPedido;

  @NonNull
  public final LinearLayout containerDriver;

  @NonNull
  public final LinearLayout containerListenr;

  @NonNull
  public final TextView direccion;

  @NonNull
  public final TextView entregaMetodo;

  @NonNull
  public final TextView estadous;

  @NonNull
  public final TextView fechaus;

  @NonNull
  public final TextView generoDriver;

  @NonNull
  public final TextView horaus;

  @NonNull
  public final CircleImageView imgDriver;

  @NonNull
  public final CircleImageView imgTienda;

  @NonNull
  public final IncludeItemPreviewProductoBinding layoutPrevizualisacion;

  @NonNull
  public final LinearLayout lineaLayout;

  @NonNull
  public final LinearLayout linealDireccion;

  @NonNull
  public final LinearLayout linealReferencia;

  @NonNull
  public final TextView localidadTienda;

  @NonNull
  public final TextView localidaduser;

  @NonNull
  public final TextView nacionalidadDriver;

  @NonNull
  public final TextView nombreDriver;

  @NonNull
  public final TextView nombreTienda;

  @NonNull
  public final TextView nombreuser;

  @NonNull
  public final TextView numerous;

  @NonNull
  public final TextView pagometodo;

  @NonNull
  public final TextView precioDelivery;

  @NonNull
  public final LinearLayout precioDriverLineal;

  @NonNull
  public final TextView referencia;

  @NonNull
  public final RelativeLayout relativeTienda;

  @NonNull
  public final RelativeLayout relativecamino;

  @NonNull
  public final RelativeLayout relativedriver;

  @NonNull
  public final RelativeLayout relativeentrega;

  @NonNull
  public final TextView tipoCompra;

  @NonNull
  public final TextView totalCancelar;

  @NonNull
  public final TextView totalDriver;

  @NonNull
  public final ImageButton verProductos;

  @NonNull
  public final ImageView whatsappDriver;

  private ItemHistorialBinding(@NonNull RelativeLayout rootView, @NonNull ImageView LlamadaDriver,
      @NonNull TextView TipoTienda, @NonNull LinearLayout TotalDriverLineal,
      @NonNull TextView Totalus, @NonNull Button cancelar, @NonNull TextView codigoPedido,
      @NonNull LinearLayout containerDriver, @NonNull LinearLayout containerListenr,
      @NonNull TextView direccion, @NonNull TextView entregaMetodo, @NonNull TextView estadous,
      @NonNull TextView fechaus, @NonNull TextView generoDriver, @NonNull TextView horaus,
      @NonNull CircleImageView imgDriver, @NonNull CircleImageView imgTienda,
      @NonNull IncludeItemPreviewProductoBinding layoutPrevizualisacion,
      @NonNull LinearLayout lineaLayout, @NonNull LinearLayout linealDireccion,
      @NonNull LinearLayout linealReferencia, @NonNull TextView localidadTienda,
      @NonNull TextView localidaduser, @NonNull TextView nacionalidadDriver,
      @NonNull TextView nombreDriver, @NonNull TextView nombreTienda, @NonNull TextView nombreuser,
      @NonNull TextView numerous, @NonNull TextView pagometodo, @NonNull TextView precioDelivery,
      @NonNull LinearLayout precioDriverLineal, @NonNull TextView referencia,
      @NonNull RelativeLayout relativeTienda, @NonNull RelativeLayout relativecamino,
      @NonNull RelativeLayout relativedriver, @NonNull RelativeLayout relativeentrega,
      @NonNull TextView tipoCompra, @NonNull TextView totalCancelar, @NonNull TextView totalDriver,
      @NonNull ImageButton verProductos, @NonNull ImageView whatsappDriver) {
    this.rootView = rootView;
    this.LlamadaDriver = LlamadaDriver;
    this.TipoTienda = TipoTienda;
    this.TotalDriverLineal = TotalDriverLineal;
    this.Totalus = Totalus;
    this.cancelar = cancelar;
    this.codigoPedido = codigoPedido;
    this.containerDriver = containerDriver;
    this.containerListenr = containerListenr;
    this.direccion = direccion;
    this.entregaMetodo = entregaMetodo;
    this.estadous = estadous;
    this.fechaus = fechaus;
    this.generoDriver = generoDriver;
    this.horaus = horaus;
    this.imgDriver = imgDriver;
    this.imgTienda = imgTienda;
    this.layoutPrevizualisacion = layoutPrevizualisacion;
    this.lineaLayout = lineaLayout;
    this.linealDireccion = linealDireccion;
    this.linealReferencia = linealReferencia;
    this.localidadTienda = localidadTienda;
    this.localidaduser = localidaduser;
    this.nacionalidadDriver = nacionalidadDriver;
    this.nombreDriver = nombreDriver;
    this.nombreTienda = nombreTienda;
    this.nombreuser = nombreuser;
    this.numerous = numerous;
    this.pagometodo = pagometodo;
    this.precioDelivery = precioDelivery;
    this.precioDriverLineal = precioDriverLineal;
    this.referencia = referencia;
    this.relativeTienda = relativeTienda;
    this.relativecamino = relativecamino;
    this.relativedriver = relativedriver;
    this.relativeentrega = relativeentrega;
    this.tipoCompra = tipoCompra;
    this.totalCancelar = totalCancelar;
    this.totalDriver = totalDriver;
    this.verProductos = verProductos;
    this.whatsappDriver = whatsappDriver;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemHistorialBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemHistorialBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_historial, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemHistorialBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.LlamadaDriver;
      ImageView LlamadaDriver = ViewBindings.findChildViewById(rootView, id);
      if (LlamadaDriver == null) {
        break missingId;
      }

      id = R.id.TipoTienda;
      TextView TipoTienda = ViewBindings.findChildViewById(rootView, id);
      if (TipoTienda == null) {
        break missingId;
      }

      id = R.id.TotalDriverLineal;
      LinearLayout TotalDriverLineal = ViewBindings.findChildViewById(rootView, id);
      if (TotalDriverLineal == null) {
        break missingId;
      }

      id = R.id.Totalus;
      TextView Totalus = ViewBindings.findChildViewById(rootView, id);
      if (Totalus == null) {
        break missingId;
      }

      id = R.id.cancelar;
      Button cancelar = ViewBindings.findChildViewById(rootView, id);
      if (cancelar == null) {
        break missingId;
      }

      id = R.id.codigoPedido;
      TextView codigoPedido = ViewBindings.findChildViewById(rootView, id);
      if (codigoPedido == null) {
        break missingId;
      }

      id = R.id.containerDriver;
      LinearLayout containerDriver = ViewBindings.findChildViewById(rootView, id);
      if (containerDriver == null) {
        break missingId;
      }

      id = R.id.containerListenr;
      LinearLayout containerListenr = ViewBindings.findChildViewById(rootView, id);
      if (containerListenr == null) {
        break missingId;
      }

      id = R.id.direccion;
      TextView direccion = ViewBindings.findChildViewById(rootView, id);
      if (direccion == null) {
        break missingId;
      }

      id = R.id.entregaMetodo;
      TextView entregaMetodo = ViewBindings.findChildViewById(rootView, id);
      if (entregaMetodo == null) {
        break missingId;
      }

      id = R.id.estadous;
      TextView estadous = ViewBindings.findChildViewById(rootView, id);
      if (estadous == null) {
        break missingId;
      }

      id = R.id.fechaus;
      TextView fechaus = ViewBindings.findChildViewById(rootView, id);
      if (fechaus == null) {
        break missingId;
      }

      id = R.id.genero_driver;
      TextView generoDriver = ViewBindings.findChildViewById(rootView, id);
      if (generoDriver == null) {
        break missingId;
      }

      id = R.id.horaus;
      TextView horaus = ViewBindings.findChildViewById(rootView, id);
      if (horaus == null) {
        break missingId;
      }

      id = R.id.imgDriver;
      CircleImageView imgDriver = ViewBindings.findChildViewById(rootView, id);
      if (imgDriver == null) {
        break missingId;
      }

      id = R.id.imgTienda;
      CircleImageView imgTienda = ViewBindings.findChildViewById(rootView, id);
      if (imgTienda == null) {
        break missingId;
      }

      id = R.id.layout_previzualisacion;
      View layoutPrevizualisacion = ViewBindings.findChildViewById(rootView, id);
      if (layoutPrevizualisacion == null) {
        break missingId;
      }
      IncludeItemPreviewProductoBinding binding_layoutPrevizualisacion = IncludeItemPreviewProductoBinding.bind(layoutPrevizualisacion);

      id = R.id.lineaLayout;
      LinearLayout lineaLayout = ViewBindings.findChildViewById(rootView, id);
      if (lineaLayout == null) {
        break missingId;
      }

      id = R.id.linealDireccion;
      LinearLayout linealDireccion = ViewBindings.findChildViewById(rootView, id);
      if (linealDireccion == null) {
        break missingId;
      }

      id = R.id.linealReferencia;
      LinearLayout linealReferencia = ViewBindings.findChildViewById(rootView, id);
      if (linealReferencia == null) {
        break missingId;
      }

      id = R.id.localidadTienda;
      TextView localidadTienda = ViewBindings.findChildViewById(rootView, id);
      if (localidadTienda == null) {
        break missingId;
      }

      id = R.id.localidaduser;
      TextView localidaduser = ViewBindings.findChildViewById(rootView, id);
      if (localidaduser == null) {
        break missingId;
      }

      id = R.id.nacionalidad_driver;
      TextView nacionalidadDriver = ViewBindings.findChildViewById(rootView, id);
      if (nacionalidadDriver == null) {
        break missingId;
      }

      id = R.id.nombreDriver;
      TextView nombreDriver = ViewBindings.findChildViewById(rootView, id);
      if (nombreDriver == null) {
        break missingId;
      }

      id = R.id.nombreTienda;
      TextView nombreTienda = ViewBindings.findChildViewById(rootView, id);
      if (nombreTienda == null) {
        break missingId;
      }

      id = R.id.nombreuser;
      TextView nombreuser = ViewBindings.findChildViewById(rootView, id);
      if (nombreuser == null) {
        break missingId;
      }

      id = R.id.numerous;
      TextView numerous = ViewBindings.findChildViewById(rootView, id);
      if (numerous == null) {
        break missingId;
      }

      id = R.id.pagometodo;
      TextView pagometodo = ViewBindings.findChildViewById(rootView, id);
      if (pagometodo == null) {
        break missingId;
      }

      id = R.id.precioDelivery;
      TextView precioDelivery = ViewBindings.findChildViewById(rootView, id);
      if (precioDelivery == null) {
        break missingId;
      }

      id = R.id.precioDriverLineal;
      LinearLayout precioDriverLineal = ViewBindings.findChildViewById(rootView, id);
      if (precioDriverLineal == null) {
        break missingId;
      }

      id = R.id.referencia;
      TextView referencia = ViewBindings.findChildViewById(rootView, id);
      if (referencia == null) {
        break missingId;
      }

      id = R.id.relativeTienda;
      RelativeLayout relativeTienda = ViewBindings.findChildViewById(rootView, id);
      if (relativeTienda == null) {
        break missingId;
      }

      id = R.id.relativecamino;
      RelativeLayout relativecamino = ViewBindings.findChildViewById(rootView, id);
      if (relativecamino == null) {
        break missingId;
      }

      id = R.id.relativedriver;
      RelativeLayout relativedriver = ViewBindings.findChildViewById(rootView, id);
      if (relativedriver == null) {
        break missingId;
      }

      id = R.id.relativeentrega;
      RelativeLayout relativeentrega = ViewBindings.findChildViewById(rootView, id);
      if (relativeentrega == null) {
        break missingId;
      }

      id = R.id.tipoCompra;
      TextView tipoCompra = ViewBindings.findChildViewById(rootView, id);
      if (tipoCompra == null) {
        break missingId;
      }

      id = R.id.totalCancelar;
      TextView totalCancelar = ViewBindings.findChildViewById(rootView, id);
      if (totalCancelar == null) {
        break missingId;
      }

      id = R.id.totalDriver;
      TextView totalDriver = ViewBindings.findChildViewById(rootView, id);
      if (totalDriver == null) {
        break missingId;
      }

      id = R.id.verProductos;
      ImageButton verProductos = ViewBindings.findChildViewById(rootView, id);
      if (verProductos == null) {
        break missingId;
      }

      id = R.id.whatsappDriver;
      ImageView whatsappDriver = ViewBindings.findChildViewById(rootView, id);
      if (whatsappDriver == null) {
        break missingId;
      }

      return new ItemHistorialBinding((RelativeLayout) rootView, LlamadaDriver, TipoTienda,
          TotalDriverLineal, Totalus, cancelar, codigoPedido, containerDriver, containerListenr,
          direccion, entregaMetodo, estadous, fechaus, generoDriver, horaus, imgDriver, imgTienda,
          binding_layoutPrevizualisacion, lineaLayout, linealDireccion, linealReferencia,
          localidadTienda, localidaduser, nacionalidadDriver, nombreDriver, nombreTienda,
          nombreuser, numerous, pagometodo, precioDelivery, precioDriverLineal, referencia,
          relativeTienda, relativecamino, relativedriver, relativeentrega, tipoCompra,
          totalCancelar, totalDriver, verProductos, whatsappDriver);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
