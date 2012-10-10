package com.theriddlebrothers.winded;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.PresetReverb;
import android.nfc.Tag;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Joshua Riddle
 * Date: 10/5/12
 * Time: 12:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class Track {

    private AudioTrack audioTrack;
    private Context context;
    byte[] soundData;

    private String TAG = "Track";

    public Track(Context context, int resourceId) {
        this.context = context;

        try {

            final int bufferSize = 512;
            InputStream fileInputStream = context.getResources().openRawResource(resourceId);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            // skip wav header
            if (dataInputStream.available() > 44)
                dataInputStream.skipBytes(44);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int count = 0;
            byte[] data = new byte[bufferSize];
            while((count = dataInputStream.read(data, 0, bufferSize)) > -1){
                buffer.write(data, 0, count);
            }

            soundData = buffer.toByteArray();

            dataInputStream.close();
            fileInputStream.close();

        }catch(Exception ex)
        {
            Log.d(TAG, "Unable to read audio track: " + ex.getMessage());
        }

    }

    public void setVolume(float vol) {
        if (audioTrack != null) {
            try {
                audioTrack.setStereoVolume(vol, vol);
            } catch(Exception ex) {
                Log.d(TAG, "Unable to set volume of audio track: " + ex.getMessage());
            }
        }
    }

    public void stopPlaying() {
        if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            try {
                audioTrack.stop();
                audioTrack.release();
            } catch(Exception ex) {
                Log.d(TAG, "Unable to stop playing: " + ex.getMessage());
            }
        }
    }

    private void initReverb() {

        /*EnvironmentalReverb  mReverb = new EnvironmentalReverb(0,0);
        mReverb.setEnabled(true);
        audioTrack.attachAuxEffect(mReverb.getId());
        audioTrack.setAuxEffectSendLevel(1.0f);*/


        // doesn't seem to be working?
        // @see http://stackoverflow.com/questions/10409122/android-mediaplayer-with-audioeffect-getting-error-22-0
        /*PresetReverb mReverb = new PresetReverb(1, 0);
        mReverb.setPreset(PresetReverb.PRESET_LARGEROOM);
        mReverb.setEnabled(true);
        audioTrack.attachAuxEffect(mReverb.getId());
        audioTrack.setAuxEffectSendLevel(0.9f);*/
    }

    public void play(int resourceId) {

        final int res = resourceId;

        Thread audioThread = new Thread(){
            public void run(){

                try {
                    stopPlaying();

                    // define the buffer size for audio track
                    int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);

                    audioTrack.play();
                    audioTrack.write(soundData, 0, soundData.length);
                    stopPlaying();
                } catch(Exception ex) {
                    Log.d(TAG, "Unable to play track: " + ex.getMessage());
                }
            }
        };
        audioThread.start();
    }

}
