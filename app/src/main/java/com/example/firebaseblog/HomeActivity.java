package com.example.firebaseblog;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private  DatabaseReference blogRef;
    private  DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseLikes;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBlogList = findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        blogRef = FirebaseDatabase.getInstance().getReference().child("Blog");
        blogRef.keepSynced(true);

        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLikes.keepSynced(true);


        mAuth=FirebaseAuth.getInstance();
        mAuthListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser()==null)
                {
                    //// No user is signed in
                    Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
                else
                {
                    // User is signed in

                }

            }
        };

       checkUserExit();

    }




    protected void onStart() {
        super.onStart();


        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerOptions<Blog> options =
                new FirebaseRecyclerOptions.Builder<Blog>()
                        .setQuery(blogRef, Blog.class)
                        .setLifecycleOwner(this)
                        .build();


        final  FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, int position, @NonNull Blog model) {
                final String post_key = getRef(position).getKey();

                holder.setTitle(model.getTitle());
                holder.setDescription(model.getDescription());
                holder.setImage(getBaseContext(),model.getImage());
                holder.setUserName(model.getUserName());

                holder.setLikeButton(post_key);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getApplicationContext(),post_key,Toast.LENGTH_SHORT).show();

                        Intent postDetailsIntent = new Intent(HomeActivity.this, PostDetailsActivity.class);
                        postDetailsIntent.putExtra("blog_id", post_key);
                        startActivity(postDetailsIntent);

                    }
                });


                holder.like_button.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Random value");
                        holder.like_button.setLiked(true);
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                        holder.like_button.setLiked(false);
                    }
                });

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
        TextView post_title;
        LikeButton like_button;
        DatabaseReference mDatabaseLikes;
        FirebaseAuth mAuth;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            like_button = mView.findViewById(R.id.fav_heart);

            post_title = mView.findViewById(R.id.post_title);

            mAuth = FirebaseAuth.getInstance();

            mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");

            mDatabaseLikes.keepSynced(true);

            post_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("statuss", "u have clicked the title ");
                }
            });
        }

        public void setLikeButton(final String post_key) {
            if (mAuth.getCurrentUser() != null) {
                mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                            like_button.setLiked(true);
                        } else {
                            like_button.setLiked(false);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

        public void setTitle(String title) {
            TextView post_title = mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setDescription(String desc) {
            TextView post_desc = mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }

        public void setUserName(String name)
        {
            TextView post_username= mView.findViewById(R.id.post_username);
            post_username.setText(name);
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

    //check if user exits in Database
    private void checkUserExit()
    {   FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null) {
            final String user_id = user.getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent setupIntent = new Intent(HomeActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

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
        else if (item.getItemId()==R.id.action_LogOut)
        {
            logOut();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut ()
    {
        mAuth.signOut();
    }

}
