package com.prosabdev.fluidmusic

import android.content.ComponentName
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.ConnectionCallback
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import com.google.android.material.color.DynamicColors
import com.prosabdev.fluidmusic.databinding.ActivityMainBinding
import com.prosabdev.fluidmusic.service.MediaPlaybackService
import com.prosabdev.fluidmusic.ui.activities.SettingsActivity
import com.prosabdev.fluidmusic.ui.fragments.*
import com.prosabdev.fluidmusic.utils.ConstantValues
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


@BuildCompat.PrereleaseSdkCheck class MainActivity : AppCompatActivity(){

    private lateinit var mActivityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        DynamicColors.applyToActivitiesIfAvailable(this.application)

        mActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupAudioSettings()
        initViews()

        MainScope().launch {
            attachFragments()
            checkInteractions()
            createMediaBrowserService()
        }
    }

    private fun attachFragments() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.main_activity_fragment_container, MainFragment.newInstance())
        }
    }

    private fun createMediaBrowserService() {
        mMediaBrowser = MediaBrowserCompat(
            applicationContext,
            ComponentName(applicationContext, MediaPlaybackService::class.java),
            mConnectionCallbacks,
            null // optional Bundle
        )
    }

    private fun checkInteractions() {
        mActivityMainBinding.navigationView.setNavigationItemSelectedListener { menuItem ->
            if(mActivityMainBinding.navigationView.checkedItem?.itemId != menuItem.itemId){
                when (menuItem.itemId) {
                    R.id.music_library -> {
                        mActivityMainBinding.drawerLayout.close()
                        supportFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(R.id.main_fragment_container, MusicLibraryFragment.newInstance())
                        }
                    }
                    R.id.folders_hierarchy -> {
                        mActivityMainBinding.drawerLayout.close()
                        supportFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(R.id.main_fragment_container, FoldersHierarchyFragment.newInstance())
                        }
                    }
                    R.id.playlists -> {
                        mActivityMainBinding.drawerLayout.close()
                        supportFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(R.id.main_fragment_container, PlaylistsFragment.newInstance())
                        }
                    }
                    R.id.streams -> {
                        mActivityMainBinding.drawerLayout.close()
                        supportFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(R.id.main_fragment_container, StreamsFragment.newInstance())
                        }
                    }
                }
            }
            when (menuItem.itemId) {
                R.id.settings -> {
                    startActivity(Intent(this.applicationContext, SettingsActivity::class.java).apply {})
                }
            }
            menuItem.isChecked = true

            true
        }
    }
    private fun updateDrawerMenu() {
        when (supportFragmentManager.findFragmentById(R.id.main_fragment_container)) {
            is MusicLibraryFragment -> {
                mActivityMainBinding.navigationView.setCheckedItem(R.id.music_library)
            }
            is FoldersHierarchyFragment -> {
                mActivityMainBinding.navigationView.setCheckedItem(R.id.folders_hierarchy)
            }
            is PlaylistsFragment -> {
                mActivityMainBinding.navigationView.setCheckedItem(R.id.playlists)
            }
            is StreamsFragment -> {
                mActivityMainBinding.navigationView.setCheckedItem(R.id.streams)
            }
        }
    }


    private fun setupAudioSettings() {
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private fun initViews(){
        mActivityMainBinding.navigationView.setCheckedItem(R.id.music_library)
    }

    private var mMediaBrowser: MediaBrowserCompat? = null
    private var mConnectionCallbacks: ConnectionCallback = object : ConnectionCallback(){
        override fun onConnected() {
            super.onConnected()
            Log.i(ConstantValues.TAG, "ConnectionCallback onConnected")
            mMediaBrowser?.sessionToken.also { token ->
                val mediaController = MediaControllerCompat(
                    this@MainActivity.applicationContext, // Context
                    token!!
                )
                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            }
            buildTransportControls()
        }
        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            Log.i(ConstantValues.TAG, "ConnectionCallback onConnectionSuspended")
        }
        override fun onConnectionFailed() {
            super.onConnectionFailed()
            Log.i(ConstantValues.TAG, "ConnectionCallback onConnectionFailed")
        }
    }
    private var mControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.i(ConstantValues.TAG, "MediaControllerCompat onMetadataChanged : $metadata")
        }
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Log.i(ConstantValues.TAG, "MediaControllerCompat onPlaybackStateChanged : $state")
        }
    }

    private fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(this)
        mediaController.transportControls.prepare()

        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        mediaController.registerCallback(mControllerCallback)
    }

    public override fun onStart() {
        super.onStart()
        mMediaBrowser?.connect()
    }
    public override fun onResume() {
        super.onResume()
        setupAudioSettings()
        updateDrawerMenu()
    }

    public override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(mControllerCallback)
        mMediaBrowser?.disconnect()
    }
}