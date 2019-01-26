package com.example.firebaseblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private ImageButton imagePost;
    private EditText titleText;
    private EditText descriptionText;
    private ProgressDialog mProgressDialog;
    private Button submit_button;
    private StorageReference storage ;
    Uri imageUri=null;
    private  DatabaseReference Blog;
    private  DatabaseReference mRef;
    private  InternetConnection internetConnection;

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
         Blog=mRef.child("Blog");


        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetConnection.checkConnection(getApplicationContext())) {
                    startPosting();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "check your Internet Connection", Toast.LENGTH_LONG).show();
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

    public  void startPosting()
    {
        // the trim() function removes all the leading and the trailing spaces in the string str.

        final String title_str = titleText.getText().toString().trim();
        final String description_str = descriptionText.getText().toString().trim();

        if (!TextUtils.isEmpty(title_str)&&!TextUtils.isEmpty(description_str)&&imageUri!=null)
        {
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.show();

            StorageReference blogStorage= storage.child("BlogImages").child(imageUri.getLastPathSegment());
            blogStorage.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            DatabaseReference newPost= Blog.push();

                            newPost.child("title").setValue(title_str);
                            newPost.child("description").setValue(description_str);
                            newPost.child("image").setValue(uri.toString());


                        }
                    });

                    mProgressDialog.dismiss();
                    startActivity(new Intent(PostActivity.this, HomeActivity.class));


                    Toast.makeText(getApplicationContext(), "Uploaded Done !",Toast.LENGTH_LONG).show();

                }
            });



        }
        else
        {
            Toast.makeText(getApplicationContext(), "Empty Fields not allowed ...",Toast.LENGTH_SHORT).show();
        }


    }
}
