package com.example.familymapclient.Views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.familymapclient.Fragments.DataCache;
import com.example.familymapclient.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import Model.Event;
import Model.Person;

public class PersonActivity extends AppCompatActivity {

    private ExpandableListView expandList;
    private ExpandableListAdapter listAdapter;
    private List<String> dataHeader;
    HashMap<String, List<String>> dataChild;
    public static final String PERSON_ACTIVITY_ID = "com.example.familymapclient.Views.PersonActivity";
    SettingsModel settings = SettingsModel.getInstance();

    private Person person;
    private Person spouse;
    private Person child;
    private Event[] events;

    private String personID;

    public static Intent newIntent(Context context, String strParam) {
        Intent intent = new Intent(context, PersonActivity.class);
        intent.putExtra(PERSON_ACTIVITY_ID, strParam);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person_activity);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        personID = getIntent().getStringExtra(PERSON_ACTIVITY_ID);
        person = DataCache.getInstance().getPerson(personID);
        findFamily();

        TextView firstName = findViewById(R.id.first_name);
        firstName.setText(person.getFirstName());

        TextView lastName = findViewById(R.id.last_name);
        lastName.setText(person.getLastName());

        TextView gender = findViewById(R.id.gender);
        gender.setText(genderString(person.getGender()));

        ///////////////CREATE EXPANDABLE LIST////////////////
        expandList = findViewById(R.id.expandable_list);
        createList();
        listAdapter = new com.example.familymapclient.Views.
                ExpandableListAdapter(this, dataHeader, dataChild);
        expandList.setAdapter(listAdapter);

        expandList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                if (i == 0) { //if its an event start an event activity
                    Intent intent = EventActivity.newIntent(PersonActivity.this, events[i1].getEventID());
                    startActivity(intent);
                }
                if (i == 1) { //if its a person start a person activity
                    if (i1 == 0) {
                        Intent intent = PersonActivity.newIntent(PersonActivity.this, spouse.getPersonID());
                        startActivity(intent);
                    }
                    else {
                        Intent intent = PersonActivity.newIntent(PersonActivity.this, child.getPersonID());
                        startActivity(intent);
                    }
                }
                return false;
            }
        });
    }

    private void findFamily() {
        //FIND SPOUSE
        Person[] spouseArr = DataCache.getInstance().getPersonArray();

        for (Person value : spouseArr) {
            if (value.getPersonID() != null) {
                if (value.getPersonID().equals(person.getSpouseID())) {
                    spouse = value;
                    break;
                }
            }
        }

        //FIND CHILD
        Person[] children = DataCache.getInstance().getPersonArray();

        for (Person value : children) {
            if (value.getMomID() != null && value.getDadID() != null) {
                child = value;
                break;
            }
        }
    }
    private void createList() {
        //EVENTS FIRST
        //1. Birth events
        //2. Events by year
        //3. Death events
        events = DataCache.getInstance().getEventsForIndividual().get(personID);
        dataHeader = new ArrayList<>();
        dataChild = new HashMap<>();
        List<String> eventList = new ArrayList<>();

        //GENDER FILTER FOR EVENTS
        if (settings.getShowingFemaleEvents() && settings.getShowingMaleEvents()) {
            for (Event event : events) {
                eventList.add(eventString(event));
            }
        }
        else if (settings.getShowingFemaleEvents() && !settings.getShowingMaleEvents()) {
            for (Event event : events) {
                Person p = DataCache.getInstance().getPerson(event.getPersonID());
                if (p.getGender().toLowerCase().equals("f")) {
                    eventList.add(eventString(event));
                }
            }
        }
        else if (settings.getShowingMaleEvents() && !settings.getShowingFemaleEvents()) {
            for (Event event : events) {
                Person p = DataCache.getInstance().getPerson(event.getPersonID());
                if (p.getGender().toLowerCase().equals("m")) {
                    eventList.add(eventString(event));
                }
            }
        }

        dataHeader.add("LIFE EVENTS");
        dataChild.put("LIFE EVENTS", eventList);

        //FAMILY
        List<String> familyList = new ArrayList<>();

        if (spouse != null) {
            String spouseStr = spouse.getFirstName() + " " + spouse.getLastName() + "\n" + "Spouse";
            familyList.add(spouseStr);
        }
        if (child != null) {
            String childStr = child.getFirstName() + " " + child.getLastName() + "\n" + "Child";
            familyList.add(childStr);
        }

        dataHeader.add("FAMILY");
        dataChild.put("FAMILY", familyList);
    }
    private String genderString(String gender) {
        if (gender.toLowerCase().equals("m")) {
            return "Male";
        }
        return "Female";
    }
    private String eventString(Event e) {
        return e.getEventType().toUpperCase() + ": " + e.getCity() + ", " + e.getCountry() +
                " (" + e.getYear() + ") " + "\n" + person.getFirstName() + " " +
                person.getLastName();
    }
}
