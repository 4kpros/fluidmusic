package com.prosabdev.fluidmusic.viewmodels.workers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*
import com.prosabdev.fluidmusic.workers.WorkerConstantValues
import com.prosabdev.fluidmusic.workers.playlist.AddSongsToPlaylistWorker
import com.prosabdev.fluidmusic.workers.playlist.RemoveSongsFromPlaylistWorker

class PlaylistActionsWorkerViewModel(app: Application) : AndroidViewModel(app) {
    private val outputWorkInfoAddSongsToPlaylist : LiveData<List<WorkInfo>>
    private val outputWorkInfoRemoveSongFromPlaylist : LiveData<List<WorkInfo>>

    private val workManager : WorkManager = WorkManager.getInstance(app.applicationContext)
    init {
        outputWorkInfoAddSongsToPlaylist = workManager.getWorkInfosByTagLiveData(AddSongsToPlaylistWorker.TAG)
        outputWorkInfoRemoveSongFromPlaylist = workManager.getWorkInfosByTagLiveData(RemoveSongsFromPlaylistWorker.TAG)
    }
    fun addSongsToPlaylist(
        playlistId: Long,
        modelType: String,
        itemList: Array<String>,
        orderBy: String,
        whereClause: String,
        indexColumn: String,
    ){
        val workRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<AddSongsToPlaylistWorker>()
            .setInputData(
                workDataOf(
                    AddSongsToPlaylistWorker.PLAYLIST_ID to playlistId,
                    WorkerConstantValues.ITEM_LIST_MODEL_TYPE to modelType,
                    WorkerConstantValues.ITEM_LIST to itemList,
                    WorkerConstantValues.ITEM_LIST_ORDER_BY to orderBy,
                    WorkerConstantValues.ITEM_LIST_WHERE to whereClause,
                    WorkerConstantValues.INDEX_COLUM to indexColumn
                )
            )
            .addTag(AddSongsToPlaylistWorker.TAG)
            .build()

        workManager.beginUniqueWork(
            AddSongsToPlaylistWorker.TAG,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        ).enqueue()
    }
    fun removeSongsFromPlaylist(
        songUriArray: Array<String>
    ){
        val workRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<RemoveSongsFromPlaylistWorker>()
            .setInputData(
                workDataOf(
                    RemoveSongsFromPlaylistWorker.SONG_URI_ARRAY to songUriArray
                )
            )
            .addTag(RemoveSongsFromPlaylistWorker.TAG)
            .build()

        workManager.beginUniqueWork(
            RemoveSongsFromPlaylistWorker.TAG,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        ).enqueue()
    }

    fun getOutputWorkInfoAddSongsToPlaylist(): LiveData<List<WorkInfo>> {
        return outputWorkInfoAddSongsToPlaylist
    }
    fun getOutputWorkInfoRemoveSongFromPlaylist(): LiveData<List<WorkInfo>> {
        return outputWorkInfoRemoveSongFromPlaylist
    }
}