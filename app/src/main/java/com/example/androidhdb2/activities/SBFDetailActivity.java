package com.example.androidhdb2.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.androidhdb2.R;
import com.example.androidhdb2.controllers.FlatController;
import com.example.androidhdb2.model.SBFlat;
import com.example.androidhdb2.utils.SBFAdapter;
import com.example.androidhdb2.utils.SBFilter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SBFDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;
    private TextView progressText;
    private final String TAG = "SBFDETAIL";
    private FirebaseFirestore db;
    private String userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sbfdetail);
        Intent intent = getIntent();
        userid = intent.getStringExtra("UserID");
        String[] detail = intent.getStringArrayExtra("FLAT DETAILS");
        ArrayList results = SBFilter.filterFlats(detail);
        Log.d(TAG, String.valueOf(results));

        String flatType;
        String[] priceRange;
        String[] flatSupplyRange;
        String ethnicGroup;
        String[] ethnicGroupQuota;


        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        recyclerView = findViewById(R.id.rV);
        db = FirebaseFirestore.getInstance();

        // Setup Recycler View
        recyclerView.setVisibility(View.GONE);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        getSBFlats(results);
    }

    private void getSBFlats(ArrayList results) {

        progressBar.setVisibility(View.VISIBLE);
        List<SBFlat> flatArrayList = new CopyOnWriteArrayList<SBFlat>();
        CollectionReference colRef = db.collection("SBFlat");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        SBFlat flat = document.toObject(SBFlat.class);
//                        Log.d(TAG, flat.toString());
                        flatArrayList.add(flat);
                    }
                }
                Log.d(TAG, String.valueOf(flatArrayList)+flatArrayList.size());
                FlatController fc = new FlatController();
                List<SBFlat> flatResults = new ArrayList<SBFlat>();
                flatResults = fc.getSBF(flatArrayList, results);

                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                mAdapter = new SBFAdapter(getApplicationContext(), flatResults , userid);
                recyclerView.setAdapter(mAdapter);
            }
        });
    }




}
