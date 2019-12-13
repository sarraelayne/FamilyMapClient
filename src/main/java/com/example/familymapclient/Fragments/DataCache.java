package com.example.familymapclient.Fragments;

import com.example.familymapclient.Views.SearchActivity;
import com.example.familymapclient.Views.SettingsModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Model.Event;
import Model.Person;

public class DataCache {
    private static DataCache instance;
    private Map<String, Person> personMap;
    private Map<String, Event> eventMap;
    private Map<String, Event[]> personsEvents;
    private Set<String> mapEventTypes;
    private ArrayList<String> momSideIDs = new ArrayList<>();
    private ArrayList<String> dadSideIDs = new ArrayList<>();
    private Event[] eventArray;
    private Person[] personArray;
    private Person user;
    private String serverHost;
    private int serverPort;
    private boolean fromSettings;
    private boolean isReg;


    public static DataCache getInstance() {
        if(instance == null) {
            instance = new DataCache();
        }
        return instance;
    }
    private DataCache() {}


    void setUser(Person person) {
        user = person;
    }
    Person getUser() {
        return user;
    }
    public void setData(String serverHost, int serverPort, Person[] persons, Event[] events) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        personMap = new HashMap<>();
        eventMap = new HashMap<>();

        //set People
        for (Person p:persons) {
            personMap.put(p.getPersonID(), p);
        }
        //set Events
        for (Event e: events) {
            eventMap.put(e.getEventID(), e);
        }
        setPersonArray(personMap);
        setEventArray(eventMap);
    }
    public void setFromSettings(boolean fromSettings) {
        this.fromSettings = fromSettings;
    }
    boolean getFromSettings() {
        return fromSettings;
    }
    private void setPersonArray(Map<String, Person> persons) {
        personArray = persons.values().toArray(new Person[0]);
    }
    public void setMaternalSide(String personID) {
        Person child = getPersonMap().get(personID);
        String momID = child.getMomID();
        String dadID = child.getDadID();

        if (momID != null && !momID.equals("")) {
            momSideIDs.add(momID);
            momSideIDs.add(dadID);
            setMaternalSide(momID);
            setMaternalSide(dadID);
        }
    }
    public void setPaternalSide(String personID) {
        Person child = getPersonMap().get(personID);
        String dadID = child.getDadID();
        String momID = child.getMomID();

        if (dadID != null && !dadID.equals("")) {
            dadSideIDs.add(dadID);
            dadSideIDs.add(momID);
            setPaternalSide(dadID);
            setPaternalSide(momID);
        }
    }
    public ArrayList<String> getMaternalSide() {
        return momSideIDs;
    }
    public ArrayList<String> getPaternalSide() {
        return dadSideIDs;
    }
//    public Event[] getEventsByPID
    public Person[] getPersonArray() {
        return personArray;
    }
    private void setEventArray(Map<String, Event> events) {
        eventArray = events.values().toArray(new Event[0]);
    }
    public Event[] getEventArray() {
        return eventArray;
    }

    public Person getPerson(String personID) {
        return personMap.get(personID);
    }
    public Map<String, Person> getPersonMap() {
        return personMap;
    }
    public void setPersonsEvents() {
        personsEvents = new HashMap<>();
        for (Person person : personArray) { //ATTEMPT TO GET LENGTH OF NULL ARRAY
            ArrayList<Event> eventList = new ArrayList<>();
            for (Event event : eventArray) {
                if (person.getPersonID().equals(event.getPersonID())) {
                    eventList.add(event);
                }
            }
            Object[] objects = eventList.toArray();
            Event[] array = new Event[objects.length];
            for (int k = 0; k < objects.length; k++) {
                array[k] = (Event) objects[k];
            }
            sortEventList(array);
            personsEvents.put(person.getPersonID(), array);
        }
    }
    private void sortEventList(Event[] e) {
        Event temp;
        for (int i = 1; i < e.length; i++) {
            for (int j = i; j > 0; j--) {
                if (e[j].getYear() < e[j - 1].getYear()) {
                    temp = e[j];
                    e[j] = e[j - 1];
                    e[j - 1] = temp;
                }
            }
        }
    }
    public Map<String, Event[]> getEventsForIndividual() {
        return personsEvents;
    }

    public Event getEvents(String eventID) {
        return eventMap.get(eventID);
    }
    Map<String, Event> getEventMap() {
        return eventMap;
    }

    public void logOut() {
        personMap = null;
        eventMap = null;
        personsEvents = null;
        mapEventTypes = null;
        eventArray = null;
        personArray = null;
        user = null;
        serverHost = null;
        serverPort = 0;
        instance = null;
    }
    public boolean isLoggedIn(){
        return user != null;
    }

    void setEventTypes() {
        mapEventTypes = new HashSet<>();
        for (Event value : eventArray) {
            String eventID = value.getEventID();
            Event event = getEvents(eventID);
            if (!mapEventTypes.contains(event.getEventType().toUpperCase())) {
                mapEventTypes.add(event.getEventType().toUpperCase());
            }
        }
    }
    Set<String> getEventTypes() {
        return mapEventTypes;
    }
    public ArrayList<SearchActivity.SearchResult> getSearchResults(String query) {
        SettingsModel settings = SettingsModel.getInstance();
        String q = query.trim().toLowerCase();
        ArrayList<SearchActivity.SearchResult> results = new ArrayList<>();
        for (Person p : personArray) {
            String last = p.getLastName().toLowerCase();
            String first = p.getFirstName().toLowerCase();
            if (first.contains(q) || last.contains(q)) {
                results.add(new SearchActivity.SearchResult(true, p.getPersonID()));
            }
        }
        if (settings.getShowingFemaleEvents()) {
            eventArray = findFemaleEvents();
            if (settings.getShowingMotherSide()) {
                for (Event e : eventArray) {
                    Person p = getPerson(e.getPersonID());
                    if (momSideIDs.contains(p.getPersonID())) {
                        String last = p.getLastName().toLowerCase();
                        String first = p.getFirstName().toLowerCase();
                        if (first.contains(q.toLowerCase()) || last.contains(q.toLowerCase())) {
                            results.add(new SearchActivity.SearchResult(false, e.getEventID()));
                        }
                    }
                }

            }
            else if (settings.getShowingFatherSide()) {
                for (Event e : eventArray) {
                    Person p = getPerson(e.getPersonID());
                    if (dadSideIDs.contains(p.getPersonID())) {
                        String last = p.getLastName().toLowerCase();
                        String first = p.getFirstName().toLowerCase();
                        if (first.contains(q.toLowerCase()) || last.contains(q.toLowerCase())) {
                            results.add(new SearchActivity.SearchResult(false, e.getEventID()));
                        }
                    }
                }

            }
            else if (settings.getShowingMotherSide() && settings.getShowingFatherSide()) {
                for (Event e : eventArray) {
                    Person p = getPerson(e.getPersonID());
                    if (dadSideIDs.contains(p.getPersonID())) {
                        String last = p.getLastName().toLowerCase();
                        String first = p.getFirstName().toLowerCase();
                        if (first.contains(q.toLowerCase()) || last.contains(q.toLowerCase())) {
                            results.add(new SearchActivity.SearchResult(false, e.getEventID()));
                        }
                    }
                }
            }
        }
        else if (settings.getShowingMaleEvents()) {
            eventArray = findMaleEvents();
            for (Event e : eventArray) {
                Person p = getPerson(e.getPersonID());
                String last = p.getLastName().toLowerCase();
                String first = p.getFirstName().toLowerCase();
                if (first.contains(q.toLowerCase()) || last.contains(q.toLowerCase())) {
                    results.add(new SearchActivity.SearchResult(false, e.getEventID()));
                }
            }
        }
        else if (settings.getShowingFemaleEvents() && settings.getShowingMaleEvents()) {
            for (Event e : eventArray) {
                Person p = getPerson(e.getPersonID());
                String last = p.getLastName().toLowerCase();
                String first = p.getFirstName().toLowerCase();
                if (first.contains(q.toLowerCase()) || last.contains(q.toLowerCase())) {
                    results.add(new SearchActivity.SearchResult(false, e.getEventID()));
                }
            }
        }
        return results;
    }
    public Event[] findMaleEvents() {
        Set<Event> mEventSet = new HashSet<>();
        Event[] events = getEventArray();
        for (Event e: events) {
            Person p = getPerson(e.getPersonID());
            if (p.getGender().equals("m")) {
                mEventSet.add(e);
            }
        }
        Object[] objects = mEventSet.toArray();
        events = new Event[objects.length];
        for (int k = 0; k < objects.length; k++) {
            events[k] = (Event) objects[k];
        }
        return events;
    }
    public Event[] findFemaleEvents() {
        Set<Event> fEventSet = new HashSet<>();
        Event[] events = getEventArray();
        for (Event e: events) {
            Person p = getPerson(e.getPersonID());
            if (p.getGender().equals("f")) {
                fEventSet.add(e);
            }
        }
        Object[] objects = fEventSet.toArray();
        events = new Event[objects.length];
        for (int k = 0; k < objects.length; k++) {
            events[k] = (Event) objects[k];
        }
        return events;
    }
    public Event[] getCurrentEvents() {
        Event[] events = getEventArray();
        SettingsModel settings = SettingsModel.getInstance();
        Set<Event> eventSet = new HashSet<>();

        for (Event e: events) {
            if (settings.getShowingMotherSide() && settings.getShowingFatherSide()) {
                if (settings.getShowingFemaleEvents() && getPerson(e.getPersonID()).getGender().equals("f")) {
                    eventSet.add(e);
                }
                if (settings.getShowingMaleEvents() && getPerson(e.getPersonID()).getGender().equals("m")) {
                    eventSet.add(e);
                }
            }
            else if (!settings.getShowingMotherSide() && !getMaternalSide().contains(e.getPersonID())) {
                if (settings.getShowingFemaleEvents() && getPerson(e.getPersonID()).getGender().equals("f")) {
                    eventSet.add(e);
                }
                if (settings.getShowingMaleEvents() && getPerson(e.getPersonID()).getGender().equals("m")) {
                    eventSet.add(e);
                }
            }
            else if (!settings.getShowingFatherSide() && !getPaternalSide().contains(e.getPersonID())) {
                if (settings.getShowingFemaleEvents() && getPerson(e.getPersonID()).getGender().equals("f")) {
                    eventSet.add(e);
                }
                if (settings.getShowingMaleEvents() && getPerson(e.getPersonID()).getGender().equals("m")) {
                    eventSet.add(e);
                }
            }
            else if (!settings.getShowingFatherSide() && !settings.getShowingMotherSide()) {
                if (!getPaternalSide().contains(e.getPersonID()) && !getMaternalSide().contains(e.getPersonID())) {
                    if (settings.getShowingFemaleEvents() && getPerson(e.getPersonID()).getGender().equals("f")) {
                        eventSet.add(e);
                    }
                    if (settings.getShowingMaleEvents() && getPerson(e.getPersonID()).getGender().equals("m")) {
                        eventSet.add(e);
                    }
                }
            }
        }
        Object[] objects = eventSet.toArray();
        events = new Event[objects.length];
        for (int k = 0; k < objects.length; k++) {
            events[k] = (Event) objects[k];
        }
        return events;
    }
    public void setRegistered(boolean isReg) {
        this.isReg = isReg;
    }
    public boolean getRegistered() { return isReg; }
}
