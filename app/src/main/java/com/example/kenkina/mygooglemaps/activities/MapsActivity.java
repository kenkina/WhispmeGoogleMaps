package com.example.kenkina.mygooglemaps.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.kenkina.mygooglemaps.adapters.WhispAdapter;
import com.example.kenkina.mygooglemaps.fragments.WhispListDialogFragment;
import com.example.kenkina.mygooglemaps.services.LocationService;
import com.example.kenkina.mygooglemaps.R;
import com.example.kenkina.mygooglemaps.utils.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import cz.intik.overflowindicator.OverflowPagerIndicator;
import cz.intik.overflowindicator.SimpleSnapHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Context mContext = this;
    private GoogleMap mMap;
    private Marker mMarker;
    private BroadcastReceiver mBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        if (runtimePermissions()) {
            startLocationService();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showWhispsDialog();
                return false;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        double latitude = intent.getExtras().getDouble(Constants.Extra.LATITUDE);
                        double longitude = intent.getExtras().getDouble(Constants.Extra.LONGITUDE);
                        LatLng position = new LatLng(latitude, longitude);

                        if (mMarker != null)
                            mMarker.remove();
                        mMarker = mMap.addMarker(new MarkerOptions().position(position).title(null));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));

                        Toast.makeText(context, "(" + latitude + ", " + longitude + ")", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "(SIN GPS)", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }
        registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.Action.LOCATION_UPDATE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        stopLocationService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.RequestCode.LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                runtimePermissions();
            }
        }
    }


    private void startLocationService() {
        startService(new Intent(mContext, LocationService.class));
    }

    private void stopLocationService() {
        stopService(new Intent(mContext, LocationService.class));
    }

    private boolean runtimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.RequestCode.LOCATION);
                return false;
            }
        }

        return true;
    }

    public void showWhispsDialog() {
        FragmentManager fm = getSupportFragmentManager();
        WhispListDialogFragment whispListDialogFragment = new WhispListDialogFragment();
        whispListDialogFragment.show(fm, "dialog_whisps_detail");
    }
}
