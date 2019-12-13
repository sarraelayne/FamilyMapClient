package com.example.familymapclient.Views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.familymapclient.Fragments.DataCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import com.example.familymapclient.R;

import Model.Event;
import Model.Person;

public class SearchActivity extends AppCompatActivity{
    private DataCache cache = DataCache.getInstance();
    private SettingsModel settings = SettingsModel.getInstance();

    private SearchView searchBar;
    private RecyclerView recyclerView;
    private SearchItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        cache.setFromSettings(true);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        searchBar = findViewById(R.id.search_view);
        recyclerView = findViewById(R.id.search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cache = DataCache.getInstance();

        itemAdapter = new SearchItemAdapter(new ArrayList<SearchResult>());
        recyclerView.setAdapter(itemAdapter);

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //returns filtered search results.
                itemAdapter.setSearchResults(cache.getSearchResults(query));
                return false;
            }
            @Override
            public boolean onQueryTextChange(String text) {
                return false;
            }
        });
    }

    private class SearchHolder extends RecyclerView.ViewHolder {
        private TextView searchText;
        private ImageView imageView;
        String activityID;
        String personID;

        private SearchHolder(View view){
            super(view);
            searchText = itemView.findViewById(R.id.expandable_top_textView);
            imageView = itemView.findViewById(R.id.expandable_list_imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if(personID != null){
                        intent = PersonActivity.newIntent(SearchActivity.this, personID);
                        intent.putExtra(PersonActivity.PERSON_ACTIVITY_ID, personID);
                    }else{
                        intent = EventActivity.newIntent(SearchActivity.this, activityID);
                        intent.putExtra(EventActivity.EVENT_ACTIVITY_ID, activityID);
                    }
                    startActivity(intent);
                }
            });
        }
    }
    private class SearchItemAdapter extends RecyclerView.Adapter<SearchHolder> {
        private List<SearchResult> searchResults;

        private SearchItemAdapter(List<SearchResult> searchResults){
            this.searchResults = searchResults;
        }
        void setSearchResults(List<SearchResult> searchResults){
            this.searchResults = searchResults;
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public SearchHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(SearchActivity.this)
                    .inflate(R.layout.expandable_list_child, viewGroup, false);
            return new SearchHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull SearchHolder itemHolder, int i) {
            SearchResult result = searchResults.get(i);
            String id = result.id;

            if (result.isPerson) {
                Person p = cache.getPerson(id);
                String setString = p.getFirstName() + " " + p.getLastName() + "\n" + "Person";
                itemHolder.personID = id;
                itemHolder.searchText.setText(setString);
                if (p.getGender().equals("m")) {
                    itemHolder.imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_male));
                } else {
                    itemHolder.imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_female));
                }
            }
            else {
                itemHolder.activityID = id;

                Event e = cache.getEvents(id);
                if(e != null){
                    Person p = cache.getPerson(e.getPersonID());
                    if(p != null){
                        Event[] events;
                        if (settings.getShowingMaleEvents()) {
                            events = cache.findMaleEvents();
                            for (Event event: events) {
                                if (e.getEventID().equals(event.getEventID())) {
                                    String eventText = e.getEventType().toUpperCase() + ": " + e.getCity() +
                                            ", " + e.getCountry() + " (" + e.getYear() + ")" + "\n" +
                                            p.getFirstName() + " " + p.getLastName();
                                    itemHolder.searchText.setText(eventText);
                                    itemHolder.imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_pin));
                                }
                            }
                        }
                        if (settings.getShowingFemaleEvents()) {
                            events = cache.findFemaleEvents();
                            for (Event event: events) {
                                if (e.getEventID().equals(event.getEventID())) {
                                    String eventText = e.getEventType().toUpperCase() + ": " + e.getCity() +
                                            ", " + e.getCountry() + " (" + e.getYear() + ")" + "\n" +
                                            p.getFirstName() + " " + p.getLastName();
                                    itemHolder.searchText.setText(eventText);
                                    itemHolder.imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_pin));
                                }
                            }
                        }
                    }
                }
            }
        }
        @Override
        public int getItemCount() {
            return searchResults.size();
        }
    }
    public static class SearchResult{
        boolean isPerson;
        String id;

        public SearchResult(boolean isPerson, String id){
            this.isPerson = isPerson;
            this.id = id;
        }
    }
}
