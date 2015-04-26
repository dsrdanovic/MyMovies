package etf.mymovies;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
import etf.mymovies.utility.MyCollectionListAdapter;
import etf.mymovies.utility.URLaddresses;

/**
 * Created by SrKy on 7.4.2015..
 */
public class MyCollectionFragmentTab extends Fragment {

    private ProgressDialog pDialog;
    private int User;
    private EditText txtCondition;
    private Button btnSearch;
    private int movieId;
    private TextView tvTotal, tvTop;
    private List<Movie> movieList = new ArrayList<Movie>();
    private ListView lvMyCollection;
    private MyCollectionListAdapter adapter;
    private View rootView;
    private Spinner snGenres;
    private Typeface sansationRegular;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_my_collection, container, false);

        sansationRegular = Typeface.createFromAsset(getActivity().getAssets(), Fonts.SANSATION_REGULAR);

        lvMyCollection = (ListView) rootView.findViewById(R.id.lvMyCollection);

        btnSearch = (Button) rootView.findViewById(R.id.btnSearch);
        btnSearch.setTypeface(sansationRegular);

        txtCondition = (EditText) rootView.findViewById(R.id.txtCondition);

        snGenres = (Spinner) rootView.findViewById(R.id.snGenres);

        tvTotal = (TextView) rootView.findViewById(R.id.tvTotal);
        tvTotal.setTypeface(sansationRegular);

        populateSpinner();

        User = super.getActivity().getIntent().getExtras().getInt("User");

        tvTop = (TextView) rootView.findViewById(R.id.tvTop);
        tvTop.setTypeface(sansationRegular);
        tvTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lvMyCollection.setSelectionAfterHeaderView();
            }
        });


        lvMyCollection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {


                String tv = (String) ((TextView) v.findViewById(R.id.id)).getText();
                movieId = Integer.parseInt(tv);
                Bundle bundle = new Bundle();

                bundle.putInt("Id", movieId);

                startActivity(new Intent(getActivity(), MovieDetailsActivity.class).putExtras(bundle));
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SearchMovies().execute();

            }
        });

        return rootView;
    }

    private void populateSpinner() {


        String[] genresList = new String[]{"Genre", "Action", "Adventure", "Animation", "Biography", "Comedy", "Crime", "Documentary",
                "Drama", "Family", "Fantasy", "Game-Show", "History", "Horror", "Music", "Musical", "Mystery", "Romance", "Sci-Fi", "Sport", "Thriller", "War", "Western"};

        snGenres.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, genresList));

    }


    @Override
    public void onStart() {
        super.onStart();
        txtCondition.setText("");
        snGenres.setSelection(0);

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

            String condition = txtCondition.getText().toString().trim();
            String genre = snGenres.getSelectedItem().toString().trim();
            if (genre.matches("Genre")) {
                genre = "";
            }

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("User", String.valueOf(User)));
            nameValuePairs.add(new BasicNameValuePair("Watched", String.valueOf(1)));
            nameValuePairs.add(new BasicNameValuePair("Condition", condition));
            nameValuePairs.add(new BasicNameValuePair("Genre", genre));

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

            movieList.clear();

            for (Movie movie : movies) {

                movie.setId(movie.getId());
                movie.setTitle(movie.getTitle());

                if (movie.getPoster().matches("Not Available")) {
                    movie.setPoster("https://cdn.amctheatres.com/Media/Default/Images/noposter.jpg");
                } else {
                    movie.setPoster(movie.getPoster());
                }

                String Rating = movie.getRating();
                if (Rating.length() == 1) {
                    Rating += ".0";
                }
                movie.setRating(Rating);

                String year = movie.getYear();
                if (year.length() == 5) {
                    year = year.substring(0, 4);
                }

                movie.setYear("(" + year + ")");
                movie.setGenre(movie.getGenre());
                movieList.add(movie);

            }
            adapter = new MyCollectionListAdapter(getActivity(), movieList);
            lvMyCollection.setAdapter(adapter);
            int total = lvMyCollection.getAdapter().getCount();
            tvTotal.setText("TOTAL: " + String.valueOf(total));

        }

    }

}
