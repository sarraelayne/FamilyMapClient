package com.example.familymapclient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.familymapclient.Fragments.DataCache;
import com.example.familymapclient.Fragments.LoginFragment;
import com.example.familymapclient.Fragments.MapFragment;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(DataCache.getInstance().isLoggedIn()) {
            displayMapFragment(); //If they're logged in, show their map
        }
        else {
            displayLoginFragment(); //If not, show the login page
        }
    }

    private void displayLoginFragment() {
        FragmentManager fm = this.getSupportFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.mainActivityPage);
        if (frag == null) {
            frag = new LoginFragment();
            fm.beginTransaction().add(R.id.mainActivityPage, frag).commit();
        }
        else {
            Fragment loginFragment = new LoginFragment();
            fm.beginTransaction().replace(R.id.mainActivityPage, loginFragment).commit();
        }
    }
    private void displayMapFragment() {
        FragmentManager fm = this.getSupportFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.mainActivityPage);
        if (frag == null) {
            frag = new MapFragment();
            fm.beginTransaction().add(R.id.mainActivityPage, frag).commit();
        }
        else {
            Fragment mapFragment = new MapFragment();
            fm.beginTransaction().replace(R.id.mainActivityPage, mapFragment).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == 0) {
            if (data == null) {
                return;
            }
            DataCache.getInstance().logOut();
            displayLoginFragment();
        }
    }

}