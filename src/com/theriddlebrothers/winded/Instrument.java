package com.theriddlebrothers.winded;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.PresetReverb;
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
		//CSharp,
		D,
		//DSharp,
		E,
		F,
		//FSharp,
		G,
		//GSharp,
		A,
		//ASharp,
		B,
		//Sharp,
		//Octave
	}

    private Context appContext;
	private ArrayList<Keys> pressedKeys;
    private AudioTrack audioTrack;
	private double breath = 0;
	private final double MIN_BREATH_LEVEL = 1.0;
	private boolean isPlaying = false;
	private Keys lastKey;
    private HashMap<Keys, byte[]> soundPool;
	
	public Instrument(AudioManager audioManager, Context context) {
		pressedKeys = new ArrayList<Keys>();
        soundPool = new HashMap<Keys, byte[]>();
        appContext = context;

        initAudio();

        /*EnvironmentalReverb  mReverb = new EnvironmentalReverb(0,0);
        mReverb.setEnabled(true);
        audioTrack.attachAuxEffect(mReverb.getId());
        audioTrack.setAuxEffectSendLevel(1.0f);*/

	}

    private void initAudio() {
        // Load audio data
        for (Keys key : Keys.values()) {

            try {
                byte[] byteData;
                int resourceId;

                // @todo - this seems like it could be refactored...
                switch(key) {
                    case C :
                        resourceId = R.raw.c;
                        break;
                    case D :
                        resourceId = R.raw.d;
                        break;
                    case E :
                        resourceId = R.raw.e;
                        break;
                    case F :
                        resourceId = R.raw.f;
                        break;
                    case G :
                        resourceId = R.raw.g;
                        break;
                    case A :
                        resourceId = R.raw.a;
                        break;
                    case B :
                        resourceId = R.raw.b;
                        break;
                    default:
                            throw new Exception("No resource found for key: " + key);
                }

                InputStream audioStream = appContext.getResources().openRawResource(resourceId);
                byteData = readBytes(audioStream);
                soundPool.put(key, byteData);
            } catch(Exception ex) {
                ex.printStackTrace();
                return;
            }
        }

        // Set and push to audio track..
        int intSize = android.media.AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);

        initReverb();
    }

    private void initReverb() {
        // doesn't seem to be working?
        // @see http://stackoverflow.com/questions/10409122/android-mediaplayer-with-audioeffect-getting-error-22-0
        PresetReverb mReverb = new PresetReverb(1, 0);
        mReverb.setPreset(PresetReverb.PRESET_LARGEROOM);
        mReverb.setEnabled(true);
        audioTrack.attachAuxEffect(mReverb.getId());
        audioTrack.setAuxEffectSendLevel(1.0f);
    }

    private void playKey(Keys key) {

        final Keys keyToPlay = key;
        final byte[] keyData = soundPool.get(key);

        Thread audioThread = new Thread(){
            public void run(){

                if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) return; //audioTrack.stop();

                if (audioTrack!=null) {
                    audioTrack.play();
                    // Write the byte array to the track
                    audioTrack.write(keyData, 0, keyData.length);
                    audioTrack.pause();
                }
                else
                    Log.d("TCAudio", "audio track is not initialised ");

            }
        };
        audioThread.start();
    }

    private void stopAudio() {
        // @todo - need to stop, not pause, right?
        audioTrack.pause();
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

	public Keys currentKey() {
		if (pressedKeys.size() <= 0) return null;
		
		return null;
	}

	public boolean isSharpPressed() {
		return false; //return pressedKeys.contains(Keys.Sharp);
	}
	
	public void play(float breath) {
		
		if (pressedKeys.size() == 0 || !this.hasBreath(breath)) {
			if (isPlaying) {
                stopAudio();
            }
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
            audioTrack.setStereoVolume(velocity, velocity);
			return;
		}

        for(int i = 0; i < pressedKeys.size(); i++) {
            Log.d(TAG, "Pressed key: " + pressedKeys.get(i));
        }
		
		lastKey = currentKey;
        isPlaying = true;

		try {
            Log.d(TAG, "Playing new note.");
            // @todo - play new sound
            playKey(currentKey);
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
