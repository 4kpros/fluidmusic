package com.prosabdev.fluidmusic.viewmodels.models.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.prosabdev.fluidmusic.models.view.ComposerItem
import com.prosabdev.fluidmusic.roomdatabase.repositories.explore.ComposerItemRepository

class ComposerItemViewModel(app: Application) : AndroidViewModel(app) {

    private var repository: ComposerItemRepository? = ComposerItemRepository(app)

    suspend fun getAtName(name : String) : ComposerItem? {
        return repository?.getAtName(name)
    }
    suspend fun getAll(order_by: String = "name") : LiveData<List<ComposerItem>>? {
        return repository?.getAll(order_by)
    }
}