package com.example.ungdungdocsach.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ungdungdocsach.Filter.CategoryFilter;
import com.example.ungdungdocsach.Model.Category;
import com.example.ungdungdocsach.databinding.RecCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.HolderCategory> implements Filterable {
    @NonNull
    private Context context;
    public List<Category> categoryList, filterList;
    private FirebaseFirestore db;
    private RecCategoryBinding binding;
    private CategoryFilter filter;

    public CategoryAdapter(@NonNull Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
        this.filterList = categoryList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RecCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        db = FirebaseFirestore.getInstance();

        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        //Lay du lieu
        Category categoryModel = categoryList.get(position);
        String category = categoryModel.getCategory();
        String uid = categoryModel.getUid();
        long timestamp = categoryModel.getTimestamp();

        //Gan du lieu
        holder.tv_category.setText(category);

        //Su kien xoa danh muc
        holder.btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xoá")
                        .setMessage("Xoá danh mục này?")
                        .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteCategory(categoryModel, holder);
                            }
                        })
                        .setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });
    }


    private void deleteCategory(Category categoryModel, HolderCategory holder) {
        String id = categoryModel.getId();
        CollectionReference cateRef = db.collection("Category");

        cateRef.whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            documentSnapshot.getReference().delete();
                            int position = categoryList.indexOf(categoryModel);
                            if (position != -1) {
                                categoryList.remove(position);
                                notifyItemRemoved(position);
                            }
                        }
                        Toast.makeText(context, "Xoá thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CategoryFilter(filterList, this);
        }
        return filter;
    }


    class HolderCategory extends RecyclerView.ViewHolder {

        TextView tv_category;
        ImageButton btn_del;
        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            tv_category = binding.tvCategory;
            btn_del = binding.btnDel;
        }
    }
}
