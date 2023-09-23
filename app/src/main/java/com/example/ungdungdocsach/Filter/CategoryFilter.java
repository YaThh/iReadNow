package com.example.ungdungdocsach.Filter;

import android.widget.Filter;

import com.example.ungdungdocsach.Adapter.CategoryAdapter;
import com.example.ungdungdocsach.Model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryFilter extends Filter {
    List<Category> categoryList;
    CategoryAdapter categoryAdapter;

    public CategoryFilter(List<Category> categoryList, CategoryAdapter categoryAdapter) {
        this.categoryList = categoryList;
        this.categoryAdapter = categoryAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //Value khong duoc null va rong
        if (charSequence != null && charSequence.length() > 0) {
            //Doi sang chu thuong hoac hoa de khong bi loi
            charSequence = charSequence.toString().toLowerCase();
            List<Category> filterList = new ArrayList<>();
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).getCategory().toLowerCase().contains(charSequence)) {
                    filterList.add(categoryList.get(i));
                }
            }
            results.count = filterList.size();
            results.values = filterList;
        } else {
            results.count = categoryList.size();
            results.values = categoryList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        categoryAdapter.categoryList = (ArrayList<Category>) filterResults.values;

        categoryAdapter.notifyDataSetChanged();
    }
}
