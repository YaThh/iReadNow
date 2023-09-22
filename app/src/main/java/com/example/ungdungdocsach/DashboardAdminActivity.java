package com.example.ungdungdocsach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.ungdungdocsach.Adapter.CategoryAdapter;
import com.example.ungdungdocsach.Model.Category;
import com.example.ungdungdocsach.databinding.ActivityDashboardAdminBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

public class DashboardAdminActivity extends AppCompatActivity {

    private ActivityDashboardAdminBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private List<Category> categoryList;
    private CategoryAdapter categoryAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Khoi tao auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        checkUser();
        loadCategory();

        //Su kien logout
        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                checkUser();
            }
        });

        //Su kien them danh muc
        binding.btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class));
            }
        });
    }

    private void loadCategory() {
        categoryList = new ArrayList<>();
        CollectionReference cateRef = db.collection("Category");

        cateRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                categoryList.clear();

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Category category = documentSnapshot.toObject(Category.class);
                    categoryList.add(category);
                }
                //Setup adapter
                categoryAdapter = new CategoryAdapter(DashboardAdminActivity.this, categoryList);
                binding.recCategory.setAdapter(categoryAdapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DashboardAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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