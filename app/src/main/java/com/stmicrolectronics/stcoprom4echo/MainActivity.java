package com.stmicrolectronics.stcoprom4echo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String FW_NAME = "OpenAMP_TTY_echo.elf";

    private EditText mCommandView;
    private TextView mReceivedView;
    private Button mSendCommandButton;

    private final BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (FirmwareService.ACTION_FW_STATUS.equals(action)) {
                String status = intent.getStringExtra(FirmwareService.EXTRA_FW_STATUS);
                if (FirmwareService.FW_STARTED.equals(status)) {
                    mSendCommandButton.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(),"Firmware started successfully",Toast.LENGTH_LONG).show();
                } else if (FirmwareService.FW_STOPPED.equals(status)) {
                    mSendCommandButton.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),"Firmware stopped",Toast.LENGTH_LONG).show();
                } else if (FirmwareService.FW_ERROR.equals(status)) {
                    mSendCommandButton.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),"Firmware start error",Toast.LENGTH_LONG).show();
                }
            } else if (FirmwareService.ACTION_UPDATE.equals(action)) {
                mReceivedView.append(intent.getStringExtra(FirmwareService.EXTRA_UPDATE));
                mReceivedView.append("\n");
                mSendCommandButton.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCommandView = findViewById(R.id.command);
        mReceivedView = findViewById(R.id.received_text);
        mSendCommandButton = findViewById(R.id.button_send);

        IntentFilter filter = new IntentFilter();
        filter.addAction(FirmwareService.ACTION_FW_STATUS);
        filter.addAction(FirmwareService.ACTION_UPDATE);
        registerReceiver(mainReceiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(MainActivity.this, FirmwareService.class);
        intent.setAction(FirmwareService.ACTION_START);
        intent.putExtra(FirmwareService.EXTRA_FW_NAME, FW_NAME);
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(MainActivity.this, FirmwareService.class);
        intent.setAction(FirmwareService.ACTION_STOP);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mainReceiver);
    }

    public void sendCommand(View view) {
        mSendCommandButton.setVisibility(View.INVISIBLE);
        String command = mCommandView.getText().toString();
        mCommandView.getText().clear();
        if (command.length() > 0) {
            Intent intent = new Intent(MainActivity.this, FirmwareService.class);
            intent.setAction(FirmwareService.ACTION_SEND_COMMAND);
            intent.putExtra(FirmwareService.EXTRA_FW_COMMAND, command);
            startService(intent);
        } else {
            Toast.makeText(getApplicationContext(),"No command to send, please enter text",Toast.LENGTH_SHORT).show();
            mSendCommandButton.setVisibility(View.VISIBLE);
        }
    }

    public void resetText(View view) {
        mReceivedView.setText(null);
    }
}
