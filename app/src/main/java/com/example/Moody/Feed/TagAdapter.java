package com.example.Moody.Feed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ToggleButton;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.Moody.Activity.IntroActivity;
import com.example.Moody.Activity.LoginActivity;
import com.example.Moody.Model.FeedItems;
import com.example.Moody.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<FeedItems> feedDataArrayList;
    Context context;
    private String imageUrl;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();//현재 로그인 정보
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    public TagAdapter(Context context,ArrayList<FeedItems> feedDataArrayList){
        this.context=context;
        this.feedDataArrayList = feedDataArrayList;

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        Button tag;
        ToggleButton star;

        MyViewHolder(View view){
            super(view);
            image = view.findViewById(R.id.upload_image);
            tag = view.findViewById(R.id.tag_btn);
            star=view.findViewById(R.id.star_btn);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_image, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final MyViewHolder myViewHolder = (MyViewHolder) holder;
        String tagtext=myViewHolder.tag.getText().toString();
        tagtext=tagtext.substring(1);

        for(int i = 0; i< IntroActivity.publicItems.size(); i++) {
            FeedItems entity = new FeedItems();
            if(tagtext.equals(IntroActivity.publicItems.get(i).getType())) {
                entity.setUrl(IntroActivity.publicItems.get(i).getUrl());
                entity.setTag(IntroActivity.publicItems.get(i).getType());
                feedDataArrayList.add(entity);
            }
        }
        if(feedDataArrayList.get(position).getUrl()==null) {
            myViewHolder.image.setImageBitmap(feedDataArrayList.get(position).getImage());
        }
        else{
            Glide.with(context).load(feedDataArrayList.get(position).getUrl()).into(myViewHolder.image);
        }
        myViewHolder.tag.setText("#" + feedDataArrayList.get(position).getTag());
        if (feedDataArrayList.get(position).getStar() == 1) {
            myViewHolder.star.setBackgroundResource(R.drawable.yj_full_heart);
        }
        else if(!LoginActivity.dbHelper.searchItem(feedDataArrayList.get(position).getUrl())){
            myViewHolder.star.setBackgroundResource(R.drawable.yj_full_heart);
        }
        else {
            myViewHolder.star.setBackgroundResource(R.drawable.yj_heart2);
        }

        myViewHolder.star.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(myViewHolder.star.isChecked()){
                    myViewHolder.star.setBackgroundResource(R.drawable.yj_full_heart);
                    if(feedDataArrayList.get(position).getUrl()==null) {
                        LoginActivity.dbHelper.setStar(1, feedDataArrayList.get(position).getId());
                    }
                    else{
                        LoginActivity.dbHelper.pblInsert(feedDataArrayList.get(position).getUrl(), feedDataArrayList.get(position).getTag(), feedDataArrayList.get(position).getResult());
                    }
                }
                else{
                    myViewHolder.star.setBackgroundResource(R.drawable.yj_heart2);
                    if(feedDataArrayList.get(position).getUrl()==null) {
                        LoginActivity.dbHelper.setStar(0,feedDataArrayList.get(position).getId());
                    }
                    else
                        LoginActivity.dbHelper.pblDelete(feedDataArrayList.get(position).getUrl());
                }
            }
        });

        myViewHolder.tag.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String tagtext=myViewHolder.tag.getText().toString();
                tagtext=tagtext.substring(1);

                ArrayList<FeedItems> tagItems= LoginActivity.dbHelper.getTagItems(tagtext);

                for(int i = 0; i< IntroActivity.publicItems.size(); i++) {
                    FeedItems entity = new FeedItems();

                    if(tagtext.equals(IntroActivity.publicItems.get(i).getType())) {
                        entity.setUrl(IntroActivity.publicItems.get(i).getUrl());
                        entity.setTag(IntroActivity.publicItems.get(i).getType());
                        tagItems.add(entity);
                    }
                }
                FeedAdapter myAdapter = new FeedAdapter(context,tagItems);
                PageAdapter pAdapter = new PageAdapter(context, tagItems);
                FragmentFeed.feedRecyclerView.setLayoutManager(new GridLayoutManager(context,2));
                FragmentFeed.pageRecyclerView.setVisibility(View.VISIBLE);
                FragmentFeed.feedRecyclerView.setAdapter(myAdapter);
                FragmentFeed.pageRecyclerView.setAdapter(pAdapter);
            }
        });

    }

    @Override
    public int getItemCount() {
        return feedDataArrayList.size();
    }
}
