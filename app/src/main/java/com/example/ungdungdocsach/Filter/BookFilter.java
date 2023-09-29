package com.example.ungdungdocsach.Filter;

import android.widget.Filter;

import com.example.ungdungdocsach.Adapter.CategoryAdapter;
import com.example.ungdungdocsach.Adapter.PdfAdminAdapter;
import com.example.ungdungdocsach.Model.Book;
import com.example.ungdungdocsach.Model.Category;

import java.util.ArrayList;
import java.util.List;

public class BookFilter extends Filter {
    List<Book> bookList;
    PdfAdminAdapter pdfAdminAdapter;

    public BookFilter(List<Book> bookList, PdfAdminAdapter pdfAdminAdapter) {
        this.bookList = bookList;
        this.pdfAdminAdapter = pdfAdminAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //Value khong duoc null va rong
        if (charSequence != null && charSequence.length() > 0) {
            //Doi sang chu thuong hoac hoa de khong bi loi
            charSequence = charSequence.toString().toLowerCase();
            List<Book> filterList = new ArrayList<>();
            for (int i = 0; i < bookList.size(); i++) {
                if (bookList.get(i).getTitle().toLowerCase().contains(charSequence)) {
                    filterList.add(bookList.get(i));
                }
            }
            results.count = filterList.size();
            results.values = filterList;
        } else {
            results.count = bookList.size();
            results.values = bookList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        pdfAdminAdapter.bookList = (ArrayList<Book>) filterResults.values;

        pdfAdminAdapter.notifyDataSetChanged();
    }
}
