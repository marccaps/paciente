package com.project.elisabet.appPaciente;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChatWindow extends AppCompatActivity {

    private MicRecorder micRecorder;
    OutputStream outputStream;
    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streaming_activity);

        RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.content);

        Socket socket = SocketHandler.getSocket();

        try {
            outputStream = socket.getOutputStream();
            Log.e("OUTPUT_SOCKET", "Éxit");
            startService(new Intent(getApplicationContext(), AudioStreamingService.class));

            micRecorder = new MicRecorder();
            t = new Thread(micRecorder);
            if(micRecorder != null) {
                MicRecorder.keepRecording = true;
            }
            t.start();
            launchingDialog();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(micRecorder != null) {
            MicRecorder.keepRecording = false;
        }
    }

    private void launchingDialog() {
        SweetAlertDialog pDialog = new SweetAlertDialog(ChatWindow.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitleText("Reproduint en temps real...");
        pDialog.setCancelable(false);
        pDialog.setCancelButton("Sortir de l'aplicació", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                finish();
                System.exit(0);
            }
        });
        pDialog.show();
    }
}
