package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //    views
    TextInputEditText mEmailRg, mPasswordRg;
    Button mRegisterBtn;
    TextView mHaveaccount;

    //    progressbar to display while registering user
    ProgressDialog progressDialog;

    //    Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

//        Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

//        Actionbar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
//        enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

//        init
        mEmailRg = findViewById(R.id.email_txt);
        mPasswordRg = findViewById(R.id.password_txt);
        mRegisterBtn = findViewById(R.id.register_btn);
        mHaveaccount =findViewById(R.id.have_account);

//        progressBar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

//        handle visibility of password
//        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked){
//                    mPasswordRg.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                    showPassword.setText("Hide");
//                }
//                else{
//                    mPasswordRg.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                    showPassword.setText("Show");
//                }
//            }
//        });

//        handle register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailRg.getText().toString().trim();
                String password = mPasswordRg.getText().toString().trim();

//                validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailRg.setError("Invalid Email");
                    mEmailRg.setFocusable(true);
                }
                else if (password.length()<6){
                    mPasswordRg.setError("Password length atleast 6 character");
                    mPasswordRg.setFocusable(true);
                }
                else{
                    registerUser(email, password);
                }
            }
        });

//        handle have account textview
        mHaveaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password) {

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

//                            Get user email and uid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();

//                            Store user informaton in real time database
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", "");
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("phone", "");
                            hashMap.put("image", "");
                            hashMap.put("cover", "");
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(uid).setValue(hashMap);


                            Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
