package com.example.firebaseblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText userName, signPass, signMail;
    private String usernameRes, mailRes, passRes;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private ProgressDialog progressDialog;
    private InternetConnection internetConnection;
    private boolean successfulSignUp;
    private Drawable errorIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();


        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Intent loginIntent = new Intent(getApplicationContext(), HomeActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                } else {
                    //// No user is signed in
                }


            }
        };


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetConnection.isConnected(getApplicationContext())) {


                    final String userNameStr = userName.getText().toString();
                    final String signMailStr = signMail.getText().toString();
                    final String signPassStr = signPass.getText().toString();
                    errorIcon.setBounds(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());
                    successfulSignUp = false;


                    if (!isValidUsername(userNameStr)) {
                        userName.setError(getString(R.string.invalidUsername), errorIcon);
                        successfulSignUp = false;
                    } else {
                        userName.setError(null);
                        //send valid data
                        usernameRes = userNameStr;
                        successfulSignUp = true;
                    }


                    if (!isValidEmail(signMailStr)) {

                        signMail.setError(getString(R.string.invalidForm), errorIcon);
                        successfulSignUp = false;
                    } else {//send valid data
                        mailRes = signMailStr;
                        successfulSignUp = true;
                    }

                    if (!isValidPassword(signPassStr)) {
                        signPass.setError(getString(R.string.passCharLess), errorIcon);
                        successfulSignUp = false;
                    } else {//send valid data
                        passRes = signPassStr;
                        successfulSignUp = true;
                    }

                    if (!successfulSignUp) {
                        Toast.makeText(getApplicationContext(), R.string.signUpDataProblem, Toast.LENGTH_LONG)
                                .show();
                    } else {
                        startRegister(usernameRes, mailRes, passRes);
                        //Check sent data on console
                        Log.i("statuss", usernameRes + mailRes + passRes);
                    }

                }
                else

                {
                    Toast.makeText(getApplicationContext(), R.string.internetConnectionError, Toast.LENGTH_SHORT).show();
                }


            }


        });

    }




    private void startRegister(final String usernameRes, String mailRes, String passRes) {

            progressDialog.setMessage(getApplicationContext().getResources().getString(R.string.signUpMsg));
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(mailRes, passRes).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        //set the new user in Database

                        String userID = user.getUid();
                        DatabaseReference current_user = mRef.child(userID);
                        current_user.child("name ").setValue(usernameRes);
                        current_user.child("image").setValue("Default");


                        progressDialog.dismiss();

                        Intent homeIntent = new Intent(RegisterActivity.this, HomeActivity.class);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);

                    } else {

                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), R.string.signUpFailed, Toast.LENGTH_LONG)
                                .show();

                    }

                }
            });
        }



    // validating Username
    private boolean isValidUsername(String userName) {
        return !TextUtils.isEmpty(userName) && (userName.length() > 2);
    }

    // validating email id
    private boolean isValidEmail(String email) {

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // validating password with retype password
    private boolean isValidPassword(String pass) {
        return !TextUtils.isEmpty(pass) && pass.length() > 7;
    }



    private void initViews()
    {
        progressDialog = new ProgressDialog(this);
        internetConnection = new InternetConnection();
        userName = findViewById(R.id.user_name);
        signPass= findViewById(R.id.password);
        signMail= findViewById(R.id.email);
        signUpButton = findViewById(R.id.sign_up);
        errorIcon = (Drawable) ContextCompat.getDrawable(this, R.drawable.ic_error);
    }




}
