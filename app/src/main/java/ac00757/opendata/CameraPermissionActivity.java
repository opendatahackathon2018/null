package ac00757.explorar;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CameraPermissionActivity extends AppCompatActivity {

    private Button cameraNext;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 98;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        setContentView(R.layout.explain_permissions_camera);

        cameraNext = (Button)findViewById(R.id.camera_next);
        cameraNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(CameraPermissionActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA );
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {

                // If the permission request is cancelled then the returned array is empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted - launch the screen that requests the next permission
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Intent launchStoragePermission = new Intent(CameraPermissionActivity.this,StoragePermissionActivity.class);
                        startActivity(launchStoragePermission);
                    }

                } else {
                    // Permission was denied.
                    Toast.makeText(this, "Permission Denied - Essential for core application functionality", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }

    }



    @Override
    public void finish() {
        super.finish();
        //keep current activity in bg and drag previous from top
        overridePendingTransition(R.anim.stay, R.anim.slide_in_left);
    }
}
