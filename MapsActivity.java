package com.example.vivien.mmap;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    ServerConnection sc;
    JSONArray user, other;
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1;
    //設定超過一個距離(尺)他就會做更新location的動作
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 500;
    //設定超過一個時間(毫秒)他就會做更新location的動作
    protected LocationManager locationManager;
    Location location;
    LatLng abc;
    boolean a=true;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_maps);
        sc = new ServerConnection ();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager ()
                .findFragmentById (R.id.map);
        mapFragment.getMapAsync (this);
        locationManager = (LocationManager) getSystemService (Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates (
                //Provider: the name of the GPS provider
                LocationManager.GPS_PROVIDER,
                //minTime: 最小時間間隔後更新位置，以毫秒為單位
                MINIMUM_TIME_BETWEEN_UPDATES,
                //minDistance: 最短距離間隔外更新位置，以公尺為單位
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                //Listener:每次地點更新時會呼叫LocationListener中onLocationChanged(Location) 		     方法
                new MyLocationListener ()
        );
        Timer timer01 = new Timer ();
        timer01.schedule (task, 0, 3000);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder (this).addApi (AppIndex.API).build ();
    }

    private TimerTask task = new TimerTask () {
        public void run() {
            //每3秒更新一次對方的位置
            Message message = new Message ();
            message.what = 3;
            h1.sendMessage (message);
        }
    };
    Handler h1 = new Handler () {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                //更新對方在地圖的位置
                mMap.clear ();
                showCurrentLocation();
                try {
                    abc = new LatLng ((other.getJSONObject (0).getDouble ("B")-22)/7, (other.getJSONObject (0).getDouble ("C")-29)/3);
                    Log.e ("ssss", abc.toString ());
                    mMap.addMarker (new MarkerOptions ().position (abc).title ("pig").icon (BitmapDescriptorFactory.defaultMarker (270)));
                } catch (JSONException other) {
                }
            }
            if (msg.what == 2) {
                //更新自己的位置(DB)
                new Thread () {
                    public void run() {
                        sc.update ("imf91", "imf91", "A='Vivi' ", "C='" + location.getLongitude ()*3+29 + "',B='" + location.getLatitude ()*7+22 + "'");
                        //因為使用共用的資料庫，所以對座標位置進行了一點加密
                    }
                }.start ();
            }
            if (msg.what == 3) {
                //取得對方的位置(DB)
                new Thread () {
                    public void run() {
                        other = sc.query ("imf91", "imf91", "A,B,C", "A='pig' ");
                        //更新地圖
                        Message message = new Message ();
                        message.what = 1;
                        h1.sendMessage (message);
                    }
                }.start ();
            }


        }
    };

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
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled (true);
        //mMap.getMaxZoomLevel ();
        mMap.getUiSettings ().setZoomControlsEnabled (true);
        CameraUpdateFactory.zoomOut();
        // Add a marker in Sydney and move the camera
        showCurrentLocation ();
    }

    public void onMapLongClick(LatLng point) {
        mMap.moveCamera (CameraUpdateFactory.newLatLng (abc));
    }

    protected void showCurrentLocation() {
        //取得最後得知道provider資訊
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (locationManager.getLastKnownLocation (LocationManager.GPS_PROVIDER) == null) {
            location = locationManager.getLastKnownLocation (LocationManager.NETWORK_PROVIDER);
        } else {
            location = locationManager.getLastKnownLocation (LocationManager.GPS_PROVIDER);
        }
        locationManager = (LocationManager) getSystemService (Context.LOCATION_SERVICE);
        //得知GPS位置時，在TextView上顯示經緯度
        if (location != null) {
            //LatLng HI = new LatLng(a,b);
//            Toast.makeText(MapsActivity.this,String.valueOf(a)+"\n"+String.valueOf(b), Toast.LENGTH_LONG).show();
            LatLng HI = new LatLng (location.getLatitude (), location.getLongitude ());
            //LatLng HI = new LatLng(1,6);
            //mMap.addMarker (new MarkerOptions ().position (HI).title ("我在這~~~").icon (BitmapDescriptorFactory.defaultMarker (200)));
            if(a) {
                mMap.moveCamera (CameraUpdateFactory.newLatLng (HI));
            }
            a=false;
            //update
            Message message = new Message ();
            message.what = 2;
            h1.sendMessage (message);
        } else
            Toast.makeText (MapsActivity.this, "null", Toast.LENGTH_LONG).show ();
    }

    @Override
    public void onStart() {
        super.onStart ();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect ();
        Action viewAction = Action.newAction (
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse ("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse ("android-app://com.example.vivien.mmap/http/host/path")
        );
        AppIndex.AppIndexApi.start (client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop ();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction (
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse ("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse ("android-app://com.example.vivien.mmap/http/host/path")
        );
        AppIndex.AppIndexApi.end (client, viewAction);
        client.disconnect ();
    }

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
// TODO Auto-generated method stub
            //將想要印出的資料用string.format的方法存入string
            //%1$s，%2$s中，1、2代表後方第幾個參數
            LatLng HI = new LatLng (location.getLatitude (), location.getLongitude ());
            //mMap.addMarker (new MarkerOptions ().position (HI).title ("我在這~~~").icon (BitmapDescriptorFactory.defaultMarker (200)));
            //mMap.moveCamera (CameraUpdateFactory.newLatLng (HI));
            Message message = new Message ();
            message.what = 2;
            h1.sendMessage (message);
        }

        @Override
        public void onProviderDisabled(String provider) {
// TODO Auto-generated method stub
//當device的GPS沒有開啟的時候他會顯示
            Toast.makeText (MapsActivity.this, "Provider disabled by the user. GPS turned off", Toast.LENGTH_LONG).show ();
        }

        @Override
        public void onProviderEnabled(String provider) {
// TODO Auto-generated method stub
//當device將GPS打開的時候他會顯示
            Toast.makeText (MapsActivity.this, "Provider enabled by the user. GPS turned on",
                    Toast.LENGTH_LONG).show ();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
// TODO Auto-generated method stub
//當provider change的時候會顯示
            Toast.makeText (MapsActivity.this, "Provider status changed", Toast.LENGTH_LONG).show ();
        }
    }
}

