package com.theriddlebrothers.winded;

import java.util.ArrayList;
import java.util.HashMap;

import android.nfc.Tag;
import android.util.Log;
import com.theriddlebrothers.winded.Instrument.Keys;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class Instrument {

    private final String TAG = "Instrument";

	private int register = 4;
		
	public enum Keys {
		C,
		CSharp,
		D,
		DSharp,
		E,
		F,
		FSharp,
		G,
		GSharp,
		A,
		ASharp,
		B,
		Sharp,
		Octave
	}

	private ArrayList<Keys> pressedKeys;
	private SoundPool mSoundPool;
	private AudioManager  mAudioManager;
	private HashMap<Keys, Integer> mSoundPoolMap;
	private int mStream = 0;
	private double breath = 0;
	private final double MIN_BREATH_LEVEL = 1.0;
	private boolean isPlaying = false;
	private Keys lastKey;
	
	public Instrument(AudioManager audioManager, Context context) {
		pressedKeys = new ArrayList<Keys>();
		
		// Init audio player
        mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        mAudioManager = audioManager;
        mSoundPoolMap = new HashMap<Keys, Integer>();
        
        // Load sounds - currently only one octave.
        mSoundPoolMap.put(Keys.C, mSoundPool.load(context, R.raw.c, 1));
        mSoundPoolMap.put(Keys.D, mSoundPool.load(context, R.raw.d, 1));
        mSoundPoolMap.put(Keys.E, mSoundPool.load(context, R.raw.e, 1));
        mSoundPoolMap.put(Keys.F, mSoundPool.load(context, R.raw.f, 1));
        mSoundPoolMap.put(Keys.G, mSoundPool.load(context, R.raw.g, 1));
        mSoundPoolMap.put(Keys.A, mSoundPool.load(context, R.raw.a, 1));
        mSoundPoolMap.put(Keys.B, mSoundPool.load(context, R.raw.b, 1));
	}
	
	public Keys currentKey() {
		if (pressedKeys.size() <= 0) return null;
		
		return null;
	}

	public boolean isSharpPressed() {
		return pressedKeys.contains(Keys.Sharp);
	}
	
	public void play(float breath) {
		
		if (pressedKeys.size() == 0 || !this.hasBreath(breath)) {
			if (isPlaying) mSoundPool.stop(mStream);
            isPlaying = false;
			return;
		}

		// Get most recently pressed key
		Keys currentKey = pressedKeys.get(pressedKeys.size() - 1);

		
		// If sharp key is pressed
		if (isSharpPressed()) {
			switch (currentKey) {
				case C:
				case D:
				case F:
				case G:
				case A:
					currentKey = Keys.values()[currentKey.ordinal() + 1];
					break;
			}
		}
		
		// get velocity based on breath level which will range from 0 to 12.x
		float velocity = breath / 10;

		// velocity must be 0.1 to 0.9. A velocity of 1.0 will mute.
		if (velocity > 0.9) velocity = 0.9f;
		
		// Don't re-play same sound, just set volume
		if (lastKey == currentKey && isPlaying) {
			mSoundPool.setVolume(mStream, velocity, velocity);
			return;
		}

        for(int i = 0; i < pressedKeys.size(); i++) {
            Log.d(TAG, "Pressed key: " + pressedKeys.get(i));
        }
		
		lastKey = currentKey;
        isPlaying = true;

		try {
            Log.d(TAG, "Playing new note.");
            mSoundPool.stop(mStream);
			mStream = mSoundPool.play(mSoundPoolMap.get(currentKey), velocity, velocity, 0, 0, 1.0f);
		} catch(Exception ex) {
            Log.d(TAG, ex.getMessage());
		}

	}

	/**
	 * User has pressed a key on the instrument.
	 * @param key
	 */
	public void pressKey(Keys key) {
		
		if (pressedKeys.contains(key)) return;
		
		pressedKeys.add(key);
	}

	/**
	 * User has released a key on the instrument.
	 * @param key
	 */
	public void releaseKey(Keys key) {
		if (!pressedKeys.contains(key)) return;
		
		pressedKeys.remove(key);
	}
	
	/**
	 * Specifies if user is blowing into instrument
	 * @return
	 */
	public boolean hasBreath(double breath) {
		return (breath >= MIN_BREATH_LEVEL);
	}
	
}
