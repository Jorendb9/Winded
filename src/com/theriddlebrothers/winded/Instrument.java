package com.theriddlebrothers.winded;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.PresetReverb;
import android.nfc.Tag;
import android.provider.ContactsContract;
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

    SoundManager soundManager;
    private ArrayList<Keys> pressedKeys;
    private final double MIN_BREATH_LEVEL = 1.0;
    private boolean isPlaying = false;
    private Keys lastKey;
    private HashMap<Keys, byte[]> soundPool;

    public Instrument(AudioManager audioManager, Context context) {
        pressedKeys = new ArrayList<Keys>();
        soundPool = new HashMap<Keys, byte[]>();
        soundManager = new SoundManager(context);
    }


    private void playKey(Keys key) {
        // @todo - make this dynamic
        soundManager.play(R.raw.c);
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
            soundManager.stop();
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
            soundManager.setVolume(velocity);
            return;
        }

        for(int i = 0; i < pressedKeys.size(); i++) {
            Log.d(TAG, "Pressed key: " + pressedKeys.get(i));
        }

        lastKey = currentKey;
        isPlaying = true;

        try {
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
