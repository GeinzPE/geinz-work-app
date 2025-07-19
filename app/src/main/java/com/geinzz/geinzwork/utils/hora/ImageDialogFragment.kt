import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import com.geinzz.geinzwork.R
import io.getstream.photoview.PhotoView

class ImageDialogFragment : DialogFragment() {

    companion object {
        private const val IMAGE_RES_ID_KEY = "image_res_id"

        fun newInstance(imageResId: Int): ImageDialogFragment {
            val fragment = ImageDialogFragment()
            val args = Bundle()
            args.putInt(IMAGE_RES_ID_KEY, imageResId)
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
        val imageResId = arguments?.getInt(IMAGE_RES_ID_KEY)

        imageResId?.let {
            photoView.setImageResource(it)
        }

        val dialog = builder.setView(view).create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.roun_zoom_img)
        dialog.window?.setDimAmount(1f)  // Ajusta la cantidad de desenfoque
        return dialog
    }
}
