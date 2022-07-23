package com.lagradost.quicknovel.providers

import com.lagradost.quicknovel.MainAPI
import com.lagradost.quicknovel.R

class NovelsOnlineProvider : MainAPI() {

    override val name = "NovelsOnline"
    override val mainUrl = "https://novelsonline.net"
    override val hasMainPage = true

    override val iconId = R.drawable.icon_novelsonline

    override val iconBackgroundId = R.color.white

}