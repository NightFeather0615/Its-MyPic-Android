package dev.nightfeather.its_mypic

import androidx.compose.runtime.Stable
import com.google.gson.stream.JsonReader

const val URL_SCHEME = "https"
const val URL_BASE = "mypic.0m0.uk"
const val URL_PATH = "images"

@Stable
class ImageData(reader: JsonReader) {
    val text: String
    private val formattedText: String
    val season: Int
    val episode: Int
    private val frameStart: Int
    val framePrefer: Int
    private val frameEnd: Int
    private val segmentId: Int
    private val character: Int
    val sourceUrl: String

    init {
        var text = ""
        var season = 0
        var episode = 0
        var frameStart = 0
        var framePrefer = 0
        var frameEnd = 0
        var segmentId = 0
        var character = 0

        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            when (name) {
                "text" -> {
                    text = reader.nextString()
                }
                "season" -> {
                    season = reader.nextInt()
                }
                "episode" -> {
                    episode = reader.nextInt()
                }
                "frame_start" -> {
                    frameStart = reader.nextInt()
                }
                "frame_prefer" -> {
                    framePrefer = reader.nextInt()
                }
                "frame_end" -> {
                    frameEnd = reader.nextInt()
                }
                "segment_id" -> {
                    segmentId = reader.nextInt()
                }
                "character" -> {
                    character = reader.nextInt()
                }
            }
        }
        reader.endObject()

        this.text = text
        this.formattedText = Utils.StringSearch.formatText(text)
        this.season = season
        this.episode = episode
        this.frameStart = frameStart
        this.framePrefer = framePrefer
        this.frameEnd = frameEnd
        this.segmentId = segmentId
        this.character = character
        this.sourceUrl = "$URL_SCHEME://$URL_BASE/$URL_PATH/${season}/${episode}/${framePrefer}"
    }

    fun calcDistanceWithQuery(queryString: String): Pair<Int, Int> {
        return Utils.StringSearch.calcDistance(
            text = formattedText,
            query = queryString
        )
    }
}
