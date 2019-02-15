package com.example.firebaseblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PostActivity extends AppCompatActivity {

    private ImageButton imagePost;
    private EditText titleText;
    private EditText descriptionText;
    private ProgressDialog mProgressDialog;
    private Button submit_button;
    private StorageReference storage;
    Uri imageUri = null;
    private DatabaseReference Blog;
    private DatabaseReference mRef;
    private InternetConnection internetConnection;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference newPost;

    private static final int GALLERY_INTENT = 25;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        internetConnection = new InternetConnection();

        mProgressDialog = new ProgressDialog(this);

        imagePost = findViewById(R.id.image_button);
        titleText = findViewById(R.id.title);
        descriptionText = findViewById(R.id.description);
        submit_button = findViewById(R.id.submit_button);

        storage = FirebaseStorage.getInstance().getReference();
        mRef = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        Blog = mRef.child("Blog");
        mDatabaseUsers = mRef.child("Users").child(mCurrentUser.getUid());


        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetConnection.checkConnection(getApplicationContext())) {
                    startPosting();
                } else {
                    Toast.makeText(getApplicationContext(), "Check your Internet Connection ", Toast.LENGTH_SHORT).show();
                }

            }
        });


        imagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_INTENT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {

            imageUri = data.getData();

            if (imageUri != null) {
                imagePost.setImageURI(imageUri);
            }


        }
    }

    public void startPosting() {
        // the trim() function removes all the leading and the trailing spaces in the string str.

        final String title_str = titleText.getText().toString().trim();
        final String description_str = descriptionText.getText().toString().trim();

        if (!TextUtils.isEmpty(title_str) && !TextUtils.isEmpty(description_str) && imageUri != null) {
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.show();

            StorageReference blogStorage = storage.child("BlogImages").child(imageUri.getLastPathSegment());
            blogStorage.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {


                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    newPost = Blog.push();
                                    newPost.child("title").setValue(title_str);
                                    newPost.child("description").setValue(description_str);
                                    newPost.child("image").setValue(uri.toString());
                                    newPost.child("uid").setValue(mCurrentUser.getUid());
                                    newPost.child("userName").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                startActivity(new Intent(PostActivity.this, HomeActivity.class));

                                                Toast.makeText(getApplicationContext(), "Uploaded Done !", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Uploading Failed", Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(getApplicationContext(), "Uploading Failed", Toast.LENGTH_LONG).show();

                                }
                            });


                        }
                    });

                    mProgressDialog.dismiss();

                }
            });


        } else {
            Toast.makeText(getApplicationContext(), "Empty Fields not allowed ...", Toast.LENGTH_SHORT).show();
        }


    }
}
