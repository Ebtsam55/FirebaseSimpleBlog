package com.example.firebaseblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText signMail, signPass;
    private Button signInButton, signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseUsers;
    private InternetConnection internetConnection;
    private Drawable errorIcon;
    private boolean successfulLogin;
    private String emailRes,passRes;
    private ProgressDialog progressDialog;
    private SignInButton mGoogleSignInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private static  final int RC_SIGN_IN=22;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        internetConnection = new InternetConnection();
        initViews();

       //Establishing Connection with firebase..
        mDatabaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });



        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });



        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (internetConnection.checkConnection(getApplicationContext())) {

                    successfulLogin = false;
                    String signMailStr = signMail.getText().toString();
                    String signPassStr = signPass.getText().toString();
                    Log.i("statuss", signMailStr);
                    Log.i("statuss", signPassStr);


                    if (!isValidEmail(signMailStr)) {

                        signMail.setError(getString(R.string.invalidForm), errorIcon);
                        successfulLogin = false;
                    } else {//send valid data
                        Log.i("statuss", "Mail OK");
                        emailRes = signMailStr;
                        successfulLogin = true;

                    }

                    if (!isValidPassword(signPassStr)) {
                        signPass.setError(getString(R.string.passCharLess), errorIcon);
                        successfulLogin = false;
                    } else {//send valid data
                        Log.i("statuss", "Pass OK");
                        passRes = signPassStr;
                        successfulLogin = true;
                    }

                    if (!successfulLogin) {
                        Toast.makeText(getApplicationContext(), R.string.loginDataProblem, Toast.LENGTH_LONG)
                                .show();
                    } else {
                        startLogIn(emailRes, passRes);
                        Log.i("statuss", emailRes + passRes);
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "Check your Internet connection", Toast.LENGTH_LONG)
                            .show();
                }

            }
        });
    }

    private void startLogIn(String mail, String pass)
    {
        progressDialog.setMessage(getApplicationContext().getResources().getString(R.string.signInMsg));
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                  progressDialog.dismiss();
                  checkIfUserExit();
                }
                else
                {    progressDialog.dismiss();
                                     Toast.makeText(getApplicationContext(),"Log in failed ", Toast.LENGTH_LONG).show();
                }

            }
        });

    }


    // validating email id
    private boolean isValidEmail(String email) {

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // validating password with retype password
    private boolean isValidPassword(String pass) {
        return !TextUtils.isEmpty(pass) && pass.length() > 7;
    }

    //check if user exits in Database
    private void checkIfUserExit() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            final String user_id = user.getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(user_id)) {

                        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);
                    } else {

                        Intent setupIntent = new Intent(LoginActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                        Toast.makeText(getApplicationContext(), R.string.logInFailed, Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }



    // Google Sign In

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressDialog.setMessage("start signing in ....");
        progressDialog.show();

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.i("statuss", "Google sign in failed", e);
                progressDialog.dismiss();

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("statuss", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.i("statuss", "signInWithCredential:success");
                            checkIfUserExit();
                        } else {
                            // If sign in fails, display a message to the user.

                            Log.i("statuss", "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.login_layout), "Authentication Failed.", Snackbar.LENGTH_LONG).show();
                        }

                        // ...
                        progressDialog.dismiss();
                    }
                });

    }



        private void initViews() {
        signMail = findViewById(R.id.email_field);
        signPass = findViewById(R.id.pass_field);
        signInButton = findViewById(R.id.sign_in);
        signUpButton = findViewById(R.id.sign_up);
        mGoogleSignInButton =findViewById(R.id.google_signin);
        errorIcon = (Drawable) ContextCompat.getDrawable(this, R.drawable.ic_error);
        progressDialog=new ProgressDialog(this);

    }
}
