package com.example.vivek.getweather;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    EditText cityName;
    Button button;
    TextView tempTV;
    TextView humidTV;
    TextView maxminTV;
    TextView resultTV;
    ImageView iconImage;
    private boolean berr=false;

    public void setImage(String main){
        String img = "rain";
        if(main.equalsIgnoreCase("rain"))
            img = "rain";
        if(main.equalsIgnoreCase("clouds"))
            img = "clouds";
        if(main.equalsIgnoreCase("thunderstorm"))
            img = "thunderstorm";
        if(main.equalsIgnoreCase("clear"))
            img = "sunny";
        if(main.equalsIgnoreCase("mist"))
            img = "mist";
        if(main.equalsIgnoreCase("haze"))
            img = "mist";

        if (img != "") {
            int resID = getResources().getIdentifier(img, "drawable", getPackageName());
            iconImage.setImageResource(resID);
        }else {
            iconImage.setImageResource(0);
        }
    }

    public void getWeather(View view){

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(cityName.getWindowToken(),0);


        try {
            if(cityName.getText().toString().trim().equals("")||cityName.getText().toString().trim().isEmpty()){
                Toast.makeText(this,"City Name can't be empty",Toast.LENGTH_SHORT).show();
                return;
            }
            String city = URLEncoder.encode(cityName.getText().toString(),"UTF-8");
            DownloadTask task = new DownloadTask();
            task.execute("http://api.openweathermap.org/data/2.5/weather?q="+city+"&appid=44b7f75d8feb214cdc4fb5d46faf4e9c").get();

        } catch (Exception e) {
            Log.i("Error city:",e.getMessage());
            Toast.makeText(getApplicationContext(),"Unable to find city",Toast.LENGTH_LONG).show();;
        }

    }

    public class DownloadTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {

            try {
                berr=false;
                String result = "";
                URL url;
                HttpURLConnection urlConnection = null;

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();

                while (data != -1)  {
                    char current = (char)data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                Log.e("Error city back:",e.getMessage());
                //Toast.makeText(getApplicationContext(),"Unable to find city",Toast.LENGTH_LONG).show();
                berr = true;
                return null;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(berr){
                Toast.makeText(getApplicationContext(),"Unable to find city",Toast.LENGTH_LONG).show();
                return;
            }

            try {
                String message = "";
                JSONObject jsonObject = new JSONObject(result);
                String weatherInfo = jsonObject.getString("weather");
                String temperature = jsonObject.getString("main");

                JSONArray array = new JSONArray(weatherInfo);

                JSONObject jsonPart = array.getJSONObject(0);
                String main = "";
                String description = "";
                main = jsonPart.getString("main");
                description = jsonPart.getString("description");

                if(main != "" && description != ""){
                    message += main + ": " + description + "\r\n";
                }

                if(message != ""){
                    resultTV.setText(message);
                }else {
                    Toast.makeText(getApplicationContext(),"Unable to find city",Toast.LENGTH_LONG).show();
                }

                JSONObject jsonTemp = new JSONObject(temperature);
                String cityTemp = jsonTemp.getString("temp");
                String humidity = jsonTemp.getString("humidity");
                String tempMax = jsonTemp.getString("temp_max");
                String tempMin = jsonTemp.getString("temp_min");

                if(cityTemp != null){
                    Log.i("T",cityTemp);
                    Double d = Double.parseDouble(cityTemp);
                    Double tM = Double.parseDouble(tempMax);
                    Double tm = Double.parseDouble(tempMin);
                    d -= 273.15;
                    tM -= 273.15;
                    tm -= 273.15;
                    tempMax = String.format("%.1f",tM);
                    tempMin = String.format("%.1f",tm);
                    cityTemp = String.format("%.1f",d);
                    tempTV.setText(cityTemp+(char) 0x00B0+"C");
                    maxminTV.setText("Max: "+tempMax+(char) 0x00B0+"C | Min: "+tempMin+(char) 0x00B0+"C");
                    humidTV.setText("Humidity: "+humidity);

                    setImage(main);
                }

            } catch (Exception e) {
                Log.i("Error city OnPost:",e.getMessage());
                Toast.makeText(getApplicationContext(),"Unable to find city",Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = (EditText)findViewById(R.id.cityName);
        button = (Button)findViewById(R.id.button);
        resultTV = (TextView)findViewById(R.id.descTV);
        tempTV = (TextView)findViewById(R.id.tempTV);
        humidTV = (TextView)findViewById(R.id.humidTV);
        maxminTV = (TextView)findViewById(R.id.maxminTV);
        iconImage = (ImageView)findViewById(R.id.iconImage);

    }
}
