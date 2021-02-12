package com.example.mychat;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.view.ViewGroup;

import com.example.mychat.adapters.AdapterUser;
import com.example.mychat.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

//    views
    RecyclerView recyclerView;
    AdapterUser adapterUser;
    List<ModelUsers> userList;

    FirebaseAuth firebaseAuth;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

//        init recycleView
        recyclerView = view.findViewById(R.id.user_recycleView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

//        init firebaseauth
        firebaseAuth = FirebaseAuth.getInstance();

//        init user list
        userList = new ArrayList<>();

        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    if (!modelUsers.getUid().equals(fUser.getUid())){
                        userList.add(modelUsers);
                    }

                    adapterUser = new AdapterUser(getActivity(), userList);
                    recyclerView.setAdapter(adapterUser);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(final String query) {

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    if (!modelUsers.getUid().equals(fUser.getUid())){
                        if (modelUsers.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUsers);
                        }
                    }

//                    adapter
                    adapterUser = new AdapterUser(getActivity(), userList);
//                    refresh adapter
                    adapterUser.notifyDataSetChanged();
//                    set adapter to recycler view
                    recyclerView.setAdapter(adapterUser);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkUserStatus(){
//        get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
//            mProfileTxt.setText(user.getEmail());
        }
        else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    //    menu inflater option
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_notification).setVisible(false);
//        search view
        MenuItem item = menu.findItem(R.id.action_search);
//        SearchView searchView = MenuItemCompat.getActionView(item);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

//        search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if (!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }
                else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }
                else {
                    getAllUsers();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

//    handle menue click listner
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        get item id
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        } else if (id == R.id.action_setting) {
            startActivity(new Intent(getActivity(), SettingActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
