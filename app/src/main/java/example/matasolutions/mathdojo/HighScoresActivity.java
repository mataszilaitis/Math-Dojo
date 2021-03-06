/* CSC3095 Portfolio Part 2
 * 2019-05-07
 * Author : Matas Zilaitis
 */

package example.matasolutions.mathdojo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.flags.Flag;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


//This class is responsible for displaying high scores to the user

    public class HighScoresActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<ScoreEntry> this_score_entries;

    BottomNavigationView bottomNavigationView;

    ArrayList<Profile> profileList;

    FirebaseAuth mAuth;
    FirebaseUser user;

    LinearLayout totalPointsHeader;
    LinearLayout xpHeader;

    MaterialSpinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        totalPointsHeader = findViewById(R.id.high_scores_total_points_header);
        xpHeader = findViewById(R.id.high_scores_xp_header);


        setTitle("High Scores");

        ReadData(new MyHighScoresCallback() {
            @Override
            public void onHighScoresCallback(ArrayList<ScoreEntry> scoreEntries, ArrayList<Profile> profiles) {

                this_score_entries = scoreEntries;
                profileList = profiles;
                SetupActivity();
            }
        });



    }

    private void SetupBottomNavigationView(){

        bottomNavigationView = findViewById(R.id.high_scores_bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int menuId = menuItem.getItemId();

                switch(menuId){

                    //Displays who scored most points in a single game

                    case R.id.high_scores_navbar_most_points:
                        setTitle("High Scores:  Most points in a single game");

                        SetupMostPointsRecyclerView();

                        break;

                    //Displays who has most XP

                    case R.id.high_scores_navbar_most_XP:
                        setTitle("High Scores: Most XP");

                        SetupMostXPRecyclerView(profileList);

                        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

                               SetupMostXPRecyclerView(Filtered_XP_ByCountry_List((String) item));
                               setTitle("High Scores: Most XP - "  +(String) item);

                            }
                        });

                        break;
                }

                return false;
            }
        });

    }

    // Returns a list of profiles filtered by a given country

    public ArrayList<Profile> Filtered_XP_ByCountry_List(String country){

        ArrayList<Profile> newList = new ArrayList<>();

        if(country.equals("Global")){

            return profileList;
        }
        else{

            for(int i=0;i<profileList.size();i++){

                if(profileList.get(i).selected_country.equals(country)){

                    newList.add(profileList.get(i));

                }


            }


        }

        return newList;

    }



    public void SetupActivity(){

        SetupSpinner();

        SortHighScores();

        SetupBottomNavigationView();

        RecyclerView recyclerView = findViewById(R.id.high_scores_recycler_view);

        MyAdapter myAdapter = new MyAdapter(this_score_entries,getApplicationContext());

        LinearLayoutManager  layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(myAdapter);

    }

    public void SetupSpinner(){

        spinner = findViewById(R.id.high_scores_country_spinner);
        spinner.setItems(new Countries().countryNames);


    }




    private void SetupMostPointsRecyclerView(){

        SortHighScores_Levels();

        RecyclerView recyclerView = findViewById(R.id.high_scores_recycler_view);

        MyAdapter myAdapter = new MyAdapter(this_score_entries,getApplicationContext());

        LinearLayoutManager  layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        xpHeader.setVisibility(View.GONE);
        totalPointsHeader.setVisibility(View.VISIBLE);

        recyclerView.setAdapter(myAdapter);

    }
    private void SetupMostXPRecyclerView(ArrayList<Profile> profileList){

        RecyclerView recyclerView = findViewById(R.id.high_scores_recycler_view);

        SortHighScores_Levels();

        MyXPAdapter myXPAdapter = new MyXPAdapter(profileList,getApplicationContext());

        LinearLayoutManager  layoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(layoutManager);

        xpHeader.setVisibility(View.VISIBLE);
        totalPointsHeader.setVisibility(View.GONE);

        recyclerView.setAdapter(myXPAdapter);

    }


    //Sorts a list from highest to lowest. Will have to be refactored.

    public void SortHighScores() {

        Collections.sort(this_score_entries, new Comparator<ScoreEntry>() {
            @Override
            public int compare(ScoreEntry lhs, ScoreEntry rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending

                double p1 = lhs.score;
                double p2 = rhs.score;


                return Double.compare(p2, p1);
            }
        });

    }

        //Sorts a list from highest to lowest. Will have to be refactored.


        public void SortHighScores_Levels() {

        Collections.sort(profileList, new Comparator<Profile>() {
            @Override
            public int compare(Profile lhs, Profile rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending

                double p1 = lhs.levels.playerLevel.totalXP;
                double p2 = rhs.levels.playerLevel.totalXP;

                return Double.compare(p2, p1);
            }
        });

    }

    //Reads a list of score entries and profiles from the database

    void ReadData(final MyHighScoresCallback myCallback){

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<ScoreEntry> scoreEntriesReturned =  ConvertSnapshot(dataSnapshot.child("high_scores").child("total_points"));

                ArrayList<Profile> profileArrayListReturned = ConvertProfileSnapshot(dataSnapshot.child("user_data"));

                myCallback.onHighScoresCallback(scoreEntriesReturned,profileArrayListReturned);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //Converts database HashMap to a list

    public ArrayList<ScoreEntry> ConvertSnapshot(DataSnapshot snapshot){

        ArrayList<ScoreEntry> list = new ArrayList<>();

        for(DataSnapshot snap : snapshot.getChildren()){

            list.add(snap.getValue(ScoreEntry.class));



        }
        return list;


    }


    public ArrayList<Profile> ConvertProfileSnapshot(DataSnapshot snapshot){

        ArrayList<Profile> list = new ArrayList<>();

        for(DataSnapshot snap : snapshot.getChildren()){

            list.add(snap.getValue(Profile.class));



        }
        return list;


    }

    //Adapter for displaying a list of items

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private ArrayList<ScoreEntry> mDataset;
        private Context context;


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case

            public TextView place;
            public TextView name;
            public TextView score;
            //  public LinearLayout layout;



            public MyViewHolder(View v) {
                super(v);

                place =  v.findViewById(R.id.place);
                name =  v.findViewById(R.id.name);
                score =  v.findViewById(R.id.score);
                // layout = v.findViewById(R.id.productprice_recyclerview_layout);

            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(ArrayList<ScoreEntry> myDataset, Context context) {
            mDataset = myDataset;
            this.context = context;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View v = inflater.inflate(R.layout.high_scores_activity_recycler_view, parent, false);

            MyAdapter.MyViewHolder vh = new MyAdapter.MyViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final ScoreEntry thisEntry = mDataset.get(position);

            // Set item views based on your views and data model
            TextView place = holder.place;
            TextView name = holder.name;
            TextView score  = holder.score;

            if(position+1 < 10) {

                place.setText(" " + (position+1));
                name.setText(" " +thisEntry.name);
                score.setText(" " + String.valueOf(thisEntry.score));

            }
            else{
                place.setText(String.valueOf(position+1));
                name.setText(thisEntry.name);
                score.setText(String.valueOf(thisEntry.score));

            }

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }



    public static class MyXPAdapter extends RecyclerView.Adapter<MyXPAdapter.MyXPViewHolder> {
        private ArrayList<Profile> mDataset;
        private Context context;


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class MyXPViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case

            public TextView place;
            public TextView name;

            public TextView level;
            public TextView xp;
            //  public LinearLayout layout;



            public MyXPViewHolder(View v) {
                super(v);

                place =  v.findViewById(R.id.place);
                name =  v.findViewById(R.id.name);
                level =  v.findViewById(R.id.level);
                xp = v.findViewById(R.id.xp);
                // layout = v.findViewById(R.id.productprice_recyclerview_layout);

            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyXPAdapter(ArrayList<Profile> myDataset, Context context) {
            mDataset = myDataset;
            this.context = context;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyXPAdapter.MyXPViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View v = inflater.inflate(R.layout.high_scores_activity_xp_recycler_view, parent, false);

            MyXPAdapter.MyXPViewHolder vh = new MyXPAdapter.MyXPViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyXPAdapter.MyXPViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final Profile thisProfile = mDataset.get(position);

            final Levels thisEntry = thisProfile.levels;

            // Set item views based on your views and data model
            TextView place = holder.place;
            TextView name = holder.name;
            TextView level  = holder.level;
            TextView score  = holder.xp;




            place.setText(String.valueOf(position+1));
            name.setText(trimNumber(String.valueOf(thisProfile.username)));
            level.setText(trimNumber(String.valueOf(thisEntry.playerLevel.levelNumber)));
            score.setText(String.valueOf(thisEntry.playerLevel.totalXP));

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        private String trimNumber(String given){

            if(given.length() > 15){
                return given.substring(0,15);
            }
            else{
                return given;
            }

        }

    }

}

