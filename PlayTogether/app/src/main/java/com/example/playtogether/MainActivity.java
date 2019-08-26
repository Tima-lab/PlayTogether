package com.example.playtogether;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Member;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener, LocationListener, GoogleMap.InfoWindowAdapter {

    private static final String TAG = "AddMerop";
    //повторная отправка
    boolean btn_verify = true;
    //проверка почты
    boolean emailVerified;
    //проверка на создание мероприятия
    boolean add_merop;


    FirebaseUser user;
    FirebaseAuth mAuth;

    private GoogleMap mMap;
    LatLng userLocation, create_merop_point;

    // сонстанта для разрешения на Fine_location
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 100;

    TextView user_email;
    FloatingActionButton fab_verify_email, fab_create_merop, fab_info_merop;

    // layout создания мероприятия
    EditText name_merop, info_merop, date_merop, time_merop, duration;
    Spinner type_merop;
    Button btnCreate_merop;

    // layout иформационного окна
    TextView host_merop, count_member_join_merop;
    Button btn_join_merop;
    int count_member;
    String id_merop;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // база данных
        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        // получение авторизованного пользователя
        user = FirebaseAuth.getInstance().getCurrentUser();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //GoogleMaps
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        fab_verify_email = findViewById(R.id.fab_verify_email);
        fab_create_merop = findViewById(R.id.fab_create_merop);
        fab_info_merop = findViewById(R.id.fab_info_merop);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // отправка подтверждающего письма
        fab_verify_email.setOnClickListener(view -> {
            send_email_verify();
            if (btn_verify) {
                Snackbar.make(view, "На почту " + user.getEmail() + " отправлено письмо с подтверждением", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        // fab создания мероприятия
        fab_create_merop.setOnClickListener(view -> {
            add_merop = true;
            Snackbar.make(view, "Нажмите по карте, чтобы создать мероприятие", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // находим TextView в headerLayout
        View headerLayout = navigationView.getHeaderView(0);
        user_email = headerLayout.findViewById(R.id.user_email);

        // create merop
        ////////////////////////////////////////////////////////////////////////////////////////////
        name_merop = findViewById(R.id.name_merop);
        info_merop = findViewById(R.id.info_merop);
        date_merop = findViewById(R.id.date_merop);
        time_merop = findViewById(R.id.time_merop);
        duration = findViewById(R.id.duration);
        type_merop = findViewById(R.id.type_merop);
        btnCreate_merop = findViewById(R.id.btnCreateMerop);
        ////////////////////////////////////////////////////////////////////////////////////////////
        //info window merop
        host_merop = findViewById(R.id.host_merop);
        count_member_join_merop = findViewById(R.id.count_member_join_merop);
        btn_join_merop = findViewById(R.id.btn_join_merop);

        btnCreate_merop.setOnClickListener(v -> {
            create_merop();
        });

        btn_join_merop.setOnClickListener(v -> {
            join_merop(id_merop);
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        emailVerified = user.isEmailVerified();

        if (emailVerified) {
            // Убираем fab отправки подтверждающего сообщения
            findViewById(R.id.fab_verify_email).setVisibility(View.INVISIBLE);
            // email авторизованного пользователя записываем в header
            user_email.setText(user.getEmail());
            findViewById(R.id.fab_create_merop).setVisibility(View.VISIBLE);
        } else {
            fab_create_merop.setEnabled(false);
            fab_info_merop.setEnabled(false);

        }
    }

    private void send_email_verify() {
        fab_verify_email.setEnabled(false);

        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    // Re-enable button
                    fab_verify_email.setEnabled(true);

                    if (!task.isSuccessful()) {
                        btn_verify = false;
                        Toast.makeText(MainActivity.this,
                                "Ошибка отправки письма",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            item.setChecked(true);
        }  else if (id == R.id.nav_tools) {

        } else if (id == R.id.logout) {
            mAuth.signOut();

            Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        My_Location();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17.0f));
        mMap.setMinZoomPreference(4.0f);
        mMap.setMaxZoomPreference(20.0f);

        mMap.setOnMapClickListener(latLng -> {
            if (add_merop){
                create_merop_point = latLng;
                findViewById(R.id.show_layout_create_merop).setVisibility(View.VISIBLE);
                findViewById(R.id.map).setVisibility(View.INVISIBLE);
                fab_create_merop.hide();
                fab_info_merop.hide();
                add_merop = false;
            } else fab_create_merop.show();
            fab_info_merop.hide();
            findViewById(R.id.info_window_merop).setVisibility(View.INVISIBLE);
        });


        // Устанавливаем слушателя
        final CollectionReference colRef = db.collection("Merop");
        colRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                Toast.makeText(MainActivity.this,"Ошибка",Toast.LENGTH_SHORT).show();
                return;
            }

            if (queryDocumentSnapshots != null) {
                Log.d(TAG, "Current data: " + queryDocumentSnapshots.getDocuments());
                List<DocumentChange> m = queryDocumentSnapshots.getDocumentChanges();
                for (DocumentSnapshot document: queryDocumentSnapshots.getDocuments()){
                    String uid;
                    String name_m;
                    Number type_m;
                    String info_m;
                    GeoPoint location;
                    String date_m;
                    String time_m;
                    Number duration_m;

                    Map<String, Object> Merop = document.getData();
                    date_m = (String) Merop.get("date_mer");
                    duration_m = (Number) Merop.get("duration");
                    info_m = (String) Merop.get("info_mer");
                    location = (GeoPoint) Merop.get("location");
                    name_m = (String) Merop.get("name_mer");
                    time_m = (String) Merop.get("time_mer");
                    type_m = (Number) Merop.get("type_mer");
                    uid = (String) Merop.get("uid");

                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(),location.getLongitude()))
                            .title(name_m)
                            .snippet("Описание: "+ info_m+"\nДата начала: "+date_m
                                    +"\nВремя начала: "+time_m+"\nДлительность(мин): "+duration_m)
                    );

                }

                for (int i = 0; i < m.size(); i++) {
                    DocumentChange.Type s = m.get(i).getType();
                }
                Toast.makeText(MainActivity.this,"Данные обновлены",Toast.LENGTH_SHORT).show();

            } else {
                Log.d(TAG, "Current data: null");
                Toast.makeText(MainActivity.this,"Нет изменений",Toast.LENGTH_SHORT).show();
            }
        });

        fab_create_merop.show();
        fab_info_merop.hide();

        //при клике по маркеру
        mMap.setOnMarkerClickListener(marker -> {
            count_member_join_merop.setText("");
            host_merop.setText("");

            fab_create_merop.hide();
            fab_info_merop.show();

            GeoPoint position = new GeoPoint(marker.getPosition().latitude, marker.getPosition().longitude);

            db.collection("Merop")
                    .whereEqualTo("location", position)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> Merop = document.getData();
                                String uid = (String) Merop.get("uid");
                                id_merop = document.getId();

                                //Метод поиска email у создателя мероп и его вывода на экран
                                find_email_host(uid);
                                // Метод вывода количества участнико в мероприятии
                                count_member_merop(document.getId());

                                if (uid == user.getUid()) btn_join_merop.setVisibility(View.GONE);
                                else btn_join_merop.setVisibility(View.VISIBLE);

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    });

            return false;
        });


        fab_info_merop.setOnClickListener(v -> {
            findViewById(R.id.info_window_merop).setVisibility(View.VISIBLE);
        });


        mMap.setInfoWindowAdapter(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);
    }



    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Текущее местоположение:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    // Info window on click marker
    @Override
    public View getInfoContents(Marker marker) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View page = inflater.inflate(R.layout.info_merop, null);

        TextView name = page.findViewById(R.id.name_sport_merop);
        TextView opis = page.findViewById(R.id.opisanie_merop);


        name.setText(marker.getTitle());
        opis.setText(marker.getSnippet());
        return page;
    }


    // Разрешение FINE LOCATION
    private void My_Location(){
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            mMap.setMyLocationEnabled(false);
            userLocation = new LatLng(0,0);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                Toast.makeText(this,
                        "Извините, но без разрешения на ваш location, приложение не определит ваше текущее местоположение",
                        Toast.LENGTH_SHORT).show();

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);

            }

        } else {
            mMap.setMyLocationEnabled(true);
            LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
            String provider = LocationManager.PASSIVE_PROVIDER;
            Location location = service.getLastKnownLocation(provider);
            userLocation = new LatLng(location.getLatitude(),location.getLongitude());
        }
    }


    //Обработка результата разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "Чтобы изменения применились, нужно перезапустить приложение!",
                            Toast.LENGTH_SHORT).show();
                }
            }

        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
                findViewById(R.id.show_layout_create_merop).setVisibility(View.GONE);
                findViewById(R.id.map).setVisibility(View.VISIBLE);
                findViewById(R.id.info_window_merop).setVisibility(View.GONE);
                fab_create_merop.show();
                fab_info_merop.hide();
                clear_window_merop();
                count_member_join_merop.setText("");
                host_merop.setText("");
        }
        return true;
    }

    public void clear_window_merop(){
        name_merop.getText().clear();
        info_merop.getText().clear();
        date_merop.getText().clear();
        time_merop.getText().clear();
        duration.getText().clear();
        type_merop.setSelection(0);
    }

    public void create_merop() {
        String uid = user.getUid();
        String name_m = name_merop.getText().toString();
        Number type_m = type_merop.getSelectedItemPosition();
        String info_m = info_merop.getText().toString();
        GeoPoint location = new GeoPoint(create_merop_point.latitude, create_merop_point.longitude);
        String date_m = date_merop.getText().toString();
        String time_m = time_merop.getText().toString();
        int duration_m = Integer.parseInt(duration.getText().toString());


        Map<String, Object> Merop = new HashMap<>();
        Merop.put("uid",uid);
        Merop.put("name_mer",name_m);
        Merop.put("type_mer",type_m);
        Merop.put("info_mer",info_m);
        Merop.put("location",location);
        Merop.put("date_mer", date_m);
        Merop.put("time_mer", time_m);
        Merop.put("duration",duration_m);

        db.collection("Merop")
                .add(Merop)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG,"Добавлен с ID: " + documentReference.getId());
                    String id_merop = documentReference.getId();
                    Toast.makeText(MainActivity.this, "Саксесфул!", Toast.LENGTH_SHORT).show();

                    Map<String, Object> Member = new HashMap<>();
                    Member.put("email0", user.getEmail());

                    db.collection("Member_merop").document(id_merop)
                            .set(Member)
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));


                })
                .addOnFailureListener(e -> {
                    Log.w(TAG,"Error добавления документа: ", e);
                    Toast.makeText(MainActivity.this, "Невозможно создать мероприятие!", Toast.LENGTH_SHORT).show();
                });





        findViewById(R.id.show_layout_create_merop).setVisibility(View.GONE);
        findViewById(R.id.map).setVisibility(View.VISIBLE);
        fab_create_merop.show();
        clear_window_merop();
    }

    public void find_email_host(String uid) {
        DocumentReference docRef = db.collection("User").document(uid);
        docRef.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                DocumentSnapshot document1 = task1.getResult();
                if (document1.exists()) {

                    Map<String, Object> Host = document1.getData();
                    String email = (String) Host.get("email");

                    host_merop.setText(email);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task1.getException());
            }
        });
    }

    public void count_member_merop(String id_merop) {
        DocumentReference docRef = db.collection("Member_merop").document(id_merop);
        docRef.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                DocumentSnapshot document1 = task1.getResult();
                if (document1.exists()) {

                    Map<String, Object> Members = document1.getData();
                    count_member = Members.size();
                    count_member_join_merop.setText(count_member + " / 10");

                    for (int i=0; i < count_member; i++) {
                        db.collection("Member_merop")
                                .whereEqualTo("email" + i, user.getEmail())
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String id_merop_this = document.getId();
                                            Log.d(TAG, document.getId() + " => " + document.getData());

                                            if (id_merop_this.equals(id_merop))
                                                btn_join_merop.setVisibility(View.GONE);
                                            else btn_join_merop.setVisibility(View.VISIBLE);

                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                });
                    }


                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task1.getException());
            }
        });
    }

    public void join_merop(String id_merop) {

        Map<String, Object> Member = new HashMap<>();
        Member.put("email" + count_member, user.getEmail());

        db.collection("Member_merop").document(id_merop)
                .set(Member, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    findViewById(R.id.info_window_merop).setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this,"Вы успешно присоединились к мероприятию",Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
    }

}

