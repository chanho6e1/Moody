package com.example.Moody.Feed;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Moody.Model.FeedItems;
import com.example.Moody.R;

import java.util.ArrayList;

public class PageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<FeedItems> feedDataArrayList;
    Context context;
    private int clickedButton=0;
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Button page;
        MyViewHolder(View view){
            super(view);
            page=view.findViewById(R.id.page_btn);
        }
    }
    public PageAdapter(Context context, ArrayList<FeedItems> feedDataArrayList){
        this.context=context;
        this.feedDataArrayList = feedDataArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page_number, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) holder;
        if((position+1)%8!=1){
            myViewHolder.page.setVisibility(View.GONE);
        }
        else {
            int num=(position+1)/8+1;
            myViewHolder.page.setVisibility(View.VISIBLE);
            myViewHolder.page.setText(Integer.toString(num));
        }
        if(clickedButton==position){
            myViewHolder.page.setBackgroundResource(R.drawable.yj_btn1_border);
            myViewHolder.page.setTextColor(Color.parseColor("#FFFFFF"));
        }else {
            myViewHolder.page.setBackgroundColor(Color.parseColor("#00FF0000"));
            myViewHolder.page.setTextColor(Color.parseColor("#707070"));
        }
        //페이지 번호 클릭
        myViewHolder.page.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                clickedButton = myViewHolder.getAdapterPosition();
                notifyDataSetChanged();
                int pagenum=Integer.parseInt(myViewHolder.page.getText().toString());
                ArrayList<FeedItems> pageItems=new ArrayList<FeedItems>();
                for(int i=(pagenum-1)*8;i<pagenum*8;i++) {
                    FeedItems entity = new FeedItems();
                    if(i<feedDataArrayList.size()) {
                        if (feedDataArrayList.get(i).getUrl() == null) {
                            entity.setId(feedDataArrayList.get(i).getId());
                            entity.setImage(feedDataArrayList.get(i).getImage());
                            entity.setTag(feedDataArrayList.get(i).getTag());
                            entity.setStar(feedDataArrayList.get(i).getStar());
                            entity.setResult(feedDataArrayList.get(i).getResult());
                            pageItems.add(entity);
                        } else {
                            entity.setUrl(feedDataArrayList.get(i).getUrl());
                            entity.setTag(feedDataArrayList.get(i).getTag());
                            entity.setResult(feedDataArrayList.get(i).getResult());
                            pageItems.add(entity);
                        }
                    }
                }
                FeedAdapter myAdapter = new FeedAdapter(context,pageItems);
                FragmentFeed.feedRecyclerView.setLayoutManager(new GridLayoutManager(context,2));
                FragmentFeed.feedRecyclerView.setAdapter(myAdapter);

            }
        });

    }

    @Override
    public int getItemCount() {
        return feedDataArrayList.size();
    }
}
