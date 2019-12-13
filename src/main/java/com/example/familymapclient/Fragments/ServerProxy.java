package com.example.familymapclient.Fragments;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import Model.Event;
import Model.Person;
import Model.User;
import Results.EventResult;
import Results.LoginResult;
import Results.PersonResult;
import Results.RegisterResult;

public class ServerProxy {

    private static ServerProxy instance;
    public static String serverHost;
    public static int serverPort;
    boolean registeredUser = false;

    static ServerProxy getInstance() {
        if(instance == null) {
            instance = new ServerProxy();
        }
        return instance;
    }
    private ServerProxy() {}

    void setHost(String host) {
        serverHost = host;
    }
    void setPort(int port) {
        serverPort = port;
    }

    public boolean getRegistered() {
        return registeredUser;
    }
    public static Object[] login(User user) {
        try {
            //FIND CONNECTION
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/login");
            DataCache dc = DataCache.getInstance();
            HttpURLConnection urlConn = openPOSTConnection(url);

            //SEND DATA
            Gson gson = new Gson();
            String requestData = gson.toJson(user);
            OutputStream requestBody = urlConn.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(requestBody);
            writer.write(requestData);
            writer.flush();
            writer.close();
            requestBody.close();
            urlConn.connect();

            if (urlConn.getResponseCode() == 200){
                InputStream responseBody = urlConn.getInputStream();

                String responseData = readString(responseBody);
                LoginResult result = gson.fromJson(responseData, LoginResult.class);

                if(result.getAuthToken()== null) {
                    return new Object[]{false, result.getResult()};
                }
                System.out.println("getData");
                if(getData(result.getAuthToken(), result.getPersonID())) {
                    Person person = dc.getPerson(result.getPersonID());
                    dc.setUser(person);
                    return new Object[]{true, "Welcome back " + person.getFirstName() + " " +
                            person.getLastName() + "!"};
                }
                else {
                    return new Object[]{false, "Error: Data could not be retrieved - " + urlConn.getResponseMessage()};
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return new Object[] {false, "We could not log you in. Sorry."};
        }
        return new Object[]{false, "We could not log you in. Sorry."};
    }
    public static Object[] register(User user) {
        HttpURLConnection urlConn = null;
        DataCache dc = DataCache.getInstance();
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/register");
            urlConn = openPOSTConnection(url);

            Gson gson = new Gson();
            String requestData = gson.toJson(user);
            OutputStream requestBody = urlConn.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(requestBody);
            writer.write(requestData);
            writer.flush();
            writer.close();
            requestBody.close();
            urlConn.connect();

            if(urlConn.getResponseCode() == 200) {
                InputStream responseBody = urlConn.getInputStream();
                String responseData = readString(responseBody);
                responseBody.close();

                RegisterResult result = gson.fromJson(responseData, RegisterResult.class);
                if (result.getAuthToken() == null) {
                    return new Object[]{false, result.getResult()};
                }
                if(getData(result.getAuthToken(), result.getPersonID())) {
                    Person person = dc.getPerson(result.getPersonID());
                    dc.setUser(person);
                    dc.setRegistered(true);
                    return new Object[]{true, "Welcome " + person.getFirstName() + " " +
                            person.getLastName() + "!"};
                }
                else {
                    return new Object[]{false, "Error: " + urlConn.getResponseMessage()};
                }
            }
            else {
                return new Object[]{false, urlConn.getResponseMessage()};
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return new Object[] {false, "Couldn't register. Sorry."};
        }
        finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }
    }
    private static boolean getData(String authToken, String personID) {
        Person[] persons;
        Event[] events;

        HttpURLConnection urlConn = null;
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/person");
            urlConn = openGETConnection(url, authToken);
            urlConn.connect();

            if (urlConn.getResponseCode() == 200) {
                InputStream responseBody = urlConn.getInputStream();
                String responseData = readString(responseBody);
                responseBody.close();
                Gson gson =  new GsonBuilder().create();
                PersonResult personsRes = gson.fromJson(responseData, PersonResult.class);
                persons = personsRes.getPerson();
            }
            else {
                return false;
            }
            urlConn.disconnect();

            url = new URL("http://" + serverHost + ":" + serverPort + "/event");
            urlConn = openGETConnection(url, authToken);
            urlConn.connect();

            if (urlConn.getResponseCode() == 200) {
                InputStream responseBody = urlConn.getInputStream();
                String responseData = readString(responseBody);
                responseBody.close();

                Gson gson = new GsonBuilder().create();
                EventResult eventRes = gson.fromJson(responseData, EventResult.class);
                events = eventRes.getEvent();
            }
            else {
                return false;
            }

            DataCache dataCache = DataCache.getInstance();
            dataCache.setData(serverHost,serverPort,persons,events);
            dataCache.getPerson(personID);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(urlConn != null) {
                urlConn.disconnect();
            }
        }
        return false;
    }
    private static String readString(InputStream input) throws IOException{
        StringBuilder sb = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(input);
        char[] buffer = new char[1024];
        int length;
        while ((length = reader.read(buffer)) > 0) {
            sb.append(buffer, 0, length);
        }
        return sb.toString();
    }
    private static HttpURLConnection openGETConnection(URL url, String authToken) {
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(false);
            conn.addRequestProperty("Authorization", authToken);
            conn.addRequestProperty("Accept", "application/json");
            return conn;
        }
        catch (IOException e) {
            System.out.println("Error opening GET Connection: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    private static HttpURLConnection openPOSTConnection(URL url) {
        try {
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setDoOutput(true);
            urlConn.addRequestProperty("Accept", "application/json");
            return urlConn;
        }
        catch (IOException e) {
            System.out.println("Error opening POST connection: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
