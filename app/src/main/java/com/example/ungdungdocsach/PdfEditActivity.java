package com.example.ungdungdocsach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.ungdungdocsach.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding binding;
    private String bookId;
    private Dialog updateProgress;
    private List<String> categoryTitleList, categoryIdList;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        //Lay bookId tu intent PdfAdminAdapter
        bookId = getIntent().getStringExtra("bookId");

        //Khoi tao updateProgress
        updateProgress = new Dialog(PdfEditActivity.this);
        updateProgress.setContentView(R.layout.dialog_update_book);
        updateProgress.setCancelable(false);

        loadCategory();
        loadBookInfo();

        //Su kien back
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Su kien chon danh muc
        binding.tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        //Su kien cap nhat
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitValidate();
            }
        });
    }

    private String title = "", description = "";
    private void submitValidate() {
        title = binding.edtTitle.getText().toString().trim();
        description = binding.edtDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(PdfEditActivity.this, "Vui lòng nhập tên sách", Toast.LENGTH_SHORT).show();
        } else if (title.isEmpty()) {
            Toast.makeText(PdfEditActivity.this, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show();
        } else if (selectedCategoryId.isEmpty()){
            Toast.makeText(PdfEditActivity.this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
        } else {
            updateBook();
        }
    }

    private void updateBook() {
        updateProgress.show();

        //Setup du lieu vao db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("categoryId", selectedCategoryId);

        CollectionReference bookRef = db.collection("Book");
        bookRef.whereEqualTo("id", bookId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            documentSnapshot.getReference().update(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            updateProgress.dismiss();
                                            Toast.makeText(PdfEditActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            updateProgress.dismiss();
                                            Toast.makeText(PdfEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        updateProgress.dismiss();
                        Toast.makeText(PdfEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private String selectedCategoryTitle = "", selectedCategoryId = "";
    private void categoryPickDialog() {
        //Lay chuoi tu list
        String[] categoryName = new String[categoryTitleList.size()];
        for (int i = 0; i < categoryName.length; i++) {
            categoryName[i] = categoryTitleList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(PdfEditActivity.this);
        builder.setTitle("Chọn Danh Mục")
                .setItems(categoryName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Lay item nhan duoc tu list
                        selectedCategoryTitle = categoryTitleList.get(i);
                        selectedCategoryId = categoryIdList.get(i);
                        binding.tvCategory.setText(selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void loadBookInfo() {
        CollectionReference bookRef = db.collection("Book");

        bookRef.whereEqualTo("id", bookId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            //Lay thong tin sach
                            selectedCategoryId = documentSnapshot.getString("categoryId");
                            String description = documentSnapshot.getString("description");
                            String title = documentSnapshot.getString("title");

                            //Set du lieu len view
                            binding.edtTitle.setText(title);
                            binding.edtDescription.setText(description);

                            CollectionReference cateRef = db.collection("Category");
                            cateRef.whereEqualTo("id", selectedCategoryId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                                                //Lay thong tin danh muc
                                                String category = documentSnapshot1.getString("category");

                                                //Set danh muc len textview
                                                binding.tvCategory.setText(category);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(PdfEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PdfEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadCategory() {
        categoryTitleList = new ArrayList<>();
        categoryIdList = new ArrayList<>();

        CollectionReference cateRef = db.collection("Category");
        cateRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        categoryTitleList.clear();
                        categoryIdList.clear();

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            //Lay thong tin danh muc tu db
                            String categoryTitle = documentSnapshot.getString("category");
                            String categoryId = documentSnapshot.getString("id");

                            categoryTitleList.add(categoryTitle);
                            categoryIdList.add(categoryId);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PdfEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}