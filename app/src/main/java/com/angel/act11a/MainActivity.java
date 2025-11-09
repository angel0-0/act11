package com.angel.act11a;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btnNormal, btnSatellite, btnMyLocation, btnZoomIn, btnZoomOut;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar botones
        initializeButtons();

        // Obtener el fragmento del mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initializeButtons() {
        btnNormal = findViewById(R.id.btnNormal);
        btnSatellite = findViewById(R.id.btnSatellite);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);

        // Configurar listeners de botones
        btnNormal.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                Toast.makeText(MainActivity.this, "Modo Normal activado", Toast.LENGTH_SHORT).show();
            }
        });

        btnSatellite.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                Toast.makeText(MainActivity.this, "Modo Satélite activado", Toast.LENGTH_SHORT).show();
            }
        });

        btnMyLocation.setOnClickListener(v -> getCurrentLocation());

        btnZoomIn.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
                Toast.makeText(MainActivity.this, "Acercando", Toast.LENGTH_SHORT).show();
            }
        });

        btnZoomOut.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
                Toast.makeText(MainActivity.this, "Alejando", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configurar ubicación inicial (Ciudad de México)
        LatLng ciudadMexico = new LatLng(19.4326, -99.1332);

        // Agregar marcador
        mMap.addMarker(new MarkerOptions()
                .position(ciudadMexico)
                .title("Ciudad de México")
                .snippet("¡Bienvenido a la capital!"));

        // Mover cámara con zoom (Nivel 12: Ciudad)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ciudadMexico, 12));

        // Habilitar controles de zoom (los botones +/- por defecto de Google)
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);

        // Configurar listener de clicks en el mapa
        mMap.setOnMapClickListener(latLng -> {
            // Agregar marcador donde el usuario toque
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Nuevo marcador")
                    .snippet("Lat: " + String.format("%.4f", latLng.latitude) +
                            ", Lng: " + String.format("%.4f", latLng.longitude)));

            Toast.makeText(MainActivity.this, "Marcador agregado", Toast.LENGTH_SHORT).show();
        });

        // Solicitar permisos de ubicación
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false); //Desactivamos el botón por defecto para usar el nuestro
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("Mi ubicación actual")
                                    .snippet("¡Aquí estoy!"));

                            // Movemos la cámara a la ubicación actual con un zoom de nivel 15 (Barrio)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                            Toast.makeText(MainActivity.this, "Ubicación encontrada", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Permisos de ubicación requeridos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
