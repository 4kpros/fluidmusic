package com.prosabdev.fluidmusic.ui.bottomsheetdialogs.filter

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.prosabdev.fluidmusic.R
import com.prosabdev.fluidmusic.databinding.BottomSheetOrganizeItemsBinding
import com.prosabdev.fluidmusic.ui.fragments.ExploreContentsForFragment
import com.prosabdev.fluidmusic.ui.fragments.PlaylistsFragment
import com.prosabdev.fluidmusic.ui.fragments.StreamsFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.*
import com.prosabdev.fluidmusic.utils.ConstantValues
import com.prosabdev.fluidmusic.viewmodels.fragments.GenericListenDataViewModel

class OrganizeItemBottomSheetDialogFragment : BottomSheetDialogFragment(), OnShowListener {

    private var mDataBidingView: BottomSheetOrganizeItemsBinding? = null

    private var mGenericListenDataViewModel: GenericListenDataViewModel? = null

    private var mFromSource: String? = null
    private var mFromSourceValue: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mDataBidingView = DataBindingUtil.inflate(inflater, R.layout._bottom_sheet_organize_items, container, false)
        val view = mDataBidingView?.root

        initViews()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkInteractions()
    }

    private fun checkInteractions() {
        mDataBidingView?.radioGroupOrganize?.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_list_extra_small -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_LIST_EXTRA_SMALL)
                }
                R.id.radio_list_small -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_LIST_SMALL)
                }
                R.id.radio_list_medium -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_LIST_MEDIUM)
                }
                R.id.radio_list_large -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_LIST_LARGE)
                }
                R.id.radio_list_small_no_image -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_LIST_SMALL_NO_IMAGE)
                }
                R.id.radio_list_medium_no_image -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_LIST_MEDIUM_NO_IMAGE)
                }
                R.id.radio_list_large_no_image -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_LIST_LARGE_NO_IMAGE)
                }

                R.id.radio_grid_extra_small -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_GRID_EXTRA_SMALL)
                }
                R.id.radio_grid_small -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_GRID_SMALL)
                }
                R.id.radio_grid_medium -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_GRID_MEDIUM)
                }
                R.id.radio_grid_large -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_GRID_LARGE)
                }
                R.id.radio_grid_extra_large -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_GRID_EXTRA_LARGE)
                }
                R.id.radio_grid_small_no_image -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_GRID_SMALL_NO_IMAGE)
                }
                R.id.radio_grid_medium_no_image -> {
                    mGenericListenDataViewModel?.setOrganizeListGrid(ConstantValues.ORGANIZE_GRID_MEDIUM_NO_IMAGE)
                }
            }
        }
    }

    private fun initViews() {
        updateTitleUI()
        updateCheckedOrganizeButtonUI()
    }

    private fun updateTitleUI() {
        when (mFromSource) {
            AllSongsFragment.TAG -> {
                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.all_songs) ?: ""
            }
            AlbumsFragment.TAG -> {
                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.albums) ?: ""
            }
            AlbumArtistsFragment.TAG -> {
                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.album_artists) ?: ""
            }
            ArtistsFragment.TAG -> {
                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.artists) ?: ""
            }
            ComposersFragment.TAG -> {
                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.composers) ?: ""
            }
            FoldersFragment.TAG -> {
                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.folders) ?: ""
            }
            GenresFragment.TAG -> {
                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.genres) ?: ""
            }
            YearsFragment.TAG -> {
                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.years) ?: ""
            }
            PlaylistsFragment.TAG -> {
                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.playlists) ?: ""
            }
//            FavoritesFragment.TAG -> {
//                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.favorites) ?: ""
//            }
            StreamsFragment.TAG -> {
                mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.streams) ?: ""
            }
            ExploreContentsForFragment.TAG -> {
                when (mFromSourceValue) {
                    AlbumsFragment.TAG -> {
                        mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.songs_for_album) ?: ""
                    }
                    AlbumArtistsFragment.TAG -> {
                        mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.songs_for_album_artist) ?: ""
                    }
                    ArtistsFragment.TAG -> {
                        mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.songs_for_artist) ?: ""
                    }
                    ComposersFragment.TAG -> {
                        mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.songs_for_composer) ?: ""
                    }
                    FoldersFragment.TAG -> {
                        mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.songs_for_folder) ?: ""
                    }
                    GenresFragment.TAG -> {
                        mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.songs_for_genre) ?: ""
                    }
                    YearsFragment.TAG -> {
                        mDataBidingView?.textOrganizeDetails?.text = context?.resources?.getString(R.string.songs_for_year) ?: ""
                    }
                }
            }
        }
    }

    private fun updateCheckedOrganizeButtonUI() {
        if(mGenericListenDataViewModel == null) return
        val tempOrganize : Int = mGenericListenDataViewModel?.getOrganizeListGrid()?.value ?: return
        mDataBidingView?.let { bottomSheetOrganizeItemsBinding ->
            when (tempOrganize) {
                ConstantValues.ORGANIZE_LIST_EXTRA_SMALL -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListExtraSmall.id)
                }
                ConstantValues.ORGANIZE_LIST_SMALL -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListSmall.id)
                }
                ConstantValues.ORGANIZE_LIST_MEDIUM -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListMedium.id)
                }
                ConstantValues.ORGANIZE_LIST_LARGE -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListLarge.id)
                }
                ConstantValues.ORGANIZE_LIST_SMALL_NO_IMAGE -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListSmallNoImage.id)
                }
                ConstantValues.ORGANIZE_LIST_MEDIUM_NO_IMAGE -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListMediumNoImage.id)
                }
                ConstantValues.ORGANIZE_LIST_LARGE_NO_IMAGE -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListLargeNoImage.id)
                }

                ConstantValues.ORGANIZE_GRID_EXTRA_SMALL -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridExtraSmall.id)
                }
                ConstantValues.ORGANIZE_GRID_SMALL -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridSmall.id)
                }
                ConstantValues.ORGANIZE_GRID_MEDIUM -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridMedium.id)
                }
                ConstantValues.ORGANIZE_GRID_LARGE -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridLarge.id)
                }
                ConstantValues.ORGANIZE_GRID_EXTRA_LARGE -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridExtraLarge.id)
                }
                ConstantValues.ORGANIZE_GRID_SMALL_NO_IMAGE -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridSmallNoImage.id)
                }
                ConstantValues.ORGANIZE_GRID_MEDIUM_NO_IMAGE -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridMediumNoImage.id)
                }
                else -> {
                    mDataBidingView?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListSmall.id)
                }
            }
        }
    }

    fun updateBottomSheetData(
        genericListenDataViewModel: GenericListenDataViewModel?,
        fromSource: String?,
        fromSourceValue: String?
    ){
        mGenericListenDataViewModel = genericListenDataViewModel
        mFromSource = fromSource
        mFromSourceValue = fromSourceValue
        initViews()
    }

    companion object {
        const val TAG = "OrganizeItemBottomSheetDialogFragment"

        fun getSpanCount(ctx: Context, organizeValue: Int?): Int {
            when (organizeValue ?: ConstantValues.ORGANIZE_LIST_MEDIUM) {
                ConstantValues.ORGANIZE_GRID_SMALL_NO_IMAGE -> {
                    return ctx.resources.getInteger(R.integer.organize_grid_no_image_span_count)
                }
                ConstantValues.ORGANIZE_GRID_MEDIUM_NO_IMAGE -> {
                    return ctx.resources.getInteger(R.integer.organize_grid_no_image_span_count)
                }
                ConstantValues.ORGANIZE_GRID_EXTRA_SMALL -> {
                    return ctx.resources.getInteger(R.integer.organize_grid_extra_small_span_count)
                }
                ConstantValues.ORGANIZE_GRID_SMALL -> {
                    return ctx.resources.getInteger(R.integer.organize_grid_small_span_count)
                }
                ConstantValues.ORGANIZE_GRID_MEDIUM -> {
                    return ctx.resources.getInteger(R.integer.organize_grid_medium_span_count)
                }
                ConstantValues.ORGANIZE_GRID_LARGE -> {
                    return ctx.resources.getInteger(R.integer.organize_grid_large_span_count)
                }
                ConstantValues.ORGANIZE_GRID_EXTRA_LARGE -> {
                    return ctx.resources.getInteger(R.integer.organize_grid_extra_large_span_count)
                }
                else -> {
                    return 1
                }
            }
        }
        @JvmStatic
        fun newInstance() =
            OrganizeItemBottomSheetDialogFragment().apply {
            }
    }

    override fun onShow(dialog: DialogInterface?) {
        Log.i(ConstantValues.TAG, "onshow dddddddddddddddddddddddd")
    }
}