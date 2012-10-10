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

    private ArrayList<Keys> pressedKeys;
    private final double MIN_BREATH_LEVEL = 1.0;
    private boolean isPlaying = false;
    private Keys lastKey;
    HashMap<Keys, Track> soundPool;
    private Keys currentKey;

    public Instrument(AudioManager audioManager, Context context) {
        pressedKeys = new ArrayList<Keys>();
        soundPool = new HashMap<Keys, Track>();

        soundPool.put(Keys.C, new Track(context, R.raw.c));
        soundPool.put(Keys.D, new Track(context, R.raw.d));
        soundPool.put(Keys.E, new Track(context, R.raw.e));
        soundPool.put(Keys.F, new Track(context, R.raw.f));
        soundPool.put(Keys.G, new Track(context, R.raw.g));
        soundPool.put(Keys.A, new Track(context, R.raw.a));
        soundPool.put(Keys.B, new Track(context, R.raw.b));
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
            if (currentKey != null) soundPool.get(currentKey).stopPlaying();
            isPlaying = false;
            return;
        }

        // Get highest pressed key
        if (isPressed(Keys.B)) currentKey = Keys.B;
        else if (isPressed(Keys.A)) currentKey = Keys.A;
        else if (isPressed(Keys.G)) currentKey = Keys.G;
        else if (isPressed(Keys.F)) currentKey = Keys.F;
        else if (isPressed(Keys.E)) currentKey = Keys.E;
        else if (isPressed(Keys.D)) currentKey = Keys.D;
        else if (isPressed(Keys.C)) currentKey = Keys.C;

        // get velocity based on breath level which will range from 0 to 12.x
        float velocity = breath / 10;

        // velocity must be 0.1 to 0.9. A velocity of 1.0 will mute.
        if (velocity > 0.9) velocity = 0.9f;

        // Don't re-play same sound, just set volume
        if (lastKey == currentKey && isPlaying) {
            soundPool.get(currentKey).setVolume(velocity);
            return;
        }

        for(int i = 0; i < pressedKeys.size(); i++) {
            Log.d(TAG, "Pressed key: " + pressedKeys.get(i));
        }

        if (lastKey != null) soundPool.get(lastKey).stopPlaying();

        lastKey = currentKey;

        int resource = 0;

        try {
            soundPool.get(currentKey).play(resource);
            isPlaying = true;
            Log.d(TAG, "Playing track now...");
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

    public boolean isPressed(Keys key) {
        return pressedKeys.contains(key);
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
