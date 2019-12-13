package com.example.familymapclient.Fragments;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.familymapclient.R;
import com.example.familymapclient.Views.PersonActivity;
import com.example.familymapclient.Views.SearchActivity;
import com.example.familymapclient.Views.SettingsActivity;
import com.example.familymapclient.Views.SettingsModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Model.Event;
import Model.Person;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap map;
    private ImageButton pImageButton;
    private TextView eventText;
    private Set<Polyline> familyLines = new HashSet<>();
    private Set<Polyline> spouseLines = new HashSet<>();
    private Set<Polyline> personLines = new HashSet<>();
    private ArrayList<String> dadSideIDs = new ArrayList<>();
    private ArrayList<String> momSideIDs = new ArrayList<>();
    private DataCache cache = DataCache.getInstance();
    private SettingsModel settings = SettingsModel.getInstance();
    private Person user = null;
    private String eventID = null;
    private String personID = null;
    private double currLat;
    private double currLong;
    private double birthLat;
    private double birthLong;
    private int gen;
    private boolean isClicked = false;
    private boolean fromEvent = false;

    private static final int[] LINES = new int[] {
            Color.WHITE, //LIFE STORY
            Color.GREEN, //MARRIAGE
            Color.BLACK //FAMILY TREE
    };
    private static final float[] MARKERS = new float[]{
            BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_YELLOW,
            BitmapDescriptorFactory.HUE_ROSE,
            BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_MAGENTA
    };

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (fromEvent) {
            setHasOptionsMenu(false);
        }
        else {
            setHasOptionsMenu(true);
        }
        user = DataCache.getInstance().getUser();
        if (cache.getEventsForIndividual() == null) {
            cache.setPersonsEvents();
        }
        cache.setMaternalSide(user.getMomID());
        cache.setPaternalSide(user.getDadID());
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(layoutInflater, container, savedInstanceState);
        View view = layoutInflater.inflate(R.layout.map_fragment, container, false);

        eventText = view.findViewById(R.id.mapTextView);
        pImageButton = view.findViewById(R.id.person_button);
        pImageButton.setEnabled(false);


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null){
            mapFragment.getMapAsync(this);
        }
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (cache.getFromSettings()) {
            eventID = null;
        }
        cache.setFromSettings(false);
        gen = 4;
        if (map != null) {
            buildMap();
        }
    }
    @Override
    public void onMapReady(GoogleMap gmap) {
        map = gmap;
        if (eventID == null) {
            final String[] eventInfo = {"Select a marker to see more details. " +
                    "Click on the text to read about the event or person."};
            eventText.setText(eventInfo[0]);
            pImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_android));
        }
        buildMap();
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                deleteLines();
                isClicked = true;
                String tag = (String) marker.getTag();
                Event e = cache.getEvents(tag);
                eventID = e.getEventID();
                centerMap(eventID);
                Person p;
                personID = e.getPersonID();
                currLat = e.getLat();
                currLong = e.getLong();
                gen = 4;

                if((p = cache.getPerson(personID)) != null){
                    pImageButton.setImageDrawable(getGenderIcon(p.getGender()));

                    eventText.setText(setEventInfo(eventID));

                    onClick(eventID);
                }
                return true;
            }
        });
        eventText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClicked) {
                    Intent intent = PersonActivity.newIntent(getActivity(), personID);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getContext(),"Please select a marker.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void buildMap() {
        map.clear();
        cache = DataCache.getInstance();
        settings = SettingsModel.getInstance();

        addEventMarkers();
        Event[] eventArr = cache.getEventArray();
        Event[] filteredEvents = filterBySide(eventArr);
        if (eventID != null) {
            for (Event e: eventArr) {
                if (e.getEventID().equals(eventID)) {
                    centerMap(eventID);
                    onClick(eventID);
                }
            }
        }
        else {
            personID = user.getPersonID();
            if(eventArr != null){
                for (Event event : filteredEvents) {
                    eventID = event.getEventID();
                    Person p = cache.getPerson(event.getPersonID());
                    personID = p.getPersonID();
                    getGenderIcon(p.getGender());
                    setEventInfo(eventID);
                }
            }
        }
    }
    private void centerMap(String eventID){
        if (eventID != null) {
            Event event = cache.getEventMap().get(eventID);
            if (event != null) {
                double lat = event.getLat();
                double longitude = event.getLong();
                LatLng newLoc = new LatLng(lat, longitude);
                map.moveCamera(CameraUpdateFactory.newLatLng(newLoc));
            }
        }
    }
    private void lineOnOffFilter(String pID) {
        if (settings.getShowingLifeLines()) {
            setPersonLines(pID);
        }
        if (settings.getShowingSpouseLines()) {
            setSpouseLines(pID);
        }
        if (settings.getShowingTreeLines()) {
            setFamilyLines(pID);
        }
    }
    private void onClick(String inEventID) {
        eventID = inEventID;
        isClicked = true;
        String personID = cache.getEvents(eventID).getPersonID();
        String gender = cache.getPerson(personID).getGender();

        pImageButton.setImageDrawable(getGenderIcon(gender));
        deleteLines();
        lineOnOffFilter(personID);

        eventText.setText(setEventInfo(eventID));
    }


    ////////////////LINE HANDLERS//////////////////
    private void setAllLines(String id, boolean isSpouse, double lat, double longitude, boolean isMale, int gen) {
        Event[] eventArr = cache.getEventsForIndividual().get(id);
        double newLat = eventArr[0].getLat();
        double newLong = eventArr[0].getLong();
        if (isSpouse) {
            if (!settings.getShowingMotherSide() && !settings.getShowingFatherSide()) {
                if (!momSideIDs.contains(id) && !dadSideIDs.contains(id)) {
                    sortLinesByGender(isMale, currLat, currLong, newLat, newLong);
                }
            }
            else if (!settings.getShowingMotherSide() && !momSideIDs.contains(id)) {
                sortLinesByGender(isMale, currLat, currLong, newLat, newLong);
            }
            else if (!settings.getShowingFatherSide() && !dadSideIDs.contains(id)) {
                sortLinesByGender(isMale, currLat, currLong, newLat, newLong);
            }
            else if (settings.getShowingMotherSide() && settings.getShowingFatherSide()) {
                sortLinesByGender(isMale, currLat, currLong, newLat, newLong);
            }
        }
        else {
            Polyline line = map.addPolyline(new PolylineOptions()
                    .add(new LatLng(lat, longitude), new LatLng(newLat, newLong))
                    .width(gen * 5)
                    .color(LINES[2]));
            familyLines.add(line);
        }
    }
    private void addOneLine(double currLat, double currLong, double newLat, double newLong) {
        Polyline line = map.addPolyline(new PolylineOptions()
                .add(new LatLng(currLat, currLong), new LatLng(newLat, newLong))
                .width(15)
                .color(LINES[1]));
        spouseLines.add(line);
    }
    private void sortLinesByGender(boolean isMale, double currLat, double currLong, double newLat, double newLong) {
        if (settings.getShowingMaleEvents() && isMale) {
            addOneLine(currLat, currLong, newLat, newLong);
        }
        if (settings.getShowingFemaleEvents() && !isMale) {
            addOneLine(currLat, currLong, newLat, newLong);
        }
    }
    private void setPersonLines(String personID) {
        Event[] eventArr = cache.getEventsForIndividual().get(personID);
        birthLat = eventArr[0].getLat();
        birthLong = eventArr[0].getLong();
        PolylineOptions polylineOptions = new PolylineOptions();

        for (Event event : eventArr) {
            double lat = event.getLat();
            double longitude = event.getLong();
            polylineOptions.add(new LatLng(lat, longitude)).width(15).color(LINES[0]);
        }
        Polyline line = map.addPolyline(polylineOptions);
        personLines.add(line);
    }
    private void setSpouseLines(String personID) {
        String spouseID = cache.getPersonMap().get(personID).getSpouseID();
        if (spouseID != null) {
            if (cache.getPerson(spouseID).getGender().equals("m")) {
                setAllLines(spouseID, true, birthLat, birthLong, true, 1);
            }
            else {
                setAllLines(spouseID, true, birthLat, birthLong, false, 1);
            }
        }
    }
    private void setFamilyLines(String personID) {
        Person child = cache.getPersonMap().get(personID); //from each birth of new family member
        String dadID = child.getDadID();
        String momID = child.getMomID();

        if (cache.getPerson(personID).getGender().equals("m")) {
            setAllLines(personID, false, currLat, currLong, true, gen);
        }
        else {
            setAllLines(personID, false, currLat, currLong, false, gen);
        }

        if (dadID != null && momID != null && !dadID.equals("") && !momID.equals("")) {
            gen--;
            drawParentLines(personID, dadID);
            drawParentLines(personID, momID);
        }
    }
    private void drawParentLines(String personID, String parentID) {
        Person parent = cache.getPersonMap().get(parentID);
        Event[] parentArr = cache.getEventsForIndividual().get(parentID);
        double newLat = parentArr[0].getLat();
        double newLong = parentArr[0].getLong();
        String dadID = parent.getDadID();
        String momID = parent.getMomID();

        if (cache.getPerson(personID).getGender().equals("m")) {
            setAllLines(personID, false, newLat, newLong, true, gen);
        }
        else {
            setAllLines(personID, false, newLat, newLong, false, gen);
        }

        if (dadID != null && momID != null && !dadID.equals("") && !momID.equals("")) {
            gen--;
            drawParentLines(parentID, dadID);
            drawParentLines(parentID, momID);
        }
    }
    private void deleteLines() {
        if (familyLines.size() > 0) {
            for (Polyline p : familyLines) {
                p.remove();
            }
        }
        if (personLines.size() > 0) {
            for (Polyline p : personLines) {
                p.remove();
            }
        }
        if (spouseLines.size() > 0) {
            for (Polyline p : spouseLines) {
                p.remove();
            }
        }
    }


    private Event[] filterBySide(Event[] eventArr) {
        Event[] filteredEvents;
        Set<Event> eventSet = new HashSet<>();
        if (settings.getShowingFatherSide()) {
            for (Event e : eventArr) {
                if (dadSideIDs.contains(e.getPersonID())) {
                    eventSet.add(e);
                }
            }
        }
        if (settings.getShowingMotherSide()) {
            for (Event e: eventArr) {
                if (momSideIDs.contains(e.getPersonID())) {
                    eventSet.add(e);
                }
            }
        }
        Object[] objects = eventSet.toArray();
        filteredEvents = new Event[objects.length];
        for (int k = 0; k < objects.length; k++) {
            filteredEvents[k] = (Event) objects[k];
        }
        return filteredEvents;
    }
    private Drawable getGenderIcon(String gender) {
        String strFind = gender.toLowerCase();
        if (strFind.equals("m")) {
            return getResources().getDrawable(R.drawable.ic_male);
        }
        else {
            return getResources().getDrawable(R.drawable.ic_female);
        }
    }
    private String setEventInfo(String eventID){
        Event e = DataCache.getInstance().getEventMap().get(eventID);
        personID = e.getPersonID();
        Person p = DataCache.getInstance().getPersonMap().get(personID);
        String name = p.getFirstName() + " " + p.getLastName() + "\n";
        String type = e.getEventType().toUpperCase();
        String loc = e.getCity() + ", " + e.getCountry();
        int year = e.getYear();

        return name + type + ": " + loc + " (" + year + ")";
    }
    public void setIDFromEventActivity(String eventID) {
        this.eventID = eventID;
        fromEvent = true;
    }
    private void addEventMarkers() {
        for (Map.Entry<String, Event> marker : DataCache.getInstance().getEventMap().entrySet()) {
            int color = 0;
            Event event = marker.getValue();
            double lat = event.getLat();
            double lon = event.getLong();
            LatLng place = new LatLng(lat, lon);
            String gender = DataCache.getInstance().getPerson(event.getPersonID()).getGender();
            DataCache.getInstance().setEventTypes();
            List<String> types = new ArrayList<>(DataCache.getInstance().getEventTypes());
            ArrayList<String> momIDs = cache.getMaternalSide();
            ArrayList<String> dadIDs = cache.getPaternalSide();
            for (int i = 0; i < types.size(); i++) {
                if (types.get(i).equals(event.getEventType().toUpperCase())) {
                    color = i % MARKERS.length;
                }
            }
            if (settings.getShowingFatherSide() && settings.getShowingMotherSide()) {
                filterMarkerByMF(gender, place, event, color);
            }
            else if (!settings.getShowingMotherSide() && !momIDs.contains(event.getPersonID())) { //not showing mom side & doesn't contain id
                filterMarkerByMF(gender, place, event, color);
            }
            else if (!settings.getShowingFatherSide() && !dadIDs.contains(event.getPersonID())) { //not showing dad side & doesn't contain id
                filterMarkerByMF(gender, place, event, color);
            }
            else if (!settings.getShowingMotherSide() && !settings.getShowingFatherSide()) {
                if (!momIDs.contains(event.getPersonID()) && !dadIDs.contains(event.getPersonID())) {
                    filterMarkerByMF(gender, place, event, color);
                }
            }
        }
    }
    private void filterMarkerByMF(String gender, LatLng place, Event event, int color) {
        Marker mapMarker;
        if (settings.getShowingMaleEvents()) {
            if (gender.toLowerCase().equals("m")) {
                mapMarker = map.addMarker(new MarkerOptions().position(place).title(event.getEventType())
                        .icon(BitmapDescriptorFactory.defaultMarker(MARKERS[color])));
                mapMarker.setTag(event.getEventID());
            }
        }
        if (settings.getShowingFemaleEvents()) {
            if (!gender.toLowerCase().equals("m")) {
                mapMarker = map.addMarker(new MarkerOptions().position(place).title(event.getEventType())
                        .icon(BitmapDescriptorFactory.defaultMarker(MARKERS[color])));
                mapMarker.setTag(event.getEventID());
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, 0);
        }
        if (id == R.id.menu_search) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, 0);
        }
        return true;
    }
}
