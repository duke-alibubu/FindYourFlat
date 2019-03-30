package com.example.androidhdb2.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
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
import com.example.androidhdb2.model.Flat;
import com.example.androidhdb2.model.PastBtoFlat;
import com.example.androidhdb2.model.UpcomingBtoFlat;
import com.example.androidhdb2.utils.BtoAdapter;
import com.example.androidhdb2.utils.UpcomingBtoAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class BtoDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;
    private TextView progressText;
    private final String TAG = "BTODETAIL";
    private FirebaseFirestore db;
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bto_detail);
        Intent intent = getIntent();
        String btotype = intent.getStringExtra("BtoType");
        userid = intent.getStringExtra("UserID");
        Log.d(TAG, btotype);
        progressBar = findViewById(R.id.pBar);
        progressText = findViewById(R.id.pText);
        recyclerView = findViewById(R.id.recyclerview);
        db = FirebaseFirestore.getInstance();

        // Setup Recycler View
        recyclerView.setVisibility(View.GONE);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        if (btotype.equals("Past"))
            getPastBTO();
        else if (btotype.equals("Upcoming"))
            getUpcomingBTO();

    }

    private void getUpcomingBTO() {
        progressBar.setVisibility(View.VISIBLE);
        ArrayList<UpcomingBtoFlat> flatArrayList = new ArrayList<UpcomingBtoFlat>();
        CollectionReference colRef = db.collection("UpcomingBTO");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        UpcomingBtoFlat flat = document.toObject(UpcomingBtoFlat.class);
                        Log.d(TAG, flat.toString());
                        flatArrayList.add(flat);
                    }
                }
                Log.d(TAG, String.valueOf(flatArrayList));
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                mAdapter = new UpcomingBtoAdapter(getApplicationContext(), flatArrayList,userid);
                recyclerView.setAdapter(mAdapter);
            }
        });
    }

    public void getPastBTO() {
        progressBar.setVisibility(View.VISIBLE);
        ArrayList<PastBtoFlat> flatArrayList = new ArrayList<PastBtoFlat>();
        CollectionReference colRef = db.collection("PastBTO");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        PastBtoFlat flat = document.toObject(PastBtoFlat.class);
                        Log.d(TAG, flat.toString());
                        flatArrayList.add(flat);
                    }
                }
                Log.d(TAG, String.valueOf(flatArrayList));
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                mAdapter = new BtoAdapter(getApplicationContext(), flatArrayList , userid);
                recyclerView.setAdapter(mAdapter);
            }
        });
    }




}
