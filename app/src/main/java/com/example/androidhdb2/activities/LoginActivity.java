package com.example.androidhdb2.activities;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidhdb2.R;
import com.example.androidhdb2.model.Bookmark;
import com.example.androidhdb2.model.User;
import com.example.androidhdb2.utils.PastBtoDataProvider;
import com.example.androidhdb2.utils.SBFDataProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 1001;
    public static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private TextView textStatus;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialization
        FirebaseApp.initializeApp(this);
        textStatus = findViewById(R.id.textStatus);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.guestBtn).setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }


    private void updateUI(FirebaseUser account) {
        if (account != null) {
            if (account.getDisplayName() != null)
                Toast.makeText(this, "Signed in as " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Signed in as Guest", Toast.LENGTH_SHORT).show();
//            checkFirebaseUser(account);

//            PastBtoDataProvider provider = new PastBtoDataProvider();
//            provider.readCSV(this);
//            SBFDataProvider p = new SBFDataProvider();
//            p.readCSV(this);
            String accId = account.getUid();
            ReadUserFile(accId);

            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.putExtra("UserID", accId);
            startActivity(mainIntent);
        }
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, "Signing in", Toast.LENGTH_SHORT).show();
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.guestBtn:
                signInAnonymously();
                break;
        }
    }

//    public void addFirebaseUser(FirebaseUser account) {
//        // Add user to Firebase
//        String userid = account.getUid();
//        if (account != null) {
//            Map<String, String> userMap = new HashMap<>();
//            db.collection("Users").document(userid)
//                    .set(userMap)
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d(TAG, "DocumentSnapshot successfully written!");
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.w(TAG, "Error writing document", e);
//                        }
//                    });
//        }
//    }


//    private void checkFirebaseUser(FirebaseUser account) {
//        String userid = account.getUid();
//        CollectionReference colRef = db.collection("Users");
//        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        if (document.equals(userid)) {
//                            Log.d(TAG, "User found!");
//                            user = document.toObject(User.class);
//
//                            return;
//                        }
//                        addFirebaseUser(account);
//                    }
//                }
//            }
//        });
//    }

    private void ReadUserFile(String uid) {
        File file = new File(getFilesDir(), uid );
        if (file.exists()) {
            Log.d(TAG, "File exists! Please import bookmarks");
        } else {
            CreateUserFile(uid);
        }
        userid = uid;
    }

    private void CreateUserFile(String uid) {

        FileOutputStream fileOutputStream = null;
        File file = new File(uid);
        User user = new User(uid, new ArrayList<Bookmark>());

        try {
            fileOutputStream = openFileOutput(uid, MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(user);
            objectOutputStream.close();
            Log.d(TAG, "File written");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

