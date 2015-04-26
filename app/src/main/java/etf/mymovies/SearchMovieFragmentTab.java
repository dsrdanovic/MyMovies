package etf.mymovies;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import etf.mymovies.model.Movie;
import etf.mymovies.utility.Fonts;
import etf.mymovies.utility.JSONParser;
import etf.mymovies.utility.MessureListView;
import etf.mymovies.utility.SessionManager;
import etf.mymovies.utility.URLaddresses;

/**
 * Created by SrKy on 7.4.2015..
 */
public class SearchMovieFragmentTab extends Fragment {

    private ProgressDialog pDialog;
    private List<Movie> m;
    private EditText txtCondition, txtYear;
    private ImageView ivPoster;
    private String url, Poster, Rating;
    private ListView lv;
    private Button btnSearch, btnAdd;
    private Typeface aliquamRegular, sansationRegular;
    private int User;
    private String Name, Surname;
    private RatingBar ratingBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_search_movie, container, false);

        User = super.getActivity().getIntent().getExtras().getInt("User");
        Name = super.getActivity().getIntent().getExtras().getString("Name");
        Surname = super.getActivity().getIntent().getExtras().getString("Surname");

        // Loading Font Face
        aliquamRegular = Typeface.createFromAsset(getActivity().getAssets(), Fonts.ALIQUAM_REGULAR);
        sansationRegular = Typeface.createFromAsset(getActivity().getAssets(), Fonts.SANSATION_REGULAR);

        // Session manager
        SessionManager session = new SessionManager(getActivity());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Show Welcome Toast.
            Toast.makeText(getActivity(), "Welcome " + Name + " " + Surname, Toast.LENGTH_LONG).show();
        }

        // Set session to false to prevent welcome toast again
        session.setLogin(false);

        txtCondition = (EditText) rootView.findViewById(R.id.txtCondition);
        txtCondition.setTypeface(sansationRegular);
        // Text max line
        txtCondition.setMaxLines(1);
        // Scrolling text horizontally
        txtCondition.setHorizontallyScrolling(true);

        txtYear = (EditText) rootView.findViewById(R.id.txtYear);
        txtYear.setTypeface(sansationRegular);
        // Text max line
        txtYear.setMaxLines(1);
        // Scrolling text horizontally
        txtYear.setHorizontallyScrolling(true);

        btnSearch = (Button) rootView.findViewById(R.id.search);
        btnSearch.setTypeface(sansationRegular);

        ivPoster = (ImageView) rootView.findViewById(R.id.ivPoster);
        Drawable myDrawable = getResources().getDrawable(
                R.drawable.ic_my_movies);
        ivPoster.setImageDrawable(myDrawable);

        lv = (ListView) rootView.findViewById(R.id.results);

        btnAdd = (Button) rootView.findViewById(R.id.addMovie);
        btnAdd.setTypeface(aliquamRegular);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (txtCondition.length() == 0) {
                    Toast.makeText(getActivity(), "Please enter movie name!", Toast.LENGTH_LONG).show();
                    return;
                }

                String c = txtCondition.getText().toString();
                c = c.replaceAll(" ", "+");

                String y = txtYear.getText().toString().trim();

                url = "http://www.omdbapi.com/?t=" + c + "&y=" + y + "&plot=short&r=json";

                new GetMovie().execute();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (lv.getCount() == 0) {
                    Toast.makeText(getActivity(), "Search movie first!", Toast.LENGTH_LONG).show();
                    return;
                }

                final Dialog settingsDialog = new Dialog(getActivity());
                settingsDialog.setTitle("Choose an action");
                settingsDialog.setContentView(getActivity().getLayoutInflater().inflate(R.layout.custom_dialog_search_movie
                        , null));
                settingsDialog.show();

                ratingBar = (RatingBar) settingsDialog.findViewById(R.id.ratingBar);
                LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
                stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);

                Button btnMyCollection = (Button) settingsDialog.findViewById(R.id.btnMyCollection);
                btnMyCollection.setTypeface(sansationRegular);
                btnMyCollection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (ratingBar.getRating() == 0.0) {
                            Toast.makeText(getActivity(), "Please rate movie!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        settingsDialog.dismiss();

                        new insertMovieToMyCollection().execute();
                    }
                });

                Button btnUnwatched = (Button) settingsDialog.findViewById(R.id.btnUnwatched);
                btnUnwatched.setTypeface(sansationRegular);
                btnUnwatched.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingsDialog.dismiss();
                        new insertMovieToUnwatched().execute();
                    }
                });

                Button btnBack = (Button) settingsDialog.findViewById(R.id.btnBack);
                btnBack.setTypeface(sansationRegular);
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingsDialog.dismiss();
                    }
                });


            }

        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        txtCondition.setText("");
        txtYear.setText("");
        lv.setAdapter(null);
    }

    private class GetMovie extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setTitle("Contacting Server");
            pDialog.setMessage("Searching in database ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Gson gson = new GsonBuilder().setDateFormat("dd.MM.yyyy. HH:mm:ss")
                    .create();

            JsonReader reader = new JsonReader(
                    new StringReader(
                            getJSON(url)));

            Type tipListe = new TypeToken<ArrayList<Movie>>() {
            }.getType();

            m = gson.fromJson(reader, tipListe);


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            pDialog.dismiss();

            ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
            for (Movie movie : m) {

                HashMap<String, String> map = new HashMap<String, String>();

                map.put("attribute", "Title:");
                map.put("value", movie.getTitle());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Year:");
                map.put("value", movie.getYear());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Released:");
                map.put("value", movie.getReleased());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Runtime:");
                map.put("value", movie.getRuntime());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Genre:");
                map.put("value", movie.getGenre());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Director:");
                map.put("value", movie.getDirector());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Writer:");
                map.put("value", movie.getWriter());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Actors:");
                map.put("value", movie.getActors());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Plot:");
                map.put("value", movie.getPlot());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Language:");
                map.put("value", movie.getLanguage());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Country:");
                map.put("value", movie.getCountry());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "Metascore:");
                map.put("value", movie.getMetascore());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "imdbRating:");
                map.put("value", movie.getImdbRating());
                results.add(map);

                map = new HashMap<String, String>();
                map.put("attribute", "imdbVotes:");
                map.put("value", movie.getImdbVotes());
                results.add(map);

                Poster = movie.getPoster();

                String response = movie.getResponse();

                if (response.matches("False")) {
                    Toast.makeText(getActivity(), movie.getError(), Toast.LENGTH_LONG).show();
                    return;

                }

            }

            SimpleAdapter mSchedule = new SimpleAdapter(getActivity(), results, R.layout.custom_row_search_movie,
                    new String[]{"attribute", "value"}, new int[]{R.id.ATTRIBUTE, R.id.VALUE});
            lv.setAdapter(mSchedule);

            MessureListView.setListViewHeightBasedOnItems(lv);

            Picasso.with(getActivity())
                    .load(Poster)
                    .placeholder(R.drawable.ic_my_movies)
                    .error(R.drawable.ic_no_poster_available)
                            //.resize(216,319)
                            //.centerInside()
                    .rotate(0)
                    .into(ivPoster);

        }

    }

    private String getJSON(String url) {

        try {
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();
            String r = response.toString();
            String rez = "[" + r + "]";

            rez = rez.replace("N/A", "Not Available");
            System.out.println(rez);
            return rez;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private class insertMovieToMyCollection extends AsyncTask<Void, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setTitle("Contacting Server");
            pDialog.setMessage("Adding to database ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            String response = "";

            Map<String, String> map0 = (Map<String, String>) lv.getItemAtPosition(0);
            Map<String, String> map1 = (Map<String, String>) lv.getItemAtPosition(1);
            Map<String, String> map2 = (Map<String, String>) lv.getItemAtPosition(2);
            Map<String, String> map3 = (Map<String, String>) lv.getItemAtPosition(3);
            Map<String, String> map4 = (Map<String, String>) lv.getItemAtPosition(4);
            Map<String, String> map5 = (Map<String, String>) lv.getItemAtPosition(5);
            Map<String, String> map6 = (Map<String, String>) lv.getItemAtPosition(6);
            Map<String, String> map7 = (Map<String, String>) lv.getItemAtPosition(7);
            Map<String, String> map8 = (Map<String, String>) lv.getItemAtPosition(8);
            Map<String, String> map9 = (Map<String, String>) lv.getItemAtPosition(9);
            Map<String, String> map10 = (Map<String, String>) lv.getItemAtPosition(10);

            String Title = map0.get("value").toString().trim();
            String Year = map1.get("value").toString().trim();
            String Released = map2.get("value").toString().trim();
            String Runtime = map3.get("value").toString().trim();
            String Genre = map4.get("value").toString().trim();
            String Director = map5.get("value").toString().trim();
            String Writer = map6.get("value").toString().trim();
            String Actors = map7.get("value").toString().trim();
            String Plot = map8.get("value").toString().trim();
            String Language = map9.get("value").toString().trim();
            String Country = map10.get("value").toString().trim();

            Float f = ratingBar.getRating();
            Rating = String.valueOf(f);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("Title", Title));
            nameValuePairs.add(new BasicNameValuePair("Year", Year));
            nameValuePairs.add(new BasicNameValuePair("Released", Released));
            nameValuePairs.add(new BasicNameValuePair("Runtime", Runtime));
            nameValuePairs.add(new BasicNameValuePair("Genre", Genre));
            nameValuePairs.add(new BasicNameValuePair("Director", Director));
            nameValuePairs.add(new BasicNameValuePair("Writer", Writer));
            nameValuePairs.add(new BasicNameValuePair("Actors", Actors));
            nameValuePairs.add(new BasicNameValuePair("Plot", Plot));
            nameValuePairs.add(new BasicNameValuePair("Language", Language));
            nameValuePairs.add(new BasicNameValuePair("Country", Country));
            nameValuePairs.add(new BasicNameValuePair("Rating", Rating));
            nameValuePairs.add(new BasicNameValuePair("Poster", Poster));
            nameValuePairs.add(new BasicNameValuePair("Watched", String.valueOf(1)));
            nameValuePairs.add(new BasicNameValuePair("User", String.valueOf(User)));

            JSONParser jsonParser = new JSONParser();

            response = jsonParser.makeHttpRequest(URLaddresses.INSERT_MOVIE, "POST", nameValuePairs);

            return response;
        }

        protected void onPostExecute(String response) {

            pDialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setIcon(R.drawable.ic_action_about);
            builder.setTitle("Info");
            builder.setMessage(response.trim())

                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            // Create new fragment and transaction
                            Fragment newFragment = new MyCollectionFragmentTab();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();

                            // Replace whatever is in the fragment_container view with this fragment,
                            // and add the transaction to the back stack
                            transaction.replace(R.id.activity_tabs, newFragment);
                            transaction.addToBackStack(null);

                            // Commit the transaction
                            transaction.commit();

                            getActivity().getActionBar().setSelectedNavigationItem(1);

                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();


        }
    }

    private class insertMovieToUnwatched extends AsyncTask<Void, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setTitle("Contacting Server");
            pDialog.setMessage("Adding to database ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            String response = "";

            Map<String, String> map0 = (Map<String, String>) lv.getItemAtPosition(0);
            Map<String, String> map1 = (Map<String, String>) lv.getItemAtPosition(1);
            Map<String, String> map2 = (Map<String, String>) lv.getItemAtPosition(2);
            Map<String, String> map3 = (Map<String, String>) lv.getItemAtPosition(3);
            Map<String, String> map4 = (Map<String, String>) lv.getItemAtPosition(4);
            Map<String, String> map5 = (Map<String, String>) lv.getItemAtPosition(5);
            Map<String, String> map6 = (Map<String, String>) lv.getItemAtPosition(6);
            Map<String, String> map7 = (Map<String, String>) lv.getItemAtPosition(7);
            Map<String, String> map8 = (Map<String, String>) lv.getItemAtPosition(8);
            Map<String, String> map9 = (Map<String, String>) lv.getItemAtPosition(9);
            Map<String, String> map10 = (Map<String, String>) lv.getItemAtPosition(10);

            String Title = map0.get("value").toString().trim();
            String Year = map1.get("value").toString().trim();
            String Released = map2.get("value").toString().trim();
            String Runtime = map3.get("value").toString().trim();
            String Genre = map4.get("value").toString().trim();
            String Director = map5.get("value").toString().trim();
            String Writer = map6.get("value").toString().trim();
            String Actors = map7.get("value").toString().trim();
            String Plot = map8.get("value").toString().trim();
            String Language = map9.get("value").toString().trim();
            String Country = map10.get("value").toString().trim();

            Float f = ratingBar.getRating();
            Rating = String.valueOf(f);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("Title", Title));
            nameValuePairs.add(new BasicNameValuePair("Year", Year));
            nameValuePairs.add(new BasicNameValuePair("Released", Released));
            nameValuePairs.add(new BasicNameValuePair("Runtime", Runtime));
            nameValuePairs.add(new BasicNameValuePair("Genre", Genre));
            nameValuePairs.add(new BasicNameValuePair("Director", Director));
            nameValuePairs.add(new BasicNameValuePair("Writer", Writer));
            nameValuePairs.add(new BasicNameValuePair("Actors", Actors));
            nameValuePairs.add(new BasicNameValuePair("Plot", Plot));
            nameValuePairs.add(new BasicNameValuePair("Language", Language));
            nameValuePairs.add(new BasicNameValuePair("Country", Country));
            nameValuePairs.add(new BasicNameValuePair("Rating", Rating));
            nameValuePairs.add(new BasicNameValuePair("Poster", Poster));
            nameValuePairs.add(new BasicNameValuePair("Watched", String.valueOf(0)));
            nameValuePairs.add(new BasicNameValuePair("User", String.valueOf(User)));

            JSONParser jsonParser = new JSONParser();

            response = jsonParser.makeHttpRequest(URLaddresses.INSERT_MOVIE, "POST", nameValuePairs);

            return response;
        }

        protected void onPostExecute(final String response) {

            pDialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setIcon(R.drawable.ic_action_about);
            builder.setTitle("Info");
            builder.setMessage(response.trim())

                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            if (response.trim().matches("Movie already exists in MY COLLECTION!")) {
                                // Create new fragment and transaction
                                Fragment newFragment = new MyCollectionFragmentTab();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                                // Replace whatever is in the fragment_container view with this fragment,
                                // and add the transaction to the back stack
                                transaction.replace(R.id.activity_tabs, newFragment);
                                transaction.addToBackStack(null);

                                // Commit the transaction
                                transaction.commit();

                                getActivity().getActionBar().setSelectedNavigationItem(1);
                            } else {
                                // Create new fragment and transaction
                                Fragment newFragment = new UnwatchedFragmentTab();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                                // Replace whatever is in the fragment_container view with this fragment,
                                // and add the transaction to the back stack
                                transaction.replace(R.id.activity_tabs, newFragment);
                                transaction.addToBackStack(null);

                                // Commit the transaction
                                transaction.commit();

                                getActivity().getActionBar().setSelectedNavigationItem(2);
                            }


                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();


        }
    }

}
