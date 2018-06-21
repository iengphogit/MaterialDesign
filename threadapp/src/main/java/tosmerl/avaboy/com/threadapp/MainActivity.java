package tosmerl.avaboy.com.threadapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    int cc = 1;

    private static final int CAMERA_REQUEST = 50;
    private boolean flashLightStatus = false;


    private Camera mcamera;
    private boolean FlashOn;
    private boolean Flash;
    private Camera.Parameters fparams;
    private MediaPlayer mp3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final boolean hasCameraFlash = getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        boolean isEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        Button btnOne = findViewById(R.id.btnOne);

        btnOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);

                if (hasCameraFlash) {
                    if (flashLightStatus)
                        flashLightOff();
                    else
                        flashLightOn();
                } else {
                    Toast.makeText(MainActivity.this, "No flash available on your device",
                            Toast.LENGTH_SHORT).show();
                }

                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d("my_","run()");
                        handler.postDelayed(this,3000);
                        if(cc >= 5)
                            handler.removeCallbacks(this);
                        cc++;

                    }
                };

                //handler.postDelayed(runnable, 0);

            }
        });
    }


    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, true);
            }
            flashLightStatus = true;
        } catch (CameraAccessException e) {
        }
    }


    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, false);
            }
            flashLightStatus = false;
        } catch (CameraAccessException e) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case CAMERA_REQUEST :
                if (grantResults.length > 0  &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied for the Camera",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void getCamera() {
        if (mcamera == null) {
            try {
                mcamera = Camera.open();
                fparams = mcamera.getParameters();
            } catch (RuntimeException e) {
                Log.e("Camera Error. Failed ", e.getMessage());
            }
        }
    }


    private void turnOnFlashOLD() {
        if (!FlashOn) {
            if (mcamera == null || fparams == null) {
                return;
            }

            fparams = mcamera.getParameters();
            fparams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mcamera.setParameters(fparams);
            mcamera.startPreview();
            FlashOn = true;

        }

    }

    private void turnOffFlashOLD() {
        if (FlashOn) {
            if (mcamera == null || fparams == null) {
                return;
            }

            fparams = mcamera.getParameters();
            fparams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mcamera.setParameters(fparams);
            mcamera.stopPreview();
            FlashOn = false;
        }
    }
}
