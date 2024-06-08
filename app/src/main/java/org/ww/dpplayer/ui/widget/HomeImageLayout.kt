package org.ww.dpplayer.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import org.ww.dpplayer.databinding.BannerImageLayoutBinding
import org.ww.dpplayer.databinding.UserImageLayoutBinding

class HomeImageLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private var userImageBinding: UserImageLayoutBinding? = null
    private var bannerImageBinding: BannerImageLayoutBinding? = null

    init {
        bannerImageBinding = BannerImageLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    }

    val userImage: ImageView
        get() = bannerImageBinding!!.userImage


    val bannerImage: ImageView?
        get() = bannerImageBinding!!.bannerImage

    val titleWelcome : TextView
        get() = bannerImageBinding!!.titleWelcome

}