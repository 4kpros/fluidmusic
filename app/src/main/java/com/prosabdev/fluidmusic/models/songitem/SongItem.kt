package com.prosabdev.fluidmusic.models.songitem

import android.content.Context
import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import androidx.room.*
import com.prosabdev.fluidmusic.models.FolderUriTree
import com.prosabdev.fluidmusic.models.generic.GenericItemListGrid
import com.prosabdev.fluidmusic.utils.FormattersAndParsersUtils

@Entity(
    foreignKeys = [
        ForeignKey(entity = FolderUriTree::class, parentColumns = ["id"], childColumns = ["uriTreeId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["uri"], unique = true)]
)
class SongItem {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var uriTreeId: Long = 0
    var uri: String = ""
    var fileName: String = ""
    var title: String = ""
    var artist: String = ""
    var albumArtist: String = ""
    var composer: String = ""
    var album: String = ""
    var genre: String = ""
    var uriPath: String = ""
    var folder: String = ""
    var folderParent: String = ""
    var folderUri: String = ""
    var year: String = ""
    var duration: Long = 0
    var language: String = ""

    var typeMime: String = ""
    var sampleRate: Int = 0
    var bitrate: Double = 0.0

    var size: Long = 0

    var channelCount: Int = 0
    var fileExtension: String = ""
    var bitPerSample: String = ""

    var lastUpdateDate: Long = 0
    var lastAddedDateToLibrary: Long = 0

    var author: String = ""
    var diskNumber: String = ""
    var writer: String = ""
    var cdTrackNumber: String = ""
    var numberTracks: String = ""

    var comments: String = ""

    var rating: Int = 0
    var playCount: Int = 0
    var lastPlayed: Long = 0

    var hashedCovertArtSignature: Int = -1
    var isValid: Boolean = true

    @Ignore
    var position: Int = -1

    companion object {
        const val TAG = "SongItem"
        const val DEFAULT_INDEX = "title"

        fun getStringIndexForSelection(dataItem: Any?): String {
            if(dataItem != null && dataItem is SongItem) {
                return dataItem.uri
            }
            return ""
        }
        fun getStringIndexForFastScroller(dataItem: Any): String {
            if(dataItem is SongItem) {
                return dataItem.title.ifEmpty { dataItem.fileName }
            }
            return "#"
        }
        fun castDataItemToGeneric(ctx: Context, dataItem: Any): GenericItemListGrid? {
            var tempResult : GenericItemListGrid? = null
            if(dataItem is SongItem) {
                tempResult = GenericItemListGrid()
                tempResult.title = dataItem.title.ifEmpty { dataItem.fileName }
                tempResult.subtitle = dataItem.title.ifEmpty { ctx.getString(com.prosabdev.fluidmusic.R.string.unknown_artist) }
                tempResult.details = ctx.getString(
                    com.prosabdev.fluidmusic.R.string.item_song_card_text_details,
                    FormattersAndParsersUtils.formatSongDurationToString(dataItem.duration),
                    dataItem.fileExtension
                )
                tempResult.imageUri = Uri.parse(dataItem.uri)
                tempResult.imageHashedSignature = dataItem.hashedCovertArtSignature
            }
            return tempResult
        }

        val diffCallback = object : DiffUtil.ItemCallback<SongItem>() {
            override fun areItemsTheSame(
                oldItem: SongItem,
                newItem: SongItem
            ): Boolean =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: SongItem, newItem: SongItem) =
                oldItem.id == newItem.id &&
                oldItem.uriTreeId == newItem.uriTreeId &&
                oldItem.uri == newItem.uri
        }

        val diffCallbackViewPager = object : DiffUtil.ItemCallback<SongItem>() {
            override fun areItemsTheSame(
                oldItem: SongItem,
                newItem: SongItem
            ): Boolean =
                false
            override fun areContentsTheSame(oldItem: SongItem, newItem: SongItem) =
                oldItem == newItem
        }
    }
}