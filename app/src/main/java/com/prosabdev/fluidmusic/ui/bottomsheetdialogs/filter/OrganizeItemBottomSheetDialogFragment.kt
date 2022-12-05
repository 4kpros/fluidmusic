package com.prosabdev.fluidmusic.ui.bottomsheetdialogs.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jbvincey.nestedradiobutton.NestedRadioGroupManager
import com.prosabdev.fluidmusic.R
import com.prosabdev.fluidmusic.databinding.BottomSheetOrganizeItemsBinding
import com.prosabdev.fluidmusic.utils.ConstantValues
import com.prosabdev.fluidmusic.viewmodels.fragments.GenericListenDataViewModel

class OrganizeItemBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var mBottomSheetOrganizeItemsBinding: BottomSheetOrganizeItemsBinding? = null

    private var mGenericListenDataViewModel: GenericListenDataViewModel? = null

    private var mFromSource: String? = null
    private var mFromSourceValue: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBottomSheetOrganizeItemsBinding = DataBindingUtil.inflate(inflater, R.layout._bottom_sheet_organize_items, container, false)
        val view = mBottomSheetOrganizeItemsBinding?.root

        initViews()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkInteractions()
    }

    private fun checkInteractions() {
        mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

                when (checkedId) {
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
        })
    }

    private fun initViews() {
        updateCheckedOrganizeButtonUI()
    }
    private fun updateCheckedOrganizeButtonUI() {
        if(mGenericListenDataViewModel == null) return
        val tempOrganize : Int = mGenericListenDataViewModel?.getOrganizeListGrid()?.value ?: return
        mBottomSheetOrganizeItemsBinding?.let { bottomSheetOrganizeItemsBinding ->
            when (tempOrganize) {
                ConstantValues.ORGANIZE_LIST_SMALL -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListSmall.id)
                }
                ConstantValues.ORGANIZE_LIST_MEDIUM -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListMedium.id)
                }
                ConstantValues.ORGANIZE_LIST_LARGE -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListLarge.id)
                }
                ConstantValues.ORGANIZE_LIST_SMALL_NO_IMAGE -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListSmallNoImage.id)
                }
                ConstantValues.ORGANIZE_LIST_MEDIUM_NO_IMAGE -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListMediumNoImage.id)
                }
                ConstantValues.ORGANIZE_LIST_LARGE_NO_IMAGE -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListLargeNoImage.id)
                }

                ConstantValues.ORGANIZE_GRID_EXTRA_SMALL -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridExtraSmall.id)
                }
                ConstantValues.ORGANIZE_GRID_SMALL -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridSmall.id)
                }
                ConstantValues.ORGANIZE_GRID_MEDIUM -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridMedium.id)
                }
                ConstantValues.ORGANIZE_GRID_LARGE -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridLarge.id)
                }
                ConstantValues.ORGANIZE_GRID_EXTRA_LARGE -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridExtraLarge.id)
                }
                ConstantValues.ORGANIZE_GRID_SMALL_NO_IMAGE -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridSmallNoImage.id)
                }
                ConstantValues.ORGANIZE_GRID_MEDIUM_NO_IMAGE -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioGridMediumNoImage.id)
                }
                else -> {
                    mBottomSheetOrganizeItemsBinding?.radioGroupOrganize?.check(bottomSheetOrganizeItemsBinding.radioListSmall.id)
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

        @JvmStatic
        fun newInstance() =
            OrganizeItemBottomSheetDialogFragment().apply {
            }
    }
}