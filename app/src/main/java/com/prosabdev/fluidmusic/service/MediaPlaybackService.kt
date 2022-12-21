package com.prosabdev.fluidmusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
import android.media.AudioManager.STREAM_MUSIC
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import androidx.media.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.prosabdev.fluidmusic.R
import com.prosabdev.fluidmusic.utils.ConstantValues

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

class MediaPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var mMediaSession: MediaSessionCompat
    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mMediaPlayer: CustomMediaPlayer
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder

    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()
    private var currentMediaItemIndex: Int = 0

//    private var mHandlerThread : HandlerThread? = null
//    private var mCustomPlayerHandler : CustomPlayerHandler? = null
//    private lateinit var mMediaPlayer: CustomMediaPlayer

    private lateinit var mAudioFocusRequest: AudioFocusRequest

    private var mOnAudioFocusChangeListener: AudioManager.OnAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { audioFocusChange -> Log.i(ConstantValues.TAG, "onAudioFocusChange $audioFocusChange") }

    private val mBecomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Log.i(ConstantValues.TAG, "BroadcastReceiver mBecomingNoisyReceiver")
        }
    }
    private val mCallback = object: MediaSessionCompat.Callback() {
        override fun onPrepare() {
            super.onPrepare()
        }
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            onPlayFrom(extras)
        }
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            onPlayFrom(extras)
        }
        override fun onPlay() {
            if(requestAudioFocus()){
                startService(Intent(applicationContext, MediaBrowserService::class.java))
                mMediaSession?.isActive = true
                mMediaPlayer.playMediaPlayer()
                registerReceivers()
                createNotification()
            }
        }
        override fun onStop() {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Abandon audio focus
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(mAudioFocusRequest)
            }else{
                audioManager.abandonAudioFocus(mOnAudioFocusChangeListener)
            }
            unregisterReceiver(mBecomingNoisyReceiver)
            // Stop the service
            stopSelf()
            // Set the session inactive  (and update metadata and state)
            mMediaSession?.isActive = false
            // stop the player (custom call)
            mMediaPlayer.stopMediaPlayer()
            // Take the service out of the foreground
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }else{
                stopForeground(false)
            }
        }
        override fun onPause() {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Update metadata and state
            // pause the player (custom call)
            mMediaPlayer.pauseMediaPlayer()
            // unregister BECOME_NOISY BroadcastReceiver
            unregisterReceiver(mBecomingNoisyReceiver)
            // Take the service out of the foreground, retain the notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH)
            }else{
                stopForeground(false)
            }
        }
        override fun onSkipToQueueItem(id: Long) {
            super.onSkipToQueueItem(id)
        }
        override fun onSkipToNext() {
            super.onSkipToNext()
        }
        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
        }
        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
        }
        override fun onSetPlaybackSpeed(speed: Float) {
            super.onSetPlaybackSpeed(speed)
        }
        override fun onSetRepeatMode(repeatMode: Int) {
            super.onSetRepeatMode(repeatMode)
        }
        override fun onSetShuffleMode(shuffleMode: Int) {
            super.onSetShuffleMode(shuffleMode)
        }
        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            super.onAddQueueItem(description)
        }
        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            super.onRemoveQueueItem(description)
        }

        fun registerReceivers() {
            registerReceiver(mBecomingNoisyReceiver, IntentFilter(ACTION_AUDIO_BECOMING_NOISY))
        }
        fun requestAudioFocus(): Boolean {
            var result : Boolean = false
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setOnAudioFocusChangeListener(mOnAudioFocusChangeListener)
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    build()
                }
            }
            val audioFocusResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.requestAudioFocus(mAudioFocusRequest)
            } else {
                audioManager.requestAudioFocus(mOnAudioFocusChangeListener, STREAM_MUSIC, 1)
            }
            if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                result = true
            }
            return result
        }
        fun createNotification(){
            val controller = mMediaSession?.controller
            val mediaMetadata = controller?.metadata
            val description = mediaMetadata?.description

            val builder = androidx.core.app.NotificationCompat.Builder(applicationContext, ConstantValues.CHANNEL_ID).apply {
                setStyle(
                    NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession?.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                applicationContext,
                                PlaybackStateCompat.ACTION_STOP
                            )
                        )
                )

                //Add the metadata for the currently playing track
                setContentTitle(description?.title ?: applicationContext.getString(R.string.unknown_title))
                setContentText(description?.subtitle ?: applicationContext.getString(R.string.unknown_artist))
                setSubText("0/0")
                setLargeIcon(description?.iconBitmap)

                //Enable launching the player by clicking the notification
                setContentIntent(controller?.sessionActivity)

                //Stop the service when the notification is swiped away
                setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
                priority = androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
                setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)

                setSmallIcon(R.drawable.ic_fluid_music_icon)

                addAction(
                    androidx.core.app.NotificationCompat.Action(
                        R.drawable.skip_previous,
                        getString(R.string.previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext,
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                    )
                )
                addAction(
                    androidx.core.app.NotificationCompat.Action(
                        R.drawable.pause,
                        getString(R.string.pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                    )
                )
                addAction(
                    androidx.core.app.NotificationCompat.Action(
                        R.drawable.skip_next,
                        getString(R.string.next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext,
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        )
                    )
                )
                addAction(
                    androidx.core.app.NotificationCompat.Action(
                        R.drawable.close,
                        getString(R.string.close),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
                )
            }
            startForeground(ConstantValues.NOTIFICATION_REQUEST_CODE, builder.build())
        }

        private fun onPlayFrom(extras: Bundle?) {
            if (extras != null) {
                setupMediaSessionWithBundleData(extras)
            }
        }

        fun setupMediaPlayer(extras : Bundle){
            val currentSongPath : String? = mMediaPlayer.setupQueueListExtras(extras)

            if(currentSongPath != null && currentSongPath.isNotEmpty())
                if(mMediaPlayer.prepareMediaPlayerWithPath(currentSongPath))
                    mMediaPlayer.playMediaPlayer()
        }
        fun setupMediaSessionWithBundleData(extras: Bundle) {
            val tempShuffle : Int = extras.getInt(ConstantValues.BUNDLE_SHUFFLE_VALUE, 0)
            val tempRepeat : Int = extras.getInt(ConstantValues.BUNDLE_REPEAT_VALUE, 0)
            val tempCurrentSongMetaData : MediaMetadataCompat? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable(ConstantValues.BUNDLE_CURRENT_SONG_META_DATA, MediaMetadataCompat::class.java)
            } else {
                extras.getParcelable(ConstantValues.BUNDLE_CURRENT_SONG_META_DATA)
            }
            mMediaSession?.setRepeatMode(tempRepeat)
            mMediaSession?.setShuffleMode(tempShuffle)
            mMediaSession?.setMetadata(tempCurrentSongMetaData)
            mMediaSession?.isActive = true
        }
    }

    override fun onCreate() {
        super.onCreate()
        createPendingIntentForUILaunch()
        setupMediaSession()
        setupPlayerHandler()
        setupNotificationChannel()
    }

    private fun createPendingIntentForUILaunch() {
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }
    }

    private fun setupPlayerHandler() {
//        mHandlerThread = HandlerThread(ConstantValues.HANDLER_THREAD, Thread.NORM_PRIORITY)
//        mHandlerThread?.start()
//        mCustomPlayerHandler = CustomPlayerHandler(mHandlerThread?.looper!!, WeakReference(this))
//        mMediaPlayer = CustomMediaPlayer(WeakReference(this))
//        mMediaPlayer.setHandler(mCustomPlayerHandler!!)
    }

    private fun setupMediaSession() {
        mMediaSession = MediaSessionCompat(applicationContext, ConstantValues.TAG).apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            mStateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                            PlaybackStateCompat.ACTION_PLAY_FROM_URI or
                            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SEEK_TO or
                            PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
                            PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE or
                            PlaybackStateCompat.ACTION_SET_RATING or
                            PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED or
                            PlaybackStateCompat.ACTION_STOP
                )
            setPlaybackState(mStateBuilder.build())
            setCallback(mCallback)
            setSessionToken(sessionToken)
        }
    }
    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = getString(R.string.music_player)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(ConstantValues.CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return if (allowBrowsing(clientPackageName, clientUid)) {
            Log.i(ConstantValues.TAG, "onGetRoot")
            Log.i(ConstantValues.TAG, "onGetRoot : $clientPackageName")
            BrowserRoot(MY_MEDIA_ROOT_ID, null)
        } else {
            BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
        }
    }
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.i(ConstantValues.TAG, "onLoadChildren")
        Log.i(ConstantValues.TAG, "onLoadChildren : $parentId")
        if (MY_EMPTY_MEDIA_ROOT_ID == parentId) {
            result.sendResult(null)
            return
        }
        val mediaItems = emptyList<MediaBrowserCompat.MediaItem>()

        if (MY_MEDIA_ROOT_ID == parentId) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems as MutableList<MediaBrowserCompat.MediaItem>?)
    }
    private fun allowBrowsing(clientPackageName: String, clientUid: Int): Boolean {
        var result = true
        Log.i(ConstantValues.TAG, "allowBrowsing clientPackageName = $clientPackageName, clientUid = $clientUid")
        return result
    }

    companion object {
        val TAG: String = MediaPlaybackService::class.java.simpleName
        const val MUSIC_PACKAGE_NAME = "com.android.music"
        const val ACTION_TOGGLE_PAUSE = "${ConstantValues.PACKAGE_NAME}.ACTION_TOGGLE_PAUSE"
        const val ACTION_PLAY = "${ConstantValues.PACKAGE_NAME}.ACTION_PLAY"
        const val ACTION_PLAY_PLAYLIST = "${ConstantValues.PACKAGE_NAME}.ACTION_PLAY_PLAYLIST"
        const val ACTION_PAUSE = "${ConstantValues.PACKAGE_NAME}.ACTION_PAUSE"
        const val ACTION_STOP = "${ConstantValues.PACKAGE_NAME}.ACTION_STOP"
        const val ACTION_SKIP = "${ConstantValues.PACKAGE_NAME}.ACTION_SKIP"
        const val ACTION_REWIND = "${ConstantValues.PACKAGE_NAME}.ACTION_REWIND"
        const val ACTION_QUIT = "${ConstantValues.PACKAGE_NAME}.ACTION_QUIT"
        const val ACTION_PENDING_QUIT = "${ConstantValues.PACKAGE_NAME}.ACTION_PENDING_QUIT"
        const val INTENT_EXTRA_PLAYLIST = "${ConstantValues.PACKAGE_NAME}.INTENT_EXTRA_PLAYLIST"
        const val INTENT_EXTRA_SHUFFLE_MODE =
            "${ConstantValues.PACKAGE_NAME}.INTENT_EXTRA_SHUFFLE_MODE"
        const val APP_WIDGET_UPDATE = "${ConstantValues.PACKAGE_NAME}.APP_WIDGET_UPDATE"
        const val EXTRA_APP_WIDGET_NAME = "${ConstantValues.PACKAGE_NAME}.EXTRA_APP_WIDGET_NAME"

        // Do not change these three strings as it will break support with other apps (e.g. last.fm
        // scrobbling)
        const val META_CHANGED = "${ConstantValues.PACKAGE_NAME}.META_CHANGED"
        const val QUEUE_CHANGED = "${ConstantValues.PACKAGE_NAME}.QUEUE_CHANGED"
        const val PLAY_STATE_CHANGED = "${ConstantValues.PACKAGE_NAME}.PLAY_STATE_CHANGED"
        const val FAVORITE_STATE_CHANGED = "${ConstantValues.PACKAGE_NAME}.FAVORITE_STATE_CHANGED"
        const val REPEAT_MODE_CHANGED = "${ConstantValues.PACKAGE_NAME}.REPEAT_MODE_CHANGED"
        const val SHUFFLE_MODE_CHANGED = "${ConstantValues.PACKAGE_NAME}.SHUFFLE_MODE_CHANGED"
        const val MEDIA_STORE_CHANGED = "${ConstantValues.PACKAGE_NAME}.MEDIA_STORE_CHANGED"
        const val CYCLE_REPEAT = "${ConstantValues.PACKAGE_NAME}.CYCLE_REPEAT"
        const val TOGGLE_SHUFFLE = "${ConstantValues.PACKAGE_NAME}.TOGGLE_SHUFFLE"
        const val TOGGLE_FAVORITE = "${ConstantValues.PACKAGE_NAME}.TOGGLE_FAVORITE"
        const val SAVED_POSITION = "SAVED_POSITION"
        const val SAVED_POSITION_IN_TRACK = "SAVED_POSITION_IN_TRACK"
        const val SAVED_SHUFFLE_MODE = "SAVED_SHUFFLE_MODE"
        const val SAVED_REPEAT_MODE = "SAVED_REPEAT_MODE"

        private const val MEDIA_SESSION_ACTIONS = (
                PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SEEK_TO

                or PlaybackStateCompat.ACTION_PLAY_FROM_URI
                or PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED
                or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                )
    }
}