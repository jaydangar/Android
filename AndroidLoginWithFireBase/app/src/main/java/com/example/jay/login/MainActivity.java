package com.example.jay.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG2 = "MainActivity";
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    TextView textView;
    GoogleSignInOptions gso;
    GoogleApiClient mGoogleApiClient;
    SignInButton loginButtonGoogle;
    LoginButton loginButtonFacebook;
    FirebaseAuth mAuth;
    String GoogleDataParseUrl = "http://192.168.56.1//Upload//insertGoogle.php";
    String FacebookDataParseUrl = "http://192.168.56.1//Upload//insertFacebook.php";
    HttpPost httpPost;
    private CallbackManager callbackManager;
    private String Email, Name;

    //  For classification
    //  This will tell us, which sign in method to use.(Google or Facebook)
      boolean GoogleButtonAct = false;
      boolean FacebookButtonAct = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize your instance of callbackManager//
        callbackManager = CallbackManager.Factory.create();
        textView = (TextView) findViewById(R.id.TextView);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        loginButtonGoogle = (SignInButton) findViewById(R.id.sign_in_button);
        loginButtonFacebook = (LoginButton) findViewById(R.id.login_button);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        loginButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        if(FacebookButtonAct){
                            GoogleButtonAct = true;
                            FacebookButtonAct = false;
                        }
                        else {
                            GoogleButtonAct = true;
                        }
                        signIn();
                        break;
                }
            }
        });

        loginButtonFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                if(GoogleButtonAct){
                    GoogleButtonAct = false;
                    FacebookButtonAct = true;
                }
                else{
                    FacebookButtonAct = true;
                }

                System.out.println("onSuccess");
                String accessToken = loginResult.getAccessToken().getToken();
                Log.i("accessToken", accessToken);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("LoginActivity", response.toString());
                        // Get facebook data from login
                        try {
                            Bundle bFacebookData = getFacebookData(object);
                            Name = bFacebookData.getString("name");
                            Email = bFacebookData.getString("email");
                            Toast.makeText(MainActivity.this, "Name is : " + Name + "Email is : " + Email, Toast.LENGTH_SHORT).show();
                            SendDataToServer(Email, Name);              //  Facebook Data
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("onError");
                Log.v("LoginActivity", exception.getCause().toString());
            }
        });
    }

    private Bundle getFacebookData(JSONObject object) throws JSONException {
        Bundle bundle = new Bundle();
        String id = object.getString("id");

        bundle.putString("idFacebook", id);
        if (object.has("name"))
            bundle.putString("name", object.getString("name"));
        if (object.has("email"))
            bundle.putString("email", object.getString("email"));

        return bundle;
    }

  /*  private void SendFaceBookDataToServer(final String email, final String name) {

        class FaceBookNetwork extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String Name = name;
                String Email = email;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("name", Name));
                nameValuePairs.add(new BasicNameValuePair("email", Email));
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    httpPost = new HttpPost(FacebookDataParseUrl);
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                } catch (ClientProtocolException e) {
                } catch (IOException e) {
                }
                return "Data Submit Successfully";
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(MainActivity.this, "Data Submitted Successfully", Toast.LENGTH_LONG).show();
            }
        }
        FaceBookNetwork Fbnetworkobj = new FaceBookNetwork();
        Fbnetworkobj.execute(name, email);
    }*/

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG2, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            textView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            Name = acct.getDisplayName();
            Email = acct.getEmail();
            //updateUI(true);
            Toast.makeText(MainActivity.this, "Name is : " + Name + "Email is : " + Email, Toast.LENGTH_SHORT).show();
            SendDataToServer(Email, Name);              //  Google Data
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
    }

    private void SendDataToServer(final String email, final String name) {
        Toast.makeText(MainActivity.this, "SendDataToServer", Toast.LENGTH_SHORT).show();
        Toast.makeText(MainActivity.this, "Name is : " + Name + "Email is : " + Email, Toast.LENGTH_SHORT).show();
        class network extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                Name = name;
                Email = email;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("name", Name));
                nameValuePairs.add(new BasicNameValuePair("email", Email));
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    if(GoogleButtonAct==true){
                        httpPost = new HttpPost(GoogleDataParseUrl);
                    }
                    if(FacebookButtonAct==true){
                        httpPost = new HttpPost(FacebookDataParseUrl);
                    }
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                } catch (ClientProtocolException e) {
                } catch (IOException e) {
                }
                return "Data Submit Successfully";
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(MainActivity.this, "Data Submitted Successfully", Toast.LENGTH_LONG).show();
            }
        }
        network networkobj = new network();
        networkobj.execute(name, email);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this, "Connection failed, try again...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //updateUI(null);
    }
}