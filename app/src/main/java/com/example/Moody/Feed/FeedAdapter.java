package com.example.Moody.Feed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.Moody.Activity.IntroActivity;
import com.example.Moody.Activity.LoginActivity;
import com.example.Moody.Model.FeedItems;
import com.example.Moody.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

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
    Context context;
    private ArrayList<FeedItems> feedDataArrayList;
    public FeedAdapter(Context context, ArrayList<FeedItems> feedDataArrayList){
        this.context=context;
        this.feedDataArrayList = feedDataArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) holder;
        //이미지 출력
        if(feedDataArrayList.size()>position) {
            if (feedDataArrayList.get(position).getUrl() == null) {
                myViewHolder.image.setImageBitmap(feedDataArrayList.get(position).getImage());
            } else {
                Glide.with(context).load(feedDataArrayList.get(position).getUrl()).into(myViewHolder.image);
            }
            myViewHolder.tag.setText("#" + feedDataArrayList.get(position).getTag());
            if (feedDataArrayList.get(position).getStar() == 1) {
                myViewHolder.star.setBackgroundResource(R.drawable.yj_full_heart);
            } else if (!LoginActivity.dbHelper.searchItem(feedDataArrayList.get(position).getUrl())) {
                myViewHolder.star.setBackgroundResource(R.drawable.yj_full_heart);
            } else {
                myViewHolder.star.setBackgroundResource(R.drawable.yj_heart2);
            }
        }
        else{
            myViewHolder.itemView.setLayoutParams(new LinearLayout.LayoutParams(0,0));
        }
        //즐겨찾기 추가 및 해제
        myViewHolder.star.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(myViewHolder.star.isChecked()){
                    myViewHolder.star.setBackgroundResource(R.drawable.yj_full_heart);
                    if(feedDataArrayList.get(position).getUrl()==null) {
                        LoginActivity.dbHelper.setStar(1, feedDataArrayList.get(position).getId());
                    } else{
                        LoginActivity.dbHelper.pblInsert(feedDataArrayList.get(position).getUrl(), feedDataArrayList.get(position).getTag(), feedDataArrayList.get(position).getResult());
                    }
                }
                else{
                    myViewHolder.star.setBackgroundResource(R.drawable.yj_heart2);
                    if(feedDataArrayList.get(position).getUrl()==null) {
                        LoginActivity.dbHelper.setStar(0,feedDataArrayList.get(position).getId());
                    } else {
                        LoginActivity.dbHelper.pblDelete(feedDataArrayList.get(position).getUrl());
                    }
                }
            }
        });

        //태그버튼 클릭 시
        myViewHolder.tag.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String tagtext=myViewHolder.tag.getText().toString();
                tagtext=tagtext.substring(1);
                ArrayList<FeedItems> tagItems = LoginActivity.dbHelper.getTagItems(tagtext);

                for (int i = 0; i < IntroActivity.publicItems.size(); i++) {
                    FeedItems entity = new FeedItems();
                    if (tagtext.equals(IntroActivity.publicItems.get(i).getType())) {
                        entity.setUrl(IntroActivity.publicItems.get(i).getUrl());
                        entity.setTag(IntroActivity.publicItems.get(i).getType());
                        entity.setResult(IntroActivity.publicItems.get(i).getResult());
                        tagItems.add(entity);
                    }
                }
                FeedAdapter myAdapter = new FeedAdapter(context,tagItems);
                PageAdapter pAdapter = new PageAdapter(context, tagItems);
                FragmentFeed.feedRecyclerView.setLayoutManager(new GridLayoutManager(context,2));
                FragmentFeed.feedRecyclerView.setAdapter(myAdapter);
                FragmentFeed.pageRecyclerView.setAdapter(pAdapter);
            }
        });

        //이미지 클릭 시
        myViewHolder.image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(context, DetailPopupActivity.class);
                if(feedDataArrayList.get(position).getUrl()==null) {
                    intent.putExtra("result", feedDataArrayList.get(position).getResult());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    feedDataArrayList.get(position).getImage().compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    intent.putExtra("image", byteArray);
                }
                else{
                    intent.putExtra("res", feedDataArrayList.get(position).getResult());
                    intent.putExtra("url",feedDataArrayList.get(position).getUrl());
                }
                context.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return 8;
    }
}

