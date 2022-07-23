package com.lagradost.quicknovel.providers

import com.lagradost.quicknovel.*
import com.lagradost.quicknovel.MainActivity.Companion.app
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class ReadNovelFullProvider : MainAPI() {
    override val mainUrl = "https://readnovelfull.com"
    override val name = "ReadNovelFull"
    override val hasMainPage = true

    override val tags = listOf(
        Pair("All", "All"),
        Pair("Shounen", "Shounen"),
        Pair("Harem", "Harem"),
        Pair("Comedy", "Comedy"),
        Pair("Martial Arts", "Martial Arts"),
        Pair("School Life", "School Life"),
        Pair("Mystery", "Mystery"),
        Pair("Shoujo", "Shoujo"),
        Pair("Romance", "Romance"),
        Pair("Sci-fi", "Sci-fi"),
        Pair("Gender Bender", "Gender Bender"),
        Pair("Mature", "Mature"),
        Pair("Fantasy", "Fantasy"),
        Pair("Horror", "Horror"),
        Pair("Drama", "Drama"),
        Pair("Tragedy", "Tragedy"),
        Pair("Supernatural", "Supernatural"),
        Pair("Ecchi", "Ecchi"),
        Pair("Xuanhuan", "Xuanhuan"),
        Pair("Adventure", "Adventure"),
        Pair("Action", "Action"),
        Pair("Psychological", "Psychological"),
        Pair("Xianxia", "Xianxia"),
        Pair("Wuxia", "Wuxia"),
        Pair("Historical", "Historical"),
        Pair("Slice of Life", "Slice of Life"),
        Pair("Seinen", "Seinen"),
        Pair("Lolicon", "Lolicon"),
        Pair("Adult", "Adult"),
        Pair("Josei", "Josei"),
        Pair("Sports", "Sports"),
        Pair("Smut", "Smut"),
        Pair("Mecha", "Mecha"),
        Pair("Yaoi", "Yaoi"),
        Pair("Shounen Ai", "Shounen Ai"),
        Pair("History", "History"),
        Pair("Reincarnation", "Reincarnation"),
        Pair("Martial", "Martial"),
        Pair("Game", "Game"),
        Pair("Eastern", "Eastern"),
        Pair("FantasyHarem", "FantasyHarem"),
        Pair("Yuri", "Yuri"),
        Pair("Magical Realism", "Magical Realism"),
        Pair("Isekai", "Isekai"),
        Pair("Supernatural Source:Explore", "Supernatural Source:Explore"),
        Pair("Video Games", "Video Games"),
        Pair("Contemporary Romance", "Contemporary Romance"),
        Pair("invayne", "invayne"),
        Pair("LitRPG", "LitRPG"),
        Pair("LGBT", "LGBT"),
        Pair(
            "Comedy Drama Romance Shounen Ai Supernatural",
            "Comedy Drama Romance Shounen Ai Supernatural"
        ),
        Pair("Shoujo Ai", "Shoujo Ai"),
        Pair("Supernatura", "Supernatura"),
        Pair("Canopy", "Canopy")
    )

    override suspend fun loadMainPage(
        page: Int,
        mainCategory: String?,
        orderBy: String?,
        tag: String?
    ): HeadMainPageResponse {
        val firstresponse = app.get(mainUrl)
        val firstdocument = Jsoup.parse(firstresponse.text)
        fun getId(tagvalue: String?): String? {
            return firstdocument.select("#hot-genre-select>option")
                .firstOrNull { it.text() == tagvalue }?.attr("value")
        }

        // I cant fix this because idk how it works
        val url = "$mainUrl/ajax-search?type=hot&genre=${getId(tag)}"
        val response = app.get(url)
        val document = Jsoup.parse(response.text)
        val headers = document.select("div.item")
        if (headers.size <= 0) return HeadMainPageResponse(url, ArrayList())
        val returnValue: ArrayList<SearchResponse> = ArrayList()
        for (h in headers) {
            val h3 = h?.selectFirst("a")
            val cUrl = mainUrl + h3?.attr("href")
            val name = h3?.attr("title") ?: throw ErrorLoadingException("Invalid name")

            val posterUrl =
                mainUrl + h.selectFirst("img")?.attr("src")

            returnValue.add(
                SearchResponse(
                    name,
                    cUrl,
                    fixUrlNull(posterUrl),
                    null,
                    null,
                    this.name
                )
            )
        }
        return HeadMainPageResponse(url, returnValue)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val response = app.get("$mainUrl/search?keyword=$query", headers = mapOf("User-Agent" to USER_AGENT))

        val document = Jsoup.parse(response.text)
        val headers = document.select("div.col-novel-main > div.list-novel > div.row")
        if (headers.size <= 0) return ArrayList()
        val returnValue: ArrayList<SearchResponse> = ArrayList()
        return headers.mapNotNull { h->
            val divs = h.select("> div > div")
            val poster = divs[0].selectFirst("> img")?.attr("src")
            val titleHeader = divs[1].selectFirst("> h3.novel-title > a")
            val href = titleHeader?.attr("href")
            val title = titleHeader?.text()
            val latestChapter = divs[2].selectFirst("> a > span")?.text()
            SearchResponse(
                title ?: return@mapNotNull null,
                fixUrl(href ?: return@mapNotNull null),
                fixUrlNull(poster),
                null,
                latestChapter,
                this.name
            )
        }
    }

    override suspend fun loadHtml(url: String): String? {
        val response = app.get(url)
        val document = Jsoup.parse(response.text)
        return document.selectFirst("div#chr-content")?.html().textClean?.replace("[Updated from F r e e w e b n o v e l. c o m]", "")
    }

    override suspend fun load(url: String): LoadResponse {
        val response = app.get(url)
        val document = Jsoup.parse(response.text)

        val header = document.selectFirst("div.col-info-desc")
        val bookInfo = header?.selectFirst("> div.info-holder > div.books")
        val title = bookInfo?.selectFirst("> div.desc > h3.title")?.text()
        val poster = bookInfo?.selectFirst("> div.book > img")?.attr("src")
        val desc = header?.selectFirst("> div.desc")
        val rateInfo = desc?.selectFirst("> div.rate-info")
        val votes = rateInfo?.select("> div.small > em > strong > span")?.last()?.text()?.toIntOrNull()
        val rate = rateInfo?.selectFirst("> div.rate")

        val novelId = rate?.selectFirst("> div#rating")?.attr("data-novel-id")
            ?: throw Exception("novelId not found")
        val rating = rate.selectFirst("> input")?.attr("value")?.toFloatOrNull()?.times(100)
            ?.toInt()

        val syno = document.selectFirst("div.desc-text")?.text()

        val infoMetas = desc.select("> ul.info-meta > li")

        fun getData(valueId: String): Element? {
            for (i in infoMetas) {
                if (i?.selectFirst("> h3")?.text() == valueId) {
                    return i
                }
            }
            return null
        }

        val author = getData("Author:")?.selectFirst("> a")?.text()
        val tags = getData("Genre:")?.select("> a")?.map { it.text() }
        val statusText = getData("Status:")?.selectFirst("> a")?.text()
        val status = when (statusText) {
            "Ongoing" -> STATUS_ONGOING
            "Completed" -> STATUS_COMPLETE
            else -> STATUS_NULL
        }

        val dataUrl = "$mainUrl/ajax/chapter-archive?novelId=$novelId"
        val dataResponse = app.get(dataUrl)
        val dataDocument = Jsoup.parse(dataResponse.text) ?: throw ErrorLoadingException("invalid dataDocument")
        val items =
            dataDocument.select("div.panel-body > div.row > div > ul.list-chapter > li > a").mapNotNull {
                ChapterData(it.selectFirst("> span")?.text() ?: return@mapNotNull null, fixUrl(it.attr("href")), null, null)
            }
        return LoadResponse(
            url,
            title ?: throw ErrorLoadingException("No name"),
            items,
            author,
            poster,
            rating,
            votes,
            null,
            syno,
            tags,
            status
        )
    }
}