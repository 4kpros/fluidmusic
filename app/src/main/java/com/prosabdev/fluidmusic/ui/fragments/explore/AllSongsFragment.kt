package com.prosabdev.fluidmusic.ui.fragments.explore

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.l4digital.fastscroll.FastScroller
import com.l4digital.fastscroll.FastScroller.FastScrollListener
import com.prosabdev.common.components.Constants
import com.prosabdev.common.models.generic.GenericItemListGrid
import com.prosabdev.common.models.songitem.SongItem
import com.prosabdev.common.persistence.PersistentStorage
import com.prosabdev.common.persistence.models.SortOrganizeItemSP
import com.prosabdev.common.utils.InsetModifiers
import com.prosabdev.fluidmusic.R
import com.prosabdev.fluidmusic.adapters.EmptyBottomAdapter
import com.prosabdev.fluidmusic.adapters.GridSpacingItemDecoration
import com.prosabdev.fluidmusic.adapters.HeadlinePlayShuffleAdapter
import com.prosabdev.fluidmusic.adapters.generic.GenericListGridItemAdapter
import com.prosabdev.fluidmusic.adapters.generic.SelectableItemListAdapter
import com.prosabdev.fluidmusic.databinding.FragmentAllSongsBinding
import com.prosabdev.fluidmusic.ui.bottomsheetdialogs.filter.OrganizeItemBottomSheetDialogFragment
import com.prosabdev.fluidmusic.ui.bottomsheetdialogs.filter.SortSongsBottomSheetDialogFragment
import com.prosabdev.fluidmusic.ui.custom.CenterSmoothScroller
import com.prosabdev.fluidmusic.ui.custom.CustomShapeableImageViewImageViewRatio11
import com.prosabdev.fluidmusic.ui.fragments.ExploreContentForFragment
import com.prosabdev.fluidmusic.ui.fragments.communication.FragmentsCommunication
import com.prosabdev.fluidmusic.utils.InjectorUtils
import com.prosabdev.fluidmusic.viewmodels.fragments.MainFragmentViewModel
import com.prosabdev.fluidmusic.viewmodels.fragments.PlayingNowFragmentViewModel
import com.prosabdev.fluidmusic.viewmodels.fragments.explore.AllSongsFragmentViewModel
import com.prosabdev.fluidmusic.viewmodels.mediacontroller.MediaControllerViewModel
import com.prosabdev.fluidmusic.viewmodels.mediacontroller.MediaPlayerDataViewModel
import com.prosabdev.fluidmusic.viewmodels.models.SongItemViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class AllSongsFragment : Fragment() {

    //Data binding
    private lateinit var mDataBinding: FragmentAllSongsBinding

    //View models
    private val mAllSongsFragmentViewModel: AllSongsFragmentViewModel by activityViewModels()
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

    private val isMatchingQueueMusicContent: Boolean
        get() {
            return mMediaPlayerDataViewModel.queueListSource.value == TAG &&
                    mMediaPlayerDataViewModel.queueListSourceColumnIndex.value == null &&
                    mMediaPlayerDataViewModel.queueListSourceColumnValue.value == null &&
                    mMediaPlayerDataViewModel.queueListIsInverted.value == mAllSongsFragmentViewModel.isInverted.value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Inflate binding layout and return binding object
        mDataBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_all_songs, container, false)
        val view = mDataBinding.root

        //Load your UI content
        if (savedInstanceState == null) {
            initViews()
            setupRecyclerViewAdapter()
            checkInteractions()
            observeLiveData()
        }

        return view
    }

    private fun observeLiveData() {
        mAllSongsFragmentViewModel.itemsList.observe(viewLifecycleOwner) {
            addDataToGenericAdapter(it)
        }
        mAllSongsFragmentViewModel.sortBy.observe(viewLifecycleOwner) {
            requestNewDataFromDatabase()
        }
        mAllSongsFragmentViewModel.isInverted.observe(viewLifecycleOwner) {
            invertSongListAndUpdateAdapter(it)
        }
        mAllSongsFragmentViewModel.organizeListGrid.observe(viewLifecycleOwner) {
            updateOrganizeListGrid(it)
        }

        //Listen to player changes
        mMediaPlayerDataViewModel.currentMediaItemIndex.observe(viewLifecycleOwner) {
            updateUICurrentMediaItemIndex(it)
        }
        mMediaPlayerDataViewModel.isPlaying.observe(viewLifecycleOwner) {
            updateUIIsPlaying(it)
        }

        //Listen for main fragment changes
        mMainFragmentViewModel.selectMode.observe(viewLifecycleOwner) {
            onSelectionModeChanged(it)
        }
        mMainFragmentViewModel.requestToggleSelectAll.observe(viewLifecycleOwner) {
            onReQuestToggleSelectAll(it)
        }
        mMainFragmentViewModel.requestToggleSelectRange.observe(viewLifecycleOwner) {
            onReQuestToggleSelectRange(it)
        }
        mMainFragmentViewModel.scrollingState.observe(viewLifecycleOwner) {
            updateOnScrollingStateUI(it)
        }
    }

    private fun updateOrganizeListGrid(organizeValue: Int?) {
        context?.let { ctx ->
            val tempSpanCount: Int =
                OrganizeItemBottomSheetDialogFragment.getSpanCount(ctx, organizeValue)
            mGenericListGridItemAdapter?.setOrganizeListGrid(
                mAllSongsFragmentViewModel.organizeListGrid.value ?: AllSongsFragmentViewModel.ORGANIZE_LIST_GRID_DEFAULT_VALUE
            )
            mLayoutManager?.spanCount = tempSpanCount
        }
    }

    private fun invertSongListAndUpdateAdapter(isInverted: Boolean?) {
        val tempNewIsInverted: Boolean = isInverted ?: false
        if (tempNewIsInverted) {
            mGenericListGridItemAdapter?.submitList(mAllSongsFragmentViewModel.itemsList.value?.reversed())
        } else {
            mGenericListGridItemAdapter?.submitList(mAllSongsFragmentViewModel.itemsList.value)
        }
        if (isMatchingQueueMusicContent) {
            mGenericListGridItemAdapter?.setPlayingPosition(
                mMediaPlayerDataViewModel.currentMediaItemIndex.value ?: -1
            )
            mGenericListGridItemAdapter?.setIsPlaying(
                mMediaPlayerDataViewModel.isPlaying.value ?: false
            )
        } else {
            mGenericListGridItemAdapter?.setPlayingPosition(-1)
            mGenericListGridItemAdapter?.setIsPlaying(false)
        }
    }

    private fun requestNewDataFromDatabase() {
        if (mAllSongsFragmentViewModel.sortBy.value?.isEmpty() == true) return
        lifecycleScope.launch {
            mAllSongsFragmentViewModel.requestDataDirectlyFromDatabase(
                mSongItemViewModel
            )
        }
    }

    private fun addDataToGenericAdapter(itemsList: List<Any>?) {
        if (mAllSongsFragmentViewModel.isInverted.value == true) {
            mGenericListGridItemAdapter?.submitList(itemsList?.reversed())
        } else {
            mGenericListGridItemAdapter?.submitList(itemsList)
        }
        if (mMainFragmentViewModel.currentSelectablePage.value == TAG) {
            mMainFragmentViewModel.totalCount.value = itemsList?.size ?: 0
        }
        if (isMatchingQueueMusicContent) {
            mGenericListGridItemAdapter?.setPlayingPosition(
                mMediaPlayerDataViewModel.currentMediaItemIndex.value ?: -1
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

    private fun onSelectionModeChanged(it: Boolean?) {
        if (mMainFragmentViewModel.currentSelectablePage.value == TAG) {
            mMainFragmentViewModel.totalCount.value = mGenericListGridItemAdapter?.itemCount ?: 0
            mLayoutManager?.let { it1 ->
                mGenericListGridItemAdapter?.selectableSetSelectionMode(it ?: false, it1)
            }
            mHeadlineTopPlayShuffleAdapter?.onSelectModeValue(it ?: false)
        }
    }

    private fun updateUIIsPlaying(isPlaying: Boolean) {
        if (isMatchingQueueMusicContent) {
            mGenericListGridItemAdapter?.setPlayingPosition(
                mMediaPlayerDataViewModel.currentMediaItemIndex.value ?: -1
            )
            mGenericListGridItemAdapter?.setIsPlaying(isPlaying)
            tryToScrollToCurrentItem(
                mMediaPlayerDataViewModel.currentMediaItemIndex.value ?: -1
            )
        } else {
            if (mGenericListGridItemAdapter?.getIsPlaying() == true) {
                mGenericListGridItemAdapter?.setIsPlaying(false)
                mGenericListGridItemAdapter?.setPlayingPosition(-1)
            }
        }
    }

    private fun updateUICurrentMediaItemIndex(index: Int) {
        if (isMatchingQueueMusicContent) {
            mGenericListGridItemAdapter?.setPlayingPosition(index)
            mGenericListGridItemAdapter?.setIsPlaying(
                mMediaPlayerDataViewModel.isPlaying.value ?: false
            )
            tryToScrollToCurrentItem(index)
        } else {
            if ((mGenericListGridItemAdapter?.getPlayingPosition() ?: -1) >= 0)
                mGenericListGridItemAdapter?.setPlayingPosition(-1)
        }
    }
    private fun tryToScrollToCurrentItem(position: Int) {
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

    private fun checkInteractions() {
        mDataBinding.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (mIsDraggingToScroll) {
                    if (dy < 0) {
                        Log.i(TAG, "Scrolling --> TOP")
                        mMainFragmentViewModel.scrollingState.value = ExploreContentForFragment.SCROLLING_TOP
                    } else if (dy > 0) {
                        Log.i(TAG, "Scrolling --> BOTTOM")
                        mMainFragmentViewModel.scrollingState.value = ExploreContentForFragment.SCROLLING_BOTTOM
                    }
                    if (!recyclerView.canScrollVertically(1) && dy > 0) {
                        Log.i(TAG, "Scrolled to BOTTOM")
                        mMainFragmentViewModel.scrollingState.value = ExploreContentForFragment.SCROLLED_BOTTOM
                    } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                        Log.i(TAG, "Scrolled to TOP")
                        mMainFragmentViewModel.scrollingState.value = ExploreContentForFragment.SCROLLED_TOP
                    }
                } else {
                    if (!recyclerView.canScrollVertically(1) && dy > 0) {
                        Log.i(TAG, "Scrolled to BOTTOM")
                        if (mMainFragmentViewModel.scrollingState.value != ExploreContentForFragment.SCROLLED_BOTTOM)
                            mMainFragmentViewModel.scrollingState.value = ExploreContentForFragment.SCROLLED_BOTTOM
                    } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                        Log.i(TAG, "Scrolled to TOP")
                        if (mMainFragmentViewModel.scrollingState.value != ExploreContentForFragment.SCROLLED_TOP)
                            mMainFragmentViewModel.scrollingState.value = ExploreContentForFragment.SCROLLED_TOP
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

    private fun setupRecyclerViewAdapter() {
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
                ): GenericItemListGrid? {
                    return SongItem.castDataItemToGeneric(
                        ctx,
                        dataItem
                    )
                }

                override fun onRequestTextIndexForFastScroller(
                    dataItem: Any,
                    position: Int
                ): String {
                    return SongItem.getStringIndexForFastScroller(
                        dataItem
                    )
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
                        FragmentsCommunication.playSongAtPositionFromGenericAdapterView(
                            mPlayingNowFragmentViewModel,
                            mAllSongsFragmentViewModel,
                            mGenericListGridItemAdapter,
                            TAG,
                            null,
                            null,
                            position
                        )
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
            mAllSongsFragmentViewModel.organizeListGrid.value
                ?: AllSongsFragmentViewModel.ORGANIZE_LIST_GRID_DEFAULT_VALUE,
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
            mAllSongsFragmentViewModel.organizeListGrid.value
        )
        mLayoutManager =
            GridLayoutManager(ctx, initialSpanCount, GridLayoutManager.VERTICAL, false)
        mLayoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val newSpanCount: Int = OrganizeItemBottomSheetDialogFragment.getSpanCount(
                    ctx,
                    mAllSongsFragmentViewModel.organizeListGrid.value
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
        mDataBinding.recyclerView.adapter = mConcatAdapter
        mDataBinding.recyclerView.layoutManager = mLayoutManager
        val newSpanCount: Int = OrganizeItemBottomSheetDialogFragment.getSpanCount(
            ctx,
            mAllSongsFragmentViewModel.organizeListGrid.value
        )
        val updatedSpan: Int =
            if (mLayoutManager?.spanCount == newSpanCount) newSpanCount else mLayoutManager?.spanCount
                ?: 1
        mItemDecoration = GridSpacingItemDecoration(updatedSpan)
        mItemDecoration?.let {
            mDataBinding.recyclerView.addItemDecoration(it)
        }

        mDataBinding.fastScroller.setSectionIndexer(mGenericListGridItemAdapter)
        mDataBinding.fastScroller.attachRecyclerView(mDataBinding.recyclerView)
        mDataBinding.fastScroller.setFastScrollListener(object : FastScrollListener {
            override fun onFastScrollStart(fastScroller: FastScroller) {
                mMainFragmentViewModel.isFastScrolling.value = true
            }

            override fun onFastScrollStop(fastScroller: FastScroller) {
                mMainFragmentViewModel.isFastScrolling.value = false
                if (!mDataBinding.recyclerView.canScrollVertically(-1)) {
                    //On scrolled to top
                    mMainFragmentViewModel.scrollingState.value = ExploreContentForFragment.SCROLLED_TOP
                } else if (!mDataBinding.recyclerView.canScrollVertically(1)) {
                    //On scrolled to bottom
                    mMainFragmentViewModel.scrollingState.value = ExploreContentForFragment.SCROLLED_BOTTOM
                }
            }
        })
    }

    private fun showSortDialog() {
        mSortItemsDialog = SortSongsBottomSheetDialogFragment.newInstance(
            mAllSongsFragmentViewModel,
            TAG,
            null
        )
        mSortItemsDialog?.show(childFragmentManager, SortSongsBottomSheetDialogFragment.TAG)
    }

    private fun showOrganizeDialog() {
        mOrganizeItemsDialog = OrganizeItemBottomSheetDialogFragment.newInstance(
            mAllSongsFragmentViewModel,
            TAG,
            null
        )
        mOrganizeItemsDialog?.show(childFragmentManager, OrganizeItemBottomSheetDialogFragment.TAG)
    }

    private fun playSongOnShuffle() {
        if ((mGenericListGridItemAdapter?.currentList?.size ?: 0) <= 0) return

        updateRecyclerViewScrollingSate()
        mMediaControllerViewModel.playContent(
            mediaItems = mAllSongsFragmentViewModel.getSelectedMediaItems(mGenericListGridItemAdapter?.currentList),
            startIndex = 0,
            startPosition = 0,
            repeatMode = Player.REPEAT_MODE_OFF,
            shuffleModeEnabled = true,
            isCompleteList = true,
            contentSource = TAG
        )
        mMediaControllerViewModel.mediaController?.shuffleModeEnabled = true
        mMediaControllerViewModel.mediaController?.play()
    }

    private fun playSong() {
        if ((mGenericListGridItemAdapter?.currentList?.size ?: 0) <= 0) return

        updateRecyclerViewScrollingSate()
        mMediaControllerViewModel.playContent(
            mediaItems = mAllSongsFragmentViewModel.getSelectedMediaItems(mGenericListGridItemAdapter?.currentList),
            startIndex = 0,
            startPosition = 0,
            repeatMode = Player.REPEAT_MODE_OFF,
            shuffleModeEnabled = false,
            isCompleteList = true,
            contentSource = TAG
        )
        mMediaControllerViewModel.mediaController?.shuffleModeEnabled = true
        mMediaControllerViewModel.mediaController?.play()
    }

    private fun updateRecyclerViewScrollingSate() {
        if (mDataBinding.recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING) {
            mIsDraggingToScroll = false
        }
        mMainFragmentViewModel.scrollingState.value = ExploreContentForFragment.SCROLLING_TOP
    }

    private fun initViews() {
        mDataBinding.recyclerView.setHasFixedSize(true)
        InsetModifiers.updateBottomViewInsets(
            mDataBinding.constraintFastScrollerContainer
        )
    }

    companion object {
        const val TAG = "AllSongsFragment"

        @JvmStatic
        fun newInstance() =
            AllSongsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}