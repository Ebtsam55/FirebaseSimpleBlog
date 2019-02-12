package com.example.firebaseblog;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class PostDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        String post_key = getIntent().getExtras().getString("blog_id");
        Toast.makeText(getApplicationContext(),post_key,Toast.LENGTH_SHORT).show();
    }
}
