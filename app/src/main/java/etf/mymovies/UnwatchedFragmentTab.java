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
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import etf.mymovies.model.Movie;
import etf.mymovies.utility.Fonts;
import etf.mymovies.utility.JSONParser;
import etf.mymovies.utility.URLaddresses;
import etf.mymovies.utility.UnwatchedListAdapter;

public class UnwatchedFragmentTab extends Fragment {

    private ProgressDialog pDialog;
    private View rootView;
    private ListView lvUnwatched;
    private int User, movieId;
    private List<Movie> movieList = new ArrayList<Movie>();
    private UnwatchedListAdapter adapter;
    private RatingBar ratingBar;
    private Typeface aliquamRegular, sansationRegular;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Loading Font Face
        aliquamRegular = Typeface.createFromAsset(getActivity().getAssets(), Fonts.ALIQUAM_REGULAR);
        sansationRegular = Typeface.createFromAsset(getActivity().getAssets(), Fonts.SANSATION_REGULAR);

        rootView = inflater.inflate(R.layout.fragment_unwatched, container, false);

        lvUnwatched = (ListView) rootView.findViewById(R.id.lvUnwatched);

        User = super.getActivity().getIntent().getExtras().getInt("User");

        lvUnwatched.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {

                String tv = (String) ((TextView) v.findViewById(R.id.id)).getText();
                movieId = Integer.parseInt(tv);

                final Dialog settingsDialog = new Dialog(getActivity());

                settingsDialog.setTitle("Choose an action");
                settingsDialog.setContentView(getActivity().getLayoutInflater().inflate(R.layout.custom_dialog_unwatched
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

                        new UpdateMovie().execute();
                    }
                });

                Button btnDelete = (Button) settingsDialog.findViewById(R.id.btnDelete);
                btnDelete.setTypeface(sansationRegular);
                btnDelete.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                getActivity());

                        alertDialogBuilder
                                .setMessage("Delete movie?")
                                .setCancelable(false)
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        dialog.cancel();
                                    }


                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        settingsDialog.dismiss();
                                        deleteMovie();
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();

                        Button Yes = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Drawable drawable = getActivity().getResources().getDrawable(
                                R.drawable.ic_action_accept);

                        // set the bounds to place the drawable a bit right
                        drawable.setBounds((int) (drawable.getIntrinsicWidth() * 0.5),
                                0, (int) (drawable.getIntrinsicWidth() * 1.5),
                                drawable.getIntrinsicHeight());
                        Yes.setCompoundDrawables(drawable, null, null, null);

                        Button No = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        Drawable drawable1 = getActivity().getResources().getDrawable(
                                R.drawable.ic_action_cancel);

                        // set the bounds to place the drawable a bit right
                        drawable1.setBounds((int) (drawable1.getIntrinsicWidth() * 0.5),
                                0, (int) (drawable1.getIntrinsicWidth() * 1.5),
                                drawable1.getIntrinsicHeight());
                        No.setCompoundDrawables(drawable1, null, null, null);
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

        new SearchMovies().execute();
    }

    private class SearchMovies extends AsyncTask<Void, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setTitle("Contacting Server");
            pDialog.setMessage("Searching in database ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            String response = "";

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("User", String.valueOf(User)));
            nameValuePairs.add(new BasicNameValuePair("Watched", String.valueOf(0)));

            JSONParser jsonParser = new JSONParser();

            response = jsonParser.makeHttpRequest(URLaddresses.SELECT_MOVIE, "POST", nameValuePairs);

            return response;


        }

        protected void onPostExecute(String response) {

            pDialog.dismiss();

            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();

            Type tipListe = new TypeToken<ArrayList<Movie>>() {
            }.getType();

            List<Movie> movies = gson.fromJson(response, tipListe);
            System.out.println(response);


            movieList.clear();

            for (Movie movie : movies) {

                movie.setId(movie.getId());
                movie.setTitle(movie.getTitle());

                System.out.println(movie.getPoster());
                if (movie.getPoster().matches("Not Available")) {
                    movie.setPoster("https://cdn.amctheatres.com/Media/Default/Images/noposter.jpg");
                } else {
                    movie.setPoster(movie.getPoster());
                }

                movie.setYear(movie.getYear());
                movie.setGenre(movie.getGenre());
                movieList.add(movie);

            }
            adapter = new UnwatchedListAdapter(getActivity(), movieList);
            lvUnwatched.setAdapter(adapter);
            //  adapter.notifyDataSetChanged();

        }

    }

    private class UpdateMovie extends AsyncTask<Void, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setTitle("Contacting Server");
            pDialog.setMessage("Updating database ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            String response = "";

            Float f = ratingBar.getRating();
            String Rating = String.valueOf(f);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("Id", String.valueOf(movieId)));
            nameValuePairs.add(new BasicNameValuePair("Rating", Rating));

            JSONParser jsonParser = new JSONParser();

            response = jsonParser.makeHttpRequest(URLaddresses.UPDATE_MOVIE, "POST", nameValuePairs);

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

    private void deleteMovie() {

        String response = "";

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("Id", String.valueOf(movieId)));

        JSONParser jsonParser = new JSONParser();

        response = jsonParser.makeHttpRequest(URLaddresses.DELETE_MOVIE, "POST", nameValuePairs);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        alertDialogBuilder
                .setIcon(R.drawable.ic_action_about)
                .setTitle("Info")
                .setMessage(response.trim())
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        dialog.cancel();
                        new SearchMovies().execute();
                    }


                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


    }

}
