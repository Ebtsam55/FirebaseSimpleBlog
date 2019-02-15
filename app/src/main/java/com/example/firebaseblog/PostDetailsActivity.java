package com.example.firebaseblog;

<<<<<<< HEAD
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostDetailsActivity extends AppCompatActivity {

    private String post_key =null;
    private DatabaseReference mDatabase ;
    private ImageView postImage ;
    private TextView postTitle , postDesc, postUserName;
    private Button removeButton;
    private FirebaseAuth mAuth;

=======
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class PostDetailsActivity extends AppCompatActivity {

>>>>>>> origin/master
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

<<<<<<< HEAD

        postImage = findViewById(R.id.post_image);
        postTitle = findViewById (R.id.post_title);
        postUserName = findViewById(R.id.post_username);
        postDesc=findViewById(R.id.post_desc);
        removeButton=findViewById(R.id.btn_remove);

        mAuth=FirebaseAuth.getInstance();

        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");

        post_key = getIntent().getExtras().getString("blog_id");
        // Toast.makeText(getApplicationContext(),post_key,Toast.LENGTH_SHORT).show();

        mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                String post_title = (String) dataSnapshot.child("title").getValue();
                final String post_desc = (String) dataSnapshot.child("description").getValue();
                String post_image = (String) dataSnapshot.child("image").getValue();
                final String post_uid = (String) dataSnapshot.child("uid").getValue();
                String post_username = (String) dataSnapshot.child("userName").getValue();

                postDesc.setText(post_desc);
                postTitle.setText(post_title);
                postUserName.setText(post_username);

                Picasso.with(getApplicationContext()).load(post_image).into(postImage);


                if(mAuth.getCurrentUser().getUid().equals(post_uid))
                {
                    Log.i("statuss", "they are equal");

                    removeButton.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(post_key).removeValue();
                Intent homeIntent = new Intent (PostDetailsActivity.this , HomeActivity.class);
                startActivity(homeIntent);
            }
        });


    }
}
=======
        String post_key = getIntent().getExtras().getString("blog_id");
        Toast.makeText(getApplicationContext(),post_key,Toast.LENGTH_SHORT).show();
    }
}
>>>>>>> origin/master
