package com.example.Moody.Chat;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.Moody.Activity.IntroActivity;
import com.example.Moody.Activity.LoginActivity;
import com.example.Moody.Activity.MainActivity;
import com.example.Moody.Model.ChatModel;

import com.example.Moody.Model.ChatRoomModel;
import com.example.Moody.Model.FeedItems;
import com.example.Moody.Model.UserModel;
import com.example.Moody.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends Activity {
    private static final String TAG = "ChatActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    private RecyclerView chatRecyclerView;
    private RecyclerView tagRecyclerView;
    private PersonalAdapter pAdapter;
    private GroupAdapter gAdapter;
    public static boolean autoSendCheck = false;

    private ArrayList<ChatModel> chatModels = new ArrayList<ChatModel>();
    private ArrayList<ChatRoomModel> chatRoomModels = new ArrayList<ChatRoomModel>();

    private String receiver; //????????? id
    private String uid; //????????? id
    public static String uName; //????????? ??????
    public static String roomid; //????????? id
    private String chatRoomName; //????????? ????????? ??????
    private String groupCheck; //???????????? ???????????? ??????
    private boolean autoCheck = false; //?????? ?????? ???????????? ??????

    private String imageUrl; //?????? ?????? url
    private int GET_GALLERY_IMAGE=200;

    private String auto_text;
    public static String emotion = "";
    public static String sText;
    public static EditText sendText;

    ValueEventListener valueEventListener;

    //?????? ??????
    SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    String date = dateFormat1.format(Calendar.getInstance().getTime());
    //?????? ?????? ??????
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
    String datetime = dateFormat.format(Calendar.getInstance().getTime());


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        setContentView(R.layout.activity_chattingroom);

        mAuth = FirebaseAuth.getInstance();//?????? ????????? ??????
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();//?????? ????????? id
        UserInfo();//?????? ????????? ?????? ???????????? ??????

        roomid = getIntent().getStringExtra("roomid");//????????? id
        chatRoomName = getIntent().getStringExtra("name"); //????????? ?????? ?????? ?????????

        RequestManager glide = Glide.with(this); //glide ????????????

        final TextView recUser = (TextView) findViewById(R.id.chatRoom_users);//????????? ??????

        //?????? ??????
        ImageView backBtn = (ImageView) findViewById(R.id.chatRoom_backBtn);
        final Button sendBtn = (Button) findViewById(R.id.chatRoom_sendBtn);
        Button galleryBtn = (Button) findViewById(R.id.chatRoom_galleryBtn);
        final Button autoBtn = (Button) findViewById(R.id.chatRoom_autoBtn);
        sendText = (EditText) findViewById(R.id.chatRoom_text); //????????? ?????????

        recUser.setText(chatRoomName);//????????? ?????? ?????? ??????

        //?????? ??????????????????
        chatRecyclerView = (RecyclerView) findViewById(R.id.chatRoom_recyclerView);
        chatRecyclerView.setHasFixedSize(true); //?????????????????? ?????? ??????
        //????????? ?????? ??????????????????
        tagRecyclerView = (RecyclerView)findViewById(R.id.tag_recyclerview);
        tagRecyclerView.setHasFixedSize(true);
        tagRecyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));

        //?????? ????????? ?????? ????????? ??????
        groupCheck = getIntent().getStringExtra("check");
        if(groupCheck !=null){
            ChatDisplay(groupCheck);
            if(groupCheck.equals("1")){//1:1 ??????
                receiver = getIntent().getStringExtra("receiver"); //????????? id
                pAdapter = new PersonalAdapter(receiver,roomid,chatModels,chatRoomModels, glide); //?????? ????????? ?????????
                chatRecyclerView.setAdapter(pAdapter);

            }else{//?????? ??????
                gAdapter = new GroupAdapter(roomid,chatModels,chatRoomModels, glide); //?????? ????????? ?????????
                chatRecyclerView.setAdapter(gAdapter);

            }
        }

        //????????????
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference("Message").child(roomid).removeEventListener(valueEventListener);
                Intent intent = new Intent(ChatActivity.this,MainActivity.class);
                intent.putExtra("fragment","chat");
                startActivity(intent);
                finish();
            }
        });

        //????????? ????????? ??????
        sendText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((keyCode == event.KEYCODE_ENTER)){
                    sText = sendText.getText().toString();
                    sText = sText.replace( System.getProperty( "line.separator" ), "" );
                    SendMsg(sText,"0");
                    sendText.setText(null);

                    if(groupCheck.equals("1")){
                        chatRecyclerView.scrollToPosition(pAdapter.getItemCount() - 1);
                    }else{
                        chatRecyclerView.scrollToPosition(gAdapter.getItemCount() - 1);
                    }
                    return true;
                }
                return false;
            }
        });

        //send?????? ?????????
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sText = sendText.getText().toString();
                SendMsg(sText,"0");
                sendText.setText(null);

                if(groupCheck.equals("1")){
                    chatRecyclerView.scrollToPosition(pAdapter.getItemCount() - 1);
                }else{
                    chatRecyclerView.scrollToPosition(gAdapter.getItemCount() - 1);
                }
            }
        });

        //????????? ?????? ?????????
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);

            }
        });


        //??????????????? ?????? ?????????
        autoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout tagLayout = (RelativeLayout)findViewById(R.id.tag_layout);

                sText = sendText.getText().toString();
                String emotion = AutoImage(sendText,sText);

                if(sText.equals("")) {
                    tagLayout.setVisibility(View.GONE);
                }
                else{
                    tagLayout.setVisibility(View.VISIBLE);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(sendText.getWindowToken(), 0);
                    ArrayList<FeedItems> tagItems = new ArrayList<>();
                    for (int i = 0; i < IntroActivity.publicItems.size(); i++) {
                        FeedItems entity = new FeedItems();
                        if (emotion.equals(IntroActivity.publicItems.get(i).getType())) {
                            entity.setUrl(IntroActivity.publicItems.get(i).getUrl());
                            entity.setTag(IntroActivity.publicItems.get(i).getType());
                            tagItems.add(entity);
                        }
                    }
                    tagItems.addAll(LoginActivity.dbHelper.getTagItems(emotion));
                    TabAdapter tAdapter = new TabAdapter(ChatActivity.this, tagItems);
                    tagRecyclerView.setAdapter(tAdapter);

                    if (groupCheck.equals("1")) {
                        chatRecyclerView.scrollToPosition(pAdapter.getItemCount() - 1);
                    } else {
                        chatRecyclerView.scrollToPosition(gAdapter.getItemCount() - 1);
                    }

                    sendText.setText("");


                }

            }


        });

        //?????? ?????? ??????
        sendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            int hour=0;
            int min=0;
            String resText = null;
            Thread th=null;
            LinearLayout layout = (LinearLayout)findViewById(R.id.late_msg_layout);
            TextView lateMsg = (TextView)findViewById(R.id.late_msg_send);

            //?????? ?????????
            @Override
            public boolean onLongClick(View v) {
                //???????????? ???
                TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Toast toast = Toast.makeText(ChatActivity.this,hourOfDay + "??? " + minute+"?????? ???????????? ?????????????????????.",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER, 0);
                        toast.show();

                        hour =hourOfDay;
                        min =minute;
                        resText = sendText.getText().toString();
                        sendText.setText(null);

                        if(th==null) {
                            newThread();
                            th.start();
                        }
                        else{
                            th.interrupt();
                            th=null;
                            newThread();
                            th.start();
                        }

                    }
                };
                TimePickerDialog oDialog = new TimePickerDialog(ChatActivity.this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog,
                        mTimeSetListener, 0, 0, false);
                oDialog.show();
                return true;
            }

            //?????????
            private void newThread() {
                th = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        int settime = hour * 100 + min;

                        //?????? ?????? ???????????? ?????????
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layout.setVisibility(View.VISIBLE);
                                lateMsg.setText(resText);
                            }
                        });

                        while (true) {
                            long mNow = System.currentTimeMillis();
                            Date mReDate = new Date(mNow);
                            SimpleDateFormat mFormat = new SimpleDateFormat("HHmm");
                            String formatDate = mFormat.format(mReDate);
                            if(Integer.parseInt(formatDate)==settime){
                                if(!resText.equals("")){
                                    SendMsg(resText,"0");
                                    sendText.setText(null);
                                    break;
                                }
                            }
                        }
                        //???????????? ?????????
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layout.setVisibility(View.GONE);
                            }
                        });
                    }


                });
            }
        });

    }

    //?????? ?????????
    public void SendMsg(String sendMsg,String msgType){
        if (!(sendMsg.equals(""))) {
            DatabaseReference ref = database.getReference("Message").child(roomid);

            Map<String,Object> read = new HashMap<>();
            read.put(currentUser.getUid(),true); //????????? ??????????????? ?????? ??????

            HashMap<String, Object> member = new HashMap<String, Object>();
            member.put("uID", currentUser.getUid()); //???????????? id
            member.put("userName", uName); //?????? ?????? ??????
            member.put("msg", sendMsg); //?????? ?????????
            member.put("timestamp", ServerValue.TIMESTAMP); //?????? ??????
            member.put("msgType", msgType); //????????? ??????
            member.put("readUsers",read); //?????? ??????
            ref.push().setValue(member);

            auto_text = sendMsg;

            HashMap<String, Object> chatroom = new HashMap<String, Object>();
            if(msgType.equals("0")){
                chatroom.put("lastMsg",sendMsg);//????????? ?????????
            }else if(msgType.equals("1")){
                chatroom.put("lastMsg","??????");
            }
            chatroom.put("lastTime",ServerValue.TIMESTAMP); //????????? ??????
            database.getReference("ChatRoom").child(roomid).updateChildren(chatroom);
        }
    }

    //???????????? ????????????
    public void ChatDisplay(final String check){

        database.getReference("ChatRoom").child(roomid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatRoomModel chatRoomModel = dataSnapshot.getValue(ChatRoomModel.class);
                chatRoomModels.add(chatRoomModel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatModels.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ChatModel chat = dataSnapshot.getValue(ChatModel.class);
                    String commentKey = dataSnapshot.getKey();

                    //????????????
                    Map<String,Object> read = new HashMap<>();
                    read.put(currentUser.getUid(),true);
                    database.getReference("Message").child(roomid).child(commentKey).child("readUsers").updateChildren(read);

                    chatModels.add(chat);

                    if (check.equals("1")) {
                        pAdapter.notifyDataSetChanged();
                    } else {
                        gAdapter.notifyDataSetChanged();
                    }
                    chatRecyclerView.scrollToPosition(chatModels.size()-1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        database.getReference("Message").child(roomid).addValueEventListener(valueEventListener);
    }


    //????????? ?????? ?????????
    public void MsgTimer(){
        Thread timerThraed = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    //????????? ??????
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData(); //????????? ?????? ??????
            System.out.println("?????????:"+selectedImageUri);
            String imagePath = "Chat/"+roomid+"/"+datetime; //???????????? ?????? ??? ??????
            UploadFiles(selectedImageUri,imagePath); //?????? ?????????

        }

    }

    //????????????????????? ?????? ?????????
    public void UploadFiles(Uri uri, final String path) {
        final StorageReference riversRef = storageRef.child(path);

        UploadTask uploadTask = riversRef.putFile(uri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    imageUrl = downloadUri.toString();
                    //DB??? ??????
                    SendMsg(imageUrl,"1");
                }
            }
        });

    }

    //????????? ?????? ????????????
    public void UserInfo(){
        database.getReference("userInfo").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel um = dataSnapshot.getValue(UserModel.class);
                uName = um.getName();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }



    //===================================================================================================================

    //?????? ?????? ?????? ?????????
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        database.getReference("Message").child(roomid).removeEventListener(valueEventListener);
        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        intent.putExtra("fragment", "chat");
        startActivity(intent);
        finish();
    }

    //????????? ?????????
    public boolean onTouchEvent(MotionEvent event) {
        //EditText sendText = (EditText)findViewById(R.id.chatRoom_text);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(sendText.getWindowToken(), 0);
        return true;
    }

    //??????????????? ??????
    public String AutoImage(TextView sendText,String sText){

        if(IntroActivity.word_set==null){
            IntroActivity.word_set=Word();
        }

        String input_text;
        System.out.println(auto_text);

        input_text = auto_text;

        if (!(sendText.getText().toString().equals(""))) {
            input_text = sendText.getText().toString();
        }
        System.out.println("input_text: "+ input_text);
        if (input_text != null) {

            int[] num = new int[50];
            int cnt = 0;

            String[] word = input_text.split(" ");

            for(int i=0; i<word.length; i++){
                System.out.println(i+" "+ word[i]);
            }

            //word_set??? ???????????? ?????? ????????? ????????? ?????? ??????
            for (int i = 0; i < word.length; i++) {
                if (IntroActivity.word_set.containsValue(word[i])) {
                    Integer key = IntroActivity.getKey(IntroActivity.word_set, word[i]);
                    System.out.println(key);
                    num[cnt] = key.intValue();
                    cnt++;
                    System.out.println(word[i] + " " + key);
                } else {
                    int temp = word[i].length();
                    int temp2 = temp - 1;
                    int check = 0;

                    for (int j = 0; j < word[i].length(); j++) {
                        String str = word[i].substring(0, temp);
                        System.out.println("str " + str);
                        for (Map.Entry<Integer, String> entry : IntroActivity.word_set.entrySet()) {
                            if (entry.getValue().contains(str)) {
                                String tstr = entry.getValue();
                                String[] arr_word = tstr.split("");
                                String[] arr_str = str.split("");
                                if (arr_word[0].equals(arr_str[0])) {
                                    System.out.println("1> " + entry.getValue());
                                    Integer key = entry.getKey();
                                    System.out.println("2> " + key);
                                    num[cnt] = key.intValue();
                                    check = cnt;
                                    cnt++;
                                    break;
                                }
                            }
                        }
                        temp--;
                        if (check != 0) break;
                    }
                    if (check == 0) {
                        for (int j = 0; j < word[i].length(); j++) {
                            if(word[i].length()!=1) {
                                String str2 = word[i].substring(1, temp2);
                                for (Map.Entry<Integer, String> entry : IntroActivity.word_set.entrySet()) {
                                    if (entry.getValue().contains(str2)) {
                                        System.out.println("1> " + entry.getValue());
                                        Integer key = entry.getKey();
                                        System.out.println("2> " + key);
                                        num[cnt] = key.intValue();
                                        check = cnt;
                                        cnt++;
                                        break;
                                    }
                                }

                                if (check != 0) break;
                            }
                        }
                        temp2--;
                        if (check != 0) continue;
                    }
                }
            }

            System.out.println(cnt);

            float[][] input = new float[1][8];

            int[] index = new int[cnt];
            for (int i = 0; i < cnt; i++) {
                index[i] = num[i];
                System.out.print("index" + index[i] + " ");
            }

            if (cnt > 7) {
                Arrays.sort(index);

                for (int j = 0; j < 8; j++) {
                    input[0][j] = (float) index[j];
                    System.out.print(index[j] + " ");
                }
            } else {
                int[] arr = new int[8];

                int j, k = 0;
                for (j = 0; j < 8; j++) {
                    if (j < (8 - cnt))
                        arr[j] = 0;
                    else arr[j] = index[k++];
                    System.out.print(arr[j] + " ");
                }

                System.out.println();
                System.out.println("here");

                //??????????????? ????????? ?????? ????????? ?????? float ??????
                for (int i = 0; i < 8; i++) {
                    System.out.print(arr[i] + " ");
                    input[0][i] = (float) arr[i];
                }
            }

            float[][] output = new float[1][6];

            Interpreter tflite = getTfliteInterpreter("new_lstm_model.tflite");
            tflite.run(input, output);

            int maxIdx = 0;
            float maxProb = output[0][0];
            for (int i = 1; i < 6; i++) {
                if (output[0][i] > maxProb) {
                    maxProb = output[0][i];
                    maxIdx = i;
                    System.out.println("??????: " + maxIdx + maxProb);
                }
            }

            System.out.println(maxIdx);
            emotion = null;
            if (maxIdx == 0)
                emotion = "Happy";
            else if (maxIdx == 1)
                emotion = "Sad";
            else if (maxIdx == 2)
                emotion = "Angry";
            else if (maxIdx == 3)
                emotion = "Surprise";
            else if (maxIdx == 4)
                emotion = "Fear";
            else if (maxIdx == 5)
                emotion = "Disgust";

            System.out.println("??????: " + emotion);
            sendText.setText(null);

            /*

            Intent intent = new Intent(ChatActivity.this, AutoChatActivity.class);
            intent.putExtra("name", chatRoomName);
            intent.putExtra("receiver", receiver);
            intent.putExtra("roomid", roomid);
            intent.putExtra("check", groupCheck);

            startActivity(intent);

             */
        }

        return emotion;

    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(ChatActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // ????????? ???????????? ??????
    // MappedByteBuffer ????????? ????????? Interpreter ????????? ???????????? ?????? ????????? ??? ??? ??????.
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private HashMap<Integer, String> Word() {

        HashMap<Integer, String> word_set = new HashMap<Integer, String>();

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.wordset);
            InputStreamReader reader = new InputStreamReader(inputStream);
            // ?????? ?????? ??????
            BufferedReader bufReader = new BufferedReader(reader);
            String line = "";

            int i=0;
            while ((line = bufReader.readLine()) != null) {
                System.out.println(line);

                String[] word = line.split(":");
                word_set.put(Integer.parseInt(word[1]), word[0]);
                i++;
            }
            bufReader.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return word_set;
    }

}