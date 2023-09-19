package com.example.ungdungdocsach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Khoi tao auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //chay main sau 2 giay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();
            }
        }, 2000);
    }

    private void checkUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {
            //user chua dang nhap
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } else {
            //Kiem tra db
            CollectionReference userInfoRef = db.collection("User").document(firebaseUser.getUid())
                    .collection("UserInfo");
            Query query = userInfoRef.limit(1); //Chi co 1 doc trong UserInfo

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userType = document.getString("userType");
                            //Kiem tra loai user
                            if (userType.equals("user")) {
                                startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
                                finish();
                            } else if (userType.equals("admin")) {
                                startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                                finish();
                            }
                        }
                    }
                }
            });
        }
    }
}