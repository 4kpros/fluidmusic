package com.prosabdev.fluidmusic.ui.bottomsheetdialogs.filter

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.prosabdev.fluidmusic.R
import com.prosabdev.fluidmusic.databinding.BottomSheetSortContentExplorerBinding
import com.prosabdev.fluidmusic.ui.fragments.PlaylistsFragment
import com.prosabdev.fluidmusic.ui.fragments.StreamsFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.AlbumArtistsFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.AlbumsFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.AllSongsFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.ArtistsFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.ComposersFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.FoldersFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.GenresFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.YearsFragment
import com.prosabdev.fluidmusic.viewmodels.fragments.GenericListenDataViewModel

class SortContentExplorerBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var mDataBiding: BottomSheetSortContentExplorerBinding? = null

    private var mGenericListenDataViewModel: GenericListenDataViewModel? = null

    private var mFromSource: String? = null
    private var mFromSourceValue: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //Set content with data biding util
        mDataBiding = DataBindingUtil.inflate(inflater, R.layout._bottom_sheet_sort_content_explorer, container, false)
        val view = mDataBiding?.root

        //Load your UI content
        initViews()
        checkInteractions()

        return view
    }

    private fun checkInteractions() {
        mDataBiding?.let { viewBiding ->
            viewBiding.radioGroupSort.setOnCheckedChangeListener { _, checkedId ->
                onRadioGroupStateChanged(
                    checkedId
                )
            }
            viewBiding.checkboxInvertItems.setOnCheckedChangeListener { _, isChecked ->
                onInvertSortChanged(
                    isChecked
                )
            }
        }
    }


    private fun onInvertSortChanged(checked: Boolean) {
        mGenericListenDataViewModel?.isInverted?.value = checked
    }

    private fun onRadioGroupStateChanged(checkedId: Int) {
        mDataBiding?.let { viewBiding ->
            when (checkedId) {
                viewBiding.radioName.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "name"
                }
                viewBiding.radioArtist.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "artist"
                }
                viewBiding.radioAlbum.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "album"
                }
                viewBiding.radioAlbumArtist.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "albumArtist"
                }
                viewBiding.radioYear.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "year"
                }
                viewBiding.radioLastAddedDateToLibrary.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "lastAddedDateToLibrary"
                }
                viewBiding.radioLastUpdateDate.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "lastUpdateDate"
                }
                viewBiding.radioTotalDuration.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "totalDuration"
                }
                viewBiding.radioNumberTracks.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "numberTracks"
                }
                viewBiding.radioNumberArtists.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "numberArtists"
                }
                viewBiding.radioNumberAlbums.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "numberAlbums"
                }
                viewBiding.radioNumberAlbumArtists.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "numberAlbumArtists"
                }
                viewBiding.radioNumberComposers.id -> {
                    mGenericListenDataViewModel?.sortBy?.value = "numberComposers"
                }
                else -> {
                    mGenericListenDataViewModel?.sortBy?.value = "name"
                }
            }
        }
    }

    private fun initViews() {
        updateTitleUI()
        setupInitialStateUI()
        updateDefaultCheckedRadioButtonUI()
        updateDefaultCheckboxInvertButtonUI()
    }
    private fun updateTitleUI() {
        when (mFromSource) {
            AlbumsFragment.TAG -> {
                mDataBiding?.textSortDetails?.text = context?.resources?.getString(
                    R.string._sort_for,
                    context?.resources?.getString(R.string.albums) ?: ""
                ) ?: ""
            }
            AlbumArtistsFragment.TAG -> {
                mDataBiding?.textSortDetails?.text = context?.resources?.getString(
                    R.string._sort_for,
                    context?.resources?.getString(R.string.album_artists) ?: ""
                ) ?: ""
            }
            ArtistsFragment.TAG -> {
                mDataBiding?.textSortDetails?.text = context?.resources?.getString(
                    R.string._sort_for,
                    context?.resources?.getString(R.string.artists) ?: ""
                ) ?: ""
            }
            ComposersFragment.TAG -> {
                mDataBiding?.textSortDetails?.text = context?.resources?.getString(
                    R.string._sort_for,
                    context?.resources?.getString(R.string.composers) ?: ""
                ) ?: ""
            }
            FoldersFragment.TAG -> {
                mDataBiding?.textSortDetails?.text = context?.resources?.getString(
                    R.string._sort_for,
                    context?.resources?.getString(R.string.folders) ?: ""
                ) ?: ""
            }
            GenresFragment.TAG -> {
                mDataBiding?.textSortDetails?.text = context?.resources?.getString(
                    R.string._sort_for,
                    context?.resources?.getString(R.string.genres) ?: ""
                ) ?: ""
            }
            YearsFragment.TAG -> {
                mDataBiding?.textSortDetails?.text = context?.resources?.getString(
                    R.string._sort_for,
                    context?.resources?.getString(R.string.years) ?: ""
                ) ?: ""
            }
            PlaylistsFragment.TAG -> {
                mDataBiding?.textSortDetails?.text = context?.resources?.getString(
                    R.string._sort_for,
                    context?.resources?.getString(R.string.playlists) ?: ""
                ) ?: ""
            }
//            FavoritesFragment.TAG -> {
//                mDataBiding?.textSortDetails?.text = context?.resources?.getString(
//                    R.string._sort_for,
//                    context?.resources?.getString(R.string.favorites) ?: ""
//                ) ?: ""
//            }
            StreamsFragment.TAG -> {
                mDataBiding?.textSortDetails?.text = context?.resources?.getString(
                    R.string._sort_for,
                    context?.resources?.getString(R.string.streams) ?: ""
                ) ?: ""
            }
        }
    }

    private fun setupInitialStateUI() {
        showAllRadioButtonsVisibility()
        updateRadioButtonsForSortGenericItems()
    }
    private fun updateRadioButtonsForSortGenericItems() {
        //Update generic first radio button text title
        mDataBiding?.let { viewBiding ->
            when (mFromSource) {
                AllSongsFragment.TAG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        viewBiding.radioName.tooltipText = context?.getString(R.string.album_name)
                    }
                    viewBiding.radioName.text = context?.getString(R.string.album_name)
                    //Now hide non necessary radio buttons
                    viewBiding.radioArtist.visibility = GONE
                    viewBiding.radioAlbum.visibility = GONE
                    viewBiding.radioAlbumArtist.visibility = GONE
                }
                AlbumArtistsFragment.TAG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        viewBiding.radioName.tooltipText = context?.getString(R.string.album_artist)
                    }
                    viewBiding.radioName.text = context?.getString(R.string.album_artist)
                    //Now hide non necessary radio buttons
                    viewBiding.radioArtist.visibility = GONE
                    viewBiding.radioAlbum.visibility = GONE
                    viewBiding.radioAlbumArtist.visibility = GONE
                }
                ArtistsFragment.TAG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        viewBiding.radioName.tooltipText = context?.getString(R.string.artist_name)
                    }
                    viewBiding.radioName.text = context?.getString(R.string.artist_name)
                    //Now hide non necessary radio buttons
                    viewBiding.radioArtist.visibility = GONE
                }
                ComposersFragment.TAG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        viewBiding.radioName.tooltipText = context?.getString(R.string.composer_name)
                    }
                    viewBiding.radioName.text = context?.getString(R.string.composer_name)
                    //Now hide non necessary radio buttons
                    viewBiding.radioNumberComposers.visibility = GONE
                }
                FoldersFragment.TAG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        viewBiding.radioName.tooltipText = context?.getString(R.string.folder_name)
                    }
                    viewBiding.radioName.text = context?.getString(R.string.folder_name)
                }
                GenresFragment.TAG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        viewBiding.radioName.tooltipText = context?.getString(R.string.genre)
                    }
                    viewBiding.radioName.text = context?.getString(R.string.genre)
                }
                YearsFragment.TAG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        viewBiding.radioName.tooltipText = context?.getString(R.string.year)
                    }
                    viewBiding.radioName.text = context?.getString(R.string.year)
                    //Now hide non necessary radio buttons
                    viewBiding.radioYear.visibility = GONE
                }
            }
        }
    }
    private fun showAllRadioButtonsVisibility() {
        mDataBiding?.let { viewBiding ->
            viewBiding.radioName.visibility = VISIBLE
            viewBiding.radioArtist.visibility = VISIBLE
            viewBiding.radioAlbum.visibility = VISIBLE
            viewBiding.radioAlbumArtist.visibility = VISIBLE
            viewBiding.radioYear.visibility = VISIBLE
            viewBiding.radioLastAddedDateToLibrary.visibility = VISIBLE
            viewBiding.radioLastUpdateDate.visibility = VISIBLE
            viewBiding.radioTotalDuration.visibility = VISIBLE
            viewBiding.radioNumberTracks.visibility = VISIBLE
            viewBiding.radioNumberArtists.visibility = VISIBLE
            viewBiding.radioNumberAlbums.visibility = VISIBLE
            viewBiding.radioNumberAlbumArtists.visibility = VISIBLE
            viewBiding.radioNumberComposers.visibility = VISIBLE
        }
    }

    private fun updateDefaultCheckboxInvertButtonUI() {
        mDataBiding?.checkboxInvertItems?.isChecked = mGenericListenDataViewModel?.isInverted?.value ?: false
    }
    private fun updateDefaultCheckedRadioButtonUI() {
        if(mGenericListenDataViewModel == null) return
        mDataBiding?.let { viewBiding ->
            val tempFilter = mGenericListenDataViewModel?.sortBy?.value ?: return
            when (tempFilter) {
                "name" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioName.id)
                }
                "artist" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioArtist.id)
                }
                "album" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioAlbum.id)
                }
                "albumArtist" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioAlbumArtist.id)
                }
                "year" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioYear.id)
                }
                "lastAddedDateToLibrary" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioLastAddedDateToLibrary.id)
                }
                "lastUpdateDate" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioLastUpdateDate.id)
                }
                "totalDuration" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioTotalDuration.id)
                }
                "numberTracks" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioNumberTracks.id)
                }
                "numberArtists" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioNumberArtists.id)
                }
                "numberAlbums" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioNumberAlbums.id)
                }
                "numberAlbumArtists" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioNumberAlbumArtists.id)
                }
                "numberComposers" -> {
                    viewBiding.radioGroupSort.check(viewBiding.radioNumberComposers.id)
                }
            }
        }
    }

    fun updateBottomSheetData(genericListenDataViewModel: GenericListenDataViewModel?, fromSource: String?, fromSourceValue: String? = null){
        mGenericListenDataViewModel = genericListenDataViewModel
        mFromSource = fromSource
        mFromSourceValue = fromSourceValue
        initViews()
    }

    companion object {
        const val TAG = "SortContentExplorerBottomSheetDialogFragment"

        @JvmStatic
        fun newInstance(genericListenDataViewModel: GenericListenDataViewModel?, fromSource: String?, fromSourceValue: String? = null) =
            SortContentExplorerBottomSheetDialogFragment().apply {
                mGenericListenDataViewModel = genericListenDataViewModel
                mFromSource = fromSource
                mFromSourceValue = fromSourceValue
            }
        @JvmStatic
        fun newInstance() =
            SortContentExplorerBottomSheetDialogFragment().apply {
            }
    }
}