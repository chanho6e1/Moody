package com.example.Moody.Chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.Moody.Model.ChatRoomModel;
import com.example.Moody.Model.ChatModel;
import com.example.Moody.Model.UserModel;
import com.example.Moody.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

public class FragmentChatting extends Fragment {
    private static final String TAG = "FragmentChatting";
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private FirebaseDatabase database =FirebaseDatabase.getInstance();

    private RecyclerView crRecyclerView;
    private ChatRoomListAdapter crAdapter;
    private ArrayList<ChatRoomModel> cList = new ArrayList<ChatRoomModel>();
    private ArrayList<String>roomID = new ArrayList<String>();

    private String uid;
    public String delchat = "off";

    public static FragmentChatting newInstance(){
        return new FragmentChatting();
    }

    public RequestManager glide; //glide를 위한 매개변수

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(); //db초기화

        glide = Glide.with(this);

        ChatListDisplay();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_chat_list,container,false);

        /**
         * 상단 사용자 프로필
         **/
        TextView myName = (TextView)view.findViewById(R.id.my_name);
        ImageView myImage = (ImageView)view.findViewById(R.id.my_image);
        EditText chatSearch = (EditText)view.findViewById(R.id.chat_room_search);
        myInfo(myName,myImage); //내정보 가져오기

        /**
         * 리사이클러뷰
         */
        crRecyclerView = (RecyclerView)view.findViewById(R.id.chat_list_recyclerView);
        crRecyclerView.setHasFixedSize(true);//리사이클러뷰 크기 고정
        /**
         * 레이아웃 매니저
         */
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(inflater.getContext());
        crRecyclerView.setLayoutManager(mLayoutManager);
        /**
         * 어뎁터
         */
        crAdapter = new ChatRoomListAdapter(cList);
        crRecyclerView.setAdapter(crAdapter);

        final LinearLayout newBtn = (LinearLayout) view.findViewById(R.id.chat_new_room);

        /**
         * 새 채팅방 생성
         */
        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(),UserSelectActivity.class);
                v.getContext().startActivity(intent);
            }
        });


        /**
         * 검색
         */
        chatSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


        return view;
    }

    //채팅방 선택
    public void Select(){

        for(int i = 0;i<cList.size();i++){
            Boolean check = cList.get(i).getCheck();
            if(check != null){
                roomID.add(cList.get(i).getRoomID());
            }
        }

    }

    /**
     * DB에서 사용자 정보 불러오기
     * @param name : 화면 상단의 텍스트뷰
     * @param image : 화면 상단의 이미지뷰
     */
    public void myInfo(final TextView name, final ImageView image){
        database.getReference("userInfo").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel um = dataSnapshot.getValue(UserModel.class);
                name.setText(um.getName()); //채팅방 상단에 사용자 정보
                if (um.getRange().equals("all")) {
                    if (!um.getProfile().equals(""))
                        glide.load(getContext()).load(um.getProfile()).apply(new RequestOptions().circleCrop()).error(R.drawable.user).into(image);
                }
                else if (um.getRange().equals("friend")) {
                    image.setBackgroundResource(R.drawable.yj_profile_border);
                    if (!um.getProfile().equals(""))
                        glide.load(getContext()).load(um.getProfile()).apply(new RequestOptions().circleCrop()).error(R.drawable.user).into(image);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * DB에서 채팅방 정보 불러오기
     */
    public void ChatListDisplay() {
        uid = currentUser.getUid();
        database.getReference("ChatRoom").orderByChild("lastTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cList.clear();
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    ChatRoomModel room = dataSnapshot1.getValue(ChatRoomModel.class);
                    Iterator<String> iter = room.getUsers().keySet().iterator();
                    //users에서 상대방 id 찾는다.
                    while (iter.hasNext()) {
                        String keys = (String) iter.next();
                        if(keys.equals(uid)){
                            cList.add(0,room);
                        }
                    }
                }
                crAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });


    }



    //=======================================================================================================================

    /**
     * 채팅방 어뎁터
     */
    class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ViewHolder> {
        private static final String TAG = "ChatRoomListAdapter";

        private ArrayList<ChatRoomModel> chatRoomList; //전체 데이터
        private ArrayList<ChatRoomModel> filterList; //검색된 데이터
        private ArrayList<String> user = new ArrayList<String>(); //상대방 id
        private ArrayList<String> roomID = new ArrayList<String>(); //채팅방 id
        private Map<Integer,String> profiles = new HashMap<Integer,String>();// 프로필
        private Map<Integer,String> names = new HashMap<Integer,String>();//상대방 이름
        private Map<Integer,String> recID = new HashMap<Integer,String>();//상대방 id

        SimpleDateFormat writeTimeFormat = new SimpleDateFormat("a hh:mm");


        public ChatRoomListAdapter(ArrayList<ChatRoomModel> list) {
            this.chatRoomList = list;
            this.filterList = list;

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView userImage;
            ImageView userImage1;
            ImageView userImage2;
            ImageView userImage3;
            ImageView userImage4;
            TextView roomName;
            TextView lastMsg;
            TextView time;
            LinearLayout itemLayout;
            TextView msgCount;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                userImage = (ImageView) itemView.findViewById(R.id.chat_image1);
                userImage1 = (ImageView) itemView.findViewById(R.id.chat_image2);
                userImage2 = (ImageView) itemView.findViewById(R.id.chat_image3);
                userImage3 = (ImageView) itemView.findViewById(R.id.chat_image4);
                userImage4 = (ImageView) itemView.findViewById(R.id.chat_image5);
                roomName = (TextView) itemView.findViewById(R.id.chat_room_name);
                lastMsg = (TextView) itemView.findViewById(R.id.chat_lastMsg);
                time = (TextView) itemView.findViewById(R.id.chat_time);
                itemLayout = (LinearLayout) itemView.findViewById(R.id.chatList_layout);
                msgCount = (TextView) itemView.findViewById(R.id.msg_count);

            }
        }

        @Override
        public int getItemViewType(int position) {
            if(filterList.get(position).getGroup().equals(true)){ //그룹채팅
                return 2;
            }
            return 1; //아니면 1:1 채팅
        }

        @NonNull
        @Override
        public ChatRoomListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if(viewType == 1){
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatlist, parent, false);
            }else{
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chatlist, parent, false);
            }

            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull final ChatRoomListAdapter.ViewHolder holder, final int position) {
            roomID.add(filterList.get(position).getRoomID()); //채팅방 id

            //유저 정보만 추출
            user.clear();
            for(String id: filterList.get(position).getUsers().keySet()){
                if(!id.equals(currentUser.getUid()))
                    user.add(id);
            }


            //유저 수 대로 정보 출력
            final int[] count = {0};

            System.out.println("size: "+filterList.get(position).getUsers().size());
            for(int i=0; i<filterList.get(position).getUsers().size()-1; i++){


                database.getReference("userInfo").child(user.get(i)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserModel um = dataSnapshot.getValue(UserModel.class);
                        //Log.d(TAG, "onDataChange: position="+um.getName());
                        recID.put(position,um.getUID());
                        profiles.put(position,um.getProfile());

                        try {

                            System.out.println("count: " + count[0]);
                            if (filterList.get(position).getGroup().equals(false)) { //개인 채팅방
                                if (um.getRange().equals("all")) {
                                    if (um.getProfile() != null && !um.getProfile().equals(""))
                                        Glide.with(holder.userImage.getContext()).load(um.getProfile()).apply(new RequestOptions().circleCrop()).into(holder.userImage);
                                } else if (um.getRange().equals("friend")) {
                                    if (um.getLiked() != null) {
                                        for (String key : um.getLiked().keySet()) {
                                            if (key.equals(uid)) {
                                                holder.userImage.setBackgroundResource(R.drawable.yj_profile_border);
                                                if (um.getProfile() != null && !um.getProfile().equals(""))
                                                    Glide.with(holder.userImage.getContext()).load(um.getProfile()).apply(new RequestOptions().circleCrop()).into(holder.userImage);
                                            }
                                        }
                                    }
                                }
                            }

                        }catch (IndexOutOfBoundsException | NullPointerException e){ }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError){}
                });

            }

            database.getReference("ChatRoom").child(filterList.get(position).getRoomID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ChatRoomModel crm = dataSnapshot.getValue(ChatRoomModel.class);
                    if(crm == null)
                        return;
                    //시간 포맷
                    long unixTime = (long) crm.getLastTime();
                    Date date = new Date(unixTime);
                    writeTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                    String time = writeTimeFormat.format(date);
                    //시간 출력
                    holder.time.setText(time);
                    //마지막 메시지
                    holder.lastMsg.setText(crm.getLastMsg());
                    //채팅방 이름
                    if(crm.getUsers().get(uid) == null)
                        return;
                    holder.roomName.setText(crm.getUsers().get(uid).toString());
                    names.put(position,crm.getUsers().get(uid).toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            //안읽은 메시지 개수
            database.getReference("Message").child(filterList.get(position).getRoomID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long count = dataSnapshot.getChildrenCount();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        ChatModel cm = dataSnapshot1.getValue(ChatModel.class);
                        for (String user : cm.getReadUsers().keySet()) {
                            if (user.equals(currentUser.getUid())) {
                                count--;
                            }
                        }
                    }
                    //Log.d(TAG, "onDataChange: count="+count);
                    if (count > 0) {
                        holder.msgCount.setVisibility(View.VISIBLE);
                        holder.msgCount.setText(String.valueOf(count));
                    } else {
                        holder.msgCount.setVisibility(View.INVISIBLE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            //채팅방 클릭시
            holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra("roomid", roomID.get(position));
                    if(filterList.get(position).getGroup().equals(true)) {
                        //단체 채팅방
                        intent.putExtra("name",names.get(position)); //채팅방 이름 전달
                        intent.putExtra("check", "2");
                        startActivity(intent);
                    }else {
                        //개인 채팅방
                        intent.putExtra("name",names.get(position)); //채팅방 이름 전달
                        intent.putExtra("receiver",recID.get(position)); //id 전달
                        intent.putExtra("check", "1");
                        startActivity(intent);
                    }


                }
            });

            //채팅방 삭제
            database.getReference("ChatRoom").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String key = snapshot.getKey();
                    System.out.println(key);

                    ChatRoomModel crm = snapshot.getValue(ChatRoomModel.class);

                    System.out.println(crm.getRoomID());

                    if(key.equals(crm.getRoomID())&&crm.getUsers().size()==1 && crm.getGroup().equals(false))
                        database.getReference("ChatRoom").child(crm.getRoomID()).removeValue();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });


            /**
             * 채팅방 길게 클릭시
             */
            holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(names.get(position));

                    final String[] arr = {"채팅방 이름 변경","채팅방 나가기"};
                    builder.setItems(arr, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String select = arr[which].toString();
                            switch (which){
                                case 0:
                                    Intent intent = new Intent(getContext(),ChangeRoomName.class);
                                    intent.putExtra("roomID",roomID.get(position));
                                    startActivity(intent);
                                    break;
                                case 1:
                                    RemoveItem(position);
                                    if(filterList.get(position).getUsers().size()>1){ //채팅방에 둘이상 있을때 채팅방 나가기 기능
                                        database.getReference("ChatRoom").child(filterList.get(position).getRoomID()).child("users").child(uid).removeValue();

                                    }else{ //채팅방에 혼자 남으면 채팅방 삭제
                                        database.getReference("ChatRoom").child(filterList.get(position).getRoomID()).removeValue();
                                        database.getReference("Message").child(filterList.get(position).getRoomID()).removeValue();
                                    }
                                    notifyDataSetChanged();
                            }
                        }
                    });
                    builder.show();
                    return true;
                }
            });

        }

        //채팅방 삭제
        public void RemoveItem(int position){
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,filterList.size());
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return filterList.size();
        }


        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String charString = constraint.toString();
                    if (charString.isEmpty()) { //입력받은게 없다면 전부 출력
                        filterList = chatRoomList;
                    } else {//있다면
                        ArrayList<ChatRoomModel> filtering = new ArrayList<>();
                        for (ChatRoomModel item : chatRoomList) {
                            //채팅방 이름으로 필터링
                            if (item.getUsers().get(uid).toString().toLowerCase().contains(charString.toLowerCase()))
                                filtering.add(item); //전체 데이터 중에서 입력받은 데이터만 추가
                        }
                        filterList = filtering; //검색창에서 입력받은 아이템만 출력한다.
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filterList;
                    return filterResults;
                }

                //필터링된걸로 리사이클러뷰 업데이트
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filterList = (ArrayList<ChatRoomModel>) results.values;
                    notifyDataSetChanged();
                }
            };
        }



    }

}