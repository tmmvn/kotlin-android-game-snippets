package com.koodipuukko.twintilt

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import com.koodipuukko.R
import java.io.IOException

class SoundManager
	( // Passed context used for loading resources
	private val mContext: Context
) {
	private val mSoundPool: SoundPool? // The sound pool
	private val soundIndexes =
		IntArray(NUM_SOUNDS) // Sound indexes (for sound pool)
	private val sounds = IntArray(NUM_SOUNDS) // Sound indexes (resources)
	private var soundTracker = 0 // Tracks the number of sounds loaded
	// set context
	private val mAudioManager: AudioManager? =
		mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager // The audio manager
	// get audio service
	private val mp =
		MediaPlayer() // Media player class instance for playing music
	// Function to pause music
	fun PauseMusic() {
		try {
			if(DEBUG) {
				Log.d(TAG, "Pausing music.")
			}
			mp.pause()
		} catch(e: IllegalStateException) {
			if(DEBUG) {
				Log.w(TAG, "Unable to pause music: $e")
			}
		}
	}
	// Function to resume music
	fun resumeMusic() {
		try {
			if(DEBUG) {
				Log.d(TAG, "Resuming music play.")
			}
			mp.start()
		} catch(e: IllegalStateException) {
			if(DEBUG) {
				Log.w(TAG, "Unable to resume music: $e")
			}
		}
	}
	// Function to stop music
	fun stopMusic(finalStop: Boolean) {
		try {
			if(DEBUG) {
				Log.d(TAG, "Stopping music play.")
			}
			mp.stop()
		} catch(e: IllegalStateException) {
			if(DEBUG) {
				Log.w(TAG, "Unable to stop music: $e")
			}
		}
		if(finalStop == true) {
			if(DEBUG) {
				Log.d(TAG, "Releasing media player")
			}
			mp.release()
		}
	}
	// Function to start playing music
	fun playMusic(trackId: Int) {
		var bgMusic = Uri.EMPTY // Holds the uri used for loading music file
		mp.reset() // reset media player
		if(trackId == 2) {
			try {  // parse the music uri, catch and log any errors
				bgMusic =
					Uri.parse("android.resource://com.koodipuukko.twintilt/" + R.raw.dstcauseway)
			} catch(e: NullPointerException) {
				if(DEBUG) {
					Log.e(TAG, "Error parsing uri: $e")
				}
			}
			try {  // set data source according to uri, catch and log any errors
				mp.setDataSource(mContext, bgMusic!!)
			} catch(e: IOException) {
				if(DEBUG) {
					Log.e(TAG, "Error setting data source: $e")
				}
			}
		}
		if(trackId == 1) {
			try {  // parse the music uri, catch and log any errors
				bgMusic =
					Uri.parse("android.resource://com.koodipuukko.twintilt/" + R.raw.dsthorizon)
			} catch(e: NullPointerException) {
				if(DEBUG) {
					Log.e(TAG, "Error parsing uri: $e")
				}
			}
			try {  // set data source according to uri, catch and log any errors
				mp.setDataSource(mContext, bgMusic!!)
			} catch(e: IOException) {
				if(DEBUG) {
					Log.e(TAG, "Error setting data source: $e")
				}
			}
		}
		if(trackId == 3) {
			try {  // parse the music uri, catch and log any errors
				bgMusic =
					Uri.parse("android.resource://com.koodipuukko.twintilt/" + R.raw.dstneonon)
			} catch(e: NullPointerException) {
				if(DEBUG) {
					Log.e(TAG, "Error parsing uri: $e")
				}
			}
			try {  // set data source according to uri, catch and log any errors
				mp.setDataSource(mContext, bgMusic!!)
			} catch(e: IOException) {
				if(DEBUG) {
					Log.e(TAG, "Error setting data source: $e")
				}
			}
		}
		if(trackId == 4) {
			try {  // parse the music uri, catch and log any errors
				bgMusic =
					Uri.parse("android.resource://com.koodipuukko.twintilt/" + R.raw.dstbreakout)
			} catch(e: NullPointerException) {
				if(DEBUG) {
					Log.e(TAG, "Error parsing uri: $e")
				}
			}
			try {  // set data source according to uri, catch and log any errors
				mp.setDataSource(mContext, bgMusic!!)
			} catch(e: IOException) {
				if(DEBUG) {
					Log.e(TAG, "Error setting data source: $e")
				}
			}
		}
		if(trackId == 5) {
			try {  // parse the music uri, catch and log any errors
				bgMusic =
					Uri.parse("android.resource://com.koodipuukko.twintilt/" + R.raw.dstaurora)
			} catch(e: NullPointerException) {
				if(DEBUG) {
					Log.e(TAG, "Error parsing uri: $e")
				}
			}
			try {  // set data source according to uri, catch and log any errors
				mp.setDataSource(mContext, bgMusic!!)
			} catch(e: IOException) {
				if(DEBUG) {
					Log.e(TAG, "Error setting data source: $e")
				}
			}
		}
		try {  // prepare the data, catch and log any errors
			mp.prepare()
		} catch(e: IOException) {
			if(DEBUG) {
				Log.e(TAG, "Error preparing media player: $e")
			}
		}				// get current volume, divide by max volume and then divide by three to balance with SFX
		var streamVolume =
			mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
		streamVolume /= mAudioManager
			.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
		streamVolume /= 3f
		if(DEBUG) // if debugging, log the set volume
		{
			Log.d(TAG, "Music volume set at: $streamVolume")
		}
		mp.setVolume(streamVolume, streamVolume) // set the media player volume
		// Set player to loop music
		mp.isLooping = true
		try {  // try to start playing the music, catch and log any errors
			mp.start()
		} catch(e: IllegalStateException) {
			if(DEBUG) {
				Log.e(TAG, "Failed to start Media Player: $e")
			}
		}
	}
	// Constructor function
	init {
		if(mAudioManager == null) // log load status if debug enabled
		{
			if(DEBUG) {
				Log.e(TAG, "Failed to initialize audio service.")
			}
		} else {
			if(DEBUG) {
				Log.d(TAG, "Audio service initialized")
			}
		}
		val soundPoolBuilder = SoundPool.Builder()
		soundPoolBuilder.setMaxStreams(4)
		val audioAttributes = AudioAttributes.Builder()
		audioAttributes.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
		audioAttributes.setUsage(AudioAttributes.USAGE_GAME)
		soundPoolBuilder.setAudioAttributes(audioAttributes.build())
		mSoundPool = soundPoolBuilder.build()
		if(mSoundPool == null) {
			if(DEBUG) {
				Log.e(TAG, "Failed to initialize sound pool")
			}
		} else {
			if(DEBUG) {
				Log.d(TAG, "Sound pool initialized.")
			}
		}
		if(loadSounds()) // load SFX
		{
			if(DEBUG) {
				Log.d(TAG, "Sounds loaded ok.")
			}
		} else {
			if(DEBUG) {
				Log.w(TAG, "Sound loading failed.")
			}
		}
	}
	// Function to load sfx
	private fun loadSounds(): Boolean {		// Set ids from resources
		sounds[0] = R.raw.wrong
		sounds[1] = R.raw.yellow
		sounds[2] = R.raw.green
		for(i in 0 until NUM_SOUNDS) {
			addSound(sounds[i])
		}
		return true
	}
	// Function to load a sound to soundpool
	private fun addSound(id: Int) {
		soundIndexes[soundTracker] =
			mSoundPool!!.load(mContext, id, 1) // load sound
		soundTracker++ // increase counter of loaded sounds
	}
	// Function for playing sounds
	fun playSound(index: Int) {
		val result: Int // holds the value if play was succesful				// get current volume, divide to fit in scale and set volume
		var streamVolume =
			mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
		streamVolume /= mAudioManager
			.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
		if(DEBUG) {
			Log.d(TAG, "SFX volume set at $streamVolume")
		}
		result = mSoundPool!!.play(
			soundIndexes[index],
			streamVolume,
			streamVolume,
			1,
			0,
			1f
		)
		if(result == 0) {
			if(DEBUG) {
				Log.w(TAG, "Failed to play SFX.")
			}
		} else {
			if(DEBUG) {
				Log.d(TAG, "Sound played succesfully.")
			}
		}
	}

	companion object {
		private const val TAG = "Twintilt" // Game tag in logs
		private const val DEBUG = true // flag to set logging on and off
		private const val NUM_SOUNDS = 3 // Constant for number of sounds
	}
}
