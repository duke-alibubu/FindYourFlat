package com.example.androidhdb2.activities;

import android.app.Activity;
import android.content.Context;
import java.lang.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidhdb2.R;
import com.example.androidhdb2.controllers.FlatController;
import com.example.androidhdb2.controllers.UserController;
import com.example.androidhdb2.map.BtoInfoWindowAdapter;
import com.example.androidhdb2.map.ClusterManagerRenderer;
import com.example.androidhdb2.map.FlatMarker;
import com.example.androidhdb2.model.Bookmark;
import com.example.androidhdb2.model.Flat;
import com.example.androidhdb2.model.ResaleFlat;
import com.example.androidhdb2.model.User;
import com.example.androidhdb2.utils.ResaleAPI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ClusterManager.OnClusterItemClickListener<FlatMarker> {

    private GoogleMap mMap;
    private String userid;
    private SupportMapFragment mapFragment;

    private static final int STROKE_WIDTH = 4;
    private static final int FILL_COLOR = 0x20ffff00;

    private Polygon pre_region=null;
    private static HashMap<Polygon, String> hm;

    // For FlatMarker
    private ClusterManager<FlatMarker> mClusterManager;
    private ClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<FlatMarker> mClusterMarkers = new ArrayList<>();
    private FlatMarker clickFlatMarker;

    // For FlatController
    String flatType;
    String priceRange;
    String leaseRange;
    String storeyRange;
    String areaRange;
    String region;

    // For BottomSheet
    TextView location;
    TextView price;
    TextView details;

    // For loading bar
    private ProgressBar pBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        final String[] flatdetail = extras.getStringArray("FLAT DETAILS");
        userid = extras.getString("UserID");
        Log.d("UserID",userid);

        flatType= flatdetail[0];
        if(flatdetail[1] != null)
            priceRange = flatdetail[1];
        if(flatdetail[2] != null)
            leaseRange = flatdetail[2];
        if(flatdetail[3] != null)
            storeyRange = flatdetail[3];
        if(flatdetail[4] != null)
            areaRange = flatdetail[4];

        for(int i = 0; i<flatdetail.length;i++)
        {
            Log.d("FLAT","flat detail = " + flatdetail[i]);
        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LinearLayout bottomSheetLayout = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        pBar = findViewById(R.id.progressMaps);
        pBar.setVisibility(View.GONE);

        location = findViewById(R.id.bottomLocation);
        price = findViewById(R.id.bottomPrice);
        details = findViewById(R.id.bottomDetails);
    }

    @Override
    public boolean onClusterItemClick(FlatMarker flatMarker) {
        clickFlatMarker = flatMarker;
//        Toast.makeText(this, "Flat Clicked!", Toast.LENGTH_SHORT).show();
        // Update Bottom Sheet
        Flat flat = flatMarker.getFlat();
        updateBottomSheet(flat);
        return false;
    }

    public void clickMarker(View v) {
        switch (v.getId()) {
            case (R.id.bookmark):
                if (clickFlatMarker == null) {
                    Toast.makeText(this, "No flat is chosen!", Toast.LENGTH_SHORT).show();}
                else {
                    // Flat bookmarking
                    Flat flat = clickFlatMarker.getFlat();
                    User user = UserController.importUser(this, getFilesDir(), userid);
                    Log.d("UserController", String.valueOf(user));

                    if (user.getBookmarkList().contains(new Bookmark(flat))) {
                        Toast.makeText(this, "Unbookmarked this flat", Toast.LENGTH_SHORT).show();
                        UserController.removeUserBookmark(this, getFilesDir(), userid, flat);
                    } else {
                        Toast.makeText(this, "Bookmarked this flat", Toast.LENGTH_SHORT).show();
                        UserController.addUserBookmark(this, getFilesDir(), userid, flat);
                    }
                    break;
                }
        }
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
        LatLng singaporeCenter = new LatLng(1.359443, 103.848104);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singaporeCenter, 10f));


        hm = new HashMap<Polygon, String>();
        List<LatLng> woodLandsBoudary = Arrays.asList(new LatLng(1.461403, 103.790672), new LatLng(1.458014, 103.785458),
                new LatLng(1.455032, 103.782304), new LatLng(1.452994, 103.778827), new LatLng(1.452718, 103.778365),
                new LatLng(1.451903, 103.777324), new LatLng(1.451313, 103.777131), new LatLng(1.450884, 103.774738),
                new LatLng(1.449865, 103.772603), new LatLng(1.449147, 103.771724), new LatLng(1.448471, 103.770694),
                new LatLng(1.448385, 103.769449), new LatLng(1.447763, 103.768880), new LatLng(1.446690, 103.767507),
                new LatLng(1.445232, 103.766788), new LatLng(1.445253, 103.766756), new LatLng(1.444041, 103.766692),
                new LatLng(1.443473, 103.764997), new LatLng(1.442733, 103.765522), new LatLng(1.441875, 103.765587),
                new LatLng(1.441231, 103.765962), new LatLng(1.440373, 103.768355), new LatLng(1.432619, 103.768655),
                new LatLng(1.432619, 103.768655), new LatLng(1.425132, 103.770887), new LatLng(1.421464, 103.771230),
                new LatLng(1.424939, 103.775994), new LatLng(1.425840, 103.778139), new LatLng(1.426634, 103.783418),
                new LatLng(1.426376, 103.786572), new LatLng(1.425025, 103.790456), new LatLng(1.422494, 103.795048),
                new LatLng(1.441220, 103.811055), new LatLng(1.444266, 103.811613), new LatLng(1.446047, 103.807386),
                new LatLng(1.448835, 103.803738), new LatLng(1.453876, 103.801292), new LatLng(1.455585, 103.800700),
                new LatLng(1.456658, 103.800486), new LatLng(1.456400, 103.799316), new LatLng(1.456196, 103.798790),
                new LatLng(1.455789, 103.797943), new LatLng(1.455542, 103.797578), new LatLng(1.457569, 103.794890),
                new LatLng(1.458535, 103.794032), new LatLng(1.460025, 103.793335), new LatLng(1.459865, 103.792401),
                new LatLng(1.460894, 103.791232));


        final Polygon woodlands = mMap.addPolygon(new PolygonOptions()
                .addAll(woodLandsBoudary)
                .strokeColor(Color.RED).strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));


        List<LatLng> angMokioBoundary = Arrays.asList(new LatLng(1.355501, 103.856891), new LatLng(1.356918, 103.857017),
                new LatLng(1.368030, 103.860837), new LatLng(1.369424, 103.860923), new LatLng(1.370851, 103.860590),
                new LatLng(1.375516, 103.858756), new LatLng(1.378841, 103.858283), new LatLng(1.386006, 103.858316),
                new LatLng(1.391154, 103.858133), new LatLng(1.394597, 103.857876), new LatLng(1.395927, 103.857554),
                new LatLng(1.396657, 103.857071), new LatLng(1.397300, 103.856288), new LatLng(1.397665, 103.855419),
                new LatLng(1.397686, 103.854099), new LatLng(1.396667, 103.851149), new LatLng(1.396549, 103.849808),
                new LatLng(1.396657, 103.844658), new LatLng(1.396281, 103.841600), new LatLng(1.393010, 103.828114),
                new LatLng(1.392881, 103.826826), new LatLng(1.393117, 103.823447), new LatLng(1.393525, 103.821591),
                new LatLng(1.394329, 103.818866), new LatLng(1.390822, 103.818233), new LatLng(1.390768, 103.817932),
                new LatLng(1.389733, 103.817645), new LatLng(1.389318, 103.817693), new LatLng(1.388713, 103.817883),
                new LatLng(1.388369, 103.817812), new LatLng(1.387503, 103.816767), new LatLng(1.387111, 103.816672),
                new LatLng(1.386518, 103.816791), new LatLng(1.386304, 103.817384), new LatLng(1.386470, 103.818643),
                new LatLng(1.386257, 103.818904), new LatLng(1.385509, 103.819248), new LatLng(1.383610, 103.819165),
                new LatLng(1.380667, 103.817681), new LatLng(1.380359, 103.817657), new LatLng(1.380074, 103.818025),
                new LatLng(1.380442, 103.819735), new LatLng(1.380359, 103.820720), new LatLng(1.379623, 103.821444),
                new LatLng(1.379587, 103.821978), new LatLng(1.379659, 103.822583), new LatLng(1.378864, 103.823426),
                new LatLng(1.378686, 103.824103), new LatLng(1.379576, 103.825207), new LatLng(1.379730, 103.825753),
                new LatLng(1.379184, 103.827070), new LatLng(1.378330, 103.827604), new LatLng(1.377724, 103.827794),
                new LatLng(1.376704, 103.826406), new LatLng(1.376502, 103.826227), new LatLng(1.374663, 103.826453),
                new LatLng(1.373761, 103.826180), new LatLng(1.373322, 103.826346), new LatLng(1.373025, 103.827153),
                new LatLng(1.372539, 103.827640), new LatLng(1.371957, 103.827782), new LatLng(1.370320, 103.827782),
                new LatLng(1.369584, 103.827320), new LatLng(1.369038, 103.827355), new LatLng(1.367555, 103.828269),
                new LatLng(1.367982, 103.828352), new LatLng(1.367578, 103.832483), new LatLng(1.364541, 103.841397),
                new LatLng(1.364042, 103.846110), new LatLng(1.362666, 103.849457), new LatLng(1.359675, 103.853030),
                new LatLng(1.358500, 103.853873), new LatLng(1.356875, 103.854644), new LatLng(1.355949, 103.855618));

        Polygon angMoKio = mMap.addPolygon(new PolygonOptions()
                .addAll(angMokioBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));


        List<LatLng> hougangBoundary = Arrays.asList(new LatLng(1.386931, 103.873637), new LatLng(1.384474, 103.876513),
                new LatLng(1.383393, 103.876880), new LatLng(1.378407, 103.876656), new LatLng(1.377999, 103.876707),
                new LatLng(1.376184, 103.877023), new LatLng(1.374073, 103.877023), new LatLng(1.369260, 103.876258),
                new LatLng(1.368077, 103.875952), new LatLng(1.363117, 103.873590), new LatLng(1.362611, 103.873346),
                new LatLng(1.362089, 103.873307), new LatLng(1.358735, 103.874082), new LatLng(1.358295, 103.874102),
                new LatLng(1.358152, 103.874673), new LatLng(1.357684, 103.875149), new LatLng(1.356094, 103.876149),
                new LatLng(1.354748, 103.877138), new LatLng(1.353871, 103.878077), new LatLng(1.352464, 103.876475),
                new LatLng(1.352005, 103.877414), new LatLng(1.351709, 103.878403), new LatLng(1.348782, 103.880224),
                new LatLng(1.347640, 103.880404), new LatLng(1.346710, 103.880711), new LatLng(1.344574, 103.882318),
                new LatLng(1.342608, 103.883302), new LatLng(1.342164, 103.883619), new LatLng(1.339711, 103.886009),
                new LatLng(1.338602, 103.886753), new LatLng(1.334796, 103.888561), new LatLng(1.333506, 103.888963),
                new LatLng(1.334214, 103.891047), new LatLng(1.334986, 103.892686), new LatLng(1.336667, 103.895118),
                new LatLng(1.338750, 103.897064), new LatLng(1.342217, 103.898819), new LatLng(1.343497, 103.899105),
                new LatLng(1.344797, 103.899105), new LatLng(1.346647, 103.898597), new LatLng(1.347704, 103.898343),
                new LatLng(1.347366, 103.895900), new LatLng(1.347789, 103.895562), new LatLng(1.347926, 103.893405),
                new LatLng(1.349195, 103.893257), new LatLng(1.353926, 103.895453), new LatLng(1.353450, 103.896586),
                new LatLng(1.353546, 103.897043), new LatLng(1.357002, 103.898783), new LatLng(1.357444, 103.899652),
                new LatLng(1.357429, 103.900787), new LatLng(1.358313, 103.901281), new LatLng(1.358436, 103.902211),
                new LatLng(1.358528, 103.902374), new LatLng(1.358388, 103.902727), new LatLng(1.365572, 103.906470),
                new LatLng(1.366349, 103.906753), new LatLng(1.374254, 103.909306), new LatLng(1.375786, 103.909958),
                new LatLng(1.378350, 103.908395), new LatLng(1.380170, 103.905695), new LatLng(1.380504, 103.904329),
                new LatLng(1.379336, 103.896773), new LatLng(1.379958, 103.895225), new LatLng(1.380155, 103.891311),
                new LatLng(1.380565, 103.890461), new LatLng(1.387405, 103.881919), new LatLng(1.388240, 103.879719),
                new LatLng(1.388437, 103.878125), new LatLng(1.388270, 103.877154), new LatLng(1.387254, 103.874742));

        Polygon hougang = mMap.addPolygon(new PolygonOptions()
                .addAll(hougangBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));


        List<LatLng> punggolBoundary = Arrays.asList(new LatLng(1.401158, 103.886884), new LatLng(1.400142, 103.894405),
                new LatLng(1.399724, 103.896916), new LatLng(1.399037, 103.899233), new LatLng(1.397783, 103.902044),
                new LatLng(1.395176, 103.905949), new LatLng(1.392637, 103.908487), new LatLng(1.387521, 103.912907),
                new LatLng(1.385848, 103.914291), new LatLng(1.386920, 103.914999), new LatLng(1.390406, 103.916512),
                new LatLng(1.393871, 103.919451), new LatLng(1.396026, 103.921619), new LatLng(1.398086, 103.924354),
                new LatLng(1.399480, 103.926811), new LatLng(1.400456, 103.929290), new LatLng(1.401110, 103.929247),
                new LatLng(1.401100, 103.928946), new LatLng(1.401754, 103.929000), new LatLng(1.401518, 103.929676),
                new LatLng(1.401727, 103.930371), new LatLng(1.402233, 103.931002), new LatLng(1.402844, 103.931195),
                new LatLng(1.403745, 103.931120), new LatLng(1.406641, 103.927837), new LatLng(1.409773, 103.924608),
                new LatLng(1.410674, 103.924157), new LatLng(1.411489, 103.923256), new LatLng(1.412701, 103.921367),
                new LatLng(1.413409, 103.919297), new LatLng(1.414085, 103.918803), new LatLng(1.414793, 103.918889),
                new LatLng(1.415361, 103.918428), new LatLng(1.415983, 103.917505), new LatLng(1.417313, 103.916314),
                new LatLng(1.417334, 103.915746), new LatLng(1.417088, 103.915198), new LatLng(1.417109, 103.913997),
                new LatLng(1.418439, 103.914083), new LatLng(1.419861, 103.913129), new LatLng(1.421116, 103.912650),
                new LatLng(1.421439, 103.910702), new LatLng(1.420979, 103.908024), new LatLng(1.420059, 103.907044),
                new LatLng(1.420351, 103.905766), new LatLng(1.420373, 103.904824), new LatLng(1.420276, 103.904053),
                new LatLng(1.419245, 103.901997), new LatLng(1.416882, 103.899208), new LatLng(1.411579, 103.896552),
                new LatLng(1.407131, 103.894182), new LatLng(1.406241, 103.893180), new LatLng(1.405382, 103.892454),
                new LatLng(1.404597, 103.890615), new LatLng(1.403393, 103.888207), new LatLng(1.402182, 103.887153));

        Polygon punggol = mMap.addPolygon(new PolygonOptions()
                .addAll(punggolBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> sengkangBoundary = Arrays.asList(new LatLng(1.397052, 103.852186), new LatLng(1.397668, 103.854085),
                new LatLng(1.397697, 103.855412), new LatLng(1.397122, 103.856547), new LatLng(1.395944, 103.857549),
                new LatLng(1.394455, 103.857903), new LatLng(1.391110, 103.858124), new LatLng(1.389047, 103.865731),
                new LatLng(1.387868, 103.870315), new LatLng(1.387234, 103.873043), new LatLng(1.386911, 103.873650),
                new LatLng(1.387269, 103.874695), new LatLng(1.388250, 103.877130), new LatLng(1.388228, 103.877409),
                new LatLng(1.388386, 103.878074), new LatLng(1.388327, 103.879187), new LatLng(1.388032, 103.880300),
                new LatLng(1.387369, 103.881841), new LatLng(1.384554, 103.885467), new LatLng(1.382064, 103.888673),
                new LatLng(1.380531, 103.890501), new LatLng(1.380221, 103.891149), new LatLng(1.380096, 103.891754),
                new LatLng(1.379927, 103.895233), new LatLng(1.379875, 103.895446), new LatLng(1.379345, 103.896891),
                new LatLng(1.380067, 103.901896), new LatLng(1.380431, 103.904366), new LatLng(1.380405, 103.904872),
                new LatLng(1.380089, 103.905822), new LatLng(1.378455, 103.908280), new LatLng(1.376885, 103.909445),
                new LatLng(1.375796, 103.909965), new LatLng(1.377303, 103.911320), new LatLng(1.379633, 103.915120),
                new LatLng(1.382355, 103.917451), new LatLng(1.386344, 103.913866), new LatLng(1.391574, 103.909433),
                new LatLng(1.394031, 103.907191), new LatLng(1.395487, 103.905518), new LatLng(1.396741, 103.903720),
                new LatLng(1.398387, 103.900870), new LatLng(1.399387, 103.898222), new LatLng(1.400071, 103.895220),
                new LatLng(1.400654, 103.890989), new LatLng(1.401249, 103.886214), new LatLng(1.401287, 103.881160),
                new LatLng(1.401084, 103.871812), new LatLng(1.400996, 103.867302), new LatLng(1.400768, 103.859588),
                new LatLng(1.400135, 103.857282), new LatLng(1.399793, 103.856624), new LatLng(1.401692, 103.855471),
                new LatLng(1.400793, 103.855838), new LatLng(1.400173, 103.855813), new LatLng(1.399160, 103.855205),
                new LatLng(1.398007, 103.853774));

        Polygon sengkang = mMap.addPolygon(new PolygonOptions()
                .addAll(sengkangBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> serangoonBoundary = Arrays.asList(new LatLng(1.391094, 103.858123), new LatLng(1.384155, 103.858394),
                new LatLng(1.378629, 103.858323), new LatLng(1.377769, 103.858347), new LatLng(1.375154, 103.858854),
                new LatLng(1.374094, 103.859207), new LatLng(1.370760, 103.860657), new LatLng(1.369440, 103.860904),
                new LatLng(1.368318, 103.860914), new LatLng(1.366774, 103.860455), new LatLng(1.356395, 103.856964),
                new LatLng(1.354801, 103.856865), new LatLng(1.353446, 103.856978), new LatLng(1.351964, 103.857359),
                new LatLng(1.345712, 103.859617), new LatLng(1.343384, 103.860460), new LatLng(1.343694, 103.862531),
                new LatLng(1.344312, 103.864598), new LatLng(1.344287, 103.865635), new LatLng(1.343906, 103.866497),
                new LatLng(1.342747, 103.869154), new LatLng(1.342761, 103.870128), new LatLng(1.343551, 103.871836),
                new LatLng(1.343480, 103.873854), new LatLng(1.343607, 103.874701), new LatLng(1.344293, 103.876589),
                new LatLng(1.344253, 103.877287), new LatLng(1.343605, 103.878371), new LatLng(1.343123, 103.879159),
                new LatLng(1.342053, 103.881395), new LatLng(1.341005, 103.883679), new LatLng(1.340916, 103.884544),
                new LatLng(1.340929, 103.884973), new LatLng(1.342214, 103.883580), new LatLng(1.342823, 103.883150),
                new LatLng(1.343934, 103.882630), new LatLng(1.345117, 103.881957), new LatLng(1.346015, 103.881355),
                new LatLng(1.346718, 103.880651), new LatLng(1.347136, 103.880457), new LatLng(1.348289, 103.880314),
                new LatLng(1.348932, 103.880162), new LatLng(1.350864, 103.879319), new LatLng(1.351537, 103.878672),
                new LatLng(1.351811, 103.878108), new LatLng(1.352104, 103.877155), new LatLng(1.352483, 103.876471),
                new LatLng(1.353863, 103.878059), new LatLng(1.354755, 103.877119), new LatLng(1.356231, 103.876033),
                new LatLng(1.357817, 103.875030), new LatLng(1.358143, 103.874685), new LatLng(1.358316, 103.874328),
                new LatLng(1.358265, 103.874073), new LatLng(1.358734, 103.874094), new LatLng(1.362242, 103.873288),
                new LatLng(1.362457, 103.873287), new LatLng(1.362868, 103.873445), new LatLng(1.364201, 103.874160),
                new LatLng(1.368455, 103.876047), new LatLng(1.369822, 103.876374), new LatLng(1.374056, 103.877018),
                new LatLng(1.376568, 103.876985), new LatLng(1.378618, 103.876669), new LatLng(1.380511, 103.876739),
                new LatLng(1.383402, 103.876862), new LatLng(1.384454, 103.876476), new LatLng(1.386767, 103.873969),
                new LatLng(1.387363, 103.872637), new LatLng(1.388253, 103.868604), new LatLng(1.389620, 103.863713));

        Polygon serangoon = mMap.addPolygon(new PolygonOptions()
                .addAll(serangoonBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> bedokBoundary = Arrays.asList(new LatLng(1.337750, 103.896175), new LatLng(1.329449, 103.904826),
                new LatLng(1.328253, 103.905612), new LatLng(1.327048, 103.905844), new LatLng(1.326037, 103.905830),
                new LatLng(1.322251, 103.905341), new LatLng(1.319310, 103.904981), new LatLng(1.317602, 103.905119),
                new LatLng(1.316842, 103.905237), new LatLng(1.315838, 103.905615), new LatLng(1.311165, 103.907617),
                new LatLng(1.310458, 103.907912), new LatLng(1.308792, 103.908325), new LatLng(1.308174, 103.908605),
                new LatLng(1.305463, 103.909960), new LatLng(1.305797, 103.910714), new LatLng(1.306801, 103.915147),
                new LatLng(1.307282, 103.916985), new LatLng(1.307425, 103.917971), new LatLng(1.304683, 103.918742),
                new LatLng(1.303089, 103.919070), new LatLng(1.301858, 103.919353), new LatLng(1.301876, 103.919710),
                new LatLng(1.302068, 103.919764), new LatLng(1.302499, 103.920648), new LatLng(1.302695, 103.921542),
                new LatLng(1.302830, 103.921599), new LatLng(1.303273, 103.923203), new LatLng(1.303238, 103.923425),
                new LatLng(1.303026, 103.923544), new LatLng(1.303090, 103.923746), new LatLng(1.303258, 103.923833),
                new LatLng(1.303362, 103.923943), new LatLng(1.303667, 103.925369), new LatLng(1.303643, 103.925519),
                new LatLng(1.303511, 103.925712), new LatLng(1.303396, 103.925605), new LatLng(1.303337, 103.925694),
                new LatLng(1.303418, 103.925950), new LatLng(1.303516, 103.925926), new LatLng(1.303555, 103.925878),
                new LatLng(1.303706, 103.925968), new LatLng(1.303818, 103.926243), new LatLng(1.303843, 103.926569),
                new LatLng(1.303782, 103.926716), new LatLng(1.303675, 103.926774), new LatLng(1.303632, 103.926684),
                new LatLng(1.303571, 103.926736), new LatLng(1.303554, 103.926713), new LatLng(1.303640, 103.926981),
                new LatLng(1.303779, 103.926894), new LatLng(1.303945, 103.927047), new LatLng(1.304051, 103.927427),
                new LatLng(1.304018, 103.927646), new LatLng(1.303925, 103.927806), new LatLng(1.303891, 103.927698),
                new LatLng(1.303773, 103.927790), new LatLng(1.303866, 103.928085), new LatLng(1.303975, 103.928038),
                new LatLng(1.304214, 103.928326), new LatLng(1.304557, 103.929674), new LatLng(1.304932, 103.930424),
                new LatLng(1.305028, 103.931095), new LatLng(1.305220, 103.931494), new LatLng(1.305539, 103.931829),
                new LatLng(1.305451, 103.932579), new LatLng(1.305196, 103.933480), new LatLng(1.307098, 103.938709),
                new LatLng(1.310776, 103.948317), new LatLng(1.310529, 103.948989), new LatLng(1.310996, 103.950674),
                new LatLng(1.310868, 103.951209), new LatLng(1.310884, 103.951752), new LatLng(1.311275, 103.952597),
                new LatLng(1.311259, 103.953379), new LatLng(1.311395, 103.953794), new LatLng(1.311810, 103.955023),
                new LatLng(1.311738, 103.955358), new LatLng(1.311506, 103.955622), new LatLng(1.312536, 103.957848),
                new LatLng(1.312457, 103.958062), new LatLng(1.312999, 103.958710), new LatLng(1.314639, 103.962863),
                new LatLng(1.313912, 103.963260), new LatLng(1.314467, 103.964596), new LatLng(1.321331, 103.959689),
                new LatLng(1.324597, 103.957374), new LatLng(1.330290, 103.952379), new LatLng(1.333801, 103.949650),
                new LatLng(1.334456, 103.949116), new LatLng(1.336169, 103.940550), new LatLng(1.344034, 103.931111),
                new LatLng(1.345666, 103.928956), new LatLng(1.345650, 103.928925), new LatLng(1.348050, 103.924219),
                new LatLng(1.346115, 103.919881), new LatLng(1.345076, 103.918420), new LatLng(1.343983, 103.917194),
                new LatLng(1.342658, 103.916045), new LatLng(1.344137, 103.914201), new LatLng(1.342702, 103.912346),
                new LatLng(1.341664, 103.911440), new LatLng(1.340914, 103.910016), new LatLng(1.340556, 103.907622),
                new LatLng(1.339231, 103.907588), new LatLng(1.339143, 103.901493), new LatLng(1.338867, 103.901393),
                new LatLng(1.340335, 103.897870), new LatLng(1.338794, 103.897059));

        Polygon bedok = mMap.addPolygon(new PolygonOptions()
                .addAll(bedokBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));


        List<LatLng> pasirRisBoundary = Arrays.asList(new LatLng(1.385921, 103.914231), new LatLng(1.380988, 103.918823),
                new LatLng(1.379701, 103.920754), new LatLng(1.378113, 103.924831), new LatLng(1.377255, 103.928414),
                new LatLng(1.374145, 103.935431), new LatLng(1.371292, 103.940581), new LatLng(1.369768, 103.942405),
                new LatLng(1.368674, 103.943564), new LatLng(1.367709, 103.945173), new LatLng(1.367173, 103.946482),
                new LatLng(1.365371, 103.953863), new LatLng(1.364749, 103.955580), new LatLng(1.363934, 103.957168),
                new LatLng(1.361917, 103.959721), new LatLng(1.359708, 103.961545), new LatLng(1.357198, 103.962811),
                new LatLng(1.352693, 103.964227), new LatLng(1.353851, 103.968004), new LatLng(1.354495, 103.968562),
                new LatLng(1.359836, 103.970064), new LatLng(1.361874, 103.971931), new LatLng(1.362550, 103.973636),
                new LatLng(1.363151, 103.974366), new LatLng(1.364084, 103.974817), new LatLng(1.366540, 103.975578),
                new LatLng(1.367141, 103.976104), new LatLng(1.369039, 103.978561), new LatLng(1.370273, 103.979773),
                new LatLng(1.370659, 103.979977), new LatLng(1.371388, 103.979988), new LatLng(1.371903, 103.979773),
                new LatLng(1.372504, 103.979001), new LatLng(1.373201, 103.975836), new LatLng(1.373930, 103.974972),
                new LatLng(1.377159, 103.978051), new LatLng(1.377942, 103.978416), new LatLng(1.378778, 103.978566),
                new LatLng(1.379770, 103.978078), new LatLng(1.386013, 103.974559), new LatLng(1.386763, 103.973808),
                new LatLng(1.387386, 103.972757), new LatLng(1.386994, 103.971566), new LatLng(1.384527, 103.969056),
                new LatLng(1.383154, 103.967232), new LatLng(1.381942, 103.967629), new LatLng(1.381304, 103.967591),
                new LatLng(1.380897, 103.967076), new LatLng(1.380242, 103.966980), new LatLng(1.380242, 103.966948),
                new LatLng(1.380800, 103.966808), new LatLng(1.381615, 103.966787), new LatLng(1.381583, 103.965971),
                new LatLng(1.381240, 103.966089), new LatLng(1.382055, 103.964587), new LatLng(1.381572, 103.961851),
                new LatLng(1.381283, 103.960146), new LatLng(1.381680, 103.956283), new LatLng(1.382302, 103.952281),
                new LatLng(1.382650, 103.951846), new LatLng(1.382543, 103.951524), new LatLng(1.382586, 103.950752),
                new LatLng(1.382929, 103.950119), new LatLng(1.383079, 103.949293), new LatLng(1.384988, 103.945677),
                new LatLng(1.385546, 103.944883), new LatLng(1.386157, 103.944400), new LatLng(1.388592, 103.940484),
                new LatLng(1.389922, 103.937888), new LatLng(1.391381, 103.937030), new LatLng(1.391853, 103.937663),
                new LatLng(1.392067, 103.937491), new LatLng(1.391670, 103.936783), new LatLng(1.391853, 103.936708),
                new LatLng(1.392496, 103.937255), new LatLng(1.392759, 103.937083), new LatLng(1.392137, 103.936504),
                new LatLng(1.396749, 103.934272), new LatLng(1.398403, 103.932558), new LatLng(1.398264, 103.932505),
                new LatLng(1.399106, 103.931287), new LatLng(1.399535, 103.930257), new LatLng(1.399127, 103.928262),
                new LatLng(1.399492, 103.926802), new LatLng(1.398055, 103.924270), new LatLng(1.393979, 103.919528),
                new LatLng(1.390440, 103.916481), new LatLng(1.386900, 103.915044));

        final Polygon pasirRis = mMap.addPolygon(new PolygonOptions()
                .addAll(pasirRisBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> tampinesBoundary = Arrays.asList(new LatLng(1.375673, 103.931959), new LatLng(1.372755, 103.930586),
                new LatLng(1.370911, 103.930371), new LatLng(1.368079, 103.930801), new LatLng(1.361150, 103.930908),
                new LatLng(1.359691, 103.930736), new LatLng(1.358233, 103.930393), new LatLng(1.349909, 103.925930),
                new LatLng(1.348622, 103.924986), new LatLng(1.348065, 103.924299), new LatLng(1.346348, 103.927818),
                new LatLng(1.342230, 103.933311), new LatLng(1.336309, 103.940349), new LatLng(1.334421, 103.949147),
                new LatLng(1.330217, 103.952408), new LatLng(1.324382, 103.957472), new LatLng(1.314600, 103.964682),
                new LatLng(1.315029, 103.965455), new LatLng(1.314814, 103.966313), new LatLng(1.315586, 103.968373),
                new LatLng(1.316015, 103.970433), new LatLng(1.315930, 103.970776), new LatLng(1.316530, 103.976355),
                new LatLng(1.315586, 103.976527), new LatLng(1.316530, 103.978673), new LatLng(1.316702, 103.981505),
                new LatLng(1.316359, 103.984509), new LatLng(1.319276, 103.985797), new LatLng(1.321078, 103.980733),
                new LatLng(1.322108, 103.979874), new LatLng(1.323481, 103.979960), new LatLng(1.325969, 103.980990),
                new LatLng(1.326999, 103.980733), new LatLng(1.327600, 103.980303), new LatLng(1.335065, 103.982878),
                new LatLng(1.336523, 103.979703), new LatLng(1.337038, 103.979788), new LatLng(1.340385, 103.972150),
                new LatLng(1.342273, 103.970090), new LatLng(1.346906, 103.967086), new LatLng(1.350596, 103.964768),
                new LatLng(1.357203, 103.962708), new LatLng(1.359434, 103.961678), new LatLng(1.361922, 103.959790),
                new LatLng(1.363982, 103.957215), new LatLng(1.365612, 103.953267), new LatLng(1.366899, 103.947344),
                new LatLng(1.367843, 103.944770), new LatLng(1.369988, 103.942109), new LatLng(1.371618, 103.940306),
                new LatLng(1.373506, 103.936358));

        Polygon tampines = mMap.addPolygon(new PolygonOptions()
                .addAll(tampinesBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> bukitBatokBoundary = Arrays.asList(new LatLng(1.379716, 103.761252), new LatLng(1.377164, 103.755759),
                new LatLng(1.376713, 103.754343), new LatLng(1.375297, 103.753034), new LatLng(1.373238, 103.752154),
                new LatLng(1.372080, 103.751489), new LatLng(1.371736, 103.751188), new LatLng(1.365580, 103.744494),
                new LatLng(1.357042, 103.737262), new LatLng(1.356012, 103.736726), new LatLng(1.354382, 103.736554),
                new LatLng(1.353438, 103.736790), new LatLng(1.351186, 103.737777), new LatLng(1.348998, 103.738292),
                new LatLng(1.347904, 103.738206), new LatLng(1.345994, 103.737252), new LatLng(1.345351, 103.738625),
                new LatLng(1.345083, 103.739773), new LatLng(1.345029, 103.740620), new LatLng(1.344933, 103.741983),
                new LatLng(1.344664, 103.743013), new LatLng(1.343924, 103.744762), new LatLng(1.340379, 103.751032),
                new LatLng(1.333972, 103.762351), new LatLng(1.332899, 103.764325), new LatLng(1.332513, 103.765506),
                new LatLng(1.335602, 103.765570), new LatLng(1.336031, 103.765892), new LatLng(1.335881, 103.765484),
                new LatLng(1.336589, 103.764583), new LatLng(1.339399, 103.764154), new LatLng(1.339635, 103.764669),
                new LatLng(1.341630, 103.764089), new LatLng(1.341909, 103.766857), new LatLng(1.342531, 103.766857),
                new LatLng(1.342853, 103.767115), new LatLng(1.343282, 103.765935), new LatLng(1.343582, 103.765913),
                new LatLng(1.344590, 103.765248), new LatLng(1.344740, 103.764197), new LatLng(1.346650, 103.762968),
                new LatLng(1.347186, 103.765135), new LatLng(1.347272, 103.767067), new LatLng(1.348902, 103.770328),
                new LatLng(1.352420, 103.769792), new LatLng(1.353707, 103.769427), new LatLng(1.355788, 103.768654),
                new LatLng(1.356496, 103.768225), new LatLng(1.357097, 103.767710), new LatLng(1.357611, 103.767539),
                new LatLng(1.361151, 103.767045), new LatLng(1.363511, 103.767582), new LatLng(1.364240, 103.767560),
                new LatLng(1.364969, 103.767260), new LatLng(1.370225, 103.763848), new LatLng(1.371019, 103.763440),
                new LatLng(1.375223, 103.762561), new LatLng(1.378055, 103.762024), new LatLng(1.378655, 103.761767));

        Polygon bukitBatok = mMap.addPolygon(new PolygonOptions()
                .addAll(bukitBatokBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> bukitPanjangBoundary = Arrays.asList(new LatLng(1.388710, 103.755160), new LatLng(1.384913, 103.757692),
                new LatLng(1.383240, 103.759323), new LatLng(1.378478, 103.761812), new LatLng(1.377791, 103.762091),
                new LatLng(1.371334, 103.763335), new LatLng(1.370498, 103.763700), new LatLng(1.364513, 103.767519),
                new LatLng(1.364019, 103.767605), new LatLng(1.363419, 103.767584), new LatLng(1.361102, 103.767047),
                new LatLng(1.357498, 103.767541), new LatLng(1.357026, 103.767777), new LatLng(1.356361, 103.768313),
                new LatLng(1.355310, 103.768828), new LatLng(1.352371, 103.769815), new LatLng(1.350012, 103.770094),
                new LatLng(1.348939, 103.770309), new LatLng(1.346408, 103.772133), new LatLng(1.344091, 103.775888),
                new LatLng(1.340594, 103.778763), new LatLng(1.341511, 103.779466), new LatLng(1.341822, 103.780024),
                new LatLng(1.343024, 103.783446), new LatLng(1.344761, 103.787330), new LatLng(1.345662, 103.788210),
                new LatLng(1.347958, 103.786601), new LatLng(1.348151, 103.787212), new LatLng(1.349127, 103.788188),
                new LatLng(1.348837, 103.788210), new LatLng(1.349137, 103.789519), new LatLng(1.348000, 103.789347),
                new LatLng(1.348794, 103.790291), new LatLng(1.349266, 103.791579), new LatLng(1.351519, 103.790141),
                new LatLng(1.352484, 103.789176), new LatLng(1.355487, 103.784798), new LatLng(1.358319, 103.782137),
                new LatLng(1.361322, 103.780507), new LatLng(1.364025, 103.779863), new LatLng(1.368401, 103.779090),
                new LatLng(1.382302, 103.775400), new LatLng(1.390453, 103.774327), new LatLng(1.390668, 103.765787),
                new LatLng(1.390067, 103.761881), new LatLng(1.389230, 103.759306), new LatLng(1.388887, 103.757375),
                new LatLng(1.388758, 103.756367));

        Polygon bukitPanjang = mMap.addPolygon(new PolygonOptions()
                .addAll(bukitPanjangBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> choaChuKangBoundary = Arrays.asList(new LatLng(1.379035, 103.732675), new LatLng(1.377018, 103.734391),
                new LatLng(1.375431, 103.736194), new LatLng(1.374358, 103.737567), new LatLng(1.373457, 103.739198),
                new LatLng(1.372428, 103.742674), new LatLng(1.371698, 103.745549), new LatLng(1.369124, 103.748425),
                new LatLng(1.371870, 103.751322), new LatLng(1.372900, 103.752008), new LatLng(1.374165, 103.752394),
                new LatLng(1.375967, 103.753467), new LatLng(1.376825, 103.754411), new LatLng(1.377254, 103.755956),
                new LatLng(1.379743, 103.761192), new LatLng(1.383303, 103.759304), new LatLng(1.384848, 103.757759),
                new LatLng(1.387787, 103.755635), new LatLng(1.388709, 103.755162), new LatLng(1.388967, 103.753231),
                new LatLng(1.392163, 103.752909), new LatLng(1.397612, 103.752051), new LatLng(1.399542, 103.751987),
                new LatLng(1.401816, 103.752373), new LatLng(1.403167, 103.752867), new LatLng(1.403725, 103.752791),
                new LatLng(1.404165, 103.752352), new LatLng(1.404894, 103.750560), new LatLng(1.404894, 103.750560),
                new LatLng(1.405581, 103.746107), new LatLng(1.403736, 103.744477), new LatLng(1.402910, 103.743983),
                new LatLng(1.401526, 103.743500), new LatLng(1.399639, 103.743189), new LatLng(1.396850, 103.742835),
                new LatLng(1.396013, 103.742620), new LatLng(1.395574, 103.742481), new LatLng(1.395021, 103.742207),
                new LatLng(1.392265, 103.740587), new LatLng(1.392163, 103.740550), new LatLng(1.390575, 103.740142),
                new LatLng(1.389481, 103.740314), new LatLng(1.388902, 103.740507), new LatLng(1.386650, 103.741558),
                new LatLng(1.381094, 103.735121));

        Polygon choaChuKang = mMap.addPolygon(new PolygonOptions()
                .addAll(choaChuKangBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> clementiBoundary = Arrays.asList(new LatLng(1.341725, 103.748751), new LatLng(1.341274, 103.748493),
                new LatLng(1.340824, 103.748665), new LatLng(1.338764, 103.748751), new LatLng(1.330655, 103.749480),
                new LatLng(1.326719, 103.750006), new LatLng(1.322943, 103.751937), new LatLng(1.322579, 103.752012),
                new LatLng(1.321292, 103.751680), new LatLng(1.313011, 103.747238), new LatLng(1.309793, 103.747324),
                new LatLng(1.309547, 103.747539), new LatLng(1.309450, 103.747968), new LatLng(1.309364, 103.749899),
                new LatLng(1.308506, 103.751894), new LatLng(1.307691, 103.753053), new LatLng(1.305996, 103.754190),
                new LatLng(1.304355, 103.754094), new LatLng(1.298536, 103.756986), new LatLng(1.301690, 103.762190),
                new LatLng(1.293806, 103.765773), new LatLng(1.292744, 103.766331), new LatLng(1.292133, 103.766728),
                new LatLng(1.291596, 103.767340), new LatLng(1.293881, 103.769024), new LatLng(1.294653, 103.769432),
                new LatLng(1.295029, 103.769539), new LatLng(1.300113, 103.769850), new LatLng(1.300832, 103.770086),
                new LatLng(1.302773, 103.771009), new LatLng(1.303513, 103.771181), new LatLng(1.304543, 103.771234),
                new LatLng(1.306559, 103.771256), new LatLng(1.307128, 103.771342), new LatLng(1.312180, 103.772296),
                new LatLng(1.316492, 103.771739), new LatLng(1.320332, 103.770805), new LatLng(1.321018, 103.770794),
                new LatLng(1.321801, 103.771073), new LatLng(1.324482, 103.772887), new LatLng(1.324922, 103.773681),
                new LatLng(1.325501, 103.774989), new LatLng(1.326488, 103.775665), new LatLng(1.329663, 103.769625),
                new LatLng(1.329749, 103.769292), new LatLng(1.330446, 103.768810), new LatLng(1.331057, 103.768177),
                new LatLng(1.331369, 103.767662), new LatLng(1.332141, 103.765409), new LatLng(1.332870, 103.764454),
                new LatLng(1.337182, 103.756632), new LatLng(1.340325, 103.751086), new LatLng(1.341333, 103.749401));

        Polygon clementi = mMap.addPolygon(new PolygonOptions()
                .addAll(clementiBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> jurongEastBoundary = Arrays.asList(new LatLng(1.353480, 103.728186), new LatLng(1.344556, 103.728186),
                new LatLng(1.345114, 103.723937), new LatLng(1.344942, 103.721877), new LatLng(1.344256, 103.721834),
                new LatLng(1.343141, 103.721405), new LatLng(1.341939, 103.721405), new LatLng(1.340824, 103.722649),
                new LatLng(1.338250, 103.724366), new LatLng(1.337306, 103.724967), new LatLng(1.335504, 103.725053),
                new LatLng(1.329669, 103.724795), new LatLng(1.328553, 103.724795), new LatLng(1.325293, 103.725911),
                new LatLng(1.323491, 103.719645), new LatLng(1.320831, 103.720246), new LatLng(1.318428, 103.720332),
                new LatLng(1.314395, 103.719388), new LatLng(1.314738, 103.717414), new LatLng(1.314052, 103.709753),
                new LatLng(1.310019, 103.710118), new LatLng(1.309890, 103.709603), new LatLng(1.308174, 103.709689),
                new LatLng(1.308431, 103.710719), new LatLng(1.307316, 103.711320), new LatLng(1.297362, 103.711062),
                new LatLng(1.297362, 103.711921), new LatLng(1.299765, 103.711921), new LatLng(1.299593, 103.721963),
                new LatLng(1.299979, 103.721920), new LatLng(1.299958, 103.720354), new LatLng(1.301073, 103.721298),
                new LatLng(1.305964, 103.715461), new LatLng(1.306479, 103.712629), new LatLng(1.307423, 103.712114),
                new LatLng(1.308968, 103.712114), new LatLng(1.308539, 103.715719), new LatLng(1.303047, 103.722242),
                new LatLng(1.300902, 103.726962), new LatLng(1.301674, 103.729967), new LatLng(1.299443, 103.738550),
                new LatLng(1.299443, 103.740095), new LatLng(1.299872, 103.741296), new LatLng(1.301588, 103.743099),
                new LatLng(1.300644, 103.744472), new LatLng(1.299271, 103.743871), new LatLng(1.298156, 103.743957),
                new LatLng(1.297641, 103.745073), new LatLng(1.296440, 103.749450), new LatLng(1.297469, 103.755544),
                new LatLng(1.298585, 103.757089), new LatLng(1.304334, 103.754257), new LatLng(1.306393, 103.753999),
                new LatLng(1.308453, 103.752197), new LatLng(1.309568, 103.749536), new LatLng(1.309740, 103.747219),
                new LatLng(1.313172, 103.747304), new LatLng(1.322525, 103.751939), new LatLng(1.326730, 103.750137),
                new LatLng(1.341146, 103.748592), new LatLng(1.341832, 103.748592), new LatLng(1.344921, 103.742755),
                new LatLng(1.345178, 103.739065), new LatLng(1.346637, 103.736318), new LatLng(1.350928, 103.732370),
                new LatLng(1.352558, 103.730224));

        Polygon jurongEast = mMap.addPolygon(new PolygonOptions()
                .addAll(jurongEastBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));


        List<LatLng> jurongWestBoundary = Arrays.asList(new LatLng(1.330931, 103.674811), new LatLng(1.330416, 103.676012),
                new LatLng(1.330245, 103.679445), new LatLng(1.327756, 103.679274), new LatLng(1.327585, 103.706053),
                new LatLng(1.327842, 103.706911), new LatLng(1.331618, 103.721073), new LatLng(1.330416, 103.720988),
                new LatLng(1.329730, 103.720988), new LatLng(1.325954, 103.719099), new LatLng(1.323637, 103.719528),
                new LatLng(1.325182, 103.726052), new LatLng(1.328099, 103.724807), new LatLng(1.329451, 103.724743),
                new LatLng(1.336944, 103.725016), new LatLng(1.337431, 103.724905), new LatLng(1.340756, 103.722759),
                new LatLng(1.341478, 103.721681), new LatLng(1.341947, 103.721410), new LatLng(1.342344, 103.721356),
                new LatLng(1.343306, 103.721540), new LatLng(1.343874, 103.721852), new LatLng(1.344906, 103.721836),
                new LatLng(1.345088, 103.723566), new LatLng(1.344360, 103.727117), new LatLng(1.344375, 103.728239),
                new LatLng(1.353526, 103.728273), new LatLng(1.354304, 103.726882), new LatLng(1.354929, 103.725236),
                new LatLng(1.355413, 103.722837), new LatLng(1.355758, 103.718168), new LatLng(1.355924, 103.717504),
                new LatLng(1.356906, 103.715054), new LatLng(1.357454, 103.714008), new LatLng(1.364406, 103.706417),
                new LatLng(1.362888, 103.704669), new LatLng(1.347097, 103.691674), new LatLng(1.339814, 103.682398),
                new LatLng(1.339571, 103.681850), new LatLng(1.339444, 103.681224), new LatLng(1.339252, 103.678660),
                new LatLng(1.338946, 103.678175), new LatLng(1.338413, 103.677588), new LatLng(1.337762, 103.677237),
                new LatLng(1.331984, 103.675553), new LatLng(1.331436, 103.675272)
        );
        Polygon jurongWest = mMap.addPolygon(new PolygonOptions()
                .addAll(jurongWestBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> bishanBoundary = Arrays.asList(new LatLng(1.341777, 103.835914), new LatLng(1.343664, 103.839175),
                new LatLng(1.344866, 103.840506), new LatLng(1.345123, 103.841278), new LatLng(1.344994, 103.842008),
                new LatLng(1.343965, 103.844626), new LatLng(1.343278, 103.853552), new LatLng(1.343450, 103.860376),
                new LatLng(1.353618, 103.856900), new LatLng(1.355506, 103.856857), new LatLng(1.356493, 103.854925),
                new LatLng(1.359410, 103.853252), new LatLng(1.362628, 103.849475), new LatLng(1.364044, 103.846171),
                new LatLng(1.364644, 103.840935), new LatLng(1.367733, 103.831923), new LatLng(1.367991, 103.828318),
                new LatLng(1.367507, 103.828281), new LatLng(1.367327, 103.828344), new LatLng(1.366623, 103.828263),
                new LatLng(1.365893, 103.827740), new LatLng(1.365848, 103.827189), new LatLng(1.364603, 103.826946),
                new LatLng(1.364603, 103.826431), new LatLng(1.364396, 103.826341), new LatLng(1.364955, 103.824230),
                new LatLng(1.364486, 103.824068), new LatLng(1.364053, 103.823418), new LatLng(1.363629, 103.823436),
                new LatLng(1.363007, 103.820161), new LatLng(1.360075, 103.817725), new LatLng(1.357370, 103.818402),
                new LatLng(1.356531, 103.819178), new LatLng(1.353410, 103.824095), new LatLng(1.354330, 103.826603),
                new LatLng(1.353383, 103.829147), new LatLng(1.351494, 103.831316), new LatLng(1.349674, 103.832467),
                new LatLng(1.346793, 103.836146), new LatLng(1.345829, 103.836796), new LatLng(1.345052, 103.836746),
                new LatLng(1.344019, 103.836510), new LatLng(1.342956, 103.835901), new LatLng(1.342360, 103.835772),
                new LatLng(1.341728, 103.835901)
        );

        Polygon bishan = mMap.addPolygon(new PolygonOptions()
                .addAll(bishanBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> geylangBoundary = Arrays.asList(new LatLng(1.327813, 103.868839),new LatLng(1.328083, 103.869388),new LatLng(1.328024, 103.869510),new LatLng(1.328519, 103.870403),
                new LatLng(1.328718, 103.871395), new LatLng(1.329114, 103.872485), new LatLng(1.329412, 103.873080), new LatLng(1.329511, 103.873874),
                new LatLng(1.329709, 103.874568), new LatLng(1.330205, 103.875460), new LatLng(1.330553, 103.876930),new LatLng(1.330884, 103.877758),
                new LatLng(1.331050, 103.878420), new LatLng(1.331464, 103.879413), new LatLng(1.331546, 103.880241), new LatLng(1.331795, 103.881069),
                new LatLng(1.332043, 103.881814), new LatLng(1.332208, 103.882559), new LatLng(1.332291, 103.883305), new LatLng(1.332540, 103.884132),
                new LatLng(1.332705, 103.884878), new LatLng(1.332871, 103.885623), new LatLng(1.332953, 103.886368), new LatLng(1.333202, 103.887361),
                new LatLng(1.333533, 103.888189), new LatLng(1.333578, 103.888918), new LatLng(1.333717, 103.889541), new LatLng(1.333993, 103.890370),
                new LatLng(1.334270, 103.890992), new LatLng(1.334684, 103.891891), new LatLng(1.334961, 103.892513), new LatLng(1.335168, 103.893135),
                new LatLng(1.335582, 103.893688), new LatLng(1.335928, 103.894241), new LatLng(1.336274, 103.894794), new LatLng(1.336826, 103.895346),
                new LatLng(1.337045, 103.895512), new LatLng(1.337506, 103.895916), new LatLng(1.337759, 103.896128), new LatLng(1.337736, 103.896148),
                new LatLng(1.337774, 103.896635), new LatLng(1.337431, 103.896635), new LatLng(1.336745, 103.897150), new LatLng(1.336487, 103.897665),
                new LatLng(1.335801, 103.898351), new LatLng(1.335715, 103.898437), new LatLng(1.334771, 103.899296), new LatLng(1.334342, 103.899639),
                new LatLng(1.333902, 103.900154), new LatLng(1.333741, 103.900498), new LatLng(1.333140, 103.900927), new LatLng(1.332711, 103.901442),
                new LatLng(1.332197, 103.901871), new LatLng(1.331596, 103.902472), new LatLng(1.331081, 103.903073), new LatLng(1.330395, 103.903931),
                new LatLng(1.329794, 103.904360), new LatLng(1.329107, 103.904961), new LatLng(1.329365, 103.904961), new LatLng(1.328764, 103.905304),
                new LatLng(1.328192, 103.905622), new LatLng(1.327740, 103.905763), new LatLng(1.327022, 103.905704), new LatLng(1.326364, 103.905823),
                new LatLng(1.325826, 103.905883), new LatLng(1.325049, 103.905524), new LatLng(1.324451, 103.905464), new LatLng(1.323673, 103.905404),
                new LatLng(1.323015, 103.905345), new LatLng(1.322417, 103.905285), new LatLng(1.321759, 103.905225), new LatLng(1.321101, 103.905045),
                new LatLng(1.320444, 103.905045), new LatLng(1.319846, 103.904926), new LatLng(1.319247, 103.904926), new LatLng(1.318829, 103.904986),
                new LatLng(1.318291, 103.904926), new LatLng(1.317752, 103.904986), new LatLng(1.317094, 103.905165), new LatLng(1.317008, 103.905218),
                new LatLng(1.316321, 103.905304), new LatLng(1.315721, 103.905562), new LatLng(1.315206, 103.905819), new LatLng(1.314519, 103.905991),
                new LatLng(1.313747, 103.906420), new LatLng(1.313216, 103.906741), new LatLng(1.313124, 103.906478), new LatLng(1.309606, 103.901632),
                new LatLng(1.309606, 103.901632), new LatLng(1.309606, 103.901632), new LatLng(1.309569, 103.901568), new LatLng(1.309536, 103.901453),
                new LatLng(1.309603, 103.894556), new LatLng(1.308058, 103.886072), new LatLng(1.306052, 103.882202), new LatLng(1.307918, 103.878376),
                new LatLng(1.308196, 103.877645), new LatLng(1.308440, 103.876705), new LatLng(1.308718, 103.876114), new LatLng(1.308858, 103.875986),
                new LatLng(1.310398, 103.875551), new LatLng(1.313303, 103.874998), new LatLng(1.314436, 103.874824), new LatLng(1.315801, 103.874708),
                new LatLng(1.316615, 103.874679), new LatLng(1.317428, 103.874766), new LatLng(1.318416, 103.874998), new LatLng(1.319288, 103.875376),
                new LatLng(1.319863, 103.875920), new LatLng(1.320095, 103.876240), new LatLng(1.320299, 103.875688), new LatLng(1.320531, 103.875252),
                new LatLng(1.320996, 103.874787), new LatLng(1.326283, 103.870690), new LatLng(1.327823, 103.868830));

        Polygon geylang = mMap.addPolygon(new PolygonOptions()
                .addAll(geylangBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> kallangBoundary = Arrays.asList(new LatLng(1.329242, 103.865130), new LatLng(1.328446, 103.867554), new LatLng(1.328342, 103.868072),
                new LatLng(1.326987, 103.869839), new LatLng(1.326358, 103.870568), new LatLng(1.321112, 103.874669), new LatLng(1.320538, 103.875280),
                new LatLng(1.320049, 103.876252), new LatLng(1.319913, 103.875994), new LatLng(1.319291, 103.875342), new LatLng(1.318608, 103.875069),
                new LatLng(1.317668, 103.874811), new LatLng(1.316758, 103.874720), new LatLng(1.316075, 103.874659), new LatLng(1.314194, 103.874811),
                new LatLng(1.310372, 103.875570), new LatLng(1.308931, 103.875949), new LatLng(1.308749, 103.876086), new LatLng(1.308673, 103.876146),
                new LatLng(1.308460, 103.876617), new LatLng(1.308244, 103.877470), new LatLng(1.308092, 103.877964), new LatLng(1.307598, 103.879142),
                new LatLng(1.306977, 103.880725), new LatLng(1.306521, 103.881574), new LatLng(1.306103, 103.882157), new LatLng(1.304445, 103.883879),
                new LatLng(1.303887, 103.884323), new LatLng(1.303419, 103.884513), new LatLng(1.302798, 103.884601), new LatLng(1.302317, 103.884779),
                new LatLng(1.302026, 103.885007), new LatLng(1.301608, 103.885374), new LatLng(1.301215, 103.885501), new LatLng(1.300810, 103.885513),
                new LatLng(1.297961, 103.885311), new LatLng(1.295782, 103.885278), new LatLng(1.295254, 103.871117), new LatLng(1.294022, 103.864377),
                new LatLng(1.294084, 103.863026), new LatLng(1.294154, 103.862623), new LatLng(1.294294, 103.862220), new LatLng(1.294912, 103.861617),
                new LatLng(1.295632, 103.861153), new LatLng(1.296498, 103.861024), new LatLng(1.297860, 103.860854), new LatLng(1.298124, 103.860692),
                new LatLng(1.299767, 103.859440), new LatLng(1.300505, 103.860351), new LatLng(1.301098, 103.861382), new LatLng(1.301526, 103.862275),
                new LatLng(1.304876, 103.860269), new LatLng(1.305578, 103.859838), new LatLng(1.305741, 103.859695), new LatLng(1.306486, 103.858930),
                new LatLng(1.306787, 103.858702), new LatLng(1.307129, 103.858325), new LatLng(1.310630, 103.854469), new LatLng(1.311814, 103.855353),
                new LatLng(1.312627, 103.854881), new LatLng(1.312867, 103.854655), new LatLng(1.313601, 103.854316), new LatLng(1.312764, 103.853515),
                new LatLng(1.312269, 103.853185), new LatLng(1.311974, 103.853079), new LatLng(1.312328, 103.852619), new LatLng(1.311609, 103.852548),
                new LatLng(1.306704, 103.848825), new LatLng(1.308412, 103.846845), new LatLng(1.309897, 103.845714), new LatLng(1.310226, 103.845196),
                new LatLng(1.310568, 103.844288), new LatLng(1.311713, 103.844713), new LatLng(1.312729, 103.844883), new LatLng(1.313279, 103.845052),
                new LatLng(1.314959, 103.845885), new LatLng(1.315622, 103.846351), new LatLng(1.316285, 103.847042), new LatLng(1.317118, 103.848313),
                new LatLng(1.317089, 103.848313), new LatLng(1.318966, 103.852548), new LatLng(1.319418, 103.853465), new LatLng(1.320928, 103.855597),
                new LatLng(1.322001, 103.856655), new LatLng(1.323313, 103.857770), new LatLng(1.325839, 103.860424), new LatLng(1.326361, 103.860961),
                new LatLng(1.327321, 103.861666), new LatLng(1.328041, 103.861977), new LatLng(1.328563, 103.862146), new LatLng(1.329057, 103.862302),
                new LatLng(1.329805, 103.862372), new LatLng(1.330474, 103.862346));

        Polygon kallang = mMap.addPolygon(new PolygonOptions()
                .addAll(kallangBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> marineParadeBoundary = Arrays.asList(new LatLng(1.284217, 103.880543), new LatLng(1.286421, 103.882963), new LatLng(1.287089, 103.883915), new LatLng(1.287342, 103.884816),
                new LatLng(1.287342, 103.885556), new LatLng(1.288628, 103.886467), new LatLng(1.290552, 103.888776), new LatLng(1.290714, 103.889464),
                new LatLng(1.290380, 103.889839), new LatLng(1.293498, 103.893383), new LatLng(1.293741, 103.894092), new LatLng(1.293619, 103.894356),
                new LatLng(1.293265, 103.894305), new LatLng(1.293994, 103.895419), new LatLng(1.294130, 103.896201), new LatLng(1.293716, 103.896556),
                new LatLng(1.294916, 103.897841), new LatLng(1.296598, 103.901569), new LatLng(1.298948, 103.910582), new LatLng(1.299590, 103.912070),
                new LatLng(1.299607, 103.912332), new LatLng(1.299497, 103.912340), new LatLng(1.299658, 103.912678), new LatLng(1.299739, 103.912611),
                new LatLng(1.300656, 103.915194), new LatLng(1.300640, 103.915103), new LatLng(1.300824, 103.915174), new LatLng(1.300993, 103.915395),
                new LatLng(1.301626, 103.917036), new LatLng(1.301601, 103.917408), new LatLng(1.301525, 103.917577), new LatLng(1.301677, 103.917889),
                new LatLng(1.301863, 103.919098), new LatLng(1.301863, 103.919335), new LatLng(1.304401, 103.918769), new LatLng(1.304679, 103.918734),
                new LatLng(1.307428, 103.917968), new LatLng(1.307324, 103.917080), new LatLng(1.305775, 103.910710), new LatLng(1.305496, 103.909953),
                new LatLng(1.308524, 103.908413), new LatLng(1.309125, 103.908180), new LatLng(1.310502, 103.907908), new LatLng(1.313139, 103.906764),
                new LatLng(1.313110, 103.906448), new LatLng(1.309610, 103.901663), new LatLng(1.309563, 103.901463), new LatLng(1.309518, 103.901259),
                new LatLng(1.309496, 103.901214), new LatLng(1.309593, 103.894586), new LatLng(1.307884, 103.886101), new LatLng(1.306057, 103.882213),
                new LatLng(1.304461, 103.883900), new LatLng(1.303891, 103.884325), new LatLng(1.303430, 103.884519), new LatLng(1.302751, 103.884628),
                new LatLng(1.302545, 103.884701), new LatLng(1.302351, 103.884786), new LatLng(1.302060, 103.884992), new LatLng(1.301562, 103.885392),
                new LatLng(1.301199, 103.885514), new LatLng(1.300835, 103.885502), new LatLng(1.297912, 103.885308), new LatLng(1.295804, 103.885318),
                new LatLng(1.295543, 103.877254), new LatLng(1.294802, 103.876251), new LatLng(1.294221, 103.875830), new LatLng(1.293495, 103.875641),
                new LatLng(1.290691, 103.875714), new LatLng(1.289195, 103.876498), new LatLng(1.288120, 103.877574), new LatLng(1.284198, 103.880523));

        Polygon marineParade = mMap.addPolygon(new PolygonOptions()
                .addAll(marineParadeBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> queenstownBoundary = Arrays.asList(new LatLng(1.255067, 103.785491), new LatLng(1.267406, 103.792746), new LatLng(1.269740, 103.796498), new LatLng(1.271908, 103.797832),
                new LatLng(1.270741, 103.800000), new LatLng(1.272741, 103.801001), new LatLng(1.272575, 103.802169), new LatLng(1.275274, 103.801962),
                new LatLng(1.278877, 103.803067), new LatLng(1.282770, 103.801207), new LatLng(1.283641, 103.801207), new LatLng(1.283641, 103.801265),
                new LatLng(1.284629, 103.801730), new LatLng(1.286662, 103.803008), new LatLng(1.292390, 103.808843), new LatLng(1.292408, 103.809396),
                new LatLng(1.291658, 103.815233), new LatLng(1.293547, 103.815781), new LatLng(1.296859, 103.816537), new LatLng(1.296706, 103.812661),
                new LatLng(1.296864, 103.812433), new LatLng(1.297272, 103.812569), new LatLng(1.297320, 103.812519), new LatLng(1.298473, 103.809985),
                new LatLng(1.299112, 103.809287), new LatLng(1.299042, 103.808770), new LatLng(1.299480, 103.808711), new LatLng(1.299481, 103.808664),
                new LatLng(1.299655, 103.807560), new LatLng(1.299190, 103.806862), new LatLng(1.299888, 103.805642), new LatLng(1.300120, 103.804712),
                new LatLng(1.300817, 103.804189), new LatLng(1.300817, 103.803898), new LatLng(1.301253, 103.803651), new LatLng(1.301660, 103.804291),
                new LatLng(1.302532, 103.803826), new LatLng(1.303287, 103.803826), new LatLng(1.303461, 103.803535), new LatLng(1.306192, 103.803303),
                new LatLng(1.306657, 103.803128), new LatLng(1.306889, 103.803012), new LatLng(1.307006, 103.802780), new LatLng(1.308923, 103.800048),
                new LatLng(1.309853, 103.799467), new LatLng(1.308749, 103.799409), new LatLng(1.307993, 103.799002), new LatLng(1.308458, 103.798246),
                new LatLng(1.309156, 103.797549), new LatLng(1.308865, 103.797316), new LatLng(1.309620, 103.796619), new LatLng(1.309853, 103.795108),
                new LatLng(1.309969, 103.794585), new LatLng(1.310608, 103.794701), new LatLng(1.311247, 103.794527), new LatLng(1.311538, 103.794236),
                new LatLng(1.312584, 103.794178), new LatLng(1.313339, 103.793016), new LatLng(1.311189, 103.791039), new LatLng(1.312351, 103.790400),
                new LatLng(1.313223, 103.789528), new LatLng(1.314501, 103.787668), new LatLng(1.315896, 103.786390), new LatLng(1.314966, 103.784646),
                new LatLng(1.314733, 103.784763), new LatLng(1.314733, 103.784763), new LatLng(1.314850, 103.785053), new LatLng(1.313978, 103.785460),
                new LatLng(1.312768, 103.785324), new LatLng(1.312652, 103.785266), new LatLng(1.311141, 103.785324), new LatLng(1.311255, 103.784841),
                new LatLng(1.310838, 103.784771), new LatLng(1.312800, 103.779077), new LatLng(1.314192, 103.777615), new LatLng(1.318786, 103.771280),
                new LatLng(1.312264, 103.772320), new LatLng(1.306929, 103.771319), new LatLng(1.303928, 103.771152), new LatLng(1.302677, 103.770985),
                new LatLng(1.300426, 103.769818), new LatLng(1.294591, 103.769484), new LatLng(1.291589, 103.767233), new LatLng(1.292340, 103.766399),
                new LatLng(1.293007, 103.766204), new LatLng(1.301637, 103.762166), new LatLng(1.299270, 103.758129), new LatLng(1.298644, 103.759312),
                new LatLng(1.296417, 103.761401), new LatLng(1.295251, 103.761980), new LatLng(1.295042, 103.761701), new LatLng(1.294834, 103.761910),
                new LatLng(1.295321, 103.762258), new LatLng(1.295112, 103.762746), new LatLng(1.294555, 103.762885), new LatLng(1.294500, 103.762860),
                new LatLng(1.293077, 103.761772), new LatLng(1.294334, 103.759597), new LatLng(1.292832, 103.761851), new LatLng(1.291058, 103.760856),
                new LatLng(1.291600, 103.760083), new LatLng(1.291299, 103.760083), new LatLng(1.290889, 103.760722), new LatLng(1.290620, 103.760671),
                new LatLng(1.283712, 103.772150), new LatLng(1.278011, 103.768830), new LatLng(1.285299, 103.756416), new LatLng(1.284794, 103.753240),
                new LatLng(1.280681, 103.750858), new LatLng(1.276568, 103.757787), new LatLng(1.276135, 103.757498), new LatLng(1.268342, 103.770562),
                new LatLng(1.279527, 103.777347), new LatLng(1.273682, 103.787163), new LatLng(1.259213, 103.778517));

        Polygon queenstown = mMap.addPolygon(new PolygonOptions()
                .addAll(queenstownBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> toaPayohBoundary = Arrays.asList(new LatLng(1.341761, 103.835816), new LatLng(1.343563, 103.839077), new LatLng(1.344940, 103.840392), new LatLng(1.345060, 103.840990),
                new LatLng(1.345000, 103.840990), new LatLng(1.345060, 103.841947), new LatLng(1.344462, 103.843024), new LatLng(1.343983, 103.844280),
                new LatLng(1.343983, 103.845058), new LatLng(1.343983, 103.847630), new LatLng(1.343505, 103.851937), new LatLng(1.343265, 103.853433),
                new LatLng(1.343206, 103.854629), new LatLng(1.343445, 103.860851), new LatLng(1.343684, 103.862526), new LatLng(1.344282, 103.864201),
                new LatLng(1.344342, 103.865398), new LatLng(1.342727, 103.869107), new LatLng(1.342787, 103.870004), new LatLng(1.343505, 103.871619),
                new LatLng(1.343565, 103.872337), new LatLng(1.343445, 103.873952), new LatLng(1.344342, 103.876106), new LatLng(1.344342, 103.877123),
                new LatLng(1.342787, 103.879636), new LatLng(1.341113, 103.883585), new LatLng(1.340993, 103.884901), new LatLng(1.338780, 103.886696),
                new LatLng(1.334414, 103.888670), new LatLng(1.333517, 103.888909), new LatLng(1.331268, 103.878969), new LatLng(1.327808, 103.868754),
                new LatLng(1.328475, 103.867503), new LatLng(1.329267, 103.865126), new LatLng(1.330309, 103.862666), new LatLng(1.330560, 103.861165),
                new LatLng(1.329101, 103.856663), new LatLng(1.329642, 103.848908), new LatLng(1.329517, 103.842445), new LatLng(1.328934, 103.840611),
                new LatLng(1.329684, 103.840068), new LatLng(1.330685, 103.839110), new LatLng(1.333602, 103.837900), new LatLng(1.334644, 103.837150),
                new LatLng(1.335103, 103.836983), new LatLng(1.337312, 103.837275), new LatLng(1.338563, 103.837233), new LatLng(1.338913, 103.837365),
                new LatLng(1.339930, 103.836958), new LatLng(1.341760, 103.835912));

        Polygon toaPayoh= mMap.addPolygon(new PolygonOptions()
                .addAll(toaPayohBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> bukitMerahBoundary = Arrays.asList(new LatLng(1.262384, 103.804998), new LatLng(1.262534, 103.805341),
                new LatLng(1.262838, 103.805389), new LatLng(1.263816, 103.805230), new LatLng(1.263589, 103.805534),
                new LatLng(1.263710, 103.805685), new LatLng(1.263841, 103.805595), new LatLng(1.264747, 103.806604),
                new LatLng(1.265058, 103.806389), new LatLng(1.265573, 103.806936), new LatLng(1.265841, 103.807226),
                new LatLng(1.265916, 103.807301), new LatLng(1.265536, 103.807821), new LatLng(1.265182, 103.807982),
                new LatLng(1.264903, 103.808937), new LatLng(1.265139, 103.809549), new LatLng(1.265064, 103.809774),
                new LatLng(1.265149, 103.809785), new LatLng(1.265149, 103.809785), new LatLng(1.264999, 103.810214),
                new LatLng(1.266383, 103.811222), new LatLng(1.266426, 103.812392), new LatLng(1.266426, 103.812392),
                new LatLng(1.267874, 103.814591), new LatLng(1.267756, 103.814774), new LatLng(1.266083, 103.813261),
                new LatLng(1.265664, 103.813679), new LatLng(1.265879, 103.814033), new LatLng(1.265814, 103.814141),
                new LatLng(1.265439, 103.813958), new LatLng(1.265021, 103.814463), new LatLng(1.264510, 103.814967),
                new LatLng(1.263706, 103.813159), new LatLng(1.264333, 103.812756), new LatLng(1.264280, 103.812477),
                new LatLng(1.264816, 103.812252), new LatLng(1.264977, 103.811968), new LatLng(1.264897, 103.811614),
                new LatLng(1.264666, 103.811297), new LatLng(1.264344, 103.811254), new LatLng(1.264079, 103.811068),
                new LatLng(1.263977, 103.810880), new LatLng(1.263924, 103.810548), new LatLng(1.263757, 103.810322),
                new LatLng(1.263672, 103.810274), new LatLng(1.263666, 103.810086), new LatLng(1.264095, 103.809534),
                new LatLng(1.264036, 103.809443), new LatLng(1.263548, 103.810065), new LatLng(1.263543, 103.810285),
                new LatLng(1.263130, 103.810365), new LatLng(1.262879, 103.810799), new LatLng(1.262906, 103.811183),
                new LatLng(1.262838, 103.811228), new LatLng(1.262816, 103.811295), new LatLng(1.262550, 103.811332),
                new LatLng(1.262577, 103.811467), new LatLng(1.262690, 103.811553), new LatLng(1.262951, 103.813402),
                new LatLng(1.263064, 103.813470), new LatLng(1.263533, 103.813163), new LatLng(1.264383, 103.815087),
                new LatLng(1.263865, 103.815777), new LatLng(1.263819, 103.815911), new LatLng(1.263884, 103.816147),
                new LatLng(1.264517, 103.816555), new LatLng(1.266490, 103.817381), new LatLng(1.266651, 103.817531),
                new LatLng(1.266576, 103.817670), new LatLng(1.266372, 103.817649), new LatLng(1.264442, 103.816866),
                new LatLng(1.263744, 103.816812), new LatLng(1.263626, 103.817531), new LatLng(1.263481, 103.818237),
                new LatLng(1.262231, 103.818237), new LatLng(1.262206, 103.817650), new LatLng(1.262014, 103.817675),
                new LatLng(1.261995, 103.819870), new LatLng(1.263105, 103.819902), new LatLng(1.263099, 103.823627),
                new LatLng(1.260095, 103.823697), new LatLng(1.260120, 103.824195), new LatLng(1.258538, 103.825394),
                new LatLng(1.257735, 103.826708), new LatLng(1.257735, 103.826708), new LatLng(1.256600, 103.829694),
                new LatLng(1.256765, 103.830574), new LatLng(1.254839, 103.836380), new LatLng(1.257894, 103.842465),
                new LatLng(1.263812, 103.836239), new LatLng(1.263672, 103.830434), new LatLng(1.260215, 103.824374),
                new LatLng(1.263008, 103.824106), new LatLng(1.263123, 103.824590), new LatLng(1.267435, 103.832118),
                new LatLng(1.267633, 103.839461), new LatLng(1.261581, 103.845923), new LatLng(1.262486, 103.846753),
                new LatLng(1.265560, 103.844303), new LatLng(1.266785, 103.845758), new LatLng(1.260694, 103.851309),
                new LatLng(1.261511, 103.852125), new LatLng(1.263080, 103.850722), new LatLng(1.263577, 103.851360),
                new LatLng(1.263603, 103.851628), new LatLng(1.272737, 103.851398), new LatLng(1.272750, 103.842212),
                new LatLng(1.275799, 103.840796), new LatLng(1.277852, 103.840451), new LatLng(1.280760, 103.839252),
                new LatLng(1.281216, 103.838911), new LatLng(1.283550, 103.835645), new LatLng(1.285361, 103.834866),
                new LatLng(1.287338, 103.835058), new LatLng(1.289314, 103.835033), new LatLng(1.292389, 103.833374),
                new LatLng(1.292108, 103.830988), new LatLng(1.292746, 103.828513), new LatLng(1.291880, 103.824063),
                new LatLng(1.293462, 103.822340), new LatLng(1.293696, 103.815825), new LatLng(1.291578, 103.815404),
                new LatLng(1.292484, 103.809024), new LatLng(1.288096, 103.805209), new LatLng(1.286655, 103.802913),
                new LatLng(1.283541, 103.801201), new LatLng(1.279096, 103.803188), new LatLng(1.275173, 103.801922),
                new LatLng(1.272648, 103.802247), new LatLng(1.272883, 103.801056), new LatLng(1.270664, 103.800118),
                new LatLng(1.271908, 103.797917), new LatLng(1.269708, 103.796509), new LatLng(1.266442, 103.801183),
                new LatLng(1.265973, 103.800966), new LatLng(1.262420, 103.804990));

        Polygon bukitMerah = mMap.addPolygon(new PolygonOptions()
                .addAll(bukitMerahBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> bukitTimahBoundary = Arrays.asList(new LatLng(1.307255, 103.802277), new LatLng(1.314720, 103.804595),
                new LatLng(1.319011, 103.809402), new LatLng(1.321413, 103.812921), new LatLng(1.328278, 103.814037),
                new LatLng(1.335744, 103.818587), new LatLng(1.338404, 103.814466), new LatLng(1.338404, 103.813350),
                new LatLng(1.339605, 103.811634), new LatLng(1.341407, 103.811548), new LatLng(1.342093, 103.810003),
                new LatLng(1.341493, 103.805797), new LatLng(1.343037, 103.803737), new LatLng(1.343380, 103.802278),
                new LatLng(1.349387, 103.795754), new LatLng(1.349645, 103.792750), new LatLng(1.348229, 103.789317),
                new LatLng(1.349001, 103.788974), new LatLng(1.348014, 103.786656), new LatLng(1.345612, 103.788244),
                new LatLng(1.341707, 103.779832), new LatLng(1.340592, 103.778717), new LatLng(1.340592, 103.778717),
                new LatLng(1.346384, 103.772065), new LatLng(1.348872, 103.770305), new LatLng(1.347242, 103.767001),
                new LatLng(1.347242, 103.765069), new LatLng(1.346641, 103.762924), new LatLng(1.344668, 103.764254),
                new LatLng(1.344625, 103.765241), new LatLng(1.343252, 103.765928), new LatLng(1.342866, 103.767087),
                new LatLng(1.341879, 103.766786), new LatLng(1.341664, 103.764082), new LatLng(1.339648, 103.764683),
                new LatLng(1.339390, 103.764125), new LatLng(1.336645, 103.764555), new LatLng(1.335701, 103.765585),
                new LatLng(1.332097, 103.765413), new LatLng(1.326605, 103.775498), new LatLng(1.325189, 103.774511),
                new LatLng(1.324503, 103.772794), new LatLng(1.320942, 103.770820), new LatLng(1.318754, 103.771207),
                new LatLng(1.313047, 103.778760), new LatLng(1.310902, 103.784768), new LatLng(1.311095, 103.785401),
                new LatLng(1.314013, 103.785487), new LatLng(1.315107, 103.784929), new LatLng(1.315965, 103.786367),
                new LatLng(1.312382, 103.790250), new LatLng(1.311150, 103.791052), new LatLng(1.311150, 103.791052),
                new LatLng(1.312655, 103.794165), new LatLng(1.311507, 103.794267), new LatLng(1.311252, 103.794599),
                new LatLng(1.310512, 103.794777), new LatLng(1.309951, 103.794650), new LatLng(1.309594, 103.796717),
                new LatLng(1.308828, 103.797329), new LatLng(1.309058, 103.797610), new LatLng(1.308063, 103.799013),
                new LatLng(1.308063, 103.799013), new LatLng(1.309747, 103.799524), new LatLng(1.308969, 103.800123)
        );
        Polygon bukitTimah = mMap.addPolygon(new PolygonOptions()
                .addAll(bukitTimahBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> yishunBoundary = Arrays.asList(new LatLng(1.435458, 103.825855), new LatLng(1.432562, 103.826649),
                new LatLng(1.428743, 103.826842), new LatLng(1.428314, 103.826821), new LatLng(1.427907, 103.826799),
                new LatLng(1.427199, 103.826649), new LatLng(1.426427, 103.826349), new LatLng(1.424861, 103.825641),
                new LatLng(1.424485, 103.825523), new LatLng(1.420560, 103.824793), new LatLng(1.413417, 103.823055),
                new LatLng(1.413781, 103.822454), new LatLng(1.414039, 103.821918), new LatLng(1.414189, 103.821253),
                new LatLng(1.414232, 103.820115), new LatLng(1.414167, 103.819772), new LatLng(1.413524, 103.816017),
                new LatLng(1.413695, 103.814901), new LatLng(1.414017, 103.813914), new LatLng(1.415133, 103.811446),
                new LatLng(1.415369, 103.810631), new LatLng(1.415476, 103.809837), new LatLng(1.415433, 103.809150),
                new LatLng(1.415197, 103.808378), new LatLng(1.414339, 103.806812), new LatLng(1.414275, 103.806168),
                new LatLng(1.414318, 103.805696), new LatLng(1.407625, 103.808356), new LatLng(1.406338, 103.809172),
                new LatLng(1.403849, 103.810974), new LatLng(1.401833, 103.812262), new LatLng(1.399259, 103.813463),
                new LatLng(1.397543, 103.814021), new LatLng(1.396770, 103.814450), new LatLng(1.395526, 103.815566),
                new LatLng(1.394969, 103.816768), new LatLng(1.393252, 103.822905), new LatLng(1.392995, 103.824707),
                new LatLng(1.392952, 103.826638), new LatLng(1.393038, 103.828269), new LatLng(1.393381, 103.830200),
                new LatLng(1.396341, 103.842002), new LatLng(1.396513, 103.843375), new LatLng(1.396642, 103.845307),
                new LatLng(1.396556, 103.849684), new LatLng(1.396599, 103.851100), new LatLng(1.397071, 103.852173),
                new LatLng(1.399430, 103.855392), new LatLng(1.400417, 103.855821), new LatLng(1.401147, 103.855692),
                new LatLng(1.402948, 103.854920), new LatLng(1.403635, 103.854834), new LatLng(1.404343, 103.854920),
                new LatLng(1.405008, 103.855177), new LatLng(1.405759, 103.855692), new LatLng(1.408719, 103.859898),
                new LatLng(1.409727, 103.860928), new LatLng(1.410521, 103.861400), new LatLng(1.411615, 103.861743),
                new LatLng(1.412923, 103.861936), new LatLng(1.413953, 103.861808), new LatLng(1.415690, 103.861507),
                new LatLng(1.417085, 103.861614), new LatLng(1.417857, 103.861829), new LatLng(1.418307, 103.862001),
                new LatLng(1.419208, 103.862473), new LatLng(1.421976, 103.864222), new LatLng(1.425998, 103.855596),
                new LatLng(1.428593, 103.853568), new LatLng(1.429709, 103.852677), new LatLng(1.430052, 103.852366),
                new LatLng(1.434653, 103.848182), new LatLng(1.436541, 103.846176), new LatLng(1.440263, 103.842292),
                new LatLng(1.442182, 103.840682), new LatLng(1.443512, 103.838644), new LatLng(1.445250, 103.836616),
                new LatLng(1.444510, 103.835393), new LatLng(1.443705, 103.834149), new LatLng(1.441614, 103.830823),
                new LatLng(1.441432, 103.830469), new LatLng(1.441206, 103.830179), new LatLng(1.440863, 103.829879),
                new LatLng(1.440262, 103.829524), new LatLng(1.438375, 103.829095), new LatLng(1.437045, 103.828601),
                new LatLng(1.436272, 103.827679));

        final Polygon yishun = mMap.addPolygon(new PolygonOptions()
                .addAll(yishunBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        List<LatLng> sembawangBoundary = Arrays.asList(new LatLng(1.461502, 103.790689), new LatLng(1.465421, 103.796820), new LatLng(1.465421, 103.796820),
                new LatLng(1.465578, 103.796689), new LatLng(1.465664, 103.797035), new LatLng(1.466269, 103.797856),
                new LatLng(1.466525, 103.797699), new LatLng(1.466712, 103.797982), new LatLng(1.466677, 103.798227),
                new LatLng(1.467080, 103.798981), new LatLng(1.466852, 103.799735), new LatLng(1.468114, 103.801243),
                new LatLng(1.468534, 103.801506), new LatLng(1.468674, 103.801979), new LatLng(1.468990, 103.802435),
                new LatLng(1.470637, 103.808794), new LatLng(1.469891, 103.809658), new LatLng(1.470534, 103.812453),
                new LatLng(1.470622, 103.818014), new LatLng(1.470530, 103.818549), new LatLng(1.470039, 103.819811),
                new LatLng(1.469128, 103.821557), new LatLng(1.468804, 103.822579), new LatLng(1.467834, 103.821642),
                new LatLng(1.467749, 103.821191), new LatLng(1.467051, 103.820177), new LatLng(1.464539, 103.817955),
                new LatLng(1.463918, 103.818960), new LatLng(1.464727, 103.819726), new LatLng(1.465084, 103.819896),
                new LatLng(1.465552, 103.820194), new LatLng(1.466685, 103.821974), new LatLng(1.466523, 103.822179),
                new LatLng(1.466310, 103.822051), new LatLng(1.465791, 103.822536), new LatLng(1.466123, 103.823141),
                new LatLng(1.465595, 103.823805), new LatLng(1.465910, 103.824086), new LatLng(1.465799, 103.824239),
                new LatLng(1.464233, 103.823192), new LatLng(1.463457, 103.824474), new LatLng(1.468726, 103.827497),
                new LatLng(1.464821, 103.834301), new LatLng(1.460707, 103.831949), new LatLng(1.460119, 103.832894),
                new LatLng(1.464171, 103.835246), new LatLng(1.464297, 103.835834), new LatLng(1.464154, 103.836593),
                new LatLng(1.464059, 103.836725), new LatLng(1.464022, 103.836945), new LatLng(1.464144, 103.837019),
                new LatLng(1.464413, 103.837129), new LatLng(1.464376, 103.837214), new LatLng(1.464083, 103.837129),
                new LatLng(1.462508, 103.839658), new LatLng(1.462410, 103.839670), new LatLng(1.461799, 103.840525),
                new LatLng(1.460627, 103.841576), new LatLng(1.460712, 103.841747), new LatLng(1.460724, 103.841857),
                new LatLng(1.460627, 103.842358), new LatLng(1.460675, 103.842468), new LatLng(1.459454, 103.844313),
                new LatLng(1.459405, 103.844496), new LatLng(1.459552, 103.844630), new LatLng(1.459503, 103.844740),
                new LatLng(1.459307, 103.844594), new LatLng(1.451497, 103.832991), new LatLng(1.445203, 103.836577),
                new LatLng(1.441369, 103.830406), new LatLng(1.440910, 103.829906), new LatLng(1.440160, 103.829489),
                new LatLng(1.438368, 103.829072), new LatLng(1.437034, 103.828572), new LatLng(1.436284, 103.827738),
                new LatLng(1.435136, 103.824908), new LatLng(1.435136, 103.824209), new LatLng(1.435336, 103.823160),
                new LatLng(1.435835, 103.822561), new LatLng(1.440379, 103.818765), new LatLng(1.441826, 103.816768),
                new LatLng(1.443125, 103.814470), new LatLng(1.445671, 103.808177), new LatLng(1.446220, 103.807079),
                new LatLng(1.448517, 103.803982), new LatLng(1.449515, 103.803283), new LatLng(1.453409, 103.801485),
                new LatLng(1.456654, 103.800387), new LatLng(1.455606, 103.797490), new LatLng(1.457553, 103.794843),
                new LatLng(1.458552, 103.793944), new LatLng(1.460050, 103.793295), new LatLng(1.459900, 103.792396));

        Polygon sembawang = mMap.addPolygon(new PolygonOptions()
                .addAll(sembawangBoundary)
                .strokeColor(Color.RED)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(FILL_COLOR)
                .clickable(true));

        hm.put(woodlands, "WOODLANDS");
        hm.put(angMoKio, "ANG MO KIO");
        hm.put(hougang, "HOUGANG");
        hm.put(punggol, "PUNGGOL");
        hm.put(sengkang, "SENGKANG");
        hm.put(serangoon, "SERANGOON");
        hm.put(bedok, "BEDOK");
        hm.put(pasirRis, "PASIR RIS");
        hm.put(tampines, "TAMPINES");
        hm.put(bukitBatok, "BUKIT BATOK");
        hm.put(bukitPanjang, "BUKIT PANJANG");
        hm.put(choaChuKang, "CHOA CHU KANG");
        hm.put(clementi, "CLEMENTI");
        hm.put(jurongEast, "JURONG EAST");
        hm.put(jurongWest, "JURONG WEST");
        hm.put(bishan, "BISHAN");
        hm.put(geylang, "GEYLANG");
        hm.put(kallang, "KALLANG");
        hm.put(marineParade, "MARINE PARADE");
        hm.put(queenstown, "QEENSTOWN");
        hm.put(toaPayoh, "TOA PAYOH");
        hm.put(bukitMerah, "BUKIT MERAH");
        hm.put(bukitTimah, "BUKIT TIMAH");
        hm.put(yishun, "YISHUN");
        hm.put(sembawang, "SEMBAWANG");

        if(mClusterManager== null){
            mClusterManager = new ClusterManager<>(this.getApplicationContext(),mMap);
        }
        if(mClusterManagerRenderer == null){
            mClusterManagerRenderer = new ClusterManagerRenderer(
                    this,
                    mMap,
                    mClusterManager
            );
            mClusterManager.setRenderer(mClusterManagerRenderer);
        }
        //for Cluster Item Click Listener
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterItemClickListener(this);


        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                Toast.makeText(MapsActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                if (pre_region != null){
                    if (!pre_region.equals(polygon)) {
                        pre_region.setFillColor(FILL_COLOR);
                        pre_region = polygon;
                    } else return;
                } else pre_region = polygon;
                polygon.setFillColor(0x0);
                ;

//                mapFragment.getView().setVisibility(View.GONE);


                double lat = 0;
                double lng = 0;
                int len = polygon.getPoints().size();
                for (LatLng i: polygon.getPoints()){
                    lat += i.latitude;
                    lng += i.longitude;
                }

                lat /= len;
                lng /= len;

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng), 13.2f));
                region = hm.get(polygon);
                ArrayList<ResaleFlat> resaleFlatArrayList = CallResaleFlatController();
                Log.d("RESALE", String.valueOf(resaleFlatArrayList));
                for(int i = 0; i<resaleFlatArrayList.size(); i++)
                    Log.d("RESALE"+String.valueOf(i), String.valueOf(resaleFlatArrayList.get(i)));
                refreshMarkers(resaleFlatArrayList);

            }
        });


    }

    public void displayFlats(List<LatLng> Flats){
        for (LatLng i: Flats){
            Marker mMarker= mMap.addMarker(new MarkerOptions().position(i));
        }
    }

    private void refreshMarkers(ArrayList<ResaleFlat> flatArrayList){   // use of wildcard
        if(mClusterManager== null){
            mClusterManager = new ClusterManager<>(this.getApplicationContext(),mMap);
        }
        if(mClusterManagerRenderer == null){
            mClusterManagerRenderer = new ClusterManagerRenderer(
                    this,
                    mMap,
                    mClusterManager
            );
            mClusterManager.setRenderer(mClusterManagerRenderer);
        }
        mMap.setInfoWindowAdapter(new BtoInfoWindowAdapter(this));

        // test a custom marker


        int avatar = R.drawable.flatava;
        mClusterManager.clearItems();
        mClusterMarkers.clear();
        for (ResaleFlat flat:flatArrayList) {
            LatLng ll = getLLFromAddress(this, flat.getLocation());
            String snippet = flat.getFlatSize() + "\nPrice: " + flat.getPrice() + "\nArea: " + flat.getFloorArea();
            FlatMarker fm = new FlatMarker(ll, flat.getLocation(),snippet,avatar, flat);
            mClusterManager.addItem(fm);
            mClusterMarkers.add(fm);
            mClusterManager.cluster();
        }


    }

    public ArrayList<ResaleFlat> CallResaleFlatController() {
        FlatController fc = new FlatController();
        ArrayList<ResaleFlat> l = fc.getResale(getResources().getStringArray(R.array.FlatType),
                getResources().getStringArray(R.array.Selling_Price_Range),
                getResources().getStringArray(R.array.Remaining_Lease_Range),
                getResources().getStringArray(R.array.Storey_Range),
                getResources().getStringArray(R.array.Floor_Area_Range),
                flatType, priceRange, leaseRange, storeyRange, areaRange, region);
        return l;
    }

    public LatLng getLLFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    public void updateBottomSheet(Flat flat) {
        // Popup bottom sheet
        LinearLayout bottomSheetLayout = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        location.setText(flat.getLocation());
        if (ResaleFlat.class.isInstance(flat)) {
            String s = String.valueOf(((ResaleFlat) flat).getPrice());
            price.setText('$'+s);
            details.setText("Flat Type: "+ flat.getFlatSize()+'\n' +
                    "Storey Range: " + ((ResaleFlat) flat).getStorey() + '\n' +
                    "Floor Area: "+((ResaleFlat) flat).getFloorArea() + " m2" + '\n' +
                    "Remaining Lease: "+((ResaleFlat) flat).getRemainingLease() + " years");
        }

    }


}
