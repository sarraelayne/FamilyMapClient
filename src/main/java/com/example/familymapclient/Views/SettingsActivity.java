package com.example.familymapclient.Views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.familymapclient.Fragments.DataCache;
import com.example.familymapclient.MainActivity;
import com.example.familymapclient.R;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Switch lifeSwitch;
        Switch treeSwitch;
        Switch spouseSwitch;
        Switch fatherSwitch;
        Switch motherSwitch;
        Switch maleSwitch;
        Switch femaleSwitch;
        Button logoutButton;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        DataCache.getInstance().setFromSettings(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        lifeSwitch = findViewById(R.id.lifeLinesSwitch);
        treeSwitch = findViewById(R.id.treeLinesSwitch);
        spouseSwitch = findViewById(R.id.spouseLinesSwitch);
        fatherSwitch = findViewById(R.id.fatherSideSwitch);
        motherSwitch = findViewById(R.id.motherSideSwitch);
        maleSwitch = findViewById(R.id.maleEventSwitch);
        femaleSwitch = findViewById(R.id.femaleEventSwitch);
        final SettingsModel mapSettings = SettingsModel.getInstance();

        lifeSwitch.setChecked(mapSettings.getShowingLifeLines()); //////Shared prefs how to use?
        lifeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                mapSettings.setShowLifeStoryLines(check);
            }
        });
        treeSwitch.setChecked(mapSettings.getShowingTreeLines());
        treeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                mapSettings.setShowFamilyTreeLines(check);
            }
        });
        spouseSwitch.setChecked(mapSettings.getShowingSpouseLines());
        spouseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                mapSettings.setShowSpouseLines(check);
            }
        });
        fatherSwitch.setChecked(mapSettings.getShowingFatherSide());
        fatherSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                mapSettings.setShowFatherSide(check);
            }
        });
        motherSwitch.setChecked(mapSettings.getShowingMotherSide());
        motherSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                mapSettings.setShowMotherSide(check);
            }
        });
        maleSwitch.setChecked(mapSettings.getShowingMaleEvents());
        maleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                mapSettings.setShowMaleEvents(check);
            }
        });
        femaleSwitch.setChecked(mapSettings.getShowingFemaleEvents());
        femaleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                mapSettings.setShowFemaleEvents(check);
            }
        });
        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogOut();
            }
        });
    }
    private void LogOut() {
        DataCache.getInstance().logOut();
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        setResult(RESULT_OK, intent);
        finish();
        Toast.makeText(getApplicationContext(), "Logging you out.", Toast.LENGTH_SHORT).show();
    }
}