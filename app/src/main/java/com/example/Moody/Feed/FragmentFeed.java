package com.example.Moody.Feed;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Moody.Activity.IntroActivity;
import com.example.Moody.Activity.LoginActivity;
import com.example.Moody.Firebase.UpLoadImageToFirebase;
import com.example.Moody.Model.FeedItems;
import com.example.Moody.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class FragmentFeed extends Fragment {
    private static final String TAG = "FragmentFeed";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static RecyclerView feedRecyclerView;
    public static RecyclerView pageRecyclerView;
    PageAdapter pAdapter;
    FeedAdapter fAdapter;
    int tab=1;
    int mode=0;
    public static FragmentFeed newInstance() {
        return new FragmentFeed();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        super.onCreate(savedInstanceState);

        feedRecyclerView = (RecyclerView) view.findViewById(R.id.feed_recyclerview);
        feedRecyclerView.setHasFixedSize(true);
        feedRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        pageRecyclerView = (RecyclerView) view.findViewById(R.id.page_recyclerview);
        pageRecyclerView.setHasFixedSize(true);
        pageRecyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext(), LinearLayoutManager.HORIZONTAL, false));

        final TextView public_btn = (TextView) view.findViewById(R.id.feed_public_btn);
        final TextView private_btn = (TextView) view.findViewById(R.id.feed_private_btn);
        final TextView mark_btn = (TextView) view.findViewById(R.id.feed_mark_btn);
        final TextView tag_btn = (TextView) view.findViewById(R.id.feed_tag_btn);
        final LinearLayout layout1 = (LinearLayout) view.findViewById(R.id.layout1);
        final LinearLayout layout2 = (LinearLayout) view.findViewById(R.id.layout2);
        final LinearLayout layout3 = (LinearLayout) view.findViewById(R.id.layout3);
        final LinearLayout layout4 = (LinearLayout) view.findViewById(R.id.layout4);

        //출력 순서
        final Spinner spinner = (Spinner) view.findViewById(R.id.feed_sortmode_spinner);
        ArrayAdapter SpinAdapter = ArrayAdapter.createFromResource(inflater.getContext(), R.array.selmode, R.layout.spinner_design);
        spinner.setAdapter(SpinAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //공용이미지 정렬
                ArrayList<FeedItems> pList = new ArrayList<FeedItems>();
                if (parent.getItemAtPosition(position).equals("Newest") && tab == 1) {
                    mode = 1;
                    for (int i = IntroActivity.publicItems.size() - 1; i >= 0; i--) {
                        FeedItems entity = new FeedItems();
                        entity.setUrl(IntroActivity.publicItems.get(i).getUrl());
                        entity.setTag(IntroActivity.publicItems.get(i).getType());
                        entity.setResult(IntroActivity.publicItems.get(i).getResult());
                        pList.add(entity);
                    }
                    fAdapter = new FeedAdapter(inflater.getContext(), pList);
                    feedRecyclerView.setAdapter(fAdapter);
                    pAdapter = new PageAdapter(inflater.getContext(), pList);
                    pageRecyclerView.setAdapter(pAdapter);
                } else if (parent.getItemAtPosition(position).equals("Oldest") && tab == 1) {
                    mode = 2;
                    for (int i = 0; i < IntroActivity.publicItems.size(); i++) {
                        FeedItems entity = new FeedItems();
                        entity.setUrl(IntroActivity.publicItems.get(i).getUrl());
                        entity.setTag(IntroActivity.publicItems.get(i).getType());
                        entity.setResult(IntroActivity.publicItems.get(i).getResult());
                        pList.add(entity);
                    }
                    fAdapter = new FeedAdapter(inflater.getContext(), pList);
                    feedRecyclerView.setAdapter(fAdapter);
                    pAdapter = new PageAdapter(inflater.getContext(), pList);
                    pageRecyclerView.setAdapter(pAdapter);
                }
                //개인 정렬
                else if (parent.getItemAtPosition(position).equals("Newest") && tab == 2) {
                    mode = 1;
                    ArrayList<FeedItems> descItems = LoginActivity.dbHelper.getItems(1);
                    fAdapter = new FeedAdapter(inflater.getContext(), descItems);
                    pAdapter = new PageAdapter(inflater.getContext(), descItems);
                    feedRecyclerView.setAdapter(fAdapter);
                    pageRecyclerView.setAdapter(pAdapter);

                } else if (parent.getItemAtPosition(position).equals("Oldest") && tab == 2) {
                    mode = 2;
                    ArrayList<FeedItems> feedItems = LoginActivity.dbHelper.getItems(2);
                    fAdapter = new FeedAdapter(inflater.getContext(), feedItems);
                    feedRecyclerView.setAdapter(fAdapter);
                    pAdapter = new PageAdapter(inflater.getContext(), feedItems);
                    pageRecyclerView.setAdapter(pAdapter);
                }
                //즐겨찾기 정렬
                else if (parent.getItemAtPosition(position).equals("Newest") && tab == 4) {
                    mode = 1;
                    ArrayList<FeedItems> descItems = LoginActivity.dbHelper.getStarItems(1);
                    descItems.addAll(LoginActivity.dbHelper.getMarkItems(1));
                    fAdapter = new FeedAdapter(inflater.getContext(), descItems);
                    feedRecyclerView.setAdapter(fAdapter);
                    pAdapter = new PageAdapter(inflater.getContext(), descItems);
                    pageRecyclerView.setAdapter(pAdapter);

                } else if (parent.getItemAtPosition(position).equals("Oldest") && tab == 4) {
                    mode = 2;
                    ArrayList<FeedItems> feedItems = LoginActivity.dbHelper.getStarItems(2);
                    feedItems.addAll(LoginActivity.dbHelper.getMarkItems(2));
                    fAdapter = new FeedAdapter(inflater.getContext(), feedItems);
                    feedRecyclerView.setAdapter(fAdapter);
                    pAdapter = new PageAdapter(inflater.getContext(), feedItems);
                    pageRecyclerView.setAdapter(pAdapter);
                } else {
                    mode = 3;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //이미지 등록
        FloatingActionButton uploadBtn = (FloatingActionButton) view.findViewById(R.id.feed_upload_btn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getUid().equals("QBriucJOQMZlerNl6p4EI1zJTuW2")) {
                    startActivity(new Intent(inflater.getContext(), UpLoadImageToFirebase.class));
                } else {
                    startActivity(new Intent(inflater.getContext(), UploadPhotoActivity.class));
                }
            }
        });

        //공용 이미지
        public_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tab = 1;
                layout1.setBackgroundResource(R.drawable.yj_feed_click_btn);
                layout2.setBackgroundColor(Color.parseColor("#00ff0000"));
                layout3.setBackgroundColor(Color.parseColor("#00ff0000"));
                layout4.setBackgroundColor(Color.parseColor("#00ff0000"));

                ArrayList<FeedItems> pList = new ArrayList<FeedItems>();
                if (mode == 2) {
                    for (int i = 0; i < IntroActivity.publicItems.size(); i++) {
                        FeedItems entity = new FeedItems();
                        entity.setUrl(IntroActivity.publicItems.get(i).getUrl());
                        entity.setTag(IntroActivity.publicItems.get(i).getType());
                        entity.setResult(IntroActivity.publicItems.get(i).getResult());
                        pList.add(entity);
                    }
                } else {
                    for (int i = IntroActivity.publicItems.size() - 1; i >= 0; i--) {
                        FeedItems entity = new FeedItems();
                        entity.setUrl(IntroActivity.publicItems.get(i).getUrl());
                        entity.setTag(IntroActivity.publicItems.get(i).getType());
                        entity.setResult(IntroActivity.publicItems.get(i).getResult());
                        pList.add(entity);
                    }
                }
                fAdapter = new FeedAdapter(inflater.getContext(), pList);
                feedRecyclerView.setAdapter(fAdapter);
                feedRecyclerView.setLayoutManager(new GridLayoutManager(inflater.getContext(), 2));
                pAdapter = new PageAdapter(inflater.getContext(), pList);
                pageRecyclerView.setAdapter(pAdapter);
                pageRecyclerView.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.VISIBLE);

            }
        });

        //개별 이미지
        private_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tab = 2;
                layout2.setBackgroundResource(R.drawable.yj_feed_click_btn);
                layout1.setBackgroundColor(Color.parseColor("#00ff0000"));
                layout3.setBackgroundColor(Color.parseColor("#00ff0000"));
                layout4.setBackgroundColor(Color.parseColor("#00ff0000"));

                ArrayList<FeedItems> privateItems = new ArrayList<>();

                if (mode == 2) {
                    privateItems = LoginActivity.dbHelper.getItems(2);
                } else {
                    privateItems = LoginActivity.dbHelper.getItems(1);
                }
                fAdapter = new FeedAdapter(inflater.getContext(),privateItems);
                feedRecyclerView.setLayoutManager(new GridLayoutManager(inflater.getContext(), 2));
                feedRecyclerView.setAdapter(fAdapter);
                pAdapter = new PageAdapter(inflater.getContext(), privateItems);
                pageRecyclerView.setAdapter(pAdapter);
                pageRecyclerView.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.VISIBLE);
            }
        });

        //즐겨찾기
        mark_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tab = 4;
                layout4.setBackgroundResource(R.drawable.yj_feed_click_btn);
                layout2.setBackgroundColor(Color.parseColor("#00ff0000"));
                layout3.setBackgroundColor(Color.parseColor("#00ff0000"));
                layout1.setBackgroundColor(Color.parseColor("#00ff0000"));

                ArrayList<FeedItems> starItems = new ArrayList<>();
                if (mode == 1) {
                    starItems = LoginActivity.dbHelper.getStarItems(1);
                    starItems.addAll(LoginActivity.dbHelper.getMarkItems(1));
                } else {
                    starItems = LoginActivity.dbHelper.getStarItems(2);
                    starItems.addAll(LoginActivity.dbHelper.getMarkItems(2));
                }

                fAdapter = new FeedAdapter(inflater.getContext(), starItems);
                feedRecyclerView.setLayoutManager(new GridLayoutManager(inflater.getContext(), 2));
                feedRecyclerView.setAdapter(fAdapter);
                pAdapter = new PageAdapter(inflater.getContext(), starItems);
                pageRecyclerView.setAdapter(pAdapter);
                pageRecyclerView.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.VISIBLE);
            }
        });

        //태그 이미지
        tag_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tab = 3;
                layout3.setBackgroundResource(R.drawable.yj_feed_click_btn);
                layout2.setBackgroundColor(Color.parseColor("#00ff0000"));
                layout1.setBackgroundColor(Color.parseColor("#00ff0000"));
                layout4.setBackgroundColor(Color.parseColor("#00ff0000"));

                String tag[] = {"Happy", "Sad", "Angry", "Surprise", "Fear", "Disgust"};
                feedRecyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
                TagImageAdapter myAdapter = new TagImageAdapter(inflater.getContext(), tag);
                feedRecyclerView.setAdapter(myAdapter);
                pageRecyclerView.setVisibility(View.INVISIBLE);
                spinner.setVisibility(View.GONE);
            }
        });

        return view;

    }
}