package com.theriddlebrothers.winded;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.PresetReverb;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        initAudio();
    }

    private void initAudio() {

            /*
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
      }      */

            // Set and push to audio track..
            int intSize = android.media.AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);

            initReverb();
        }


    private void stopAudio() {
        // @todo - need to stop, not pause, right?
        /*if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
        } */
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 512;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public void setVolume(float vol) {
        //audioTrack.setStereoVolume(vol, vol);
    }

    public void stop() {
        // pause and flush for an immediate stop
        //audioTrack.pause();
        //audioTrack.flush();
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

                InputStream fin = context.getResources().openRawResource(res);
                DataInputStream dis = new DataInputStream(fin);

                // Skip WAV header
                try
                {
                    if (dis.available() > 44)
                        dis.skipBytes(44);
                } catch (IOException e) {

                }

                if (audioTrack!=null) {
                    try {
                         /*
                        switch (audioTrack.getPlayState()) {
                            case AudioTrack.PLAYSTATE_PAUSED:
                                SoundManager.this.stop();
                                //audioTrack.reloadStaticData();
                                audioTrack.play();
                                break;
                            case AudioTrack.PLAYSTATE_PLAYING:
                                SoundManager.this.stop();
                                //audioTrack.reloadStaticData();
                                audioTrack.play();
                                break;
                            case AudioTrack.PLAYSTATE_STOPPED:
                                //audioTrack.reloadStaticData();
                                audioTrack.play();
                                break;
                        } */

                        audioTrack.play();

                        // Write the byte array to the track
                        int i = 0;
                        byte[] s = new byte[bufferSize];
                        while((i = dis.read(s, 0, bufferSize)) > -1){
                            audioTrack.write(s, 0, i);
                        }
                        audioTrack.stop();
                        //audioTrack.setPlaybackHeadPosition(0);
                        //audioTrack.release();
                        dis.close();
                        fin.close();
                    }   catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else
                    Log.d("TCAudio", "audio track is not initialised ");

            }
        };
        audioThread.start();
    }

}
