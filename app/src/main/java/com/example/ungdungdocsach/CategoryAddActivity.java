package com.example.ungdungdocsach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.ungdungdocsach.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {

    private ActivityCategoryAddBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Dialog addCategoryProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Khoi tao auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Setup progress dialog
        addCategoryProgress = new Dialog(CategoryAddActivity.this);
        addCategoryProgress.setContentView(R.layout.dialog_add_category);
        addCategoryProgress.setCancelable(false);

        //Su kien back
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Su kien them
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitValidate();
            }
        });
    }

    private String category = "";
    private void submitValidate() {
        category = binding.edtCategory.getText().toString().trim();
        if (category.isEmpty()) {
            Toast.makeText(CategoryAddActivity.this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
        } else {
            addCategory();
        }
    }

    private void addCategory() {
        addCategoryProgress.show();

        long timestamp = System.currentTimeMillis();

        //Setup du lieu cho danh muc
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("timestamp", timestamp);
        hashMap.put("category", category);
        hashMap.put("uid", auth.getUid());

        //Them vao firebase
        CollectionReference categoryRef = db.collection("Category");
        categoryRef.add(hashMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                addCategoryProgress.dismiss();
                Toast.makeText(CategoryAddActivity.this, "Thêm danh mục thành công",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                addCategoryProgress.dismiss();
                Toast.makeText(CategoryAddActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }
}