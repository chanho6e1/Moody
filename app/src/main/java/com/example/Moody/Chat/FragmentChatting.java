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

    public RequestManager glide; //glide??? ?????? ????????????

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(); //db?????????

        glide = Glide.with(this);

        ChatListDisplay();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_chat_list,container,false);

        /**
         * ?????? ????????? ?????????
         **/
        TextView myName = (TextView)view.findViewById(R.id.my_name);
        ImageView myImage = (ImageView)view.findViewById(R.id.my_image);
        EditText chatSearch = (EditText)view.findViewById(R.id.chat_room_search);
        myInfo(myName,myImage); //????????? ????????????

        /**
         * ??????????????????
         */
        crRecyclerView = (RecyclerView)view.findViewById(R.id.chat_list_recyclerView);
        crRecyclerView.setHasFixedSize(true);//?????????????????? ?????? ??????
        /**
         * ???????????? ?????????
         */
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(inflater.getContext());
        crRecyclerView.setLayoutManager(mLayoutManager);
        /**
         * ?????????
         */
        crAdapter = new ChatRoomListAdapter(cList);
        crRecyclerView.setAdapter(crAdapter);

        final LinearLayout newBtn = (LinearLayout) view.findViewById(R.id.chat_new_room);

        /**
         * ??? ????????? ??????
         */
        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(),UserSelectActivity.class);
                v.getContext().startActivity(intent);
            }
        });


        /**
         * ??????
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

    //????????? ??????
    public void Select(){

        for(int i = 0;i<cList.size();i++){
            Boolean check = cList.get(i).getCheck();
            if(check != null){
                roomID.add(cList.get(i).getRoomID());
            }
        }

    }

    /**
     * DB?????? ????????? ?????? ????????????
     * @param name : ?????? ????????? ????????????
     * @param image : ?????? ????????? ????????????
     */
    public void myInfo(final TextView name, final ImageView image){
        database.getReference("userInfo").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel um = dataSnapshot.getValue(UserModel.class);
                name.setText(um.getName()); //????????? ????????? ????????? ??????
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
     * DB?????? ????????? ?????? ????????????
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
                    //users?????? ????????? id ?????????.
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
     * ????????? ?????????
     */
    class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ViewHolder> {
        private static final String TAG = "ChatRoomListAdapter";

        private ArrayList<ChatRoomModel> chatRoomList; //?????? ?????????
        private ArrayList<ChatRoomModel> filterList; //????????? ?????????
        private ArrayList<String> user = new ArrayList<String>(); //????????? id
        private ArrayList<String> roomID = new ArrayList<String>(); //????????? id
        private Map<Integer,String> profiles = new HashMap<Integer,String>();// ?????????
        private Map<Integer,String> names = new HashMap<Integer,String>();//????????? ??????
        private Map<Integer,String> recID = new HashMap<Integer,String>();//????????? id

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
            if(filterList.get(position).getGroup().equals(true)){ //????????????
                return 2;
            }
            return 1; //????????? 1:1 ??????
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
            roomID.add(filterList.get(position).getRoomID()); //????????? id

            //?????? ????????? ??????
            user.clear();
            for(String id: filterList.get(position).getUsers().keySet()){
                if(!id.equals(currentUser.getUid()))
                    user.add(id);
            }


            //?????? ??? ?????? ?????? ??????
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
                            if (filterList.get(position).getGroup().equals(false)) { //?????? ?????????
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
                    //?????? ??????
                    long unixTime = (long) crm.getLastTime();
                    Date date = new Date(unixTime);
                    writeTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                    String time = writeTimeFormat.format(date);
                    //?????? ??????
                    holder.time.setText(time);
                    //????????? ?????????
                    holder.lastMsg.setText(crm.getLastMsg());
                    //????????? ??????
                    if(crm.getUsers().get(uid) == null)
                        return;
                    holder.roomName.setText(crm.getUsers().get(uid).toString());
                    names.put(position,crm.getUsers().get(uid).toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            //????????? ????????? ??????
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

            //????????? ?????????
            holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra("roomid", roomID.get(position));
                    if(filterList.get(position).getGroup().equals(true)) {
                        //?????? ?????????
                        intent.putExtra("name",names.get(position)); //????????? ?????? ??????
                        intent.putExtra("check", "2");
                        startActivity(intent);
                    }else {
                        //?????? ?????????
                        intent.putExtra("name",names.get(position)); //????????? ?????? ??????
                        intent.putExtra("receiver",recID.get(position)); //id ??????
                        intent.putExtra("check", "1");
                        startActivity(intent);
                    }


                }
            });

            //????????? ??????
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
             * ????????? ?????? ?????????
             */
            holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(names.get(position));

                    final String[] arr = {"????????? ?????? ??????","????????? ?????????"};
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
                                    if(filterList.get(position).getUsers().size()>1){ //???????????? ????????? ????????? ????????? ????????? ??????
                                        database.getReference("ChatRoom").child(filterList.get(position).getRoomID()).child("users").child(uid).removeValue();

                                    }else{ //???????????? ?????? ????????? ????????? ??????
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

        //????????? ??????
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
                    if (charString.isEmpty()) { //??????????????? ????????? ?????? ??????
                        filterList = chatRoomList;
                    } else {//?????????
                        ArrayList<ChatRoomModel> filtering = new ArrayList<>();
                        for (ChatRoomModel item : chatRoomList) {
                            //????????? ???????????? ?????????
                            if (item.getUsers().get(uid).toString().toLowerCase().contains(charString.toLowerCase()))
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
                    filterList = (ArrayList<ChatRoomModel>) results.values;
                    notifyDataSetChanged();
                }
            };
        }



    }

}