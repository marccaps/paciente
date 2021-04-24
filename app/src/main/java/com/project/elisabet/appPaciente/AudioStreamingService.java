package com.project.elisabet.appPaciente;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class AudioStreamingService extends Service {
    private static final int SAMPLE_RATE = 16000;
    public boolean keepPlaying = true;
    private AudioTrack audioTrack;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException(getString(R.string.no_implementat));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startStreaming();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        keepPlaying = false;
        if(audioTrack != null)
            audioTrack.release();
    }



    public void startStreaming() {
        Runnable audioPlayerRunnable = new Runnable() {
            @Override
            public void run() {
                int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }

                Log.d(getString(R.string.play), "buffersize = "+bufferSize);

                 audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM);

                audioTrack.play();

                Log.v(getString(R.string.play), getString(R.string.streaming_comença));

                byte[] buffer = new byte[bufferSize];
                int offset = 0;

                try {
                    InputStream inputStream = SocketHandler.getSocket().getInputStream();
                    int bytes_read = inputStream.read(buffer, 0, bufferSize);

                    while(keepPlaying && (bytes_read != -1)) {
                        audioTrack.write(buffer, 0,buffer.length);
                        bytes_read = inputStream.read(buffer, 0, bufferSize);
                    }
                    inputStream.close();
                    audioTrack.release();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t = new Thread(audioPlayerRunnable);
        t.start();
    }
}
