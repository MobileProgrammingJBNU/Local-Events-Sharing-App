package com.example.mobileprogrammingproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainPageActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private MapPOIItem marker;
    private BottomNavigationView bottomNavigationView;
    private Button btnWritePost;
    private Throwable e;
    private ImageView btnMoveToMyLocation;

    private TextView tvLocationName;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    double set_lat, set_long;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.d("Location Update", "Latitude: " + latitude + ", Longitude: " + longitude);

                    // 여기서 위도(latitude)와 경도(longitude)를 사용할 수 있습니다.
                }
            }
        };

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("키해시는 :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1000);
            }
            return;
        }

        mapView = new MapView(this);
        mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        fetchDataFromFirestore();

        //하단 네비게이션
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        //temp 버튼
        btnMoveToMyLocation = findViewById(R.id.btnMoveToMyLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // 1초마다 위치 업데이트

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        set_lat = location.getLatitude();
                        set_long = location.getLongitude();
                        // 여기에서 위도와 경도를 사용하면 됩니다.
                    }
                }
            }
        };

        requestLocationPermission();
        btnMoveToMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(set_lat, set_long);
                mapView.setMapCenterPoint(mapPoint, true);
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.tab_write) {
                    showLocationSelectionDialog(); // 작성 눌렀을 시
                    return true;
                } else if (itemId == R.id.tab_refresh) {
                    fetchDataFromFirestore(); // 조회 눌렀을 시
                    return true;
                } else if (itemId == R.id.tab_user) { //마이페이지 눌렀을 시
                    Intent intent = new Intent(MainPageActivity.this, Mypage.class);
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });
        //TextView
        tvLocationName = findViewById(R.id.tvMyTextView);

            /*btnMoveToMyLocation = findViewById(R.id.btnMoveToMyLocation);

            btnMoveToMyLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Move the map to the user's current location
                    moveToCurrentLocation();
                }
            });
             */

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                return false;
            }
        });

    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // 이미 권한이 허용되어 있으면 위치 업데이트를 시작합니다.
            startLocationUpdates();
        } else {
            // 권한이 없으면 권한 요청
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            // 권한이 없으면 권한 요청
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 액티비티가 중지되면 위치 업데이트 중지
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }



    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        // ...
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
        // ...
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
        // ...
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
        // ...
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        // ...
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        // ...
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        // ...
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        double lat = mapPoint.getMapPointGeoCoord().latitude;
        double lng = mapPoint.getMapPointGeoCoord().longitude;

        if (marker != null) {
            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat, lng));
        }

        // getAddressFromCoordinates 메서드를 호출하여 주소(장소명) 가져오기
        String locationName = getAddressFromCoordinates(lat, lng);

        // 로그에 위치 정보 및 주소(장소명) 표시
        Log.d("클릭한 위치 정보", "위도: " + lat + ", 경도: " + lng + ", 주소: " + locationName);

        if (tvLocationName != null) {
            tvLocationName.setText(locationName);
        }

    }

    private String getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                return address.getAddressLine(0);
            } else {
                return null;
            }
        } catch (IOException e) {
            Log.e("주소(장소명)를 가져오는 중 오류 발생:", e.getMessage());
            return null;
        }
    }


    private void fetchDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        mapView.removeAllPOIItems(); // 마커와 리스너를 모두 제거

        db.collection("posts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            double latitude = Double.parseDouble(document.getString("Latitude"));
                            double longitude = Double.parseDouble(document.getString("Longitude"));
                            String title = document.getString("Title");
                            String id = document.getString("PostID");

                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(title);
                            marker.setTag(1);
                            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                            marker.setCustomImageResourceId(R.drawable.marker);
                            marker.setCustomImageAutoscale(false);
                            marker.setCustomImageAnchor(0.5f, 1.0f);

                            marker.setUserObject(document.getData());

                            // 마커에 클릭 리스너 추가
                            mapView.addPOIItem(marker);
                        }

                        // 마커 클릭 리스너 추가 (기존 리스너를 모두 제거한 후에 새로운 리스너를 추가)
                        mapView.setPOIItemEventListener(new MapView.POIItemEventListener() {
                            @Override
                            public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
                                // 마커를 선택했을 때의 동작
                                showMarkerDetails(mapPOIItem);
                            }

                            @Override
                            public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
                                // 마커의 말풍선을 터치했을 때의 동작
                                showMarkerDetails(mapPOIItem);
                            }

                            @Override
                            public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
                                // 말풍선의 버튼을 터치했을 때의 동작
                                showMarkerDetails(mapPOIItem);
                            }

                            @Override
                            public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
                                // 마커를 드래그했을 때의 동작
                            }
                        });

                    } else {
                        Log.e("FirestoreData", "문서 가져오기 실패: ", task.getException());
                    }
                });
    }


    private void showMarkerDetails(MapPOIItem mapPOIItem) {
        // 마커에서 정보 추출
        Map<String, Object> markerData = (Map<String, Object>) mapPOIItem.getUserObject();
        String id = (String) markerData.get("PostID");

        // Screen 액티비티 시작 및 Intent를 사용하여 정보 전달
        Intent intent = new Intent(MainPageActivity.this, Screen.class);
        intent.putExtra("PostID", id);

        startActivity(intent);
    }

    private void showLocationSelectionDialog() {
        ViewGroup.LayoutParams mapLayoutParams = mapViewContainer.getLayoutParams();
        mapLayoutParams.height = (int) getResources().getDimension(R.dimen.map_height_small);
        mapViewContainer.setLayoutParams(mapLayoutParams);

        // Show the new layout
        LinearLayout writeLayout = findViewById(R.id.writeLayout);
        writeLayout.setVisibility(View.VISIBLE);

        // Get the button from the new layout
        Button btnSetLocation = findViewById(R.id.btnMyButton);

        MapPoint centerMapPoint = mapView.getMapCenterPoint();
        if (marker != null) {
            mapView.removePOIItem(marker);
        }

        marker = new MapPOIItem();
        marker.setItemName("행사");
        marker.setTag(1);
        marker.setMapPoint(centerMapPoint);
        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        marker.setCustomImageResourceId(R.drawable.marker);
        marker.setCustomImageAutoscale(false);
        marker.setCustomImageAnchor(0.5f, 1.0f);

        mapView.addPOIItem(marker);

        // "이 위치로 설정" button 동작
        btnSetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (marker != null) {
                    MapPoint markerMapPoint = marker.getMapPoint();
                    double markerLatitude = markerMapPoint.getMapPointGeoCoord().latitude;
                    double markerLongitude = markerMapPoint.getMapPointGeoCoord().longitude;

                    // 역 지오코딩을 통해 주소(장소명) 가져오기
                    String locationName = getAddressFromCoordinates(markerLatitude, markerLongitude);

                    // Intent를 사용하여 WritingBoardActivity로 전환하면서 마커 위치 정보 및 주소(장소명) 전달
                    Intent intent = new Intent(MainPageActivity.this, WritingBoardActivity.class);
                    intent.putExtra("markerLatitude", markerLatitude);
                    intent.putExtra("markerLongitude", markerLongitude);
                    intent.putExtra("locationName", locationName);

                    startActivity(intent);
                } else {
                    // 마커가 없을 경우 처리 (예: 에러 메시지 표시 등)
                    Toast.makeText(MainPageActivity.this, "마커를 먼저 찍어주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


         /*
        private void moveToCurrentLocation() {
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        }
         */

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
        // ...
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
        // ...
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        // ...
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        // ...
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        // ...
    }

    public void printLatLong(){

    }
}