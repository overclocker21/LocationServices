package com.androbro.location;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    private Context context;
    GPSTracker gpsTracker;
    double deviceLatitude;
    double deviceLongitude;

    private TextView stationIdTV;
    private TextView observation_timeTV;
    private TextView weatherTV;
    private TextView temperatureTV;
    private TextView windTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = MainActivity.this;

        stationIdTV = (TextView) findViewById(R.id.station_id);
        observation_timeTV = (TextView) findViewById(R.id.observation_time);
        weatherTV = (TextView) findViewById(R.id.weather);
        temperatureTV = (TextView) findViewById(R.id.temperature_string);
        windTV = (TextView) findViewById(R.id.wind_string);

        gpsTracker = new GPSTracker(MainActivity.this);

        if (gpsTracker.canGetLocation()) {
            deviceLatitude = gpsTracker.getLatitude();
            deviceLongitude = gpsTracker.getLongitude();
            Toast.makeText(getApplicationContext(), "Your location is: \nLat: " + deviceLatitude + "\nLong: " + deviceLongitude, Toast.LENGTH_LONG).show();
        } else {
            gpsTracker.showSettingsAlert();
        }

        MyAsyncTask asyncTask = new MyAsyncTask();
        asyncTask.execute();
    }

    public class MyAsyncTask extends AsyncTask<Void, Void, NodeList> {

        NodeList list = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(NodeList list) {
            processNodeList(list);
        }

        @Override
        protected NodeList doInBackground(Void... params) {

            InputStream inputStream = context.getResources().openRawResource(R.raw.station_lookup2);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = null;
            try {
                documentBuilder = builderFactory.newDocumentBuilder();
                Document xmlDocument = documentBuilder.parse(inputStream);
                //getting root element of the XML doc
                Element rootElement = xmlDocument.getDocumentElement();
                Log.i("Root Element: ", "" + rootElement.getTagName());

                NodeList itemList = rootElement.getElementsByTagName("station");
                NodeList itemChildren = null;
                Node currentItem = null;
                Node currentChild = null;
                ArrayList<Station> data = null;
                Station station = null;

                double stationLatitude = 0;
                double stationLongitude = 0;
                String xml = null;
                double distance = 0;

                data = new ArrayList<Station>();
                station = new Station();
                for (int i = 0; i < itemList.getLength(); i++) {
                    currentItem = itemList.item(i);
                    //Log.i("Current Item: ", "" + currentItem.getNodeName());
                    itemChildren = currentItem.getChildNodes();

                    for (int j = 0; j < itemChildren.getLength(); j++) {

                        currentChild = itemChildren.item(j);
                        //Log.i("Current Child: ", "" + currentChild.getNodeName());
                        if (currentChild.getNodeName().equalsIgnoreCase("latitude")) {

                            stationLatitude = Double.parseDouble(currentChild.getTextContent());

                            //Log.i("Latitude: ", "" + stationLatitude);

                            station.setLatitude(stationLatitude);

                        } else if (currentChild.getNodeName().equalsIgnoreCase("longitude")) {

                            stationLongitude = Double.parseDouble(currentChild.getTextContent());

                            //Log.i("Longitude: ", "" + stationLongitude);

                            station.setLongitude(stationLongitude);

                        } else if (currentChild.getNodeName().equalsIgnoreCase("xml_url")) {

                            xml = currentChild.getTextContent();

                            // Log.i("XML: ", "" + xml);

                            station.setUrl(xml);
                        }

                        distance = calcDistance(deviceLongitude, deviceLatitude, station.getLongitude(), station.getLatitude());

                        station.setDistance(distance);

                        data.add(station);

                    }
                    //access abject here
                    //Log.i("Distance", "" + station.getDistance());

                }
                Collections.sort(data);

                Log.i("Shortest distance", "" + data.get(0).getDistance());
                Log.i("Corresponding XML", "" + data.get(0).getUrl());

                String parsedUrl = data.get(0).getUrl();

                list = returnNodes(parsedUrl);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }
    }

    private double calcDistance(double rLong1, double rLat1, double rLong2, double rLat2) {

        rLong1 = Math.toRadians(rLong1);
        rLong2 = Math.toRadians(rLong2);
        rLat1 = Math.toRadians(rLat1);
        rLat2 = Math.toRadians(rLat2);

        double dist = 0;
        double dLong = rLong2 - rLong1;
        double dLat = rLat2 - rLat1;
        double a = Math.sin(dLat / 2d) * Math.sin(dLat / 2d) + Math.sin(dLong / 2d) * Math.sin(dLong / 2d) * Math.cos(rLat1) * Math.cos(rLat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        dist = (6371 * c) * 0.621371d;

        return dist;
    }

    public NodeList returnNodes(String parsedUrl) {
        try {
            URL url = new URL(parsedUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("GET");
            InputStream inputStream = httpURLConnection.getInputStream();
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            Document xmlDocument = documentBuilder.parse(inputStream);

            //getting root element of the XML doc
            Element rootElement = xmlDocument.getDocumentElement();
            Log.i("", "" + rootElement.getTagName());

            NodeList list = rootElement.getChildNodes();

            return list;
        } catch (Exception e) {
            Log.i("", "" + e);
        }
        return null;
    }

    public void processNodeList(NodeList list) {
        if (list != null && list.getLength() > 0) {
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) list.item(i);
                    if (element.getNodeName().equals("station_id")) {

                        String stationId = element.getTextContent();
                        stationIdTV.setText("Station ID: " + stationId);
                        Log.i("stationID: ", "" + stationId);

                    } else if (element.getNodeName().equals("observation_time")) {

                        String observationTime = element.getTextContent();
                        observation_timeTV.setText("Observation time: " + observationTime);
                        Log.i("observationTime: ", "" + observationTime);

                    } else if (element.getNodeName().equals("weather")) {

                        String weather = element.getTextContent();
                        weatherTV.setText("Weather: " + weather);
                        Log.i("weather: ", "" + weather);

                    } else if (element.getNodeName().equals("temperature_string")) {

                        String tempString = element.getTextContent();
                        temperatureTV.setText("Temperature: " + tempString);
                        Log.i("tempString: ", "" + tempString);

                    } else if (element.getNodeName().equals("wind_string")) {

                        String windString = element.getTextContent();
                        windTV.setText("Wind: " + windString);
                        Log.i("windString: ", "" + windString);

                    }
                }
            }
        }
    }
}
