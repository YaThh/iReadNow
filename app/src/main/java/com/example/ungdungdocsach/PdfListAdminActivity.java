package com.example.ungdungdocsach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.ungdungdocsach.Adapter.PdfAdminAdapter;
import com.example.ungdungdocsach.Model.Book;
import com.example.ungdungdocsach.databinding.ActivityPdfListAdminBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PdfListAdminActivity extends AppCompatActivity {

    private ActivityPdfListAdminBinding binding;
    private List<Book> bookList;
    private PdfAdminAdapter pdfAdminAdapter;
    private String categoryId, categoryTitle;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        //Lay du lieu tu intent CategoryAdapter
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle =  intent.getStringExtra("categoryTitle");

        binding.tvSubTitle.setText(categoryTitle);

        loadPdfList();

        //Su kien tim kiem
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    pdfAdminAdapter.getFilter().filter(charSequence);
                } catch (Exception e) {
                    Toast.makeText(PdfListAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Su kien back
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadPdfList() {
        bookList = new ArrayList<>();

        CollectionReference cateRef = db.collection("Book");
        cateRef.whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Book book = documentSnapshot.toObject(Book.class);
                            bookList.add(book);
                        }

                        pdfAdminAdapter = new PdfAdminAdapter(PdfListAdminActivity.this, bookList);
                        binding.recBook.setAdapter(pdfAdminAdapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PdfListAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}