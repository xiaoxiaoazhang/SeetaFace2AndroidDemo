package com.chihun.learn.seetafacedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static int PERMISSION_REQ = 0x123456;

    private String[] mPermission = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW
    };

    private List<String> mRequestPermission = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            for (String one : mPermission) {
                if (PackageManager.PERMISSION_GRANTED != this.checkPermission(one, Process.myPid(), Process.myUid())) {
                    mRequestPermission.add(one);
                }
            }
            if (!mRequestPermission.isEmpty()) {
                this.requestPermissions(mRequestPermission.toArray(new String[mRequestPermission.size()]), PERMISSION_REQ);
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.detect:
                if (mRequestPermission.isEmpty()) {
                    startActivity(new Intent(this, FaceDetectorActivity.class));
                } else {
                    Toast.makeText(this, "PERMISSION DENIED!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.recognize:
                if (mRequestPermission.isEmpty()) {
                    startActivity(new Intent(this, FaceRecognizerActivity.class));
                } else {
                    Toast.makeText(this, "PERMISSION DENIED!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        // 版本兼容
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return;
        }
        if (requestCode == PERMISSION_REQ) {
            for (int i = 0; i < grantResults.length; i++) {
                for (String one : mPermission) {
                    if (permissions[i].equals(one) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        mRequestPermission.remove(one);
                    }
                }
            }
        }
    }
}
