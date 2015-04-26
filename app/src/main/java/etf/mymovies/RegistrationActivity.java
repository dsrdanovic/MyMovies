package etf.mymovies;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import etf.mymovies.utility.Fonts;
import etf.mymovies.utility.GetMD5Hash;
import etf.mymovies.utility.JSONParser;
import etf.mymovies.utility.URLaddresses;


public class RegistrationActivity extends Activity {

    private ProgressDialog pDialog;
    private EditText txtName, txtSurname, txtUsername, txtPassword, txtPasswordAgain;
    private Button btnRegister;
    private TextView tvName, tvSurname, tvUsername, tvPassword, tvPasswordAgain, tvLogin;
    private Typeface aliquamRegular, sansationRegular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        getActionBar().setDisplayShowHomeEnabled(false);

        // Loading Font Face
        aliquamRegular = Typeface.createFromAsset(getAssets(), Fonts.ALIQUAM_REGULAR);
        sansationRegular = Typeface.createFromAsset(getAssets(), Fonts.SANSATION_REGULAR);

        int titleId = getResources().getIdentifier("action_bar_title", "id",
                "android");
        TextView yourTextView = (TextView) findViewById(titleId);
        yourTextView.setTypeface(aliquamRegular);

        tvName = (TextView) findViewById(R.id.tvName);
        tvName.setTypeface(sansationRegular);

        tvSurname = (TextView) findViewById(R.id.tvSurname);
        tvSurname.setTypeface(sansationRegular);

        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvUsername.setTypeface(sansationRegular);

        tvPassword = (TextView) findViewById(R.id.tvPassword);
        tvPassword.setTypeface(sansationRegular);

        tvPasswordAgain = (TextView) findViewById(R.id.tvPasswordAgain);
        tvPasswordAgain.setTypeface(sansationRegular);

        txtName = (EditText) findViewById(R.id.txtName);
        txtName.setTypeface(sansationRegular);

        txtSurname = (EditText) findViewById(R.id.txtSurname);
        txtSurname.setTypeface(sansationRegular);

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtUsername.setTypeface(sansationRegular);

        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtPassword.setTypeface(sansationRegular);

        txtPasswordAgain = (EditText) findViewById(R.id.txtPasswordAgain);
        txtPasswordAgain.setTypeface(sansationRegular);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setTypeface(aliquamRegular);

        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvLogin.setTypeface(aliquamRegular);

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));

            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!control()) {

                    return;
                }

                new Register().execute();

            }
        });

    }

    private class Register extends AsyncTask<Void, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(RegistrationActivity.this);
            pDialog.setTitle("Contacting Server");
            pDialog.setMessage("Registration in process ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            String response = "";

            String Name = txtName.getText().toString().trim();
            String Surname = txtSurname.getText().toString().trim();
            String Username = txtUsername.getText().toString().trim();
            String Password = GetMD5Hash.md5(txtPassword.getText().toString().trim());

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("Name", Name));
            nameValuePairs.add(new BasicNameValuePair("Surname", Surname));
            nameValuePairs.add(new BasicNameValuePair("Username", Username));
            nameValuePairs.add(new BasicNameValuePair("Password", Password));

            JSONParser jsonParser = new JSONParser();

            response = jsonParser.makeHttpRequest(URLaddresses.INSERT_USER, "POST", nameValuePairs);

            return response;
        }

        protected void onPostExecute(String response) {

            pDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
            builder.setTitle("Info");
            builder.setMessage(response.trim())
                    .setIcon(R.drawable.ic_action_about)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent myIntent = new Intent(RegistrationActivity.this, LoginActivity.class);

                            RegistrationActivity.this.startActivity(myIntent);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }
    }

    private boolean control() {

        return controlName() && controlSurname() && controlUsername() && controlPassword() && controlPasswordsIfTheSame();
    }

    private boolean controlName() {
        if (txtName.getText().toString().trim().length() == 0) {

            Toast.makeText(RegistrationActivity.this, "Please enter name!", Toast.LENGTH_SHORT).show();
            txtName.requestFocus();


            return false;
        }
        return true;

    }

    private boolean controlSurname() {
        if (txtSurname.getText().toString().trim().length() == 0) {

            Toast.makeText(RegistrationActivity.this, "Please enter surname!", Toast.LENGTH_SHORT).show();
            txtSurname.requestFocus();


            return false;
        }
        return true;

    }

    private boolean controlUsername() {
        if (txtUsername.getText().toString().trim().length() == 0) {

            Toast.makeText(RegistrationActivity.this, "Please enter username!", Toast.LENGTH_SHORT).show();
            txtUsername.requestFocus();


            return false;
        }
        return true;

    }

    private boolean controlPassword() {
        if (txtPassword.getText().toString().trim().length() == 0) {

            Toast.makeText(RegistrationActivity.this, "Please enter password!", Toast.LENGTH_SHORT).show();
            txtPassword.requestFocus();


            return false;
        }
        return true;

    }

    private boolean controlPasswordsIfTheSame() {
        if (!txtPassword.getText().toString().trim().equals(txtPasswordAgain.getText().toString().trim())) {

            Toast.makeText(RegistrationActivity.this, "Passwords are not the same!", Toast.LENGTH_SHORT).show();
            txtPassword.requestFocus();


            return false;
        }
        return true;

    }

}
