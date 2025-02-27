// Generated by view binder compiler. Do not edit!
package com.geinzz.geinzwork.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.geinzz.geinzwork.R;
import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import com.google.android.material.textfield.TextInputLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class BottomSheetFiltradoComprasCarritoBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final RadioButton EnTienda;

  @NonNull
  public final RadioGroup RadioGrup;

  @NonNull
  public final Switch Tienda;

  @NonNull
  public final AutoCompleteTextView TiendaED;

  @NonNull
  public final Button buttonSetMax;

  @NonNull
  public final RadioButton camino;

  @NonNull
  public final BottomSheetDragHandleView cerrar;

  @NonNull
  public final RadioButton entragado;

  @NonNull
  public final LinearLayout estadosLineal;

  @NonNull
  public final TextInputLayout fecha;

  @NonNull
  public final EditText fechaED;

  @NonNull
  public final Button filtraID;

  @NonNull
  public final Switch filtradoEstado;

  @NonNull
  public final Switch filtradoFecha;

  @NonNull
  public final LinearLayout linealFecha;

  @NonNull
  public final LinearLayout linealSakeBAr;

  @NonNull
  public final LinearLayout linealTienda;

  @NonNull
  public final TextView nombress;

  @NonNull
  public final RadioButton pendiente;

  @NonNull
  public final Switch preciofiltrado;

  @NonNull
  public final SeekBar seekBar;

  @NonNull
  public final TextView textViewValue;

  @NonNull
  public final EditText valoMAXED;

  @NonNull
  public final TextInputLayout valomax;

  private BottomSheetFiltradoComprasCarritoBinding(@NonNull LinearLayout rootView,
      @NonNull RadioButton EnTienda, @NonNull RadioGroup RadioGrup, @NonNull Switch Tienda,
      @NonNull AutoCompleteTextView TiendaED, @NonNull Button buttonSetMax,
      @NonNull RadioButton camino, @NonNull BottomSheetDragHandleView cerrar,
      @NonNull RadioButton entragado, @NonNull LinearLayout estadosLineal,
      @NonNull TextInputLayout fecha, @NonNull EditText fechaED, @NonNull Button filtraID,
      @NonNull Switch filtradoEstado, @NonNull Switch filtradoFecha,
      @NonNull LinearLayout linealFecha, @NonNull LinearLayout linealSakeBAr,
      @NonNull LinearLayout linealTienda, @NonNull TextView nombress,
      @NonNull RadioButton pendiente, @NonNull Switch preciofiltrado, @NonNull SeekBar seekBar,
      @NonNull TextView textViewValue, @NonNull EditText valoMAXED,
      @NonNull TextInputLayout valomax) {
    this.rootView = rootView;
    this.EnTienda = EnTienda;
    this.RadioGrup = RadioGrup;
    this.Tienda = Tienda;
    this.TiendaED = TiendaED;
    this.buttonSetMax = buttonSetMax;
    this.camino = camino;
    this.cerrar = cerrar;
    this.entragado = entragado;
    this.estadosLineal = estadosLineal;
    this.fecha = fecha;
    this.fechaED = fechaED;
    this.filtraID = filtraID;
    this.filtradoEstado = filtradoEstado;
    this.filtradoFecha = filtradoFecha;
    this.linealFecha = linealFecha;
    this.linealSakeBAr = linealSakeBAr;
    this.linealTienda = linealTienda;
    this.nombress = nombress;
    this.pendiente = pendiente;
    this.preciofiltrado = preciofiltrado;
    this.seekBar = seekBar;
    this.textViewValue = textViewValue;
    this.valoMAXED = valoMAXED;
    this.valomax = valomax;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static BottomSheetFiltradoComprasCarritoBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static BottomSheetFiltradoComprasCarritoBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.bottom_sheet_filtrado_compras_carrito, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static BottomSheetFiltradoComprasCarritoBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.EnTienda;
      RadioButton EnTienda = ViewBindings.findChildViewById(rootView, id);
      if (EnTienda == null) {
        break missingId;
      }

      id = R.id.RadioGrup;
      RadioGroup RadioGrup = ViewBindings.findChildViewById(rootView, id);
      if (RadioGrup == null) {
        break missingId;
      }

      id = R.id.Tienda;
      Switch Tienda = ViewBindings.findChildViewById(rootView, id);
      if (Tienda == null) {
        break missingId;
      }

      id = R.id.TiendaED;
      AutoCompleteTextView TiendaED = ViewBindings.findChildViewById(rootView, id);
      if (TiendaED == null) {
        break missingId;
      }

      id = R.id.buttonSetMax;
      Button buttonSetMax = ViewBindings.findChildViewById(rootView, id);
      if (buttonSetMax == null) {
        break missingId;
      }

      id = R.id.camino;
      RadioButton camino = ViewBindings.findChildViewById(rootView, id);
      if (camino == null) {
        break missingId;
      }

      id = R.id.cerrar;
      BottomSheetDragHandleView cerrar = ViewBindings.findChildViewById(rootView, id);
      if (cerrar == null) {
        break missingId;
      }

      id = R.id.entragado;
      RadioButton entragado = ViewBindings.findChildViewById(rootView, id);
      if (entragado == null) {
        break missingId;
      }

      id = R.id.estadosLineal;
      LinearLayout estadosLineal = ViewBindings.findChildViewById(rootView, id);
      if (estadosLineal == null) {
        break missingId;
      }

      id = R.id.fecha;
      TextInputLayout fecha = ViewBindings.findChildViewById(rootView, id);
      if (fecha == null) {
        break missingId;
      }

      id = R.id.fechaED;
      EditText fechaED = ViewBindings.findChildViewById(rootView, id);
      if (fechaED == null) {
        break missingId;
      }

      id = R.id.filtraID;
      Button filtraID = ViewBindings.findChildViewById(rootView, id);
      if (filtraID == null) {
        break missingId;
      }

      id = R.id.filtradoEstado;
      Switch filtradoEstado = ViewBindings.findChildViewById(rootView, id);
      if (filtradoEstado == null) {
        break missingId;
      }

      id = R.id.filtradoFecha;
      Switch filtradoFecha = ViewBindings.findChildViewById(rootView, id);
      if (filtradoFecha == null) {
        break missingId;
      }

      id = R.id.linealFecha;
      LinearLayout linealFecha = ViewBindings.findChildViewById(rootView, id);
      if (linealFecha == null) {
        break missingId;
      }

      id = R.id.linealSakeBAr;
      LinearLayout linealSakeBAr = ViewBindings.findChildViewById(rootView, id);
      if (linealSakeBAr == null) {
        break missingId;
      }

      id = R.id.lineal_tienda;
      LinearLayout linealTienda = ViewBindings.findChildViewById(rootView, id);
      if (linealTienda == null) {
        break missingId;
      }

      id = R.id.nombress;
      TextView nombress = ViewBindings.findChildViewById(rootView, id);
      if (nombress == null) {
        break missingId;
      }

      id = R.id.pendiente;
      RadioButton pendiente = ViewBindings.findChildViewById(rootView, id);
      if (pendiente == null) {
        break missingId;
      }

      id = R.id.preciofiltrado;
      Switch preciofiltrado = ViewBindings.findChildViewById(rootView, id);
      if (preciofiltrado == null) {
        break missingId;
      }

      id = R.id.seekBar;
      SeekBar seekBar = ViewBindings.findChildViewById(rootView, id);
      if (seekBar == null) {
        break missingId;
      }

      id = R.id.textViewValue;
      TextView textViewValue = ViewBindings.findChildViewById(rootView, id);
      if (textViewValue == null) {
        break missingId;
      }

      id = R.id.valoMAXED;
      EditText valoMAXED = ViewBindings.findChildViewById(rootView, id);
      if (valoMAXED == null) {
        break missingId;
      }

      id = R.id.valomax;
      TextInputLayout valomax = ViewBindings.findChildViewById(rootView, id);
      if (valomax == null) {
        break missingId;
      }

      return new BottomSheetFiltradoComprasCarritoBinding((LinearLayout) rootView, EnTienda,
          RadioGrup, Tienda, TiendaED, buttonSetMax, camino, cerrar, entragado, estadosLineal,
          fecha, fechaED, filtraID, filtradoEstado, filtradoFecha, linealFecha, linealSakeBAr,
          linealTienda, nombress, pendiente, preciofiltrado, seekBar, textViewValue, valoMAXED,
          valomax);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
