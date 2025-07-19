package com.geinzz.geinzwork.utils.constantes.hora

import android.annotation.SuppressLint
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.R
import io.getstream.photoview.PhotoView

class ImageDialogFragmentURI : DialogFragment() {

    companion object {
        private const val IMAGE_URI_KEY = "image_uri"
        private const val TAG = "ImageDialogFragment"

        fun newInstance(imageUri: Uri): ImageDialogFragmentURI {
            val fragment = ImageDialogFragmentURI()
            val args = Bundle()
            args.putString(IMAGE_URI_KEY, imageUri.toString())
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder =AlertDialog.Builder(requireActivity(), R.style.RoundedDialog)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_image_dialog, null)

        val photoView = view.findViewById<PhotoView>(R.id.photoView)
        val imageUriString = arguments?.getString(IMAGE_URI_KEY)
        val imageUri = imageUriString?.let { Uri.parse(it) }

        imageUri?.let {
            Log.d(TAG, "URI passed/obtained: $it") // Log para ver la URI
            println("URI passed/obtained: $it") // Println para ver la URI en la consola
            Glide.with(this)
                .load(it)
                .into(photoView)
        }
        val dialog = builder.setView(view).create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.roun_zoom_img)
        dialog.window?.setDimAmount(1f)  // Ajusta la cantidad de desenfoque
        return dialog
    }
}