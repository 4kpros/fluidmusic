package com.prosabdev.fluidmusic.viewmodels.models.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.prosabdev.fluidmusic.models.view.FolderItem
import com.prosabdev.fluidmusic.roomdatabase.repositories.explore.FolderItemRepository

class FolderItemViewModel(app: Application) : AndroidViewModel(app) {

    private var repository: FolderItemRepository? = FolderItemRepository(app)

    suspend fun getAtName(name : String) : FolderItem? {
        return repository?.getAtName(name)
    }
    suspend fun getAll(order_by: String = "name") : LiveData<List<FolderItem>>? {
        return repository?.getAll(order_by)
    }
}