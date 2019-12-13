package com.example.familymapclient;

import com.example.familymapclient.Fragments.DataCache;
import com.example.familymapclient.Fragments.ServerProxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Model.Event;
import Model.Person;
import Model.User;

import static org.junit.Assert.*;

public class ServerProxyTest {
    private DataCache cache = DataCache.getInstance();
    private User user = null;
    @Before
    public void setUp() {
        ServerProxy.serverPort = 8080;
        ServerProxy.serverHost = "localhost";
        user = new User("sarra", "smith", "sarra@aol.com", "Sarra",
                "Smith", "f", "Sarra_Smith");
    }
    @After
    public void tearDown() {

    }
    @Test
    public void registerPass() {
        ServerProxy.register(user);
        assertTrue(cache.isLoggedIn());
    }
    @Test
    public void registerFail() {
        User newUser = new User("colin", null, "colin@aol.com", "Colin",
                "Smith", "m", "Colin_Smith");
        ServerProxy.register(newUser);
        assertFalse(cache.getRegistered());
    }
    @Test
    public void loginPass() {
        ServerProxy.login(user);
        assertTrue(cache.isLoggedIn());
    }
    @Test
    public void loginFail() {
        User newUser = new User("colin", "smith", "colin@aol.com", "Colin",
                "Smith", "m", "Colin_Smith");
        ServerProxy.login(newUser);
        assertFalse(cache.isLoggedIn());
    }
    @Test
    public void getPeoplePass() {
        ServerProxy.login(user);
        String id = "";
        Person[] personArr = cache.getPersonArray();
        for (Person p: personArr) {
            if (p.getFirstName().equals("Sarra") && p.getLastName().equals("Smith")) {
                id = p.getPersonID();
            }
        }
        Map<String, Person> personMap = cache.getPersonMap();
        assertTrue(personMap.containsKey(id));
    }
    @Test
    public void getPeopleFail() {
        ServerProxy.login(user);
        String id = "";
        Person[] personArr = cache.getPersonArray();
        for (Person p: personArr) {
            if (p.getFirstName().equals("Colin") && p.getLastName().equals("Smith")) {
                id = p.getPersonID();
            }
        }
        Map<String, Person> personMap = cache.getPersonMap();
        assertFalse(personMap.containsKey(id));
    }
    @Test
    public void getEventsPass() {
        ServerProxy.login(user);
        Event[] events = cache.getEventArray();
        assert(events.length == 91);
    }
    @Test
    public void getEventsFail() {
        ServerProxy.login(user);
        Set<Event> eventSet = new HashSet<>();
        Event[] events = cache.getEventArray();
        for (Event e: events) {
            if (e.getPersonID().equals("Colin_Smith")) {
                eventSet.add(e);
            }
        }
        assertTrue(eventSet.isEmpty());
    }
}
