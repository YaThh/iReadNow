package com.example.ungdungdocsach;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ungdungdocsach.Model.Category;
import com.example.ungdungdocsach.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ActivityResultLauncher<Intent> pdfPickLauncher;
    private Uri pdfUri = null;
    private List<String> categoryTitleList, categoryIdList;
    private Dialog uploadProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Khoi tao auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        //Set up uploadProgress
        uploadProgress = new Dialog(PdfAddActivity.this);
        uploadProgress.setContentView(R.layout.dialog_pdf_upload);
        uploadProgress.setCancelable(false);

        loadPdfCategory();

        //Khoi tao pdfPickLauncher
        pdfPickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            pdfUri = data.getData();
                        }
                    }
                    else {
                        Toast.makeText(this, "Đã huỷ", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        //Su kien back
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Su kien up pdf
        binding.btnAttach.setOnClickListener(view -> pdfPickIntent());

        //Su kien chon danh muc
        binding.tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDiaglog();
            }
        });

        //Su kien xac nhan
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitValidate();
            }
        });
    }

    String title = "", description = "";
    private void submitValidate() {
        title = binding.edtTitle.getText().toString().trim();
        description = binding.edtDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(PdfAddActivity.this, "Vui lòng nhập tên sách", Toast.LENGTH_SHORT).show();
        } else if (description.isEmpty()) {
            Toast.makeText(PdfAddActivity.this, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show();
        } else if (selectedCategoryTitle.isEmpty()) {
            Toast.makeText(PdfAddActivity.this, "Vui lòng nhập danh mục", Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(PdfAddActivity.this, "Vui lòng tải lên file pdf", Toast.LENGTH_SHORT).show();
        } else {
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        uploadProgress.show();

        long timestamp = System.currentTimeMillis();
        String filePath = "Books/" + timestamp;

        StorageReference storageReference = storage.getReference(filePath);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //Lay url cua pdf
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String pdfUrl = "" + uriTask.getResult();

                        uploadPdfToDb(pdfUrl, timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadProgress.dismiss();
                        Toast.makeText(PdfAddActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfToDb(String pdfUrl, long timestamp) {
        String uid = auth.getUid();

        //Setup du lieu
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("id", timestamp);
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("categoryId", selectedCategoryId);
        hashMap.put("url", pdfUrl);
        hashMap.put("timestamp", timestamp);

        CollectionReference pdfRef = db.collection("Book");
        pdfRef.add(hashMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        uploadProgress.dismiss();
                        Toast.makeText(PdfAddActivity.this, "Tải lên thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadProgress.dismiss();
                        Toast.makeText(PdfAddActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfCategory() {
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
                            String categoryTitle = documentSnapshot.getString("category");
                            String categoryId = documentSnapshot.getString("id");

                            Log.d("CategoryTag", ""+ categoryTitle);

                            categoryTitleList.add(categoryTitle);
                            categoryIdList.add(categoryId);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PdfAddActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Chon danh muc
    private String selectedCategoryId = "", selectedCategoryTitle = "";
    private void categoryPickDiaglog() {
        //Lay chuoi tu list
        String[] categoryName = new String[categoryTitleList.size()];
        for (int i = 0; i < categoryTitleList.size(); i++) {
            categoryName[i] = categoryTitleList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Danh Mục")
                .setItems(categoryName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Lay item duoc nhan tu list
                        selectedCategoryTitle = categoryTitleList.get(i);
                        selectedCategoryId = categoryIdList.get(i);
                        binding.tvCategory.setText(selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void pdfPickIntent() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pdfPickLauncher.launch(Intent.createChooser(intent, "Chọn file PDF"));
    }

}