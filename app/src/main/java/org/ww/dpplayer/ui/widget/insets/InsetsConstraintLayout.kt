package org.ww.dpplayer.ui.widget.insets

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import dev.chrisbanes.insetter.applyInsetter

class InsetsConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
            applyInsetter {
                type(navigationBars = true) {
                    padding(vertical = true)
                }
            }
    }
}