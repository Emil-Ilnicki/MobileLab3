package com.example.lab3;

import androidx.fragment.app.FragmentActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker mMarker;
    private TextView weatherDisplay;
    JSONTask apiCall = new JSONTask();
    HttpURLConnection conn = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        weatherDisplay = (TextView) findViewById(R.id.weatherText);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mMarker != null) {
                    mMarker.remove();
                }
                mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                String longitude = String.valueOf(mMarker.getPosition().latitude);
                String latitude = String.valueOf(mMarker.getPosition().longitude);
                String APIKey = "c2d254e876b226f6171c934a9b28b5e9";
                String API_URL = "https://api.openweathermap.org/data/2.5/weather?lat="+longitude+"&lon="+latitude+"&appid="+APIKey;
                System.out.println(API_URL);
                new JSONTask().execute(API_URL);

            }
        });
    }

    class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000); //5 sec.
                conn.connect();
                if (conn.getResponseCode()== HttpURLConnection.HTTP_OK){

                    InputStream stream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader( new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line= "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    return buffer.toString();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }finally {
                conn.disconnect();
            }

            return null;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
            String jsonString = result;
            System.out.println(jsonString);
            try {
                JSONObject obj = new JSONObject(jsonString);

                JSONArray arr = obj.getJSONArray("weather");
                JSONObject weatherObj = arr.getJSONObject(0);
                String decsription = weatherObj.getString("description");


                JSONObject windObj = obj.getJSONObject("wind");
                String windSpeed = windObj.getString("speed");
                String windDeg = windObj.getString("deg");

                JSONObject mainObj = obj.getJSONObject("main");
                double tempKelvin = Double.parseDouble(mainObj.getString("temp"));
                String tempCel = String.valueOf(tempKelvin-273.15);
                double tempFeelsLikeKelvin = Double.parseDouble(mainObj.getString("feels_like"));
                String tempFeelsLike = String.valueOf(tempFeelsLikeKelvin-273.15);

                System.out.println(
                        "Description: " + decsription + "\n" +
                        "Wind speed: " + windSpeed + "\n" +
                        "Wind degrees: " + windDeg + "\n" +
                        "Temp: " + tempCel + "\n" +
                        "Feels Like: " + tempFeelsLike);

                weatherDisplay.setText("Description: " + decsription + "\n" +
                        "Wind speed: " + windSpeed + "\n" +
                        "Wind degrees: " + windDeg + "\n" +
                        "Temp: " + tempCel + "\n" +
                        "Feels Like: " + tempFeelsLike);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
