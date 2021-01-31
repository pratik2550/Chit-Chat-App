package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;

    EditText titleEt, descriptionEt;
    ImageView postImage;
    Button uploadBtn;
    ImageButton addImageBtn;

    String name, email, uid, dp;

    Uri image_uri = null;

    ProgressDialog mProgressDialog;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    String cameraPermissions[];
    String storagePermission[];
    String editTitle, editDesc, editImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        titleEt = findViewById(R.id.postTitleEt);
        descriptionEt = findViewById(R.id.postDescriptionEt);
        postImage = findViewById(R.id.postImageIv);
        uploadBtn = findViewById(R.id.postUploadButton);
        addImageBtn = findViewById(R.id.addImageBtn);

        Intent intent = getIntent();
        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");
        if (isUpdateKey.equals("editPost")) {
            actionBar.setTitle("Update Post");
            uploadBtn.setText("Update");
            loadPostData(editPostId);
        } else {
            actionBar.setTitle("Add New Post");
            uploadBtn.setText("Post");
        }

        actionBar.setSubtitle(email);

        mProgressDialog = new ProgressDialog(this);

        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
                postImage.setVisibility(View.VISIBLE);
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isUpdateKey.equals("editPost")) {
                    beingUpdate(title, description, editPostId);
                }else {
                    uploadData(title, description);
                }
            }
        });
    }

    private void beingUpdate(String title, String description, String editPostId) {
        mProgressDialog.setMessage("Updating Post...");
        mProgressDialog.show();

        if (!editImage.equals("noImage")) {
            updateWithImage(title, description, editPostId);
        } else if (postImage.getDrawable() != null){
            updateWithNewImage(title, description, editPostId);
        } else {
            updateWithoutImage(title, description, editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescription", description);
        hashMap.put("pImage", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateWithNewImage(String title, String description, String editPostId) {

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Posts/"+"post_"+timeStamp;

        Bitmap bitmap = ((BitmapDrawable)postImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pTitle", title);
                            hashMap.put("pDescription", description);
                            hashMap.put("pImage", downloadUri);
                            Toast.makeText(AddPostActivity.this, ""+downloadUri, Toast.LENGTH_SHORT).show();

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateWithImage(String title, String description, String editPostId) {
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathName = "Posts/"+"post_"+timeStamp;

                        Bitmap bitmap = ((BitmapDrawable)postImage.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());

                                        String downloadUri = uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()) {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("uid", uid);
                                            hashMap.put("uName", name);
                                            hashMap.put("uEmail", email);
                                            hashMap.put("uDp", dp);
                                            hashMap.put("pTitle", title);
                                            hashMap.put("pDescription", description);
                                            hashMap.put("pImage", downloadUri);

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                            ref.child(editPostId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mProgressDialog.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            mProgressDialog.dismiss();
                                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    editTitle = ""+ds.child("pTitle").getValue();
                    editDesc = ""+ds.child("pDescription").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDesc);
                    if (!editImage.equals("noImage")) {
                        try {
                            Picasso.get().load(editImage).into(postImage);
                        } catch (Exception e) {

                        }
                    } else {
                        postImage.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(final String title, final String description) {
        mProgressDialog.setMessage("Publishing Post");
        mProgressDialog.show();

        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Posts/" + "post_" + timeStamp;

        if (postImage.getDrawable() != null) {

            Bitmap bitmap = ((BitmapDrawable)postImage.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", dp);
                                hashMap.put("pId", timeStamp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pDescription", description);
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("pLikes", "0");
                                hashMap.put("pTime", timeStamp);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                postImage.setImageResource(R.drawable.ic_add_image);
                                                image_uri = null;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescription", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pLikes", "0");
            hashMap.put("pTime", timeStamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mProgressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                            titleEt.setText("");
                            descriptionEt.setText("");
                            postImage.setImageResource(R.drawable.ic_add_image);
                            image_uri = null;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chose Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else pickFromCamera();
                }
                if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private boolean checkStoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void pickFromCamera() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Please enable camera and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE: {

                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();

                postImage.setImageURI(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {

                postImage.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
//        get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
//            mProfileTxt.setText(user.getEmail());
            email = user.getEmail();
            uid = user.getUid();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}