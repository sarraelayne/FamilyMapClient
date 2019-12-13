package com.example.familymapclient.Views;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

public class SettingsModel extends AppCompatActivity implements Serializable {

    private static SettingsModel instance;

    private boolean showLifeStoryLines = true;
    private boolean showFamilyTreeLines = true;
    private boolean showSpouseLines = true;
    private boolean showFatherSide = true;
    private boolean showMotherSide = true;
    private boolean showMaleEvents = true;
    private boolean showFemaleEvents = true;


    /////////////////SINGLETON///////////////////
    public static SettingsModel getInstance() {
        if(instance == null) {
            instance = new SettingsModel();
        }
        //setPreferences();
        return instance;
    }
    private SettingsModel() {}

    /////////////////GETTERS//////////////////
    public boolean getShowingLifeLines() {
        return showLifeStoryLines;
    }
    public boolean getShowingTreeLines() {
        return showFamilyTreeLines;
    }
    public boolean getShowingSpouseLines() {
        return showSpouseLines;
    }
    public boolean getShowingFatherSide() { return showFatherSide; }
    public boolean getShowingMotherSide() { return showMotherSide; }
    public boolean getShowingMaleEvents() { return showMaleEvents; }
    public boolean getShowingFemaleEvents() { return showFemaleEvents; }


    /////////////////SETTERS//////////////////
    void setShowLifeStoryLines(boolean showLifeStoryLines) {
        this.showLifeStoryLines = showLifeStoryLines;
    }
    void setShowFamilyTreeLines(boolean showFamilyTreeLines) {
        this.showFamilyTreeLines = showFamilyTreeLines;
    }
    void setShowSpouseLines(boolean showSpouseLines) {
        this.showSpouseLines = showSpouseLines;
    }
    void setShowFatherSide(boolean showFatherSide) {
        this.showFatherSide = showFatherSide;
    }
    void setShowMotherSide(boolean showMotherSide) {
        this.showMotherSide = showMotherSide;
    }
    void setShowMaleEvents(boolean showMaleEvents) {
        this.showMaleEvents = showMaleEvents;
    }
    void setShowFemaleEvents(boolean showFemaleEvents) {
        this.showFemaleEvents = showFemaleEvents;
    }
}
