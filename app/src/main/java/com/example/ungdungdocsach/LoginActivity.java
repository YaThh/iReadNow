package com.example.ungdungdocsach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.ungdungdocsach.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Dialog loginProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Khoi tao auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Setup progress dialog
        loginProgress = new Dialog(LoginActivity.this);
        loginProgress.setContentView(R.layout.dialog_login);
        loginProgress.setCancelable(false);

        binding.btnLoginMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginValidate();
            }
        });

        binding.tvNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private String email = "", password = "";
    private void loginValidate() {
        email = binding.edtLoginEmail.getText().toString().trim();
        password = binding.edtLoginPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(LoginActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
        } else {
            loginUser();
        }
    }

    private void loginUser() {
        loginProgress.show();

        //Dang nhap nguoi dung
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                checkUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loginProgress.dismiss();
                Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {
        //Kiem tra user va admin
        //Lay user hien tai
        FirebaseUser firebaseUser = auth.getCurrentUser();

        //Kiem tra db
        CollectionReference userInfoRef = db.collection("User").document(firebaseUser.getUid())
                .collection("UserInfo");
        Query query = userInfoRef.limit(1); //Chi co 1 doc trong UserInfo

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    loginProgress.dismiss();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String userType = document.getString("userType");
                        //Kiem tra loai user
                        if (userType.equals("user")) {
                            startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                            finish();
                        } else if (userType.equals("admin")) {
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                        }
                    }
                }
            }
        });
    }
}