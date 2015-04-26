package etf.mymovies;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import etf.mymovies.model.User;
import etf.mymovies.utility.ConnectionDetector;
import etf.mymovies.utility.Fonts;
import etf.mymovies.utility.GetMD5Hash;
import etf.mymovies.utility.JSONParser;
import etf.mymovies.utility.SessionManager;
import etf.mymovies.utility.URLaddresses;

public class LoginActivity extends Activity {

    private ProgressDialog pDialog;
    private EditText txtUsername, txtPassword;
    private Button btnLogin;
    private JSONParser jsonParser;
    private String Username, Password, Name, Surname;
    private int User;
    private TextView tvRegister;
    private Typeface aliquamRegular, sansationRegular;

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide ActionBar icon
        getActionBar().setDisplayShowHomeEnabled(false);

        // Change ActionBar title
        //  getActionBar().setTitle("Login to your Account");

        // Loading Font Face
        aliquamRegular = Typeface.createFromAsset(getAssets(), Fonts.ALIQUAM_REGULAR);
        sansationRegular = Typeface.createFromAsset(getAssets(), Fonts.SANSATION_REGULAR);

        // Set custom font to ActionBar
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView yourTextView = (TextView) findViewById(titleId);
        yourTextView.setTypeface(aliquamRegular);

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtUsername.setTypeface(sansationRegular);

        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtPassword.setTypeface(sansationRegular);

        tvRegister = (TextView) findViewById(R.id.tvRegister);
        tvRegister.setTypeface(aliquamRegular);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setTypeface(aliquamRegular);

        cd = new ConnectionDetector(getApplicationContext());

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                // check for Internet status
                if (!isInternetPresent) {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    showAlertDialog(LoginActivity.this, "No Internet Connection",
                            "You don't have internet connection.", false);
                    return;
                }


                if (!control()) {

                    return;
                }

                new Login().execute();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));

            }
        });

    }

    class Login extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Authenticating...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String response = "";

            Username = txtUsername.getText().toString().trim();
            Password = GetMD5Hash.md5(txtPassword.getText().toString().trim());

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("Username", Username));
            nameValuePairs.add(new BasicNameValuePair("Password", Password));

            jsonParser = new JSONParser();

            response = jsonParser.makeHttpRequest(URLaddresses.LOGIN, "POST", nameValuePairs);

            return response;

        }


        protected void onPostExecute(String response) {

            pDialog.dismiss();

            Gson gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
                    .create();


            Type tipListe = new TypeToken<ArrayList<etf.mymovies.model.User>>() {
            }.getType();


            List<User> users = gson.fromJson(response, tipListe);

            if (users.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Incorrect username or password!", Toast.LENGTH_LONG).show();
                return;
            }

            for (User u : users) {

                if (u.getUsername().matches(Username) && u.getPassword().matches(Password)) {

                    User = u.getId();
                    Name = u.getName();
                    Surname = u.getSurname();

                    Bundle bundle = new Bundle();
                    bundle.putInt("User", User);
                    bundle.putString("Name", Name);
                    bundle.putString("Surname", Surname);

                    SessionManager session = new SessionManager(LoginActivity.this);
                    session.setLogin(true);

                    startActivity(new Intent(LoginActivity.this, TabsActivity.class).putExtras(bundle));

                }

            }

        }

    }

    private boolean control() {
        return controlUsername() && controlPassword();
    }

    private boolean controlUsername() {
        if (txtUsername.getText().length() == 0) {

            Toast.makeText(LoginActivity.this, "Please enter username!", Toast.LENGTH_SHORT).show();
            txtUsername.requestFocus();


            return false;
        }
        return true;

    }

    private boolean controlPassword() {
        if (txtPassword.getText().length() == 0) {

            Toast.makeText(LoginActivity.this, "Please enter password!", Toast.LENGTH_SHORT).show();
            txtPassword.requestFocus();


            return false;
        }
        return true;

    }

    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        alertDialog.setIcon((status) ? R.drawable.ic_action_accept : R.drawable.ic_action_cancel);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                final Dialog dialog = new Dialog(LoginActivity.this, R.style.DialogTheme);
                dialog.setContentView(R.layout.custom_dialog_login);

                TextView tvDescription = (TextView) dialog.findViewById(R.id.tvDescription);
                tvDescription.setTypeface(sansationRegular);

                TextView tvDeveloper = (TextView) dialog.findViewById(R.id.tvDeveloper);
                tvDeveloper.setTypeface(sansationRegular);

                TextView tvPhoneNumber = (TextView) dialog.findViewById(R.id.tvPhoneNumber);

                tvPhoneNumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:+38598323185"));
                        startActivity(callIntent);
                    }
                });

                Button dialogButton = (Button) dialog.findViewById(R.id.btnOK);
                dialogButton.setTypeface(sansationRegular);

                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

                return true;

            case R.id.action_exit:

                finish();
                System.exit(0);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
