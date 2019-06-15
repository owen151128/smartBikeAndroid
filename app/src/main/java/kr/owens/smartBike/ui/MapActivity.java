package kr.owens.smartBike.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import kr.owens.smartBike.R;
import kr.owens.smartBike.util.ToastUtil;

public class MapActivity extends AppCompatActivity {
    private final int REQUEST_GPS = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ToastUtil.show(getString(R.string.gps_permission_request_message));
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_GPS);
                ToastUtil.show(getString(R.string.gps_permission_request_message));
            }
        } else {
            initializeMapView();
        }
    }

    public void initializeMapView() {
        MapView mapView = new MapView(this);

        ViewGroup mapViewContainer = findViewById(R.id.map_layout);
        mapViewContainer.addView(mapView);
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633), true);
        mapView.setZoomLevel(7, true);
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(33.41, 126.52), 9, true);
        mapView.zoomIn(true);
        mapView.zoomOut(true);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
        mapView.setCustomCurrentLocationMarkerTrackingImage(R.drawable.gps_currentlocation, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_GPS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMapView();
            } else {
                ToastUtil.show(getString(R.string.gps_permission_request_message));
            }
        }
    }
}
