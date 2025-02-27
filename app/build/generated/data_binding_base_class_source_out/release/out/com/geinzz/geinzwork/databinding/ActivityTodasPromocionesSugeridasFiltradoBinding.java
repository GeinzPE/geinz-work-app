// Generated by view binder compiler. Do not edit!
package com.geinzz.geinzwork.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.geinzz.geinzwork.R;
import com.google.android.material.appbar.AppBarLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityTodasPromocionesSugeridasFiltradoBinding implements ViewBinding {
  @NonNull
  private final CoordinatorLayout rootView;

  @NonNull
  public final RecyclerView PromocionesSugeridasRecicle;

  @NonNull
  public final AppBarLayout linealappLayout;

  @NonNull
  public final CoordinatorLayout main;

  private ActivityTodasPromocionesSugeridasFiltradoBinding(@NonNull CoordinatorLayout rootView,
      @NonNull RecyclerView PromocionesSugeridasRecicle, @NonNull AppBarLayout linealappLayout,
      @NonNull CoordinatorLayout main) {
    this.rootView = rootView;
    this.PromocionesSugeridasRecicle = PromocionesSugeridasRecicle;
    this.linealappLayout = linealappLayout;
    this.main = main;
  }

  @Override
  @NonNull
  public CoordinatorLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityTodasPromocionesSugeridasFiltradoBinding inflate(
      @NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityTodasPromocionesSugeridasFiltradoBinding inflate(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_todas_promociones_sugeridas_filtrado, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityTodasPromocionesSugeridasFiltradoBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.PromocionesSugeridasRecicle;
      RecyclerView PromocionesSugeridasRecicle = ViewBindings.findChildViewById(rootView, id);
      if (PromocionesSugeridasRecicle == null) {
        break missingId;
      }

      id = R.id.linealappLayout;
      AppBarLayout linealappLayout = ViewBindings.findChildViewById(rootView, id);
      if (linealappLayout == null) {
        break missingId;
      }

      CoordinatorLayout main = (CoordinatorLayout) rootView;

      return new ActivityTodasPromocionesSugeridasFiltradoBinding((CoordinatorLayout) rootView,
          PromocionesSugeridasRecicle, linealappLayout, main);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
