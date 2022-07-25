package com.lagradost.quicknovel.providers

import com.lagradost.quicknovel.MainAPI
import com.lagradost.quicknovel.R

class ReadWebNovelsProvider : MainAPI() {
    override val name = "ReadWebNovels"
    override val mainUrl = "https://readwebnovels.net"
    override val hasMainPage = true

    override val iconId = R.drawable.icon_readwebnovels

    override val iconBackgroundId = R.color.red
}