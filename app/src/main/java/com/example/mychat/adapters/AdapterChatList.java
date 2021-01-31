package com.example.mychat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mychat.ChatActivity;
import com.example.mychat.R;
import com.example.mychat.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {

    Context context;
    List<ModelUsers> usersList;
    private HashMap<String, String> lastMsgMap;

    public AdapterChatList(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastMsgMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chat_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String hisUid = usersList.get(position).getUid();
        String userImage = usersList.get(position).getImage();
        String userName = usersList.get(position).getName();
        String lastMsg = lastMsgMap.get(hisUid);

        holder.nameTv.setText(userName);
        if (lastMsg==null || lastMsg.equals("default")) {
            holder.lastMsgTv.setVisibility(View.GONE);
        } else {
            holder.lastMsgTv.setVisibility(View.VISIBLE);
            holder.lastMsgTv.setText(lastMsg);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default).into(holder.profileIv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.ic_default).into(holder.profileIv);
        }

        if (usersList.get(position).getOnlineStatus().equals("online")) {
            holder.onlineStatusTv.setImageResource(R.drawable.circle_online);
        } else {
            holder.onlineStatusTv.setImageResource(R.drawable.circle_offline);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public void setLastMsgMap(String userId, String lastMsg) {
        lastMsgMap.put(userId, lastMsg);
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView profileIv, onlineStatusTv;
        TextView nameTv, lastMsgTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusTv = itemView.findViewById(R.id.onlineStatusTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMsgTv = itemView.findViewById(R.id.lastMsgTv);
        }
    }
}
