package com.example.firebaseblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private DatabaseReference mRef;
    private  DatabaseReference blogRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mBlogList = findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
        blogRef = FirebaseDatabase.getInstance().getReference().child("Blog");
        blogRef.keepSynced(true);




    }


    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Blog> options =
                new FirebaseRecyclerOptions.Builder<Blog>()
                        .setQuery(blogRef, Blog.class)
                        .setLifecycleOwner(this)
                        .build();


        final  FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BlogViewHolder holder, int position, @NonNull Blog model) {

                holder.setTitle(model.getTitle());
                holder.setDescription(model.getDescription());
                holder.setImage(getBaseContext(),model.getImage());

            }

            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                return new BlogViewHolder(LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.blog_card, viewGroup, false));

            }
        };

        mBlogList.setAdapter(firebaseRecyclerAdapter);
    }



    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title) {
            TextView post_title = mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setDescription(String desc) {
            TextView post_desc = mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }

        public  void setImage (final Context ctx, final String img)
        {
            final ImageView post_image= mView.findViewById(R.id.post_image);

            // Load images from disk cache with Picasso if offline
            Picasso.with(ctx).load(img).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(img).into(post_image);
                }
            });


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.add_icon) {
            startActivity(new Intent(HomeActivity.this, PostActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
