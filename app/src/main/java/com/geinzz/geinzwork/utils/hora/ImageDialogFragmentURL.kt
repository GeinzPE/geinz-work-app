import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.R
import io.getstream.photoview.PhotoView

class ImageDialogFragmentURL : DialogFragment() {
    companion object {
        private const val IMAGE_URL_KEY = "image_url"

        fun newInstance(imageUrl: String?): ImageDialogFragmentURL {
            val fragment = ImageDialogFragmentURL()
            val args = Bundle()
            args.putString(IMAGE_URL_KEY, imageUrl)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(), R.style.RoundedDialog)
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_image_dialog, null)

        val imageView = view.findViewById<PhotoView>(R.id.photoView)


        val imageUrl = arguments?.getString(IMAGE_URL_KEY)

        imageUrl?.let {
            println("Cargando imagen desde URL: $it")
            try {
                Glide.with(requireContext())
                    .load(it)
                    .into(imageView)
            } catch (e: Exception) {
                println("No se pudo setear la imagen")
            }
        }

        val dialog = builder.setView(view).create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.roun_zoom_img)
        dialog.window?.setDimAmount(1f)  // Ajusta la cantidad de desenfoque
        return dialog
    }
}
