package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {
    TextView infoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        setTitle("Weather page");

        Intent intent = getIntent();
        int itemIndex = intent.getIntExtra("placeNumber", 0);

        String address = MainActivity.places.get(itemIndex);
        Log.i("address", address);

        infoTextView = findViewById(R.id.info);
        if (itemIndex >= 0) {
            LatLng curLocation = MainActivity.locations.get(itemIndex);

            double lat = curLocation.latitude;
            double lng = curLocation.longitude;


//            getCity(lat, lng);

            getWeather(lat, lng);

        } else {
            TextView titleTextView = findViewById(R.id.title);
            titleTextView.setText("Could not find the place :(");
        }

    }

    public void getCity(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String adminArea = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
            if (addressList != null && addressList.size() > 0) {
                adminArea = addressList.get(0).getLocality();

                TextView titleTextView = findViewById(R.id.title);
                titleTextView.setText(adminArea);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getWeather(double lat, double lng) {
        try {
            DownloadTask task = new DownloadTask();

            task.execute("https://openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lng +"&appid=439d4b804bc8187953eb36d2a8c26a02");
//                task.onPostExecute("https://samples.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=439d4b804bc8187953eb36d2a8c26a02");
        } catch (Exception e) {
            e.printStackTrace();
            infoTextView.setText("Could not find weather :( Please try again");
        }
    }

    public class DownloadTask extends AsyncTask<String,Void,String> {

        @SuppressLint({"SetTextI18n", "WrongThread"})
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                infoTextView.setText("Could not find weather :( Please try again");
                return null;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);

                String weatherInfo = jsonObject.getString("weather");
                String mainInfo = jsonObject.getString("main");
                String name = jsonObject.getString("name");
                String cod = jsonObject.getString("cod");
                TextView title = findViewById(R.id.title);
                title.setText(name + "");
                Log.i("name:------", name);
                Log.i("cod:------", cod);
                JSONArray weatherArr = new JSONArray(weatherInfo);

                String weatherInfoMessage = "";
                String tempInfoMessage = "";

                for (int i = 0; i < weatherArr.length(); i++) {
                    JSONObject jsonPart = weatherArr.getJSONObject(i);

                    String main = jsonPart.getString("main");
                    String description = jsonPart.getString("description");

                    if (!main.equals("") && !description.equals("")) {
                        weatherInfoMessage += main + ": " + description + "\r\n";
                    }
                }

                JSONObject mainInfoObject = new JSONObject(mainInfo);
                String temp = mainInfoObject.getString("temp");
                String temp_min = mainInfoObject.getString("temp_min");
                String temp_max = mainInfoObject.getString("temp_max");
                String humidity = mainInfoObject.getString("humidity");
                if (!temp.equals("") && !temp_min.equals("") && !temp_max.equals("")) {
                    tempInfoMessage += tempInfoMessage + "\r\n" +
                            "Temp :" + convertKtoF(temp) + "°C\r\n" +
                            "Temp min: " + convertKtoF(temp_min) + "°C\r\n" +
                            "Temp max: " + convertKtoF(temp_max) + "°C\r\n" +
                            "Humidity: " + humidity + "%";
                }

//                Log.i("weatherInfoMessage", weatherInfoMessage);
//                Log.i("tempInfoMessage ", tempInfoMessage);

                if (!weatherInfoMessage.equals("") && !tempInfoMessage.equals("")) {
                    String result = weatherInfoMessage + "" + "\r\n" + tempInfoMessage;
                    infoTextView.setText(result);
                } else {
                    infoTextView.setText("Could not find weather :(   Please try again");
                }

            } catch (Exception e) {

                infoTextView.setText("Could not find weather :( Please try again");

                e.printStackTrace();
            }
        }
    }

    private String convertKtoF(String s) {
        double F = Double.parseDouble(s);
        return String.format("%.1f",F);
    }
}