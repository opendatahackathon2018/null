package ac00757.explorar;

import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import com.wikitude.architect.ArchitectView;

import ac00757.explorar.utilities.LocationProvider;

public class GeoARActivity extends ARActivity implements LocationListener {

    private LocationProvider locationProvider;


    private final LocationProvider.ErrorCallback errorCallback = new LocationProvider.ErrorCallback() {
        @Override
        public void noProvidersEnabled() {
            Toast.makeText(GeoARActivity.this, "No Location Provider Available", Toast.LENGTH_LONG).show();
        }
    };



    private final ArchitectView.SensorAccuracyChangeListener sensorAccuracyChangeListener = new ArchitectView.SensorAccuracyChangeListener() {
        @Override
        public void onCompassAccuracyChanged(int accuracy) {
            if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) { // UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3
                Toast.makeText(GeoARActivity.this, "Accuracy low", Toast.LENGTH_LONG ).show();
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationProvider = new LocationProvider(this, this, errorCallback);

        }


    @Override
    public void onLocationChanged(Location location) {
        float accuracy = location.hasAccuracy() ? location.getAccuracy() : 1000;

        if (location.hasAltitude()) {
            architectView.setLocation(location.getLatitude(), location.getLongitude(), location.getAltitude(), accuracy);
        } else {
            architectView.setLocation(location.getLatitude(), location.getLongitude(), accuracy);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        locationProvider.onResume();
        architectView.registerSensorAccuracyChangeListener(sensorAccuracyChangeListener);
    }

    @Override
    protected void onPause() {
        locationProvider.onPause();
        super.onPause();
        architectView.unregisterSensorAccuracyChangeListener(sensorAccuracyChangeListener);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
