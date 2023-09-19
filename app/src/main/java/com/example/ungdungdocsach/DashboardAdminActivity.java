package com.example.ungdungdocsach;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.ungdungdocsach.databinding.ActivityDashboardAdminBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardAdminActivity extends AppCompatActivity {

    private ActivityDashboardAdminBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Khoi tao auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        checkUser();

        //Su kien logout
        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                checkUser();
            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {
            //Chua login
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            //lay email gan cho textview
            String email = firebaseUser.getEmail();
            binding.tvSubTitle.setText(email);
        }
    }
}