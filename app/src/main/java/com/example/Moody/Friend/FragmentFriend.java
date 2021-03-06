package com.example.Moody.Friend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.Moody.Chat.ChatActivity;
import com.example.Moody.Model.ChatRoomModel;
import com.example.Moody.Model.UserModel;
import com.example.Moody.R;
import com.example.Moody.Setting.ProfilePageActivity;
import com.example.Moody.Setting.QR_code;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class FragmentFriend extends Fragment {
    private static final String TAG = "FragmentFriend";
    public static FragmentFriend newInstance() {
        return new FragmentFriend();
    }

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private String email;
    public String myName;
    static public int state;

    private RecyclerView fRecyclerView;
    private RecyclerView oRecyclerView;
    private RecyclerView lRecyclerView;

    private FriendAdapter fAdapter;
    private FriendAdapter lAdapter;
    private FriendAdapter oAdapter;

    private ArrayList<UserModel> uList = new ArrayList<UserModel>();
    private ArrayList<UserModel> oList = new ArrayList<UserModel>();
    private ArrayList<UserModel> lList = new ArrayList<UserModel>();

    private ArrayList<String> fid = new ArrayList<String>();
    private ArrayList<String> lid = new ArrayList<String>();

    private Boolean ostate;
    private Boolean lstate;

    private DatabaseReference myOstateRef;
    private DatabaseReference myLstateRef;

    Handler myHandler;
    Handler fHandler;

    public RequestManager mGlideRequestManager;

    //?????? ?????? ??????
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userUid = currentUser.getUid();

        myOstateRef = FirebaseDatabase.getInstance().getReference("/userInfo/"+userUid+"/ostate");
        myLstateRef = FirebaseDatabase.getInstance().getReference("/userInfo/"+userUid+"/lstate");

        mGlideRequestManager = Glide.with(this);

        FriendListDisplay();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_friend,container,false);

        Button profileBtn = (Button)view.findViewById(R.id.profile_edit_btn); //????????? ?????? ??????
        Button qrBtn = (Button)view.findViewById(R.id.my_qr_btn); //QR ?????? ?????? ??????

        final TextView myName = (TextView) view.findViewById(R.id.my_name);//????????? ??? ??????
        ImageView myImage = (ImageView) view.findViewById(R.id.my_image); //????????? ??? ?????????

        myHandler = new Handler(Looper.myLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:

                    case 1:
                    case 2:
                }
            }
        };

        final EditText friendSearch = (EditText)view.findViewById(R.id.chat_room_search); //?????? ?????? ???

        Button onlineBtn = (Button)view.findViewById(R.id.online_toggle_btn);
        Button likedBtn = (Button)view.findViewById(R.id.liked_toggle_btn);

        MyInfo(myName,myImage);

        oRecyclerView = (RecyclerView)view.findViewById(R.id.online_list_recyclerView);
        oRecyclerView.setHasFixedSize(true);
        oRecyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        lRecyclerView = (RecyclerView)view.findViewById(R.id.liked_list_recyclerView);
        lRecyclerView.setHasFixedSize(true);
        lRecyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        fRecyclerView = (RecyclerView)view.findViewById(R.id.chat_list_recyclerView);
        fRecyclerView.setHasFixedSize(true);
        fRecyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        oAdapter = new FriendAdapter(getContext(), oList);
        lAdapter = new FriendAdapter(getContext(), lList);
        fAdapter = new FriendAdapter(getContext(), uList);

        oRecyclerView.setAdapter(oAdapter);
        lRecyclerView.setAdapter(lAdapter);
        fRecyclerView.setAdapter(fAdapter);

        oRecyclerView.setVisibility(View.GONE);
        lRecyclerView.setVisibility(View.GONE);

        oRecyclerView.setNestedScrollingEnabled(false);
        lRecyclerView.setNestedScrollingEnabled(false);
        fRecyclerView.setNestedScrollingEnabled(false);

        final LinearLayout online_list = (LinearLayout)view.findViewById(R.id.online_list);
        final LinearLayout liked_list = (LinearLayout)view.findViewById(R.id.liked_list);

        //????????? ??????
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = 0;
                startActivity(new Intent(getActivity(), ProfilePageActivity.class));
            }
        });

        //QR??????
        qrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(email)){
                    return;
                }
                Intent intent =  new Intent(getActivity(), QR_code.class);
                intent.putExtra("email",email);
                startActivity(intent);
            }
        });

        //??????
        friendSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if(text.length()==0){
                    online_list.setVisibility(View.VISIBLE);
                    liked_list.setVisibility(View.VISIBLE);
                }
                else{
                    online_list.setVisibility(View.GONE);
                    liked_list.setVisibility(View.GONE);
                }
                fAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        //?????? ????????? ??????
        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendListDisplay();
                if (oList.size()==0){
                    return;
                }
                if(!ostate) { //???????????? ??????
                    oRecyclerView.setVisibility(View.VISIBLE);
                    myOstateRef.setValue(true);
                    ostate=true;
                }
                else {
                    oRecyclerView.setVisibility(View.GONE);
                    myOstateRef.setValue(false);
                    ostate=false;
                }
            }
        });

        //???????????? ??????
        likedBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FriendListDisplay();
                if (lList.size()==0){
                    return;
                }
                if(!lstate) { //???????????? ??????
                    lRecyclerView.setVisibility(View.VISIBLE);
                    myLstateRef.setValue(true);
                    lstate=true;
                }
                else{
                    lRecyclerView.setVisibility(View.GONE);
                    myLstateRef.setValue(false);
                    lstate=false;
                }
            }
        });

        final FloatingActionButton addBtn = (FloatingActionButton)view.findViewById(R.id.friend_add_btn);
        final LinearLayout top1 = (LinearLayout)view.findViewById(R.id.top1);
        final LinearLayout layout1 = (LinearLayout)view.findViewById(R.id.layout1);
        final LinearLayout layout2 = (LinearLayout)view.findViewById(R.id.layout2);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), AddFriendActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    /**
     * ????????? ????????????
     * @param name
     * @param image
     */
    public void MyInfo(final TextView name, final ImageView image){
        /*
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                database.getReference("userInfo").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userModel = snapshot.getValue(UserModel.class);
                        Message message = new Message();
                        message.what = 0; //?????? ?????????
                        myHandler.sendMessage(message);
                        if(userModel == null)
                            message.what = 2; //???????????? ??????????????? ?????? ??????
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error){
                        Message message = new Message();
                        message.what = 1; //????????? ????????? ??????
                        myHandler.sendMessage(message);
                    }
                });
            }
        });

         */
        database.getReference("userInfo").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel um = dataSnapshot.getValue(UserModel.class);

                name.setText(um.getName());
                myName = um.getName();
                email = um.getEmail();
                ostate = um.getOstate();
                lstate = um.getLstate();

                if(ostate) oRecyclerView.setVisibility(View.VISIBLE);
                else oRecyclerView.setVisibility(View.GONE);

                if(lstate) lRecyclerView.setVisibility(View.VISIBLE);
                else lRecyclerView.setVisibility(View.GONE);

                if(um.getRange().equals("friend"))
                    image.setBackgroundResource(R.drawable.yj_profile_border);
                if (!um.getProfile().equals(""))
                    mGlideRequestManager.load(getContext()).load(um.getProfile()).apply(new RequestOptions().circleCrop()).error(R.drawable.user).into(image);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }


    /**
     * ?????? ?????? ????????????
     */
    public void FriendListDisplay(){
        //?????????????????? ?????? id ????????????
        database.getReference("friend").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fid.clear();
                lid.clear();
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    fid.add(dataSnapshot1.getKey());

                    if(dataSnapshot1.getValue().equals(true))
                        lid.add(dataSnapshot1.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        //???????????? ????????????
        database.getReference("userInfo").orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uList.clear();
                oList.clear();
                lList.clear();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    UserModel users = dataSnapshot1.getValue(UserModel.class);
                    for(int i=0;i<lid.size();i++){
                        if(users.getUID().equals(lid.get(i))){
                            lList.add(users);
                        }
                    }
                    for(int i=0;i<fid.size();i++){
                        if(users.getUID().equals(fid.get(i))) {
                            uList.add(users);
                            if (users.getConnection() != null)
                                if (users.getConnection() == true)
                                    oList.add(users);
                        }
                    }
                }

                lAdapter.notifyDataSetChanged();
                oAdapter.notifyDataSetChanged();
                fAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }


    //===============================================================================================================================

    public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
        private static final String TAG = "FriendAdapter";

        private Context context;
        private ArrayList<UserModel> uData; //????????? ?????? ?????????(?????? ?????????)
        private ArrayList<UserModel> filterList; //????????? ??? ?????????(???????????? ????????? ??????)

        private String uid;
        private String roomid = null;
        private Boolean check=false;

        //??????????????? ????????? ????????? ????????? ????????????
        public FriendAdapter(Context context, ArrayList<UserModel> list){
            super();
            this.context = context;
            this.uData = list;
            this.filterList = list;

        }

        //????????? ?????? ???????????? ????????? ?????????
        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView photo;
            public TextView uName;
            public Button chatBtn;
            public ToggleButton heartBtn;
            public ImageView online_img;

            ViewHolder(final View view) {
                super(view);

                photo = view.findViewById(R.id.chat_image1);
                uName = view.findViewById(R.id.user_sel_name);
                chatBtn = view.findViewById(R.id.friend_chatBtn);
                heartBtn = view.findViewById(R.id.heartBtn);
                online_img = view.findViewById(R.id.online_image);
                online_img.setVisibility(View.GONE);

            }

        }

        //position??? ???????????? ???????????? ???????????? ??????????????? ??????.
        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            uid = currentUser.getUid();

            final String[] temp = {""};

            //?????? ?????? ?????????
            if (filterList.get(position).getRange().equals("all")) {
                if(filterList.get(position).getProfile() == null)
                    return;
                if (!filterList.get(position).getProfile().equals(""))
                    mGlideRequestManager.load(getContext()).load(filterList.get(position).getProfile()).apply(new RequestOptions().circleCrop()).into(holder.photo);
           }
            else if (filterList.get(position).getRange().equals("friend")) {
                if (filterList.get(position).getLiked() != null) {
                    for (String key : filterList.get(position).getLiked().keySet()) {
                        if (key.equals(uid)) {
                            holder.photo.setBackgroundResource(R.drawable.yj_profile_border);
                            if(filterList.get(position).getProfile() == null)
                                return;
                            if (!filterList.get(position).getProfile().equals(""))
                                mGlideRequestManager.load(getContext()).load(filterList.get(position).getProfile()).apply(new RequestOptions().circleCrop()).into(holder.photo);
                            //else
                              //  mGlideRequestManager.load(getContext()).load("https://firebasestorage.googleapis.com/v0/b/graduation-project-ebb3e.appspot.com/o/Users%2FKakaoTalk_Photo_2020-10-31-18-19-11.png?alt=media&token=3b1cb276-6a10-47ee-a84a-126d85bf2469").apply(new RequestOptions().circleCrop()).into(holder.photo);

                        }
                    }
                }
            }


            holder.uName.setText(filterList.get(position).getName());//????????? ??????

            //liked ??????, ?????? ??????
            database.getReference("userInfo").child(currentUser.getUid()).child("liked").addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    try {
                        String friend = filterList.get(position).getUID();

                        if (snapshot.getKey().equals(friend))
                            holder.heartBtn.setBackgroundResource(R.drawable.yj_full_heart);

                    }catch (IndexOutOfBoundsException e){
                        System.out.println(e);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    String key = snapshot.getKey();
                    try {
                        String friend = filterList.get(position).getUID();

                        if (key.equals(friend))
                            holder.heartBtn.setBackgroundResource(R.drawable.yj_heart2);

                    }catch (IndexOutOfBoundsException e){
                        System.out.println(e);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });


            holder.chatBtn.setOnClickListener(new View.OnClickListener() { //????????? ?????? ?????????
                @Override
                public void onClick(View v) {
                    ChatDisplay(filterList.get(position).getUID(),filterList.get(position).getName(),filterList.get(position).getProfile());
                }
            });

            //?????? ???????????? ?????? ??????
            holder.heartBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (holder.heartBtn.isChecked()) {
                        holder.heartBtn.setBackgroundResource(R.drawable.yj_full_heart);

                        String friend = filterList.get(position).getUID();
                        temp[0] = friend;

                        Map<String, Object> likedMap = new HashMap<>();
                        likedMap.put(friend, true);

                        database.getReference("friend").child(currentUser.getUid()).child(friend).removeValue();
                        database.getReference("friend").child(currentUser.getUid()).updateChildren(likedMap);
                        database.getReference("userInfo").child(currentUser.getUid()).child("liked").updateChildren(likedMap);

                        FriendListDisplay();

                    }
                    else{
                        holder.heartBtn.setBackgroundResource(R.drawable.yj_heart2);

                        String friend = filterList.get(position).getUID();

                        Map<String, Object> likedMap = new HashMap<>();
                        likedMap.put(friend, false);

                        database.getReference("friend").child(currentUser.getUid()).child(friend).removeValue();
                        database.getReference("friend").child(currentUser.getUid()).updateChildren(likedMap);
                        database.getReference("userInfo").child(currentUser.getUid()).child("liked/"+friend).removeValue();

                        FriendListDisplay();

                    }

                }
            });

            //?????? ?????????
            Boolean connection = uData.get(position).getConnection();
            if(connection!=null) {
                if (connection == true)
                    holder.online_img.setVisibility(View.VISIBLE);
                else
                    holder.online_img.setVisibility(View.GONE);
            }

        }

        //????????? ?????? ?????? ????????? ????????? ???????????? ??????
        @NonNull
        @Override
        public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

            ViewHolder vh = new ViewHolder(view);
            return vh;
        }


        @Override
        public int getItemCount() {
            return filterList.size();
        }

        //??????????????? ?????? ??????
        public void ChatDisplay(final String rec, final String name, final String profile) {

            //?????? ???????????? ????????? ???????????? ????????? ?????? ??????
            database.getReference("ChatRoom").orderByChild("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ChatRoomModel croom = null;
                    Log.d(TAG, "onDataChange: "+dataSnapshot.exists());

                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        croom = dataSnapshot1.getValue(ChatRoomModel.class); // ???????????? ?????? ????????? ????????? ??????
                        if(croom.getGroup().equals(false)){ //1:1 ???????????? ????????? ?????????.
                            Iterator<String> iter = croom.getUsers().keySet().iterator();
                            //users?????? ????????? id ?????????.
                            ArrayList<String> user_id = new ArrayList<String>();
                            while (iter.hasNext()) {
                                String keys = (String) iter.next();
                                user_id.add(keys);
                            }
                            if (user_id.contains(rec)&&user_id.contains(currentUser.getUid())) {
                                roomid = croom.getRoomID();
                                check = true;
                                break;
                            } else {
                                roomid = null;
                                check = false;
                            }
                        }else{check = false;}

                        if(check == true)
                            break;
                    }

                    if (check == true) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);

                        intent.putExtra("roomid", roomid);
                        intent.putExtra("receiver", rec);
                        intent.putExtra("recName", name);
                        intent.putExtra("recProfile",profile);
                        intent.putExtra("check","1");
                        intent.putExtra("name", name);

                        startActivity(intent);

                    } else {
                        ChatRoomModel room = new ChatRoomModel();
                        //????????? id ??????
                        Map<String, Object> map = new HashMap<String, Object>();
                        final String roomkey = database.getReference().child("ChatRoom").push().getKey();
                        room.setRoomID(roomkey);
                        database.getReference().child("ChatRoom").updateChildren(map);

                        //?????? ???????????? ?????? ?????????
                        HashMap<String, Object> users = new HashMap<String, Object>();
                        users.put(uid,name);
                        users.put(rec,myName);
                        room.setUsers(users);

                        //DB??? roomID??? ?????? ?????? ??????
                        Map<String, Object> objectMap = new HashMap<String, Object>();
                        objectMap.put("roomID", roomkey);
                        objectMap.put("users", users);
                        objectMap.put("lastTime", ServerValue.TIMESTAMP);//????????? ?????? ??????

                        //DB??? ??????
                        database.getReference().child("ChatRoom").child(roomkey).setValue(objectMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra("roomid", roomkey);
                                intent.putExtra("receiver", rec);
                                intent.putExtra("recName", name);
                                intent.putExtra("check","1");
                                intent.putExtra("name", name);
                                startActivity(intent);

                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {  }
            });

        }

        //?????? ????????????
        public Filter getFilter(){
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String charString = constraint.toString();
                    if(charString.isEmpty()){ //??????????????? ????????? ?????? ??????
                        filterList = uData;

                    }else{//?????????
                        ArrayList<UserModel> filtering = new ArrayList<>();
                        for(UserModel item: uData){
                            //????????? ???????????? ?????????
                            if(item.getName().toLowerCase().contains(charString.toLowerCase()))
                                filtering.add(item); //?????? ????????? ????????? ???????????? ???????????? ??????
                        }
                        filterList = filtering; //??????????????? ???????????? ???????????? ????????????.
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filterList;
                    return filterResults;
                }

                //?????????????????? ?????????????????? ????????????
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filterList = (ArrayList<UserModel>)results.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

}