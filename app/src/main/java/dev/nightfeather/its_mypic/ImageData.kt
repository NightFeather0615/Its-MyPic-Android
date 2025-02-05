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
    val formattedText: String
    val episode: String
    val frameStart: Int
    val frameEnd: Int
    val segmentId: Int
    val sourceUrl: String

    init {
        var text: String = ""
        var episode: String = ""
        var frameStart: Int = 0
        var frameEnd: Int = 0
        var segmentId: Int = 0

        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName();
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

    fun isMatchWithQuery(queryString: String): Boolean {
        return Utils.StringSearch.containsApproximateSubstring(
            text = formattedText,
            query = queryString
        )
    }
}
