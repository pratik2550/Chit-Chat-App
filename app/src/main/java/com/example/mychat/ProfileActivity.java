package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mychat.adapters.AdapterPost;
import com.example.mychat.models.ModelPost;
import com.google.android.gms.auth.api.Auth;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    //    views
    ImageView profileImage, coverImage;
    TextView nameTv, emailTv, phoneTv;
    RecyclerView recyclerViewPost;

    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid, pUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        profileImage = findViewById(R.id.profile_image);
        coverImage = findViewById(R.id.cover_image);
        nameTv = findViewById(R.id.name_Tv);
        emailTv = findViewById(R.id.email_Tv);
        phoneTv = findViewById(R.id.phone_Tv);
        recyclerViewPost = findViewById(R.id.recyclerViewPost);

        Intent intent = getIntent();
        pUid = intent.getStringExtra("uid");
        Log.i("Uid 1", pUid);
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(pUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

//                checks until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                    get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        Picasso.get().load(image).into(profileImage);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_cover).into(profileImage);
                    }
                    try {
                        Picasso.get().load(cover).into(coverImage);
                    } catch (Exception e) {
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postList = new ArrayList<>();

        checkUserstatus();
        loadHisPost();
    }

    private void searchHisPost(final String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerViewPost.setLayoutManager(layoutManager);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(pUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPost = ds.getValue(ModelPost.class);
                    if (myPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) || myPost.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postList.add(myPost);
                    }
                    adapterPost = new AdapterPost(ProfileActivity.this, postList);
                    recyclerViewPost.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHisPost() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerViewPost.setLayoutManager(layoutManager);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Log.i("Uid", pUid);
        Query query = ref.orderByChild("uid").equalTo(pUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPost = ds.getValue(ModelPost.class);
                    postList.add(myPost);
                    adapterPost = new AdapterPost(ProfileActivity.this, postList);
                    recyclerViewPost.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserstatus() {
//        get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
//            mProfileTxt.setText(user.getEmail());
            uid = user.getUid();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    searchHisPost(query);
                } else {
                    loadHisPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (!TextUtils.isEmpty(query)) {
                    searchHisPost(query);
                } else {
                    loadHisPost();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            mAuth.signOut();
            checkUserstatus();
        }
        return super.onOptionsItemSelected(item);
    }
}