package com.example.favplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
ListView listView;
public static SharedPreferences sharedPreferences;
static ArrayList<String> favPlaces = new ArrayList<>();
static ArrayList<LatLng> favPlacesLocation = new ArrayList<>();
  static  ArrayAdapter<String> adapter;
boolean changesMade = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
            sharedPreferences = this.getSharedPreferences("com.example.favplaces",MODE_PRIVATE);

        ArrayList<String> latitues = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();
        favPlaces.clear();
        favPlacesLocation.clear();

        try {
            favPlaces = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("favPlaces",ObjectSerializer.serialize(new ArrayList<>())));
            latitues = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lat",ObjectSerializer.serialize(new ArrayList<>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("long",ObjectSerializer.serialize(new ArrayList<>())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(favPlaces.size()>0 && latitues.size()>0 && longitudes.size()>0) {
            if(favPlaces.size()==latitues.size() && favPlaces.size()==longitudes.size()) {
              for(int i = 0;i<favPlaces.size();i++) {
                  favPlacesLocation.add(new LatLng(Double.parseDouble(latitues.get(i)),Double.parseDouble(longitudes.get(i))));
              }
            }
        } else {
            favPlaces.add("Add a new place...");
            favPlacesLocation.add(new LatLng(0,0));
        }









       adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,favPlaces);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(),mapActivity.class);
            intent.putExtra("favPlace",position);
            startActivity(intent);
            }
        });

listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if(position!=0) {
            favPlacesLocation.remove(position);
            favPlaces.remove(position);
            adapter.notifyDataSetChanged();
            changesMade = true;
        }
        return true;
    }
});


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(changesMade) {
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
                    MainActivity.sharedPreferences.edit().putString("favPlaces",favPlaces).apply();
                    MainActivity.sharedPreferences.edit().putString("lat",latSeriazible).apply();
                    MainActivity.sharedPreferences.edit().putString("long",lotSeriazible).apply();
                }
            }catch (IOException e) {
                e.getMessage();
            }
            changesMade = !changesMade;
        }
    }
}
