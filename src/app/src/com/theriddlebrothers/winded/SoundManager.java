package com.theriddlebrothers.winded;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.PresetReverb;
import android.util.Log;

import java.io.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Joshua Riddle
 * Date: 10/5/12
 * Time: 12:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class SoundManager {

    private AudioTrack audioTrack;

    private Context context;

    public SoundManager(Context context) {
        this.context = context;
    }

    public void setVolume(float vol) {
        audioTrack.setStereoVolume(vol, vol);
    }

    public void stop() {
        // pause and flush for an immediate stop
        audioTrack.stop();
        audioTrack.release();
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
        final int bufferSize = 512;

        Thread audioThread = new Thread(){
            public void run(){

                // define the buffer size for audio track
                int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                int bufferSize = 512;
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
                //String filepath = "File Path";

                int count = 0;
                byte[] data = new byte[bufferSize];
                try {
                    InputStream fileInputStream = context.getResources().openRawResource(res);  // new FileInputStream(filepath);
                    DataInputStream dataInputStream = new DataInputStream(fileInputStream);

                    // skip wav header
                    if (dataInputStream.available() > 44)
                        dataInputStream.skipBytes(44);

                    audioTrack.play();

                    while((count = dataInputStream.read(data, 0, bufferSize)) > -1){
                        audioTrack.write(data, 0, count);
                    }

                    if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                        audioTrack.pause();
                        audioTrack.flush();
                        audioTrack.release();
                    }
                    dataInputStream.close();
                    fileInputStream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        audioThread.start();
    }

}
