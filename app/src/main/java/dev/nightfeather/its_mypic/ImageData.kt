package dev.nightfeather.its_mypic

import androidx.compose.runtime.Stable
import com.google.gson.stream.JsonReader

const val URL_SCHEME = "https"
const val URL_BASE = "mygodata.0m0.uk"
const val URL_PATH = "images"
const val URL_IMAGE_FORMAT = "jpg"

@Stable
class ImageData(reader: JsonReader) {
    val text: String
    private val formattedText: String
    val episode: String
    val frameStart: Int
    private val frameEnd: Int
    private val segmentId: Int
    val sourceUrl: String

    init {
        var text = ""
        var episode = ""
        var frameStart = 0
        var frameEnd = 0
        var segmentId = 0

        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            when (name) {
                "text" -> {
                    text = reader.nextString()
                }
                "episode" -> {
                    episode = reader.nextString()
                }
                "frame_start" -> {
                    frameStart = reader.nextInt()
                }
                "frame_end" -> {
                    frameEnd = reader.nextInt()
                }
                "segment_id" -> {
                    segmentId = reader.nextInt()
                }
            }
        }
        reader.endObject()

        this.text = text
        this.formattedText = Utils.StringSearch.formatText(text)
        this.episode = episode
        this.frameStart = frameStart
        this.frameEnd = frameEnd
        this.segmentId = segmentId
        this.sourceUrl = "$URL_SCHEME://$URL_BASE/$URL_PATH/${episode}_${frameStart}.$URL_IMAGE_FORMAT"
    }

    fun calcDistanceWithQuery(queryString: String): Pair<Int, Int> {
        return Utils.StringSearch.calcDistance(
            text = formattedText,
            query = queryString
        )
    }
}
