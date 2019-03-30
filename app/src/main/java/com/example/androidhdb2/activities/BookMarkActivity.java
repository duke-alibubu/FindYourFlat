package com.example.androidhdb2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidhdb2.R;
import com.example.androidhdb2.controllers.UserController;
import com.example.androidhdb2.model.Bookmark;
import com.example.androidhdb2.model.User;
import com.example.androidhdb2.utils.BookmarkAdapter;

import java.util.ArrayList;

public class BookMarkActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;
    private TextView progressText;
    private String userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_mark);
        Intent intent = getIntent();
        userid = intent.getStringExtra("UserID");
        progressBar = findViewById(R.id.bBar);
        progressText = findViewById(R.id.bText);
        recyclerView = findViewById(R.id.brecyclerview);

        recyclerView.setVisibility(View.GONE);
        recyclerView.setHasFixedSize(true);

        // Setup Recycler View
        recyclerView.setVisibility(View.GONE);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

        displayBookmark();
    }

    private void displayBookmark(){
        progressBar.setVisibility(View.VISIBLE);
        User user = UserController.importUser(this, getFilesDir(), userid);
        ArrayList<Bookmark> bookmarkArrayList = user.getBookmarkList();
        if (bookmarkArrayList.size()==0)
            Toast.makeText(this, "No bookmarks!", Toast.LENGTH_SHORT).show();

        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        mAdapter = new BookmarkAdapter(getApplicationContext(), bookmarkArrayList , userid);
        recyclerView.setAdapter(mAdapter);
    }
}
