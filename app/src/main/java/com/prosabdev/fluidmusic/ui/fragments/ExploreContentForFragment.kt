package com.prosabdev.fluidmusic.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.l4digital.fastscroll.FastScroller
import com.prosabdev.common.components.Constants
import com.prosabdev.common.models.songitem.SongItem
import com.prosabdev.common.persistence.PersistentStorage
import com.prosabdev.common.persistence.models.SortOrganizeItemSP
import com.prosabdev.common.utils.ImageLoaders
import com.prosabdev.common.utils.InsetModifiers
import com.prosabdev.fluidmusic.R
import com.prosabdev.fluidmusic.adapters.EmptyBottomAdapter
import com.prosabdev.fluidmusic.adapters.GridSpacingItemDecoration
import com.prosabdev.fluidmusic.adapters.HeadlinePlayShuffleAdapter
import com.prosabdev.fluidmusic.adapters.generic.GenericListGridItemAdapter
import com.prosabdev.fluidmusic.adapters.generic.SelectableItemListAdapter
import com.prosabdev.fluidmusic.databinding.FragmentExploreContentsForBinding
import com.prosabdev.fluidmusic.ui.bottomsheetdialogs.filter.OrganizeItemBottomSheetDialogFragment
import com.prosabdev.fluidmusic.ui.bottomsheetdialogs.filter.SortSongsBottomSheetDialogFragment
import com.prosabdev.fluidmusic.ui.custom.CenterSmoothScroller
import com.prosabdev.fluidmusic.ui.custom.CustomShapeableImageViewImageViewRatio11
import com.prosabdev.fluidmusic.ui.fragments.communication.FragmentsCommunication
import com.prosabdev.fluidmusic.ui.fragments.explore.AlbumArtistsFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.AlbumsFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.ArtistsFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.ComposersFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.FoldersFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.GenresFragment
import com.prosabdev.fluidmusic.ui.fragments.explore.YearsFragment
import com.prosabdev.fluidmusic.utils.InjectorUtils
import com.prosabdev.fluidmusic.viewmodels.fragments.ExploreContentsForFragmentViewModel
import com.prosabdev.fluidmusic.viewmodels.fragments.MainFragmentViewModel
import com.prosabdev.fluidmusic.viewmodels.fragments.PlayingNowFragmentViewModel
import com.prosabdev.fluidmusic.viewmodels.mediacontroller.MediaControllerViewModel
import com.prosabdev.fluidmusic.viewmodels.mediacontroller.MediaPlayerDataViewModel
import com.prosabdev.fluidmusic.viewmodels.models.SongItemViewModel
import kotlinx.coroutines.launch


class ExploreContentForFragment : Fragment() {

    //Data binding
    private lateinit var mDataBinding: FragmentExploreContentsForBinding

    //View models
    private val mExploreContentsForFragmentViewModel: ExploreContentsForFragmentViewModel by activityViewModels()
    private val mMainFragmentViewModel: MainFragmentViewModel by activityViewModels()
    private val mPlayingNowFragmentViewModel: PlayingNowFragmentViewModel by activityViewModels()

    private val mMediaPlayerDataViewModel: MediaPlayerDataViewModel by activityViewModels()
    private val mMediaControllerViewModel by activityViewModels<MediaControllerViewModel> {
        InjectorUtils.provideMediaControllerViewModel(mMediaPlayerDataViewModel.mediaEventsListener)
    }

    private val mSongItemViewModel: SongItemViewModel by activityViewModels()

    //Dialogs
    private var mOrganizeItemsDialog: OrganizeItemBottomSheetDialogFragment? = null
    private var mSortItemsDialog: SortSongsBottomSheetDialogFragment? = null

    //Adapters
    private var mConcatAdapter: ConcatAdapter? = null
    private var mEmptyBottomAdapter: EmptyBottomAdapter? = null
    private var mHeadlineTopPlayShuffleAdapter: HeadlinePlayShuffleAdapter? = null
    private var mGenericListGridItemAdapter: GenericListGridItemAdapter? = null
    private var mLayoutManager: GridLayoutManager? = null
    private var mItemDecoration: GridSpacingItemDecoration? = null

    private var mIsDraggingToScroll: Boolean = false

    private var mLoadSongFromSource: String? = null
    private var mWhereColumnIndex: String? = null
    private var mWhereColumnValue: String? = null
    private var mPreferencesKey: String? = null

    private var mImageUri: String? = null
    private var mHashedCoverArtSignature: Int = -1
    private var mTextTitle: String? = null
    private var mTextSubTitle: String? = null
    private var mTextDetails: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Apply transition animation
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Inflate binding layout and return binding object
        mDataBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_explore_contents_for,
            container,
            false
        )
        val view = mDataBinding.root

        //Load your UI content
        if (savedInstanceState == null){
            mExploreContentsForFragmentViewModel.loadPreferencesManually(mPreferencesKey)
            initViews()
            setupRecyclerViewAdapter()
            checkInteractions()
            observeLiveData()
        }

        return view
    }

    private fun observeLiveData() {
        mExploreContentsForFragmentViewModel.itemsList.observe(viewLifecycleOwner) {
            updateItemsListUI(it)
        }
        mExploreContentsForFragmentViewModel.sortBy.observe(viewLifecycleOwner) {
            updateSortListUI(it)
        }
        mExploreContentsForFragmentViewModel.isInverted.observe(viewLifecycleOwner) {
            updateIsInvertedListUI(it)
        }
        mExploreContentsForFragmentViewModel.organizeListGrid.observe(viewLifecycleOwner) {
            updateOrganizeListGridUI(it)
        }

        //Listen to player changes
        mMediaPlayerDataViewModel.currentMediaItem.observe(viewLifecycleOwner) {
            updateCurrentMediaItemUI(it)
        }
        mMediaPlayerDataViewModel.isPlaying.observe(viewLifecycleOwner) {
            updateIsPlayingUI(it)
        }

        //Listen for main fragment changes
        mMainFragmentViewModel.selectMode.observe(viewLifecycleOwner) {
            updateSelectionModeUI(it)
        }
        mMainFragmentViewModel.requestToggleSelectAll.observe(viewLifecycleOwner) {
            updateSelectAllItemsUI(it)
        }
        mMainFragmentViewModel.requestToggleSelectRange.observe(viewLifecycleOwner) {
            updateSelectRangeItemsUI(it)
        }
        mMainFragmentViewModel.scrollingState.observe(viewLifecycleOwner) {
            updateScrollingStateUI(it)
        }
        mMainFragmentViewModel.isFastScrolling.observe(viewLifecycleOwner) {
            updateIsFastScrollingUI(it)
        }
    }

    private fun updateIsFastScrollingUI(isFastScrolling: Boolean) {
        //
    }

    private fun updateScrollingStateUI(scrollingState: Int) {
        //
    }

    private fun updateSelectRangeItemsUI(counter: Int) {
        //
    }

    private fun updateSelectAllItemsUI(counter: Int) {
        //
    }

    private fun updateIsPlayingUI(isPlaying: Boolean) {
        //
    }

    private fun updateCurrentMediaItemUI(mediaItem: MediaItem?) {
        //
    }

    private fun updateOrganizeListGridUI(organizeValue: Int) {
        context?.let { ctx ->
            val tempSpanCount: Int =
                OrganizeItemBottomSheetDialogFragment.getSpanCount(ctx, organizeValue)
            mGenericListGridItemAdapter?.setOrganizeListGrid(
                mExploreContentsForFragmentViewModel.organizeListGrid.value
                    ?: ExploreContentsForFragmentViewModel.ORGANIZE_LIST_GRID_DEFAULT_VALUE
            )
            mLayoutManager?.spanCount = tempSpanCount
        }
    }

    private fun updateIsInvertedListUI(isInverted: Boolean) {
        if (isInverted) {
            mGenericListGridItemAdapter?.submitList(mExploreContentsForFragmentViewModel.itemsList.value?.reversed())
        } else {
            mGenericListGridItemAdapter?.submitList(mExploreContentsForFragmentViewModel.itemsList.value)
        }
        if (
            mMediaPlayerDataViewModel.queueListSource.value == TAG &&
            mMediaPlayerDataViewModel.queueListSourceColumnIndex.value == mWhereColumnIndex &&
            mMediaPlayerDataViewModel.queueListSourceColumnValue.value == mWhereColumnValue &&
            mMediaPlayerDataViewModel.queueListSortBy.value == mExploreContentsForFragmentViewModel.sortBy.value &&
            mMediaPlayerDataViewModel.queueListIsInverted.value == mExploreContentsForFragmentViewModel.isInverted.value
        ) {
            mGenericListGridItemAdapter?.setPlayingPosition(
                mMediaPlayerDataViewModel.currentMediaItemIndex.value ?: 0
            )
            mGenericListGridItemAdapter?.setIsPlaying(
                mMediaPlayerDataViewModel.isPlaying.value ?: false
            )
        } else {
            mGenericListGridItemAdapter?.setPlayingPosition(-1)
            mGenericListGridItemAdapter?.setIsPlaying(false)
        }
    }

    private fun updateSortListUI(it: String?) {
        //
    }

    private fun updateItemsListUI(items: List<Any>?) {
        //
    }

    private fun tryToUpdateFastScrollStateUI(isFastScrolling: Boolean = true) {
        if (isFastScrolling) {
            mDataBinding.appBarLayout.setExpanded(false)
        }
    }

    private suspend fun requestNewDataFromDatabase() {
        if (mExploreContentsForFragmentViewModel.sortBy.value?.isEmpty() == true) return

        mExploreContentsForFragmentViewModel.requestDataDirectlyWhereColumnEqualFromDatabase(
            mSongItemViewModel,
            mWhereColumnIndex ?: return,
            mWhereColumnValue,
        )
    }

    private fun addDataToGenericAdapter(dataList: List<Any>?) {
        if (mExploreContentsForFragmentViewModel.isInverted.value == true) {
            mGenericListGridItemAdapter?.submitList(dataList?.reversed())
        } else {
            mGenericListGridItemAdapter?.submitList(dataList)
        }
        if (mMainFragmentViewModel.currentSelectablePage.value == TAG) {
            mMainFragmentViewModel.totalCount.value = dataList?.size ?: 0
        }
        if (
            mMediaPlayerDataViewModel.queueListSource.value == TAG &&
            mMediaPlayerDataViewModel.queueListSourceColumnIndex.value == mWhereColumnIndex &&
            mMediaPlayerDataViewModel.queueListSourceColumnValue.value == mWhereColumnValue &&
            mMediaPlayerDataViewModel.queueListSortBy.value == mExploreContentsForFragmentViewModel.sortBy.value &&
            mMediaPlayerDataViewModel.queueListIsInverted.value == mExploreContentsForFragmentViewModel.isInverted.value
        ) {
            mGenericListGridItemAdapter?.setPlayingPosition(
                mMediaPlayerDataViewModel.currentMediaItemIndex.value ?: 0
            )
            mGenericListGridItemAdapter?.setIsPlaying(
                mMediaPlayerDataViewModel.isPlaying.value ?: false
            )
        } else {
            mGenericListGridItemAdapter?.setPlayingPosition(-1)
            mGenericListGridItemAdapter?.setIsPlaying(false)
        }
    }

    private fun updateOnScrollingStateUI(i: Int) {
        if (mMainFragmentViewModel.currentSelectablePage.value == TAG) {
            if (i == 2)
                mEmptyBottomAdapter?.onSetScrollState(2)
        }
    }

    private fun onReQuestToggleSelectRange(requestCount: Int?) {
        if (requestCount == null || requestCount <= 0) return
        if (mMainFragmentViewModel.currentSelectablePage.value == TAG) {
            mGenericListGridItemAdapter?.selectableSelectRange(mLayoutManager)
        }
    }

    private fun onReQuestToggleSelectAll(requestCount: Int?) {
        if (requestCount == null || requestCount <= 0) return
        if (mMainFragmentViewModel.currentSelectablePage.value == TAG) {
            val totalItemCount = mGenericListGridItemAdapter?.itemCount ?: 0
            val selectedItemCount =
                mGenericListGridItemAdapter?.selectableGetSelectedItemCount() ?: 0
            if (totalItemCount > selectedItemCount) {
                mGenericListGridItemAdapter?.selectableSelectAll(mLayoutManager)
            } else {
                mGenericListGridItemAdapter?.selectableClearSelection(mLayoutManager)
            }
        }
    }

    private fun updateSelectionModeUI(it: Boolean?) {
        if (mMainFragmentViewModel.currentSelectablePage.value == TAG) {
            mMainFragmentViewModel.totalCount.value = mGenericListGridItemAdapter?.itemCount ?: 0
            mLayoutManager?.let { it1 ->
                mGenericListGridItemAdapter?.selectableSetSelectionMode(it ?: false, it1)
            }
            mHeadlineTopPlayShuffleAdapter?.onSelectModeValue(it ?: false)
        }
    }

    private fun updatePlayingSongUI(songItem: SongItem?) {
        val songPosition: Int = songItem?.position ?: -1

        if (
            mMediaPlayerDataViewModel.queueListSource.value == TAG &&
            mMediaPlayerDataViewModel.queueListSourceColumnIndex.value == mWhereColumnIndex &&
            mMediaPlayerDataViewModel.queueListSourceColumnValue.value == mWhereColumnValue &&
            mMediaPlayerDataViewModel.queueListSortBy.value == mExploreContentsForFragmentViewModel.sortBy.value &&
            mMediaPlayerDataViewModel.queueListIsInverted.value == mExploreContentsForFragmentViewModel.isInverted.value
        ) {
            mGenericListGridItemAdapter?.setPlayingPosition(songPosition)
            mGenericListGridItemAdapter?.setIsPlaying(
                mMediaPlayerDataViewModel.isPlaying.value ?: false
            )
            tryToScrollOnCurrentItem(songPosition)
        } else {
            if ((mGenericListGridItemAdapter?.getPlayingPosition() ?: -1) >= 0)
                mGenericListGridItemAdapter?.setPlayingPosition(-1)
        }
    }

    private fun tryToScrollOnCurrentItem(position: Int) {
        if (position >= 0) {
            val tempCanScrollToPlayingSong: Boolean =
                mPlayingNowFragmentViewModel.canSmoothScrollViewpager.value ?: false
            if (!tempCanScrollToPlayingSong) return
            mPlayingNowFragmentViewModel.canSmoothScrollViewpager.value = false
            val tempFV: Int = (mLayoutManager?.findFirstVisibleItemPosition() ?: 0) - 1
            val tempLV: Int = mLayoutManager?.findLastVisibleItemPosition() ?: +1
            val tempVisibility: Boolean = position in tempFV..tempLV
            if (!tempVisibility) return
            context?.let { ctx ->
                val tempListSize: Int = mGenericListGridItemAdapter?.currentList?.size ?: 0
                val tempTargetPosition =
                    if (position + 2 <= tempListSize) position + 2 else tempListSize
                lifecycleScope.launch {
                    mLayoutManager?.let {
                        it.startSmoothScroll(
                            CenterSmoothScroller(ctx).apply {
                                targetPosition = tempTargetPosition
                            }
                        )
                    }
                }
            }
        }
    }

    private fun updatePlaybackStateUI(isPlaying: Boolean) {
        if (
            mMediaPlayerDataViewModel.queueListSource.value == TAG &&
            mMediaPlayerDataViewModel.queueListSourceColumnIndex.value == mWhereColumnIndex &&
            mMediaPlayerDataViewModel.queueListSourceColumnValue.value == mWhereColumnValue &&
            mMediaPlayerDataViewModel.queueListSortBy.value == mExploreContentsForFragmentViewModel.sortBy.value &&
            mMediaPlayerDataViewModel.queueListIsInverted.value == mExploreContentsForFragmentViewModel.isInverted.value
        ) {
            mGenericListGridItemAdapter?.setPlayingPosition(
                mMediaPlayerDataViewModel.currentMediaItemIndex.value ?: 0
            )
            mGenericListGridItemAdapter?.setIsPlaying(isPlaying)
            tryToScrollOnCurrentItem(
                mMediaPlayerDataViewModel.currentMediaItemIndex.value ?: 0
            )
        } else {
            if (mGenericListGridItemAdapter?.getIsPlaying() == true) {
                mGenericListGridItemAdapter?.setIsPlaying(false)
                mGenericListGridItemAdapter?.setPlayingPosition(-1)
            }
        }
    }

    private fun checkInteractions() {
        mDataBinding.topAppBar.setNavigationOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        mDataBinding.topAppBar.setOnMenuItemClickListener {
            if (it?.itemId == R.id.search) {
                goToSearchScreen()
            }
            true
        }
        mDataBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (mIsDraggingToScroll) {
                    if (dy < 0) {
                        Log.i(TAG, "Scrolling --> TOP")
                        mMainFragmentViewModel.scrollingState.value = SCROLLING_TOP
                    } else if (dy > 0) {
                        Log.i(TAG, "Scrolling --> BOTTOM")
                        mMainFragmentViewModel.scrollingState.value = SCROLLING_BOTTOM
                    }
                    if (!recyclerView.canScrollVertically(1) && dy > 0) {
                        Log.i(TAG, "Scrolled to BOTTOM")
                        mMainFragmentViewModel.scrollingState.value = SCROLLED_BOTTOM
                    } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                        Log.i(TAG, "Scrolled to TOP")
                        mMainFragmentViewModel.scrollingState.value = SCROLLED_TOP
                    }
                } else {
                    if (!recyclerView.canScrollVertically(1) && dy > 0) {
                        Log.i(TAG, "Scrolled to BOTTOM")
                        if (mMainFragmentViewModel.scrollingState.value != SCROLLED_BOTTOM)
                            mMainFragmentViewModel.scrollingState.value = SCROLLED_BOTTOM
                    } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                        Log.i(TAG, "Scrolled to TOP")
                        if (mMainFragmentViewModel.scrollingState.value != SCROLLED_TOP)
                            mMainFragmentViewModel.scrollingState.value = SCROLLED_TOP
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        mIsDraggingToScroll = false
                        println("The RecyclerView is SCROLL_STATE_IDLE")
                    }

                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        mIsDraggingToScroll = true
                        println("The RecyclerView is SCROLL_STATE_DRAGGING")
                    }

                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        println("The RecyclerView is SCROLL_STATE_SETTLING")
                    }
                }
            }
        })
    }

    private fun goToSearchScreen() {
        TODO("Not yet implemented")
    }

    private fun setupRecyclerViewAdapter() {
        if (mGenericListGridItemAdapter != null) return
        val ctx: Context = context ?: return

        //Setup headline adapter
        val listHeadlines: ArrayList<Int> = ArrayList()
        listHeadlines.add(0)
        mHeadlineTopPlayShuffleAdapter = HeadlinePlayShuffleAdapter(
            listHeadlines,
            object : HeadlinePlayShuffleAdapter.OnItemClickListener {
                override fun onPlayButtonClicked() {
                    playSong()
                }

                override fun onShuffleButtonClicked() {
                    playSongOnShuffle()
                }

                override fun onSortButtonClicked() {
                    showSortDialog()
                }

                override fun onOrganizeButtonClicked() {
                    showOrganizeDialog()
                }
            })

        //Setup generic item adapter
        mGenericListGridItemAdapter = GenericListGridItemAdapter(
            ctx,
            object : GenericListGridItemAdapter.OnItemRequestDataInfo {
                override fun onRequestDataInfo(
                    dataItem: Any,
                    position: Int
                ): com.prosabdev.common.models.generic.GenericItemListGrid? {
                    return SongItem.castDataItemToGeneric(ctx, dataItem)
                }

                override fun onRequestTextIndexForFastScroller(
                    dataItem: Any,
                    position: Int
                ): String {
                    return SongItem.getStringIndexForFastScroller(dataItem)
                }
            },
            object : GenericListGridItemAdapter.OnItemClickListener {
                override fun onItemClicked(
                    position: Int,
                    imageviewCoverArt: CustomShapeableImageViewImageViewRatio11,
                    textTitle: MaterialTextView,
                    textSubtitle: MaterialTextView,
                    textDetails: MaterialTextView
                ) {
                    if (mMainFragmentViewModel.selectMode.value == true) {
                        mGenericListGridItemAdapter?.selectableSelectFromPosition(
                            position,
                            mLayoutManager
                        )
                    } else {
                        playSongAtPosition(position)
                    }
                }

                override fun onItemLongPressed(position: Int) {
                    mGenericListGridItemAdapter?.selectableSelectFromPosition(
                        position,
                        mLayoutManager
                    )
                }
            },
            object : SelectableItemListAdapter.OnSelectSelectableItemListener {
                override fun onSelectModeChange(selectMode: Boolean) {
                    if (selectMode) {
                        mMainFragmentViewModel.currentSelectablePage.value = TAG
                    }
                    mMainFragmentViewModel.selectMode.value = selectMode
                }

                override fun onRequestGetStringIndex(position: Int): String {
                    return SongItem.getStringIndexForSelection(
                        mGenericListGridItemAdapter?.currentList?.get(position)
                    )
                }

                override fun onSelectedListChange(selectedList: HashMap<Int, String>) {
                    mMainFragmentViewModel.selectedItems.value = selectedList
                }
            },
            SongItem.diffCallback as DiffUtil.ItemCallback<Any>,
            mExploreContentsForFragmentViewModel.organizeListGrid.value
                ?: ExploreContentsForFragmentViewModel.ORGANIZE_LIST_GRID_DEFAULT_VALUE,
            mIsSelectable = true,
            mHavePlaybackState = true,
            mIsImageFullCircle = false
        )

        //Setup empty bottom space adapter
        val listEmptyBottomSpace: ArrayList<String> = ArrayList()
        listEmptyBottomSpace.add("")
        mEmptyBottomAdapter = EmptyBottomAdapter(listEmptyBottomSpace)

        //Setup concat adapter
        mConcatAdapter = ConcatAdapter()
        mHeadlineTopPlayShuffleAdapter?.let {
            mConcatAdapter?.addAdapter(it)
        }
        mGenericListGridItemAdapter?.let {
            mConcatAdapter?.addAdapter(it)
        }
        mEmptyBottomAdapter?.let {
            mConcatAdapter?.addAdapter(it)
        }

        //Add Layout manager
        val initialSpanCount: Int = OrganizeItemBottomSheetDialogFragment.getSpanCount(
            ctx,
            mExploreContentsForFragmentViewModel.organizeListGrid.value
        )
        mLayoutManager = GridLayoutManager(ctx, initialSpanCount, GridLayoutManager.VERTICAL, false)
        mLayoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val newSpanCount: Int = OrganizeItemBottomSheetDialogFragment.getSpanCount(
                    ctx,
                    mExploreContentsForFragmentViewModel.organizeListGrid.value
                )
                val updatedSpan: Int =
                    if (mLayoutManager?.spanCount == newSpanCount) newSpanCount else mLayoutManager?.spanCount
                        ?: 1
                return when (position) {
                    0 -> updatedSpan
                    ((mLayoutManager?.itemCount ?: 0) - 1) -> updatedSpan
                    else -> 1
                }
            }
        }
        lifecycleScope.launch {
            mDataBinding.recyclerView.adapter = mConcatAdapter
            mDataBinding.recyclerView.layoutManager = mLayoutManager
        }
        val newSpanCount: Int = OrganizeItemBottomSheetDialogFragment.getSpanCount(
            ctx,
            mExploreContentsForFragmentViewModel.organizeListGrid.value
        )
        val updatedSpan: Int =
            if (mLayoutManager?.spanCount == newSpanCount) newSpanCount else mLayoutManager?.spanCount
                ?: 1
        mItemDecoration = GridSpacingItemDecoration(updatedSpan)
        mItemDecoration?.let {
            lifecycleScope.launch {
                mDataBinding.recyclerView.addItemDecoration(it)
            }
        }

        mDataBinding.fastScroller.setSectionIndexer(mGenericListGridItemAdapter)
        mDataBinding.fastScroller.attachRecyclerView(mDataBinding.recyclerView)
        mDataBinding.fastScroller.setFastScrollListener(object :
            FastScroller.FastScrollListener {
            override fun onFastScrollStart(fastScroller: FastScroller) {
                mMainFragmentViewModel.isFastScrolling.value = true
            }

            override fun onFastScrollStop(fastScroller: FastScroller) {
                mMainFragmentViewModel.isFastScrolling.value = false
                if (!mDataBinding.recyclerView.canScrollVertically(-1)) {
                    //On scrolled to top
                    mMainFragmentViewModel.scrollingState.value = -2
                } else if (!mDataBinding.recyclerView.canScrollVertically(1)) {
                    //On scrolled to bottom
                    mMainFragmentViewModel.scrollingState.value = 2
                }
            }
        })
    }

    private fun showSortDialog() {
        mSortItemsDialog = SortSongsBottomSheetDialogFragment.newInstance(
            mExploreContentsForFragmentViewModel,
            TAG,
            null
        )
        mSortItemsDialog?.show(childFragmentManager, SortSongsBottomSheetDialogFragment.TAG)
    }

    private fun showOrganizeDialog() {
        mOrganizeItemsDialog = OrganizeItemBottomSheetDialogFragment.newInstance(
            mExploreContentsForFragmentViewModel,
            TAG,
            null
        )
        mOrganizeItemsDialog?.show(childFragmentManager, OrganizeItemBottomSheetDialogFragment.TAG)
    }

    private fun playSongAtPosition(position: Int) {
        if ((mGenericListGridItemAdapter?.currentList?.size ?: 0) <= 0) return

        updateRecyclerViewScrollingSate()
        TODO("Start playing song at position")
    }

    private fun playSong() {
        if ((mGenericListGridItemAdapter?.currentList?.size ?: 0) <= 0) return

        updateRecyclerViewScrollingSate()
        TODO("Start playing song at position 0 without shuffle")
    }

    private fun playSongOnShuffle() {
        if ((mGenericListGridItemAdapter?.currentList?.size ?: 0) <= 0) return

        updateRecyclerViewScrollingSate()
        TODO("Start playing song and enable shuffle")
    }

    private fun updateRecyclerViewScrollingSate() {
        if (mDataBinding.recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING) {
            mIsDraggingToScroll = false
        }
        mMainFragmentViewModel.scrollingState.value = -1
    }

    private fun initViews() {
        mMainFragmentViewModel.scrollingState.value = -2
        mDataBinding.recyclerView.setHasFixedSize(true)
        mDataBinding.constraintFastScrollerContainer.let {
            InsetModifiers.updateBottomViewInsets(
                it
            )
        }
        updateCoverArtUI()
        updateBackButtonTitleUI()
        updateTextsUI()
    }
    private fun updateTextsUI() {
        mDataBinding.let { mDataBinding ->
            mDataBinding.textTitle.text = mTextTitle
            mDataBinding.textSubtitle.text = mTextSubTitle
            mDataBinding.textDetails.text = mTextDetails
        }
    }
    private fun updateBackButtonTitleUI() {
        mDataBinding.collapsingToolBarLayout.title = when (mLoadSongFromSource) {

            AlbumsFragment.TAG -> context?.resources?.getString(R.string.album)
            AlbumArtistsFragment.TAG -> context?.resources?.getString(R.string.album_artist)
            ArtistsFragment.TAG -> context?.resources?.getString(R.string.artist)
            ComposersFragment.TAG -> context?.resources?.getString(R.string.composer)
            FoldersFragment.TAG -> context?.resources?.getString(R.string.folder)
            GenresFragment.TAG -> context?.resources?.getString(R.string.genre)
            YearsFragment.TAG -> context?.resources?.getString(R.string.year)

            else -> context?.resources?.getString(R.string.unknown_content)
        }
    }
    private fun updateCoverArtUI() {
        context?.let { ctx ->
            val imageRequestLargeImage =
                ImageLoaders.ImageRequestItem.newOriginalMediumCardInstance()
            imageRequestLargeImage.uri = Uri.parse(mImageUri ?: return)
            imageRequestLargeImage.hashedCovertArtSignature = mHashedCoverArtSignature
            imageRequestLargeImage.imageView = mDataBinding.imageViewCoverArt
            ImageLoaders.startExploreContentImageLoaderJob(
                ctx,
                imageRequestLargeImage
            )
        }
    }

    companion object {
        const val TAG = "ExploreContentFor"

        const val SCROLLING_TOP: Int = -1
        const val SCROLLING_BOTTOM: Int = 1
        const val SCROLLED_TOP: Int = -2
        const val SCROLLED_BOTTOM: Int = 2

        @JvmStatic
        fun newInstance(
            preferencesKey: String,
            loadSongFromSource: String?,
            whereColumnIndex: String?,
            whereColumnValue: String?,
            imageUri: String?,
            hashedCoverArtSignature: Int,
            textTitle: String?,
            textSubTitle: String?,
            textDetails: String?,
        ) =
            ExploreContentForFragment().apply {
                mLoadSongFromSource = loadSongFromSource
                mWhereColumnIndex = whereColumnIndex
                mWhereColumnValue = whereColumnValue
                mPreferencesKey = preferencesKey

                mImageUri = imageUri
                mHashedCoverArtSignature = hashedCoverArtSignature
                mTextTitle = textTitle
                mTextSubTitle = textSubTitle
                mTextDetails = textDetails

            }
    }
}