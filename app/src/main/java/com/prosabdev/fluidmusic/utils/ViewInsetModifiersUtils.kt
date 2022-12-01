package com.prosabdev.fluidmusic.utils

import android.os.Build
import android.util.Log
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

abstract class ViewInsetModifiersUtils {
    companion object{

        fun updateTopViewInsets(view: View) {
            Log.i(ConstantValues.TAG, "APPPPPPPPPPPPLY INSETSSSSSSSSSSSSSSSSSSS ------------->")
            view.setOnApplyWindowInsetsListener { v, insets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val finalInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.updatePadding(
                        top = finalInsets.top
                    )
                } else {
                    v.updatePadding(
                        top = insets.systemWindowInsetTop
                    )
                }
                insets
            }
        }
        fun updateBottomViewInsets(view: View) {
            view.setOnApplyWindowInsetsListener { v, insets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val finalInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.updatePadding(
                        bottom = finalInsets.bottom
                    )
                } else {
                    v.updatePadding(
                        bottom = insets.systemWindowInsetBottom
                    )
                }
                insets
            }
        }
    }
}