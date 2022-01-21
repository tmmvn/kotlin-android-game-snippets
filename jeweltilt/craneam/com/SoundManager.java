package jeweltilt.craneam.com;

import java.io.IOException;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.util.Log;

public class SoundManager
  {
    private static final String TAG = "Jewel Tilt";  // Game tag in logs
    private static final boolean DEBUG = false;  // flag to set logging on and off
    private SoundPool mSoundPool;  // The sound pool
    private final static int NUM_SOUNDS = 3;  // Constant for number of sounds
    private int[] soundIndexes = new int[NUM_SOUNDS];  // Sound indexes (for sound pool)
    private int[] sounds = new int[NUM_SOUNDS];  // Sound indexes (resources)
    private int soundTracker = 0;  // Tracks the number of sounds loaded
    private AudioManager mAudioManager;  // The audio manager
    private Context mContext;  // Passed context used for loading resources
    private MediaPlayer mp = new MediaPlayer();  // Media player class instance for playing music

    // Function to pause music
    public void PauseMusic()
      {
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Pausing music.");
              }
            mp.pause();
          }
        catch (IllegalStateException e)
          {
            if (DEBUG)
              {
                Log.w(TAG, "Unable to pause music: " + e);
              }
          }
      }

    // Function to resume music
    public void ResumeMusic()
      {
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Resuming music play.");
              }
            mp.start();
          }
        catch (IllegalStateException e)
          {
            if (DEBUG)
              {
                Log.w(TAG, "Unable to resume music: " + e);
              }
          }
      }

    // Function to stop music
    public void StopMusic(boolean finalStop)
      {
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Stopping music play.");
              }
            mp.stop();
          }
        catch (IllegalStateException e)
          {
            if (DEBUG)
              {
                Log.w(TAG, "Unable to stop music: " + e);
              }
          }
        if (finalStop == true)
          {
            if (DEBUG)
              {
                Log.d(TAG, "Releasing media player");
              }
            mp.release();
          }
      }

    // Function to start playing music
    public void PlayMusic(int trackId)
      {
        Uri bgMusic = Uri.EMPTY;  // Holds the uri used for loading music file
        mp.reset();  // reset media player
        if (trackId == 2)
          {
            try
              {  // parse the music uri, catch and log any errors
                bgMusic = Uri.parse("android.resource://jeweltilt.craneam.com/" + R.raw.real1);
              }
            catch (NullPointerException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Error parsing uri: " + e);
                  }
              }
            try
              {  // set data source according to uri, catch and log any errors
                mp.setDataSource(mContext, bgMusic);
              }
            catch (IOException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Error setting data source: " + e);
                  }
              }
          }
        if (trackId == 1)
          {
            try
              {  // parse the music uri, catch and log any errors
                bgMusic = Uri.parse("android.resource://jeweltilt.craneam.com/" + R.raw.menusong);
              }
            catch (NullPointerException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Error parsing uri: " + e);
                  }
              }
            try
              {  // set data source according to uri, catch and log any errors
                mp.setDataSource(mContext, bgMusic);
              }
            catch (IOException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Error setting data source: " + e);
                  }
              }
          }
        if (trackId == 3)
          {
            try
              {  // parse the music uri, catch and log any errors
                bgMusic = Uri.parse("android.resource://jeweltilt.craneam.com/" + R.raw.dark1);
              }
            catch (NullPointerException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Error parsing uri: " + e);
                  }
              }
            try
              {  // set data source according to uri, catch and log any errors
                mp.setDataSource(mContext, bgMusic);
              }
            catch (IOException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Error setting data source: " + e);
                  }
              }
          }
        if (trackId == 4)
          {
            try
              {  // parse the music uri, catch and log any errors
                bgMusic = Uri.parse("android.resource://jeweltilt.craneam.com/" + R.raw.brite1);
              }
            catch (NullPointerException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Error parsing uri: " + e);
                  }
              }
            try
              {  // set data source according to uri, catch and log any errors
                mp.setDataSource(mContext, bgMusic);
              }
            catch (IOException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Error setting data source: " + e);
                  }
              }
          }
        if (trackId == 5)
          {
            try
              {  // parse the music uri, catch and log any errors
                bgMusic = Uri.parse("android.resource://jeweltilt.craneam.com/" + R.raw.slow1);
              }
            catch (NullPointerException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Error parsing uri: " + e);
                  }
              }
            try
              {  // set data source according to uri, catch and log any errors
                mp.setDataSource(mContext, bgMusic);
              }
            catch (IOException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Error setting data source: " + e);
                  }
              }
          }
        try
          {  // prepare the data, catch and log any errors
            mp.prepare();
          }
        catch (IOException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Error preparing media player: " + e);
              }
          }
        // get current volume, divide by max volume and then divide by three to balance with SFX
        float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolume /= mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        streamVolume /= 3;
        if (DEBUG)  // if debugging, log the set volume
          {
            Log.d(TAG, "Music volume set at: " + streamVolume);
          }
        mp.setVolume(streamVolume, streamVolume);  // set the media player volume
        // Set player to loop music
        mp.setLooping(true);
        try
          {  // try to start playing the music, catch and log any errors
            mp.start();
          }
        catch (IllegalStateException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Failed to start Media Player: " + e);
              }
          }
      }

    // Constructor function
    public SoundManager(Context passedContext)
      {
        mContext = passedContext;  // set context
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);  // get audio service
        if (mAudioManager == null)  // log load status if debug enabled
          {
            if (DEBUG)
              {
                Log.e(TAG, "Failed to initialize audio service.");
              }
          }
        else
          {
            if (DEBUG)
              {
                Log.d(TAG, "Audio service initialized");
              }
          }
        mSoundPool = new SoundPool(4,
          AudioManager.STREAM_MUSIC,
          100);  // create a sound pool with a maximum of 4 sound streams
        if (mSoundPool == null)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Failed to initialize sound pool");
              }
          }
        else
          {
            if (DEBUG)
              {
                Log.d(TAG, "Sound pool initialized.");
              }
          }
        if (LoadSounds())  // load SFX
          {
            if (DEBUG)
              {
                Log.d(TAG, "Sounds loaded ok.");
              }
          }
        else
          {
            if (DEBUG)
              {
                Log.w(TAG, "Sound loading failed.");
              }
          }
      }

    // Function to load sfx
    private boolean LoadSounds()
      {
        // Set ids from resources
        sounds[0] = R.raw.wrong;
        sounds[1] = R.raw.yellow;
        sounds[2] = R.raw.green;
        for (int i = 0; i < NUM_SOUNDS; i++)
          {
            AddSound(sounds[i]);
          }
        return true;
      }

    // Function to load a sound to soundpool
    private void AddSound(int id)
      {
        soundIndexes[soundTracker] = mSoundPool.load(mContext, id, 1);  // load sound
        soundTracker++;  // increase counter of loaded sounds
      }

    // Function for playing sounds
    public void PlaySound(int index)
      {
        int result;  // holds the value if play was succesful
        // get current volume, divide to fit in scale and set volume
        float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolume /= mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (DEBUG)
          {
            Log.d(TAG, "SFX volume set at " + streamVolume);
          }
        result = mSoundPool.play(soundIndexes[index], streamVolume, streamVolume, 1, 0, 1f);
        if (result == 0)
          {
            if (DEBUG)
              {
                Log.w(TAG, "Failed to play SFX.");
              }
          }
        else
          {
            if (DEBUG)
              {
                Log.d(TAG, "Sound played succesfully.");
              }
          }
      }
  }
