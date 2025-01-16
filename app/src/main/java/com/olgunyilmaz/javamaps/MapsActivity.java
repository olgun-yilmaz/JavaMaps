package com.olgunyilmaz.javamaps;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.olgunyilmaz.javamaps.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher <String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //casting
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                System.out.println("location : "+location.toString());
            }

        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            // request permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.getRoot(),"Permission needed for maps",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // requests permission
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

                            }
                        }).show();

            }else{
                // requests permission
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }

        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,
                    100,locationListener);



    }


        goToLocation(40.69888446616988, 36.47947100999238,"Degirmenli Municipality",17);


    }

    private void goToLocation(double latitude, double longitude, String title, int zoom){
        //37.421998,-122.084000 --> google current loc
        //40.69888446616988, 36.47947100999238 --> degirmenli municipality

        LatLng location = new LatLng(latitude,longitude);
        mMap.addMarker(new MarkerOptions().position(location).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,zoom));
        // zoom -> range (0,25)
    }

    private void registerLauncher(){
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){ //permission granted

                    if(ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000, 100,locationListener);

                    }




                }else{
                    // permission denied
                    Toast.makeText(MapsActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}