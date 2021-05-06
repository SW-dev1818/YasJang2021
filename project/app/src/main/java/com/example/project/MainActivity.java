package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.internal.bind.ArrayTypeAdapter;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Projection;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import org.w3c.dom.Text;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Overlay.OnClickListener, OnMapReadyCallback {
    public List<GymSample> GymList = new ArrayList<>();
    public String name;
    public String state;
    public double latitude;
    public double longitude;
    public String address;
    public String number;
    public double lat;
    public double lon;
    public Marker marker;
    public  NaverMap naverMap;
    ListView listView;
    SearchView searchView;
    CameraPosition cameraPosition;
    NaverMapOptions options;
    private InfoWindow infoWindow;
    private  static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLoadDB();

        searchView = (SearchView) findViewById(R.id.search_view);
        listView = (ListView)findViewById(R.id.listView);

        final ArrayAdapter<GymSample> Adapter = new ArrayAdapter<GymSample>(
                this, android.R.layout.simple_list_item_1,GymList);
        listView.setAdapter(Adapter);

        //NaverMap객체 생성
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

                cameraPosition = new CameraPosition(
                new LatLng(35.1798159, 129.0750222), // 대상 지점
                16, // 줌 레벨
                20, // 기울임 각도
                180 // 베어링 각도
        );

        //검색창 확장했을 때 이벤트 리스너
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    listView.setVisibility(View.VISIBLE);
                }else{
                    listView.setVisibility(View.GONE);
                }
            }
        });

        //검색창 이벤트리스너
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //검색했을때 호출
            public boolean onQueryTextSubmit(String text) {

            return false;
            }

            @Override
            //글자를 입력할 때 마다 들어옴
            public boolean onQueryTextChange(String text) {
                Adapter.getFilter().filter(text);
                return false;
            }
        });

        //리스트뷰 클릭시 실행
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             lat = GymList.get(position).getLatitude();
             lon = GymList.get(position).getLongitude();
             CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(
                     new LatLng(lat, lon),18)
                     .animate(CameraAnimation.Fly, 3000);
             naverMap.moveCamera(cameraUpdate);
             listView.setVisibility(View.GONE);
             searchView.setIconified(true);

            }
        });

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        options = new NaverMapOptions()
                .camera(cameraPosition)
                .mapType(NaverMap.MapType.Terrain)
                .enabledLayerGroups(NaverMap.LAYER_GROUP_BUILDING)
                .compassEnabled(true)
                .scaleBarEnabled(true)
                .locationButtonEnabled(true);
    }

    //db파일 불러오기
    private void initLoadDB() {
        DataAdapter mDbHelper = new DataAdapter(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();
        Log.d("MainActivity","db가 연결되었습니다..");

        // db에 있는 값들을 model을 적용해서 넣는다.
        GymList = mDbHelper.getTableData();

        // db 닫기
        mDbHelper.close();
    }

    //위치 권한 받기
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults){
        if(locationSource.onRequestPermissionsResult(
                requestCode,permissions,grantResults
        )){
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );
    }

    //네이버맵
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        infoWindow = new InfoWindow();
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(this) {

            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                Marker marker = infoWindow.getMarker();
                GymSample gyms = (GymSample)marker.getTag();
                return gyms.getName() + "\n"+gyms.getAddress()+"\n" +
                      gyms.getState()+ "\n"+"업체번호: "+gyms.getNumber();
            }
        });

        //마커생성
        if(GymList.size() > 0){
            for(GymSample gyms : GymList){
                name = gyms.getName();
                latitude = gyms.getLatitude();
                longitude = gyms.getLongitude();
                state = gyms.getState();
                address = gyms.getAddress();
                number = gyms.getNumber();
                marker = new Marker();
                marker.setTag(gyms);
                marker.setPosition(new LatLng(latitude,longitude));
                marker.setMap(naverMap);
                marker.setOnClickListener(this);
            }
        }

    }

    //마커 눌렀을때 이벤트 리스너
    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        if(overlay instanceof  Marker){
            Marker marker = (Marker) overlay;
            if(marker.getInfoWindow() != null){
                infoWindow.close();
            }
            else{
                infoWindow.open(marker);
            }
            return true;
        }
        return false;
    }
}











