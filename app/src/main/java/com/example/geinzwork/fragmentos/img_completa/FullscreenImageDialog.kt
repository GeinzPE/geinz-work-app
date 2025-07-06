package com.example.geinzwork.fragmentos.img_completa

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.geinzz.geinzwork.R

class FullscreenImageDialog(private val imageUrl: String) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val photoView = PhotoView(requireContext())
        photoView.setBackgroundColor(Color.BLACK)
        photoView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        photoView.scaleType = ImageView.ScaleType.FIT_CENTER // 👈 muestra completa centrada
        Glide.with(requireContext()).load(imageUrl).into(photoView)

        photoView.setOnClickListener {
            dismiss()
        }

        return photoView
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}