package com.example.familymapclient;

import com.example.familymapclient.Fragments.DataCache;
import com.example.familymapclient.Views.SettingsModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Model.Event;
import Model.Person;

import static org.junit.Assert.*;

public class DataCacheTest {
    DataCache cache = DataCache.getInstance();
    SettingsModel settings = SettingsModel.getInstance();
    Person[] persons = null;
    Event[] events = null;
    @Before
    public void setUp() {
        persons = new Person[] {
                new Person("Sheila_Parker", "sheila",
                        "Sheila", "Parker", "f", "Betty_White",
                        "Blaine_McGary", "Davis_Hyer"),
                new Person("Davis_Hyer", "sheila",
                        "Davis", "Hyer", "m", null, null,
                        "Sheila_Parker"),
                new Person("Blaine_McGary", "sheila", "Blaine",
                        "McGary", "m", "Mrs_Rodham", "Ken_Rodham",
                        "Betty_White"),
                new Person("Betty_White", "sheila", "Betty",
                        "White", "f", "Mrs_Jones", "Frank_Jones",
                        "Blaine_McGary"),
                new Person("Ken_Rodham", "sheila", "Ken",
                        "Rodham", "m", null, null, "Mrs_Rodham"),
                new Person("Mrs_Rodham", "sheila", "Mrs",
                        "Rodham", "f", null, null, "Ken_Rodham"),
                new Person("Frank_Jones", "sheila", "Frank",
                        "Jones", "m", null, null, "Mrs_Jones"),
                new Person("Mrs_Jones", "sheila", "Mrs",
                        "Jones", "f", null, null, "Frank_Jones"),

        };
        events = new Event[] {
                new Event("Sheila_Birth", "sheila", "Sheila_Parker",
                        -36.1833, 144.9667, "Australia",
                        "Melbourne", "birth", 1970),
                new Event("Sheila_Marriage", "sheila", "Sheila_Parker",
                        34.0500, -117.7500, "United States", "Los Angeles",
                        "marriage", 2012),
                new Event("Sheila_Death", "sheila", "Sheila_Parker",
                        40.2444, 111.6608, "United States", "Provo",
                        "death", 2015),
                new Event("Davis_Birth", "sheila", "Davis_Hyer",
                        41.7667, 140.7333, "Japan", "Hakodate",
                        "birth", 1970),
                new Event("Blaine_Birth", "sheila", "Blaine_McGary",
                        56.1167, 101.6000, "Russia", "Bratsk",
                        "birth", 1948),
                new Event("Betty_Death", "sheila", "Betty_White",
                        52.4833, -0.1000, "United Kingdom", "Birmingham",
                        "death", 2017),
                new Event("Rodham_Marriage", "sheila", "Ken_Rodham",
                        39.15, 127.45, "North Korea", "Wonsan",
                        "marriage", 1895),
                new Event("Back_Flip", "sheila", "Mrs_Rodham",
                        32.6667, -114.5333, "Mexico", "Mexicali",
                        "Did a backflip", 1890),
                new Event("Got_Delhi_Belly", "sheila", "Frank_Jones",
                        28.644800, 77.216721, "India", "Delhi",
                        "Got sick", 2015),
                new Event("Jones_Frog", "sheila", "Frank_Jones",
                        25.0667, -76.6667, "Bahamas", "Nassau",
                        "Caught a frog", 1993),
                new Event("Jones_Barbecue", "sheila", "Mrs_Jones",
                        -24.5833, -48.75, "Brazil", "Curitiba",
                        "Ate Brazilian barbecue", 2012)
        };
        cache.setData("10.0.2.2", 8080, persons, events);
    }
    @After
    public void tearDown() {
        persons = null;
        events = null;
    }
    @Test
    public void calculateRelationshipsPass() {
        Person p = persons[0];
        assert(p.getMomID().equals("Betty_White"));
        assert(p.getDadID().equals("Blaine_McGary"));
        assert(p.getSpouseID().equals("Davis_Hyer"));
        for (Person person : persons) {
            assertNotEquals("Sheila_Parker", person.getMomID());
        }
    }
    @Test
    public void calculateRelationshipsPass2() {
        Person p = persons[1]; //DAVIS
        assertNull(p.getMomID());
        assertNull(p.getDadID());
        assert(p.getSpouseID().equals("Sheila_Parker"));
        for (Person person : persons) {
            assertNotEquals("Sheila_Parker", person.getMomID());
        }
    }
    @Test
    public void filterEventsGenderPass() {
        Event[] mEvents = cache.findMaleEvents();
        Event[] fEvents = cache.findFemaleEvents();
        assert(mEvents.length == 5);
        assert(fEvents.length == 6);
    }
    @Test
    public void filterEventsSidePass() {
        ArrayList<String> dadIDs;
        ArrayList<String> momIDs;

        cache.setMaternalSide(persons[0].getMomID());
        cache.setPaternalSide(persons[0].getDadID());
        dadIDs = cache.getPaternalSide();
        momIDs = cache.getMaternalSide();
        System.out.println(dadIDs.size());
        assert(dadIDs.size() == 2);
        assert(momIDs.size() == 2);
    }

    @Test
    public void sortPersonsEventsByDatePass() {
        cache.setPersonsEvents();
        Map<String, Event[]> eventMap = cache.getEventsForIndividual();
        events = eventMap.get("Sheila_Parker");
        String[] order = new String[] {"birth", "marriage", "death"};
        for (int i = 0; i < events.length; i++) {
            assert(events[i].getEventType().equals(order[i]));
        }
    }
    @Test
    public void sortPersonsEventsByDatePass2() {
        cache.setPersonsEvents();
        Map<String, Event[]> eventMap = cache.getEventsForIndividual();
        events = eventMap.get("Frank_Jones");
        String[] order = new String[] {"Caught a frog","Got sick"};
        for (int i = 0; i < events.length; i++) {
            assert(events[i].getEventType().equals(order[i]));
        }
    }

    @Test
    public void searchPeopleAndEventsPass() {
        Set<Person> personSet = new HashSet<>();
        cache.setPersonsEvents();
        Map<String, Event[]> eventMap = cache.getEventsForIndividual();
        events = eventMap.get("Betty_White");
        Person[] personArr = cache.getPersonArray();
        for (Person p: personArr) {
            if (p.getPersonID().equals("Betty_White")) {
                personSet.add(p);
            }
            else if (p.getMomID() != null) {
                if (p.getMomID().equals("Betty_White")) {
                    personSet.add(p);
                }
            }
            else if (p.getSpouseID() != null) {
                if (p.getSpouseID().equals("Betty_White")) {
                    personSet.add(p);
                }
            }

        }
        assert(events.length == 1);
        assert(personSet.size() == 2);
    }
    @Test
    public void searchPeopleAndEventsPass2() {
        Set<Person> personSet = new HashSet<>();
        cache.setPersonsEvents();
        Map<String, Event[]> eventMap = cache.getEventsForIndividual();
        events = eventMap.get("Sheila_Parker");
        Person[] personArr = cache.getPersonArray();
        for (Person p: personArr) {
            if (p.getPersonID().equals("Sheila_Parker")) {
                personSet.add(p);
            }
            else if (p.getMomID() != null) {
                if (p.getMomID().equals("Sheila_Parker")) {
                    personSet.add(p);
                }
            }
            else if (p.getSpouseID() != null) {
                if (p.getSpouseID().equals("Sheila_Parker")) {
                    personSet.add(p);
                }
            }

        }
        assert(events.length == 3);
        assert(personSet.size() == 2);
    }
}
