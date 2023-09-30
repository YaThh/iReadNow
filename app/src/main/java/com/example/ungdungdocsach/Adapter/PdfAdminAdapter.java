package com.example.ungdungdocsach.Adapter;

import static com.example.ungdungdocsach.Utility.Constant.MAX_BYTES_PDF;

import android.app.Dialog;
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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ungdungdocsach.Filter.BookFilter;
import com.example.ungdungdocsach.Model.Book;
import com.example.ungdungdocsach.PdfListAdminActivity;
import com.example.ungdungdocsach.R;
import com.example.ungdungdocsach.Utility.Convert;
import com.example.ungdungdocsach.databinding.RecPdfAdminBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class PdfAdminAdapter extends RecyclerView.Adapter<PdfAdminAdapter.HolderPdfAdmin> implements Filterable {
    private Context context;
    public List<Book> bookList, filterList;
    private BookFilter filter;
    private RecPdfAdminBinding binding;
    private FirebaseFirestore db;
    private Dialog deleteProgress;

    public PdfAdminAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
        this.filterList = bookList;

        //Khoi tao progress dialog
        deleteProgress = new Dialog(context);
        deleteProgress.setContentView(R.layout.dialog_delete_book);
        deleteProgress.setCancelable(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RecPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        db = FirebaseFirestore.getInstance();

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        //Lay du lieu
        Book book = bookList.get(position);
        String title = book.getTitle();
        String description = book.getDescription();
        long timestamp = book.getTimestamp();
        String formattedDate = Convert.formatTimeStamp(timestamp);

        holder.progressView.setVisibility(View.VISIBLE);

        //Set du lieu
        holder.tv_title.setText(title);
        holder.tv_description.setText(description);
        holder.tv_date.setText(formattedDate);

        //Load danh muc, pdf tu url, kich thuoc pdf
        loadCategory(book, holder);
        loadPdfFromUrl(book, holder);
        loadPdfSize(book, holder);

        //Su kien xoa, sua
        holder.btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionDialog(book, holder);
            }
        });

    }

    private void optionDialog(Book book, HolderPdfAdmin holder) {
        String[] options = {"Sửa", "Xoá"};

        //Alert diaglog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chức Năng")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                         if (i == 0) { //Su kien sua

                         } else if (i == 1) { //Su kien xoa
                             deleteBook(book, holder);
                         }
                    }
                })
                .show();
    }

    private void deleteBook(Book book, HolderPdfAdmin holder) {
        String bookId = book.getId();
        String bookUrl = book.getUrl();

        deleteProgress.show();

        //Xoa file trong storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                         CollectionReference bookRef = db.collection("Book");
                         bookRef.whereEqualTo("id", bookId)
                                 .get()
                                 .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                     @Override
                                     public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                         for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                             documentSnapshot.getReference()
                                                     .delete()
                                                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                         @Override
                                                         public void onSuccess(Void unused) {
                                                             int position = bookList.indexOf(book);
                                                             if (position != -1) {
                                                                 bookList.remove(position);
                                                                 notifyItemRemoved(position);
                                                             }
                                                         }
                                                     })
                                                     .addOnFailureListener(new OnFailureListener() {
                                                         @Override
                                                         public void onFailure(@NonNull Exception e) {
                                                             deleteProgress.dismiss();
                                                             Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                         }
                                                     });
                                             deleteProgress.dismiss();
                                             Toast.makeText(context, "Xoá sách thành công", Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                 })
                                 .addOnFailureListener(new OnFailureListener() {
                                     @Override
                                     public void onFailure(@NonNull Exception e) {
                                         deleteProgress.dismiss();
                                         Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                     }
                                 });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfSize(Book book, HolderPdfAdmin holder) {
        String pdfUrl = book.getUrl();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();

                        //Chuye doi bytes sang KB, MB
                        double kb = bytes / 1024;
                        double mb = kb / 1024;

                        if (mb >= 1) {
                            holder.tv_size.setText(String.format("%.2f", mb) + " MB");
                        } else if (kb >= 1) {
                            holder.tv_size.setText(String.format("%.2f", kb) + " KB");
                        } else {
                            holder.tv_size.setText(String.format("%.2f", bytes) + " bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfFromUrl(Book book, HolderPdfAdmin holder) {
        String pdfUrl = book.getUrl();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        //Set pdfview
                        holder.pdfView.fromBytes(bytes)
                                .pages(0) //Chi hien thi trang dau
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        holder.progressView.setVisibility(View.INVISIBLE);
                                        Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        holder.progressView.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .load();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCategory(Book book, HolderPdfAdmin holder) {
        String categoryId = book.getCategoryId();

        CollectionReference cateRef = db.collection("Category");
        cateRef.whereEqualTo("id", categoryId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                            String category = queryDocumentSnapshot.getString("category");
                            holder.tv_category.setText(category);
                        }
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
        return bookList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new BookFilter(filterList, this);
        }
        return filter;
    }

        class HolderPdfAdmin extends RecyclerView.ViewHolder {

            PDFView pdfView;
            com.rey.material.widget.ProgressView progressView;
            TextView tv_title, tv_description, tv_category, tv_size, tv_date;
            ImageButton btn_more;

            public HolderPdfAdmin(@NonNull View itemView) {
                super(itemView);
                pdfView = binding.pdfView;
                progressView = binding.pbarPdf;
                tv_title = binding.tvTitle;
                tv_description = binding.tvDescription;
                tv_category = binding.tvCategory;
                tv_size = binding.tvSize;
                tv_date = binding.tvDate;
                btn_more = binding.btnMore;
            }
        }
}
