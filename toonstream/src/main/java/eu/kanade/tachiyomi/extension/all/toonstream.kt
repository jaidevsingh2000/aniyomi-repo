package eu.kanade.tachiyomi.extension.all.toonstream

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Toonstream : ParsedHttpSource() {

    override val name = "Toonstream"
    override val baseUrl = "https://toonstream.net" // Update if different
    override val lang = "all"
    override val client: OkHttpClient = network.client

    // -------------------------------
    // Manga Listing
    // -------------------------------
    override fun popularMangaSelector() = "div.manga-item"
    override fun popularMangaRequest(page: Int) = GET("$baseUrl/popular?page=$page")
    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        manga.title = element.select("h3.title").text()
        manga.thumbnail_url = element.select("img").attr("abs:src")
        manga.url = element.select("a").attr("href")
        return manga
    }
    override fun popularMangaNextPageSelector() = "a.next"

    // -------------------------------
    // Latest Manga / Updates
    // -------------------------------
    override fun latestUpdatesSelector() = "div.manga-item"
    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/latest?page=$page")
    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = "a.next"

    // -------------------------------
    // Manga Details
    // -------------------------------
    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        manga.title = document.select("h1.title").text()
        manga.description = document.select("div.summary").text()
        manga.thumbnail_url = document.select("div.thumb img").attr("abs:src")
        return manga
    }

    // -------------------------------
    // Chapters
    // -------------------------------
    override fun chapterListSelector() = "ul.chapter-list li"
    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()
        chapter.name = element.select("a").text()
        chapter.url = element.select("a").attr("href")
        chapter.date_upload = 0 // Can parse date if site provides
        return chapter
    }

    // -------------------------------
    // Pages
    // -------------------------------
    override fun pageListParse(document: Document) =
        document.select("div.page img").map { it.attr("abs:src") }.mapIndexed { i, url ->
            SChapter.create().apply { name = "Page ${i + 1}"; this.url = url }
        }

    // -------------------------------
    // Search
    // -------------------------------
    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaFromElement(element: Element): SManga = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()
    override fun searchMangaRequest(page: Int, query: String, filters: List<Nothing>) =
        GET("$baseUrl/search?q=$query&page=$page")
}
