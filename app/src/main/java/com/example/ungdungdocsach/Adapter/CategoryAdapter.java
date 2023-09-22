package com.example.ungdungdocsach.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ungdungdocsach.Model.Category;
import com.example.ungdungdocsach.databinding.RecCategoryBinding;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.HolderCategory> {
    @NonNull
    private Context context;
    private List<Category> categoryList;
    private RecCategoryBinding binding;

    public CategoryAdapter(@NonNull Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RecCategoryBinding.inflate(LayoutInflater.from(context), parent, false);

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

            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
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
