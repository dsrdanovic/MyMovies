package etf.mymovies;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import etf.mymovies.model.Movie;
import etf.mymovies.utility.Fonts;
import etf.mymovies.utility.JSONParser;
import etf.mymovies.utility.URLaddresses;


public class MovieDetailsActivity extends Activity {

    private ProgressDialog pDialog;
    private int Id;
    private ImageView ivPoster;
    private ListView lvMovieDetails;
    private String Poster;
    private Button btnDelete;
    private Typeface aliquamRegular, sansationRegular;
    private TextView tvAdded, tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(R.drawable.ic_action_view_as_list);

        // Loading Font Face
        aliquamRegular = Typeface.createFromAsset(getAssets(), Fonts.ALIQUAM_REGULAR);
        sansationRegular = Typeface.createFromAsset(getAssets(), Fonts.SANSATION_REGULAR);

        // Set custom font to ActionBar
        int titleId = getResources().getIdentifier("action_bar_title", "id",
                "android");
        TextView yourTextView = (TextView) findViewById(titleId);
        yourTextView.setTypeface(aliquamRegular);

        tvAdded = (TextView) findViewById(R.id.tvAdded);
        tvAdded.setTypeface(sansationRegular);

        Id = this.getIntent().getExtras().getInt("Id");

        ivPoster = (ImageView) findViewById(R.id.ivPoster);

        lvMovieDetails = (ListView) findViewById(R.id.lvMovieDetails);

        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setTypeface(sansationRegular);
        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MovieDetailsActivity.this);

                Map<String, String> map = (Map<String, String>) lvMovieDetails.getItemAtPosition(0);
                String Title = map.get("value").toString().trim();

                alertDialogBuilder
                        .setMessage("Delete movie (" + Title + ") ?")
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }

                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                deleteMovie();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                Button Yes = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Drawable drawable = MovieDetailsActivity.this.getResources().getDrawable(
                        R.drawable.ic_action_accept);

                // set the bounds to place the drawable a bit right
                drawable.setBounds((int) (drawable.getIntrinsicWidth() * 0.5),
                        0, (int) (drawable.getIntrinsicWidth() * 1.5),
                        drawable.getIntrinsicHeight());
                Yes.setCompoundDrawables(drawable, null, null, null);

                Button No = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                Drawable drawable1 = MovieDetailsActivity.this.getResources().getDrawable(
                        R.drawable.ic_action_cancel);

                // set the bounds to place the drawable a bit right
                drawable1.setBounds((int) (drawable1.getIntrinsicWidth() * 0.5),
                        0, (int) (drawable1.getIntrinsicWidth() * 1.5),
                        drawable1.getIntrinsicHeight());
                No.setCompoundDrawables(drawable1, null, null, null);
            }
        });

        new GetMovieDetail().execute();
    }

    private class GetMovieDetail extends AsyncTask<Void, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MovieDetailsActivity.this);
            pDialog.setTitle("Contacting Server");
            pDialog.setMessage("Searching in database ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            String response = "";

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("Id", String.valueOf(Id)));

            JSONParser jsonParser = new JSONParser();

            response = jsonParser.makeHttpRequest(URLaddresses.MOVIE_DETAILS, "POST", nameValuePairs);

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


            ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
            for (Movie movie : movies) {

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

                DateFormat df = new SimpleDateFormat("dd.MM.yyyy. HH:mm");
                Date today = movie.getAdded();
                String reportDate = df.format(today);
                tvDate = (TextView) findViewById(R.id.tvDate);
                tvDate.setTypeface(sansationRegular);
                tvDate.setText(reportDate);

                SimpleAdapter mSchedule = new SimpleAdapter(MovieDetailsActivity.this, results, R.layout.custom_row_movie_details,
                        new String[]{"attribute", "value"}, new int[]{R.id.ATTRIBUTE, R.id.VALUE});
                lvMovieDetails.setAdapter(mSchedule);

                Poster = movie.getPoster();

                Picasso.with(MovieDetailsActivity.this)
                        .load(Poster)
                        .placeholder(R.drawable.ic_my_movies)
                        .error(R.drawable.ic_no_poster_available)
                        .resize(216, 319)
                        .rotate(0)
                        .into(ivPoster);

            }
        }

    }

    private void deleteMovie() {

        String response = "";

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("Id", String.valueOf(Id)));

        JSONParser jsonParser = new JSONParser();

        response = jsonParser.makeHttpRequest(URLaddresses.DELETE_MOVIE, "POST", nameValuePairs);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MovieDetailsActivity.this);

        alertDialogBuilder
                .setIcon(R.drawable.ic_action_about)
                .setTitle("Info")
                .setMessage(response.trim())
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        MovieDetailsActivity.this.finish();
                    }


                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
