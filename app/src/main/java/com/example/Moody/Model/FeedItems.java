package com.example.Moody.Model;

import android.graphics.Bitmap;

public class FeedItems {
    int id;
    Bitmap image;
    String tag;
    int star;
    String url;
    String result;

    public int getId(){return id;}
    public void setId(int id){this.id=id;}
    public Bitmap getImage(){ return image;}
    public void setImage(Bitmap image){
        this.image=image;
    }
    public String getTag(){
        return tag;
    }
    public void setTag(String tag){
        this.tag=tag;
    }
    public int getStar(){return star;}
    public void setStar(int star){this.star=star;}
    public String getUrl(){return url;}
    public void setUrl(String url){this.url=url;}
    public String getResult(){return result;}
    public void setResult(String result){this.result=result;}
}