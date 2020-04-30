package com.pentagon.pharmavision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int requestPermissionID = 101;
    private CameraSource mCameraSource;
    private SurfaceView mCameraView;
    private TextView mResult, mName, mGenName, mPrice, mUse, mSideEff;
    private Button mClick;
    private String[] Medicines = {"Zaroxolyn", "Medicine2", "Medicine3", "Medicine4", "Medicine5"};
    private String[] Result;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.activity_main_surface_view);
        mResult = findViewById(R.id.activity_main_text_view_result);
        mName = findViewById(R.id.activity_main_text_view_name);
        mGenName = findViewById(R.id.activity_main_text_view_gen_name);
        mPrice = findViewById(R.id.activity_main_text_view_price);
        mUse = findViewById(R.id.activity_main_text_view_use);
        mSideEff = findViewById(R.id.activity_main_text_view_side_effects);
        mClick = findViewById(R.id.activity_main_click);
        startCameraSource();
        mClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getResult();
            }
        });
    }

    private void startCameraSource() {
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()){
            Log.w(TAG, "Detector dependencies not loaded yet");
        }else {
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, requestPermissionID);
                            return;
                        }
                        mCameraSource.start(mCameraView.getHolder());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    mCameraSource.stop();
                }
            });
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0 ) {
                        mResult.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < items.size(); i++) {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append(" ");
                                }
                                mResult.setText(stringBuilder.toString());
                                Result = stringBuilder.toString().split(" ");
                            }
                        });
                    }
                }
            });
        }
    }

    private void getResult(){
        if (Result.length != 0){
            boolean Found = false;
            for (int i=0; i<Result.length; i++){
                for (int j=0; j<Medicines.length; j++){
                    if (Result[i].equals(Medicines[j])){
                        result(Result[i]);
                        Found = true;
                        break;
                    }
                }
            }
            if (!Found){
                Toast.makeText(this, "Result not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void result(String result){
        String text = "";
        mName.setText(result);
        try {
            InputStream is = getAssets().open(result+".txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            text = new String(buffer);
        }catch (Exception e){
            e.printStackTrace();
        }
        String[] medicine = text.split("@");
        if (medicine.length == 5){
            mGenName.setText(medicine[1]);
            mUse.setText(medicine[2]);
            mSideEff.setText(medicine[3]);
            mPrice.setText(medicine[4]);
        }else {
            Toast.makeText(this, "Insufficient data", Toast.LENGTH_SHORT).show();
        }
        //mUse.setText(med[0]);
        //mGenName.setText(med[1]);
        //mPrice.setText(med[2]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID){
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

