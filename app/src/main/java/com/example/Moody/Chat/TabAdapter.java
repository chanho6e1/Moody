package com.example.Moody.Chat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Moody.Activity.LoginActivity;
import com.example.Moody.R;
import com.example.Moody.Model.FeedItems;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TabAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<FeedItems> feedDataArrayList;
    Context context;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();//현재 로그인 정보
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public TabAdapter(Context context,ArrayList<FeedItems> feedDataArrayList){
        this.context=context;
        this.feedDataArrayList = feedDataArrayList;

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Button public_btn;
        Button personal_btn;
        Button favorites_btn;
        RecyclerView tagRecyclerview;

        MyViewHolder(View view){
            super(view);
            public_btn=view.findViewById(R.id.public_btn);
            personal_btn=view.findViewById(R.id.personal_btn);
            favorites_btn=view.findViewById(R.id.favorites_btn);
            tagRecyclerview=view.findViewById(R.id.tag_recyclerview);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auto_tab, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final MyViewHolder myViewHolder = (MyViewHolder) holder;
        ArrayList<FeedItems> tagItems=new ArrayList<>();
        for(int i=0;i<feedDataArrayList.size();i++) {
            FeedItems entity = new FeedItems();
            if (feedDataArrayList.get(i).getUrl() != null) {
                entity.setUrl(feedDataArrayList.get(i).getUrl());
                entity.setTag(feedDataArrayList.get(i).getTag());
                tagItems.add(entity);
            }
        }
        TabImageAdapter myAdapter = new TabImageAdapter(context,tagItems);
        myViewHolder.tagRecyclerview.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context,2);
        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        myViewHolder.tagRecyclerview.setLayoutManager(gridLayoutManager);
        myViewHolder.tagRecyclerview.setAdapter(myAdapter);

        myViewHolder.public_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                myViewHolder.public_btn.setBackgroundResource(R.drawable.auto_click_btn);
                myViewHolder.personal_btn.setBackgroundColor(Color.parseColor("#00ff0000"));
                myViewHolder.favorites_btn.setBackgroundColor(Color.parseColor("#00ff0000"));
                ArrayList<FeedItems> tagItems=new ArrayList<>();
                for(int i=0;i<feedDataArrayList.size();i++) {
                    FeedItems entity = new FeedItems();
                    if (feedDataArrayList.get(i).getUrl() != null) {
                        entity.setUrl(feedDataArrayList.get(i).getUrl());
                        entity.setTag(feedDataArrayList.get(i).getTag());
                        tagItems.add(entity);
                    }
                }
                TabImageAdapter myAdapter = new TabImageAdapter(context,tagItems);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context,2);
                gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                myViewHolder.tagRecyclerview.setLayoutManager(gridLayoutManager);
                myViewHolder.tagRecyclerview.setAdapter(myAdapter);

            }
        });

        myViewHolder.personal_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                myViewHolder.personal_btn.setBackgroundResource(R.drawable.auto_click_btn);
                myViewHolder.public_btn.setBackgroundColor(Color.parseColor("#00ff0000"));
                myViewHolder.favorites_btn.setBackgroundColor(Color.parseColor("#00ff0000"));
                ArrayList<FeedItems> tagItems=new ArrayList<>();
                for(int i=0;i<feedDataArrayList.size();i++) {
                    FeedItems entity = new FeedItems();
                    if (feedDataArrayList.get(i).getImage() != null) {
                        entity.setImage(feedDataArrayList.get(i).getImage());
                        entity.setTag(feedDataArrayList.get(i).getTag());
                        entity.setStar(feedDataArrayList.get(i).getStar());

                        tagItems.add(entity);
                    }
                }

                TabImageAdapter myAdapter = new TabImageAdapter(context,tagItems);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context,2);
                gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                myViewHolder.tagRecyclerview.setLayoutManager(gridLayoutManager);
                myViewHolder.tagRecyclerview.setAdapter(myAdapter);
            }
        });

        myViewHolder.favorites_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                myViewHolder.favorites_btn.setBackgroundResource(R.drawable.auto_click_btn);
                myViewHolder.personal_btn.setBackgroundColor(Color.parseColor("#00ff0000"));
                myViewHolder.public_btn.setBackgroundColor(Color.parseColor("#00ff0000"));
                ArrayList<FeedItems> tagItems=new ArrayList<>();
                ArrayList<FeedItems> starItems= LoginActivity.dbHelper.getMarkItems(2);
                for(int i=0;i<starItems.size();i++){
                    for(int j=0;j<feedDataArrayList.size();j++){
                        FeedItems entity=new FeedItems();
                        if(feedDataArrayList.get(j).getUrl()==null){
                            continue;
                        }
                        else if(feedDataArrayList.get(j).getUrl().equals(starItems.get(i).getUrl())){
                            entity.setUrl(feedDataArrayList.get(j).getUrl());
                            entity.setTag(feedDataArrayList.get(j).getTag());
                            entity.setStar(1);
                            feedDataArrayList.set(j,entity);
                        }
                    }
                }
                for(int i=0;i<feedDataArrayList.size();i++){
                    FeedItems entity=new FeedItems();
                    if(feedDataArrayList.get(i).getImage()!=null&&feedDataArrayList.get(i).getStar()==1){
                        entity.setImage(feedDataArrayList.get(i).getImage());
                        entity.setTag(feedDataArrayList.get(i).getTag());
                        entity.setStar(feedDataArrayList.get(i).getStar());
                        tagItems.add(entity);
                    }
                    else if(feedDataArrayList.get(i).getImage()==null&&feedDataArrayList.get(i).getStar()==1){
                        entity.setUrl(feedDataArrayList.get(i).getUrl());
                        entity.setTag(feedDataArrayList.get(i).getTag());
                        entity.setStar(feedDataArrayList.get(i).getStar());
                        tagItems.add(entity);
                    }
                }
                TabImageAdapter myAdapter = new TabImageAdapter(context,tagItems);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context,2);
                gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                myViewHolder.tagRecyclerview.setLayoutManager(gridLayoutManager);
                myViewHolder.tagRecyclerview.setAdapter(myAdapter);

            }
        });

    }

    @Override
    public int getItemCount() {
        return 1;
    }




}