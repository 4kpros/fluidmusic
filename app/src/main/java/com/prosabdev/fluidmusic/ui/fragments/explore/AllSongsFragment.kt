package com.prosabdev.fluidmusic.ui.fragments.explore

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.prosabdev.fluidmusic.R
import com.prosabdev.fluidmusic.adapters.HeadlinePlayShuffleAdapter
import com.prosabdev.fluidmusic.adapters.explore.SongItemAdapter
import com.prosabdev.fluidmusic.databinding.FragmentAllSongsBinding
import com.prosabdev.fluidmusic.roomdatabase.bus.DatabaseAccessApplication
import com.prosabdev.fluidmusic.ui.bottomsheetdialogs.SortOrganizeItemsBottomSheetDialog
import com.prosabdev.fluidmusic.utils.ConstantValues
import com.prosabdev.fluidmusic.utils.adapters.SelectableItemListAdapter
import com.prosabdev.fluidmusic.viewmodels.SongItemViewModel
import com.prosabdev.fluidmusic.viewmodels.SongItemViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class AllSongsFragment : Fragment() {
    private var mPageIndex: Int? = -1

    private lateinit var mFragmentAllSongsBinding: FragmentAllSongsBinding

    private var mContext: Context? = null
    private var mActivity: FragmentActivity? = null

    private lateinit var mSongItemViewModel: SongItemViewModel

    private var mEmptyBottomSpaceAdapter: HeadlinePlayShuffleAdapter? = null
    private var mHeadlineTopPlayShuffleAdapter: HeadlinePlayShuffleAdapter? = null
    private var mSongItemAdapter: SongItemAdapter? = null
    private var mLayoutManager: GridLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mPageIndex = it.getInt(ConstantValues.EXPLORE_ALL_SONGS)
        }
        mContext = requireContext()
        mActivity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mFragmentAllSongsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_all_songs,container,false)
        val view = mFragmentAllSongsBinding.root

        initViews()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MainScope().launch {
            setupRecyclerViewAdapter()
            observeLiveData()
            checkInteractions()
        }
    }

    private suspend fun observeLiveData() = lifecycleScope.launch(context = Dispatchers.IO){
        var tempList = mSongItemViewModel.getAllSongs(null, null)
        Log.i(ConstantValues.TAG, "Songs : ${tempList}")
    }

//    private fun observeLiveData() {
//        if(mAllSongsFragmentViewModel.getIsLoadingInBackground().value == false && (mAllSongsFragmentViewModel.getDataRequestCounter().value ?: 0) <= 0){
//            mAllSongsFragmentViewModel.requestLoadDataAsync(mActivity as Activity, 0, 1000)
//        }
//        mAllSongsFragmentViewModel.getSongList().observe(mActivity as LifecycleOwner
//        ) {
//            MainScope().launch {
//                addSongsToAdapter(it)
//            }
//        }
//        mAllSongsFragmentViewModel.getIsLoading().observe(mActivity as LifecycleOwner
//        ) {
//
//        }
//        mPlayerFragmentViewModel.getCurrentSong().observe(mActivity as LifecycleOwner
//        ) { onCurrentPlayingSongChanged(it) }
//        mMainFragmentViewModel.getSelectMode().observe(mActivity as LifecycleOwner
//        ) { onSelectionModeChanged(it) }
//        mMainFragmentViewModel.getTotalSelected().observe(mActivity as LifecycleOwner
//        ){ onTotalSelectedItemsChanged(it) }
//        mMainFragmentViewModel.getToggleRange().observe(mActivity as LifecycleOwner
//        ){ onToggleRangeChanged() }
//        mMainFragmentViewModel.getScrollingState().observe(mActivity as LifecycleOwner
//        ){ updateOnScrollingStateUI(it) }
//    }
//    private fun updateOnScrollingStateUI(i: Int) {
//        if(mMainFragmentViewModel.getActivePage().value == mPageIndex){
//            if(i == 2)
//                mEmptyBottomSpaceAdapter?.onSetScrollState(2)
//        }
//    }
//    private fun onToggleRangeChanged() {
//        mSongItemAdapter?.selectableToggleSelectRange(mLayoutManager)
//    }
//    private fun onTotalSelectedItemsChanged(it: Int?) {
//        if((it ?: 0) > 0 && (it ?: 0) >= (mMainFragmentViewModel.getTotalCount().value ?: 0)){
//            mSongItemAdapter?.selectableSelectAll(mLayoutManager)
//        }else if((it ?: 0) <= 0 && (mSongItemAdapter?.selectableGetSelectedItemCount() ?: 0) > 0){
//            mSongItemAdapter?.selectableClearSelection(mLayoutManager)
//        }
//    }
//    private fun onSelectionModeChanged(it: Boolean?) {
//        mSongItemAdapter?.selectableSetSelectionMode(it?:false)
//        mHeadlineTopPlayShuffleAdapter?.onSelectModeValue(it ?: false)
//    }
//    private fun onCurrentPlayingSongChanged(it: Int?) {
//        if (mPlayerFragmentViewModel.getSourceOfQueueList().value == ConstantValues.EXPLORE_ALL_SONGS)
//            mSongItemAdapter?.setCurrentPlayingSong(it ?: -1)
//    }
//    private suspend fun addSongsToAdapter(songList: ArrayList<SongItem>?) = coroutineScope{
//        if (songList != null) {
//            val startPosition: Int = mSongList.size
//            val itemCount: Int = songList.size
//            mSongList.addAll(startPosition, songList)
//            mSongItemAdapter?.notifyItemRangeInserted(startPosition, itemCount)
//        }
//        mMainFragmentViewModel.setTotalCount(mSongList.size)
//    }

    private suspend fun checkInteractions() = coroutineScope {
        mFragmentAllSongsBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy < 0){
                    Log.i(ConstantValues.TAG, "Scrolling --> TOP")
//                    mMainFragmentViewModel.setScrollingState(-1)
                }else if(dy > 0){
                    Log.i(ConstantValues.TAG, "Scrolling --> BOTTOM")
//                    mMainFragmentViewModel.setScrollingState(1)
                }
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    Log.i(ConstantValues.TAG, "Scrolled to BOTTOM")
//                    mMainFragmentViewModel.setScrollingState(2)
                } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                    Log.i(ConstantValues.TAG, "Scrolled to TOP")
//                    mMainFragmentViewModel.setScrollingState(-2)
                }
            }
        })
    }

    private suspend fun setupRecyclerViewAdapter() {
        val spanCount = 1
        var touchHelper : ItemTouchHelper ? = null
        //Setup headline adapter
        val listHeadlines : ArrayList<Long> = ArrayList<Long>()
        listHeadlines.add(0)
        mHeadlineTopPlayShuffleAdapter = HeadlinePlayShuffleAdapter(listHeadlines, R.layout.item_top_play_shuffle, object : HeadlinePlayShuffleAdapter.OnItemClickListener{
            override fun onPlayButtonClicked() {
//                onPlayButton(0)
            }
            override fun onShuffleButtonClicked() {
//                onShuffleButton()
            }
            override fun onFilterButtonClicked() {
                onShowFilterDialog()
            }
        })
        //Setup song adapter
        mSongItemAdapter?.submitList(null)
        mSongItemAdapter = SongItemAdapter(
            mContext!!,
            object : SongItemAdapter.OnItemClickListener{
                override fun onSongItemClicked(position: Int) {
//                    if(mSongItemAdapter?.selectableGetSelectionMode() == true){
//                        mSongItemAdapter?.selectableToggleSelection(position, mLayoutManager)
//                        mMainFragmentViewModel.setTotalSelected(mSongItemAdapter?.selectableGetSelectedItemCount() ?: 0)
//                    }else{
//                        onPlayButton(position)
//                    }
                }
                override fun onSongItemPlayClicked(position: Int) {
//                    updateCurrentPlayingSong(position)
                }
                override fun onSongItemLongClicked(position: Int) {
//                    onLongPressedToItemSong(position)
                }
                override fun onItemMovedTo(position: Int) {
//                    scrollDrag(position)
                }

            },
            object : SelectableItemListAdapter.OnSelectSelectableItemListener {
                override fun onTotalSelectedItemChange(totalSelected: Int) {
//                    mMainFragmentViewModel.setTotalSelected(totalSelected)
                }
            },
            object : SongItemAdapter.OnTouchListener{
                override fun requestDrag(viewHolder: RecyclerView.ViewHolder?) {
                    if (viewHolder != null) {
                        touchHelper?.startDrag(viewHolder)
                    }
                }

            }
        )
        mEmptyBottomSpaceAdapter = HeadlinePlayShuffleAdapter(listHeadlines, R.layout.item_empty_bottom_space, object : HeadlinePlayShuffleAdapter.OnItemClickListener{
            override fun onPlayButtonClicked() {
            }
            override fun onShuffleButtonClicked() {
            }
            override fun onFilterButtonClicked() {
            }
        })
        //Setup concat adapter
        val concatAdapter = ConcatAdapter()
        concatAdapter.addAdapter(mHeadlineTopPlayShuffleAdapter!!)
        concatAdapter.addAdapter(mSongItemAdapter!!)
        concatAdapter.addAdapter(mEmptyBottomSpaceAdapter!!)
        mFragmentAllSongsBinding.recyclerView.adapter = concatAdapter

        //Add Layout manager
        mLayoutManager = GridLayoutManager(mContext, spanCount, GridLayoutManager.VERTICAL, false)
        mFragmentAllSongsBinding.recyclerView.layoutManager = mLayoutManager

        //Setup Item touch helper callback for drag feature
//        val callback : ItemTouchHelper.Callback = SongItemMoveCallback(mSongItemAdapter!!)
//        touchHelper = ItemTouchHelper(callback)
//        touchHelper.attachToRecyclerView(mRecyclerView)
    }

    private fun onShowFilterDialog() {
        if(mContext == null)
            return
        SortOrganizeItemsBottomSheetDialog().show(childFragmentManager, SortOrganizeItemsBottomSheetDialog.TAG)
    }

//    private fun onLongPressedToItemSong(position: Int) {
//        if(mSongItemAdapter?.selectableGetSelectionMode() == true){
//            mSongItemAdapter?.selectableToggleSelection(position, mLayoutManager)
//            mMainFragmentViewModel.setTotalSelected(mSongItemAdapter?.selectableGetSelectedItemCount() ?: 0)
//        }else{
//            mSongItemAdapter?.selectableSetSelectionMode(true, mLayoutManager)
//            mMainFragmentViewModel.setSelectMode(mSongItemAdapter?.selectableGetSelectionMode() ?: false)
//            mSongItemAdapter?.selectableToggleSelection(position, mLayoutManager)
//            mMainFragmentViewModel.setTotalSelected(mSongItemAdapter?.selectableGetSelectedItemCount() ?: 0)
//        }
//    }
//    private fun onPlayButton(position: Int) {
//        mPlayerFragmentViewModel.setShuffle( PlaybackStateCompat.SHUFFLE_MODE_NONE)
//        mPlayerFragmentViewModel.setRepeat( PlaybackStateCompat.REPEAT_MODE_NONE)
//        updateCurrentPlayingSong(position)
//        mMainFragmentViewModel.setScrollingState(-1)
//    }
//    private fun onShuffleButton() {
//        mPlayerFragmentViewModel.setRepeat( PlaybackStateCompat.REPEAT_MODE_NONE)
//        when (mPlayerFragmentViewModel.getShuffle().value) {
//            PlaybackStateCompat.SHUFFLE_MODE_NONE -> {
//                mPlayerFragmentViewModel.setShuffle(PlaybackStateCompat.SHUFFLE_MODE_ALL)
//            }
//            PlaybackStateCompat.SHUFFLE_MODE_ALL -> {
//                mPlayerFragmentViewModel.setShuffle(PlaybackStateCompat.SHUFFLE_MODE_NONE)
//            }
//            else -> {
//                mPlayerFragmentViewModel.setShuffle(PlaybackStateCompat.SHUFFLE_MODE_NONE)
//            }
//        }
//        updateCurrentPlayingSong(CustomMathComputations.randomExcluded(mPlayerFragmentViewModel.getCurrentSong().value ?: 0, mSongList.size-1))
//        mMainFragmentViewModel.setScrollingState(-1)
//    }
//    private fun updateCurrentPlayingSong(position: Int) {
//        if(mPlayerFragmentViewModel.getSourceOfQueueList().value != ConstantValues.EXPLORE_ALL_SONGS){
//            mPlayerFragmentViewModel.setSongList(mSongList)
//            mPlayerFragmentViewModel.setSourceOfQueueList(ConstantValues.EXPLORE_ALL_SONGS)
//        }
//        mPlayerFragmentViewModel.setCurrentSong(position)
//        mPlayerFragmentViewModel.setIsPlaying(true)
//    }
//    fun scrollDrag(position: Int) {
//        Log.i(ConstantValues.TAG, "scrollDrag To $position")
//    }

    private fun initViews() {
        mSongItemViewModel = SongItemViewModelFactory(
            (activity?.application as DatabaseAccessApplication).database.songItemDao()
        ).create(SongItemViewModel::class.java)
    }

    companion object {
        @JvmStatic
        fun newInstance(pageIndex: Int) =
            AllSongsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ConstantValues.EXPLORE_ALL_SONGS, pageIndex)
                }
            }
    }
}