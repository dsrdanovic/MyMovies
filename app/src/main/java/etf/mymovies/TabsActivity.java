package etf.mymovies;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import etf.mymovies.utility.TabListener;


public class TabsActivity extends Activity {

    // Declaring our tabs and the corresponding fragments.
    ActionBar.Tab movieSearchTab, movieCollectionTab, notViewedTab;
    Fragment movieSearchFragmentTab = new SearchMovieFragmentTab();
    Fragment movieCollectionFragmentTab = new MyCollectionFragmentTab();
    Fragment notViewedFragmentTab = new UnwatchedFragmentTab();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        // Asking for the default ActionBar element that our platform supports.
        ActionBar actionBar = getActionBar();

        // Screen handling while hiding ActionBar icon.
        actionBar.setDisplayShowHomeEnabled(false);

        // Screen handling while hiding Actionbar title.
        actionBar.setDisplayShowTitleEnabled(false);

        // Creating ActionBar tabs.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Setting custom tab icons.
        movieSearchTab = actionBar.newTab().setText("SEARCH MOVIE");
        movieCollectionTab = actionBar.newTab().setText("MY COLLECTION");
        notViewedTab = actionBar.newTab().setText("UNWATCHED");

        // Setting tab listeners.
        movieSearchTab.setTabListener(new TabListener(movieSearchFragmentTab));
        movieCollectionTab.setTabListener(new TabListener(movieCollectionFragmentTab));
        notViewedTab.setTabListener(new TabListener(notViewedFragmentTab));

        // Adding tabs to the ActionBar.
        actionBar.addTab(movieSearchTab);
        actionBar.addTab(movieCollectionTab);
        actionBar.addTab(notViewedTab);

    }
}
