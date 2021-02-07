package com.example.mychat.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mychat.ChatActivity;
import com.example.mychat.ProfileActivity;
import com.example.mychat.R;
import com.example.mychat.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {

    Context context;
    List<ModelUsers> usersList;
    FirebaseAuth mAuth;
    String myUid;

//    constructor
    public AdapterUser(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
        mAuth = FirebaseAuth.getInstance();
        myUid = mAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        final String hisUID = usersList.get(position).getUid();
        String userImage = usersList.get(position).getImage();
        String userName = usersList.get(position).getName();
        final String userEmail = usersList.get(position).getEmail();

//        set data
        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(holder.mAvatarIv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.blockIv.setImageResource(R.drawable.ic_unblock_green);

        checkIsBlocked(hisUID, holder, position);

        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usersList.get(position).isBlocked()) {
                    unBlockUser(hisUID);
                } else {
                    blockUser(hisUID);
                }
            }
        });

//        handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0) {
                            Intent intent = new Intent(context, ProfileActivity.class);
                            intent.putExtra("uid", hisUID);
                            context.startActivity(intent);
                        }
                        if (which==1) {
                            isBlockedOrNot(hisUID);
                        }
                    }
                });
                builder.create().show();
            }
        });

    }

    private void isBlockedOrNot(String hisUID) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                Toast.makeText(context, "You are Blocked by user, can't send message...", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUid", hisUID);
                            context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUID, MyHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                holder.blockIv.setImageResource(R.drawable.ic_block_red);
                                usersList.get(position).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String hisUID) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Blocked Successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unBlockUser(String hisUID) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Unblocked successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //    view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarIv, blockIv;
        TextView mNameTv, mEmailTv;

    public MyHolder(@NonNull View itemView) {
        super(itemView);

//        init viwes
        mAvatarIv = itemView.findViewById(R.id.avatarIv);
        blockIv = itemView.findViewById(R.id.blockIv);
        mNameTv = itemView.findViewById(R.id.nameTv);
        mEmailTv = itemView.findViewById(R.id.emailTv);
    }
}
}
