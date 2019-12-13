package com.example.familymapclient.Views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.familymapclient.Fragments.MapFragment;
import com.example.familymapclient.R;

public class EventActivity extends AppCompatActivity {

    public static final String EVENT_ACTIVITY_ID = "com.example.familymapclient.Views.EventActivity";

    public static Intent newIntent(Context packageContext, String eventID) {
        Intent intent = new Intent(packageContext, EventActivity.class);
        intent.putExtra(EVENT_ACTIVITY_ID, eventID);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_activity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String eventID = getIntent().getStringExtra(EVENT_ACTIVITY_ID);

        FragmentManager fm = this.getSupportFragmentManager();
        MapFragment frag = new MapFragment();
        frag.setIDFromEventActivity(eventID);
        fm.beginTransaction().add(R.id.event_activity_fragment, frag).commit();

    }
}
