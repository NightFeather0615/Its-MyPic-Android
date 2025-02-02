package dev.nightfeather.its_mypic

import com.google.gson.annotations.SerializedName

const val URL_BASE = "mygodata.0m0.uk"
const val URL_PATH = "images"
const val URL_IMAGE_FORMAT = "jpg"

class ImageData(
    @SerializedName("text")
    val text: String,
    @SerializedName("episode")
    val episode: String,
    @SerializedName("frame_start")
    val frameStart: Int,
    @SerializedName("frame_end")
    val frameEnd: Int,
    @SerializedName("segment_id")
    val segmentId: Int
) {
    fun toUrl(): String {
        return "https://$URL_BASE/$URL_PATH/${episode}_${frameStart}.$URL_IMAGE_FORMAT"
    }
}
