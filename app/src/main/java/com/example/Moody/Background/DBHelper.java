package com.example.Moody.Background;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.BitmapFactory;

import com.example.Moody.Model.ChatRoomModel;
import com.example.Moody.Model.FeedItems;
import com.example.Moody.Model.UserModel;

import java.util.ArrayList;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
    SQLiteDatabase db;

    // DBHelper 생성자
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        db.execSQL("CREATE TABLE image (_id INTEGER PRIMARY KEY AUTOINCREMENT, img BLOB, tag TEXT, star INTEGER, results TEXT);");
        db.execSQL("CREATE TABLE mark (_id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, tag TEXT, results TEXT);");
        db.execSQL("CREATE TABLE friend (fid TEXT PRIMARY KEY NOT NULL, profile TEXT, name TEXT, email TEXT, liked INTEGER, range TEXT)");
        db.execSQL("CREATE TABLE myInfo (uid TEXT PRIMARY KEY NOT NULL, profile TEXT, name TEXT, email TEXT, oState INTEGER, lState INTEGER)");
        db.execSQL("CREATE TABLE chatRoom (roomID TEXT PRIMARY KEY NOT NULL, lastTime INTEGER, lastMsg TEXT, roomName TEXT, isCheck INTEGER)");
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS image");
        db.execSQL("DROP TABLE IF EXISTS mark");
        db.execSQL("DROP TABLE IF EXISTS friend");
        db.execSQL("DROP TABLE IF EXISTS myInfo");
        db.execSQL("DROP TABLE IF EXISTS chatRoom");
        onCreate(db);
    }

    //친구 정보 추가
    public void insertFriend(UserModel info, int liked){
        db = getWritableDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO fiend(fid, profile, name, email, liked, range) VALUES(?,?,?,?,?,?)");
        statement.bindString(1,info.getUID());
        statement.bindString(2,info.getProfile());
        statement.bindString(3,info.getName());
        statement.bindString(4,info.getEmail());
        statement.bindLong(5,liked);
        statement.bindString(6,info.getRange());

        statement.execute();
        db.close();
    }

    //내 정보 추가
    public void insertMyInfo(UserModel myInfo){
        db = getWritableDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO myInfo(uid, profile, name, email, oState, lState ) VALUES(?,?,?,?,?,?)");
        statement.bindString(1,myInfo.getUID());
        statement.bindString(2,myInfo.getProfile());
        statement.bindString(3,myInfo.getName());
        statement.bindString(4,myInfo.getEmail());
        if(myInfo.getOstate() || myInfo.getLstate()){
            statement.bindLong(5,1);
            statement.bindLong(6,1);
        }else if(myInfo.getOstate() || !myInfo.getLstate()){
            statement.bindLong(5,1);
            statement.bindLong(6,0);
        }else if(!myInfo.getOstate() || myInfo.getLstate()){
            statement.bindLong(5,0);
            statement.bindLong(6,1);
        }else{
            statement.bindLong(5,0);
            statement.bindLong(6,0);
        }


        statement.execute();
        db.close();
    }

    //채팅방 정보 추가
    public void insertChatRoom(ChatRoomModel chatRoom,int isCheck){
        db = getWritableDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO chatRoom (roomID, lastTime, lastMsg, roomName, isCheck) VALUES(?,?,?,?,?)");
        statement.bindString(1,chatRoom.getRoomID());
        statement.bindLong(2,(long)chatRoom.getLastTime());
        statement.bindString(3,chatRoom.getLastMsg());
        statement.bindString(4,chatRoom.getRoomName());
        statement.bindLong(5,isCheck);

        statement.execute();
        db.close();
    }

    //친구 정보 불러오기
    public ArrayList<UserModel> readFriend(){
        ArrayList<UserModel> list = new ArrayList<UserModel>();

        db = getReadableDatabase();

        String sql = "SELECT * FROM friend";

        Cursor cursor = db.rawQuery(sql, null);
        while(cursor.moveToNext()){
            UserModel entity = new UserModel();
            entity.setUID(cursor.getString(1));
            entity.setProfile(cursor.getString(2));
            entity.setName(cursor.getString(3));
            entity.setEmail(cursor.getString(4));
            //entity.setRange(cursor.getString(6));
            list.add(entity);
        }

        db.close();
        return list;
    }

    //내 정보 불러오기
    public void readMyInfo(){

    }

    //채팅방 정보 불러오기
    public void readChatRoom(){

    }

    //이미지 추가
    public void insert(byte[]image, String tag, String results) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement p = db.compileStatement("INSERT INTO image(img, tag, star, results) VALUES(?,?,0,?);");

        p.bindBlob(1,image);
        p.bindString(2,tag);
        p.bindString(3,results);
        p.execute();
        db.close();
    }
    //이미지 조회(1이면 역순)
    public ArrayList<FeedItems> getItems(int mode){
        SQLiteDatabase db = getReadableDatabase();
        String sql=null;
        if(mode==1)
            sql="SELECT * FROM image ORDER BY _id DESC;";
        else if(mode==2)
            sql="SELECT * FROM image;";
        Cursor cursor = db.rawQuery(sql, null);
        ArrayList<FeedItems> list=new ArrayList<FeedItems>();
        byte []image=null;
        while(cursor.moveToNext()){
            FeedItems entity = new FeedItems();
            entity.setId(cursor.getInt(0));
            image=cursor.getBlob(1);
            entity.setImage(BitmapFactory.decodeByteArray(image,0,image.length));
            entity.setTag(cursor.getString(2));
            entity.setStar(cursor.getInt(3));
            entity.setResult(cursor.getString(4));
            list.add(entity);
        }
        db.close();
        return list;
    }
    //즐겨찾기 이미지 조회(1이면 역순)
    public ArrayList<FeedItems> getStarItems(int mode){
        SQLiteDatabase db = getReadableDatabase();
        String sql=null;
        if(mode==1){
            sql="SELECT * FROM image WHERE star=1 ORDER BY _id DESC;";
        }
        else{
            sql="SELECT * FROM image WHERE star=1;";
        }
        Cursor cursor = db.rawQuery(sql, null);
        ArrayList<FeedItems> list=new ArrayList<FeedItems>();
        byte []image=null;
        while(cursor.moveToNext()){
            FeedItems entity = new FeedItems();
            entity.setId(cursor.getInt(0));
            image=cursor.getBlob(1);
            entity.setImage(BitmapFactory.decodeByteArray(image,0,image.length));
            entity.setTag(cursor.getString(2));
            entity.setStar(cursor.getInt(3));
            entity.setResult(cursor.getString(4));
            list.add(entity);
        }
        db.close();
        return list;
    }
    //태그내용에 대한 이미지출력
    public ArrayList<FeedItems> getTagItems(String tag){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM image WHERE tag='"+tag+"';", null);
        ArrayList<FeedItems> list=new ArrayList<FeedItems>();
        byte []image=null;
        while(cursor.moveToNext()){
            FeedItems entity = new FeedItems();
            entity.setId(cursor.getInt(0));
            image=cursor.getBlob(1);
            entity.setImage(BitmapFactory.decodeByteArray(image,0,image.length));
            entity.setTag(cursor.getString(2));
            entity.setStar(cursor.getInt(3));
            entity.setResult(cursor.getString(4));
            list.add(entity);
        }
        db.close();
        return list;
    }
    //즐겨찾기 설정
    public void setStar(int star, int position){
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement p = db.compileStatement("UPDATE image SET star="+star+" WHERE _id="+position+";");

        p.execute();
        db.close();
    }
    //공용이미지 즐겨찾기
    public void pblInsert(String url, String tag, String result) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement p = db.compileStatement("INSERT INTO mark(url, tag, results) VALUES(?,?,?);");

        p.bindString(1,url);
        p.bindString(2,tag);
        p.bindString(3,result);
        p.execute();
        db.close();
    }
    //공용이미지 즐겨찾기 해제
    public void pblDelete(String url){
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement p = db.compileStatement("DELETE FROM mark WHERE url=?;");
        p.bindString(1,url);
        p.execute();
        db.close();
    }
    //즐겨찾기 이미지 조회(1이면 역순)
    public ArrayList<FeedItems> getMarkItems(int mode){
        SQLiteDatabase db = getReadableDatabase();
        String sql=null;
        if(mode==1){
            sql="SELECT * FROM mark ORDER BY _id DESC;";
        }
        else{
            sql="SELECT * FROM mark;";
        }
        Cursor cursor = db.rawQuery(sql, null);
        ArrayList<FeedItems> list=new ArrayList<FeedItems>();
        byte []image=null;
        String url=null;
        while(cursor.moveToNext()){
            FeedItems entity = new FeedItems();
            entity.setUrl(cursor.getString(1));
            entity.setTag(cursor.getString(2));
            entity.setResult(cursor.getString(3));
            entity.setStar(1);
            list.add(entity);
        }
        db.close();
        return list;
    }
    //공용 즐겨찾기 표시
    public boolean searchItem(String url){
        SQLiteDatabase db = getReadableDatabase();
        String sql="SELECT url FROM mark WHERE url='"+url+"';";
        Cursor cursor = db.rawQuery(sql, null);
        int i=0;
        while(cursor.moveToNext()){
            i++;
        }
        db.close();
        if(i==0)
            return true;
        else
            return false;
    }
}
