package com.example.favplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class mapActivity extends FragmentActivity implements OnMapReadyCallback {
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm yyyyMM/dd");
    private int locationRequestCode = 1000;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String address = "";


                Geocoder geocoder = new Geocoder(getApplicationContext(),Locale.getDefault());

                try {
                    List<Address> loc = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);

                    if(loc!=null && loc.size()>0) {
                        if (loc.get(0).getAddressLine(0) != null) {
                            address = loc.get(0).getAddressLine(0);
                            address =  address.replaceAll("Unnamed Road, ","");

                        } }else {
                        Date date = new Date();
                        address = formatter.format(date);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }






                MainActivity.favPlacesLocation.add(latLng);
                MainActivity.favPlaces.add(address);
                MainActivity.adapter.notifyDataSetChanged();
                mMap.addMarker(new MarkerOptions().position(latLng).title(address));

                Toast.makeText(getApplicationContext(), "Location Saved!", Toast.LENGTH_SHORT).show();

            }
        });

        Intent intent = getIntent();
        int position = intent.getIntExtra("favPlace",0);

        if(position==0) {
            if(MainActivity.favPlaces.size()<2) {
                mMap.clear();
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    Log.i("getLastLocation: ", "You are here!");
                                    centerLocation(location, "You are here");
                                }
                            }
                        });
            } else {
                mMap.clear();
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),15));
                                }
                            }
                        });
                for (int i=1;i<MainActivity.favPlaces.size();i++) {
                    mMap.addMarker(new MarkerOptions().position(MainActivity.favPlacesLocation.get(i)).title(MainActivity.favPlaces.get(i)));
                }
            }

        } else {
            LatLng loc = MainActivity.favPlacesLocation.get(position);
            String title = MainActivity.favPlaces.get(position);
            centerLocation(loc,title);
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Log.i("onRequest: ", "You are here!");
                            centerLocation(location,"You are here");
                        }
                    });
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    public void centerLocation(Location location, String title) {
        LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(currentLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
    }

    public void centerLocation(LatLng location, String title) {
        mMap.addMarker(new MarkerOptions().position(location).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
    }

    @Override
    protected void onPause() {
        super.onPause();
        ArrayList<String> latitues = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();
        for(LatLng coord: MainActivity.favPlacesLocation) {
            latitues.add(Double.toString(coord.latitude));
            longitudes.add(Double.toString(coord.longitude));
        }
        try {
            String latSeriazible = ObjectSerializer.serialize(latitues);
            String lotSeriazible = ObjectSerializer.serialize(longitudes);
         String favPlaces =   ObjectSerializer.serialize(MainActivity.favPlaces);
         if(MainActivity.sharedPreferences!=null) {
             Log.i("onPause: ", "We are here so it means it should work!");
             MainActivity.sharedPreferences.edit().putString("favPlaces",favPlaces).apply();
        MainActivity.sharedPreferences.edit().putString("lat",latSeriazible).apply();
             MainActivity.sharedPreferences.edit().putString("long",lotSeriazible).apply();
         }
        }catch (IOException e) {
            e.getMessage();
        }
    }
}
