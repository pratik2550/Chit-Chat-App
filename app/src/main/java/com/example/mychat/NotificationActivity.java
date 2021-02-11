package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.mychat.adapters.AdapterNotification;
import com.example.mychat.models.ModelNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView notificationRv;

    FirebaseAuth mAuth;

    public ArrayList<ModelNotification> notificationList;

    public AdapterNotification adapterNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationRv = findViewById(R.id.notificationRv);

        mAuth = FirebaseAuth.getInstance();

        getAllNotifications();
    }

    private void getAllNotifications() {
        notificationList  = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("Notification")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelNotification modelNotification = ds.getValue(ModelNotification.class);

                            notificationList.add(modelNotification);
                        }
                        adapterNotification = new AdapterNotification(NotificationActivity.this, notificationList);
                        notificationRv.setAdapter(adapterNotification);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}