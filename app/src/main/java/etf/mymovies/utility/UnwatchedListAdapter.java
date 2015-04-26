package etf.mymovies.utility;

/**
 * Created by SrKy on 21.4.2015..
 */
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import etf.mymovies.model.Movie;
import etf.mymovies.R;

public class UnwatchedListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Movie> movieItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();



    public UnwatchedListAdapter(Activity activity, List<Movie> movieItems) {
        this.activity = activity;
        this.movieItems = movieItems;
    }

    @Override
    public int getCount() {
        return movieItems.size();
    }

    @Override
    public Object getItem(int location) {
        return movieItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.custom_row_unwatched, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        NetworkImageView poster = (NetworkImageView) convertView
                .findViewById(R.id.poster);
        TextView id = (TextView) convertView.findViewById(R.id.id);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView genre = (TextView) convertView.findViewById(R.id.genre);
        TextView year = (TextView) convertView.findViewById(R.id.releaseYear);

        // getting movie data for the custom_row_search_movie
        Movie m = movieItems.get(position);

        // thumbnail image

        poster.setImageUrl(m.getPoster(), imageLoader);

        id.setText(String.valueOf(m.getId()));

        // title
        title.setText(m.getTitle());

        // rating
      //  rating.setText("Rating: " + String.valueOf(m.getRating()));

        // genre

        genre.setText(m.getGenre());

        // release year
        year.setText("("+m.getYear()+")");

        return convertView;
    }

}
