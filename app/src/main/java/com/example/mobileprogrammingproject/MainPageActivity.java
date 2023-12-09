package com.example.mobileprogrammingproject;

import android.Manifest;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;



public class MainPageActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private MapPOIItem marker;
    private BottomNavigationView bottomNavigationView;
    private Button btnWritePost;
    private Throwable e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

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

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.tab_write) {
                    showLocationSelectionDialog();
                    return true;
                } else if (itemId == R.id.tab_refresh) {
                    fetchDataFromFirestore(); // 조회버튼 눌렀을 때
                    return true;
                } else if (itemId == R.id.tab_user) {
                    Intent intent = new Intent(MainPageActivity.this, Mypage.class);
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });

        btnWritePost = findViewById(R.id.btnWritePost);
        btnWritePost.setVisibility(View.GONE);

        MapPolyline polyline = new MapPolyline();
        polyline.setTag(1000);
        polyline.setLineColor(Color.argb(128, 255, 51, 0));

        polyline.addPoint(MapPoint.mapPointWithGeoCoord(37.479928, 126.900169));
        polyline.addPoint(MapPoint.mapPointWithGeoCoord(37.480624, 126.900735));
        polyline.addPoint(MapPoint.mapPointWithGeoCoord(37.481667, 126.900713));

        mapView.addPolyline(polyline);

        MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
        int padding = 100;
        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                return false;
            }
        });
    }

    private void showLocationSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 지정하시오");

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MapPoint centerMapPoint = mapView.getMapCenterPoint();

                if (marker != null) {
                    mapView.removePOIItem(marker);
                }

                marker = new MapPOIItem();
                marker.setItemName("행사 이름");
                marker.setTag(1);
                marker.setMapPoint(centerMapPoint);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.marker);
                marker.setCustomImageAutoscale(false);
                marker.setCustomImageAnchor(0.5f, 1.0f);

                mapView.addPOIItem(marker);

                btnWritePost.setVisibility(View.VISIBLE);

                // 역 지오코딩을 통해 주소(장소명) 가져오기
                getAddressFromCoordinates(centerMapPoint.getMapPointGeoCoord().latitude, centerMapPoint.getMapPointGeoCoord().longitude);
            }
        });

        builder.create().show();
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

        // "posts"를 실제 컬렉션 이름으로 대체하세요
        db.collection("posts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 기존의 마커를 지도에서 모두 제거합니다
                        mapView.removeAllPOIItems();

                        // 검색된 문서를 처리합니다
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // document.getData()를 사용하여 데이터에 접근합니다
                            double latitude = Double.parseDouble(document.getString("Latitude"));
                            double longitude = Double.parseDouble(document.getString("Longitude"));
                            String title = document.getString("Title");
                            String content = document.getString("Content");
                            String startdate = document.getString("StartDate");
                            String enddate =  document.getString("EndDate");


                            // 각 문서에 대한 마커를 생성합니다
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(title);
                            marker.setTag(1);
                            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                            marker.setCustomImageResourceId(R.drawable.marker);
                            marker.setCustomImageAutoscale(false);
                            marker.setCustomImageAnchor(0.5f, 1.0f);

                            marker.setUserObject(document.getData()); // 문서의 데이터 전체를 마커의 UserObject로 설정

                            mapView.addPOIItem(marker);
                        }
                    } else {
                        Log.e("FirestoreData", "문서 가져오기 실패: ", task.getException());
                    }
                });
    }



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

    @Override
    protected void onResume() {
        super.onResume();

        btnWritePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "게시물 작성" 버튼이 클릭되었을 때 실행되는 부분

                // AlertDialog를 통해 확인 다이얼로그 표시
                AlertDialog.Builder builder = new AlertDialog.Builder(MainPageActivity.this);
                builder.setMessage("이 주소로 설정");

                // 확인 버튼 클릭 시의 동작 정의
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // "확인" 버튼이 클릭되었을 때 실행되는 부분

                        // 마커의 위치 정보 가져오기
                        MapPoint centerMapPoint = mapView.getMapCenterPoint();
                        double markerLatitude = centerMapPoint.getMapPointGeoCoord().latitude;
                        double markerLongitude = centerMapPoint.getMapPointGeoCoord().longitude;

                        // 역 지오코딩을 통해 주소(장소명) 가져오기
                        String locationName = getAddressFromCoordinates(markerLatitude, markerLongitude);

                        // Intent를 사용하여 WritingBoardActivity로 전환하면서 마커 위치 정보 및 주소(장소명) 전달
                        Intent intent = new Intent(MainPageActivity.this, WritingBoardActivity.class);
                        intent.putExtra("markerLatitude", markerLatitude);
                        intent.putExtra("markerLongitude", markerLongitude);
                        intent.putExtra("locationName", locationName);

                        startActivity(intent);
                    }
                });

                // 취소 버튼 클릭 시의 동작 정의
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                // AlertDialog 생성 및 표시
                builder.create().show();
            }
        });
    }

}
