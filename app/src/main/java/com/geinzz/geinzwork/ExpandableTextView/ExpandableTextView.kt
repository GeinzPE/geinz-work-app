import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.geinzz.geinzwork.R

class ExpandableTextView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val tvContent: TextView
    private val btnExpand: Button

    private var isExpanded = false
    private var maxLinesCollapsed = 3

    init {
        LayoutInflater.from(context).inflate(R.layout.expandabletextview, this, true)
        orientation = VERTICAL

        tvContent = findViewById(R.id.tvContent)
        btnExpand = findViewById(R.id.btnExpand)

        btnExpand.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                tvContent.maxLines = Integer.MAX_VALUE
                btnExpand.text = "Ver menos"
            } else {
                tvContent.maxLines = maxLinesCollapsed
                btnExpand.text = "Ver más"
            }
        }
    }

    fun setText(text: CharSequence) {
        tvContent.text = text
        tvContent.maxLines = maxLinesCollapsed
        btnExpand.visibility = if (tvContent.lineCount > maxLinesCollapsed) VISIBLE else GONE
    }
}
