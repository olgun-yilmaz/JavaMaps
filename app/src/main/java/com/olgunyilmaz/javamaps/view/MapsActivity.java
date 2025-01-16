package com.olgunyilmaz.javamaps.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.olgunyilmaz.javamaps.R;
import com.olgunyilmaz.javamaps.databinding.ActivityMapsBinding;
import com.olgunyilmaz.javamaps.model.Place;
import com.olgunyilmaz.javamaps.roomdb.PlaceDao;
import com.olgunyilmaz.javamaps.roomdb.PlaceDatabase;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher <String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean isFirstTime;
    PlaceDatabase db;
    PlaceDao placeDao;

    Double selectedLatitude = 0.0;
    Double selectedLongitude = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = MapsActivity.this.getSharedPreferences("com.olgunyilmaz.javamaps",MODE_PRIVATE);
        isFirstTime = sharedPreferences.getBoolean("isFirstTime",true);

        registerLauncher();

        db = Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").build();
        placeDao = db.placeDao();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(MapsActivity.this); //activity implements listener

        binding.saveButton.setEnabled(false);

        //casting
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                if (isFirstTime){ // calling just get started.
                    goToLocation(location.getLatitude(),location.getLongitude(),"current location",15);
                    sharedPreferences.edit().putBoolean("isFirstTime",false).apply();
                }



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

            goLastLocation();

    }


        //goToLocation(40.69888446616988, 36.47947100999238,"Degirmenli Municipality",17);


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
                        goLastLocation();
                    }




                }else{
                    // permission denied
                    Toast.makeText(MapsActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @SuppressLint("MissingPermission")
    private void goLastLocation(){
        Location lastLocation =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (lastLocation != null){ // default : last location onCreate
            goToLocation(lastLocation.getLatitude(),lastLocation.getLongitude(),"last location",17);
        }

        mMap.setMyLocationEnabled(true); // check my fine location
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear(); // only one marker is displayed at a time
        mMap.addMarker(new MarkerOptions().position(latLng));
        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.saveButton.setEnabled(true);

    }
    public void save(View view){
        Place place = new Place(binding.placeText.getText().toString(),selectedLatitude,selectedLongitude);
        placeDao.insert(place);

    }
    public void delete(View view){

    }

}