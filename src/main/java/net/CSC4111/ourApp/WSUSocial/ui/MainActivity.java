package net.CSC4111.ourApp.WSUSocial.ui;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import net.CSC4111.ourApp.WSUSocial.R;
import net.CSC4111.ourApp.WSUSocial.models.Post;
import net.CSC4111.ourApp.WSUSocial.viewholder.PostViewHolder;

import static net.CSC4111.ourApp.WSUSocial.Const.POSTS;
public class MainActivity extends BaseActivity {private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;private RecyclerView mPostsList;boolean isPopulateViewHolderCalled;public static final int POSTS_QUERY_LIMIT = 100;private static final int ONE_LIKE = 1;private static final String USER_POSTS = "user-posts";private static final String SHARE_INTENT_TYPE = "text/plain";private static final long HANDLER_DELAY = 5000;private FirebaseAuth.AuthStateListener mAuthListener;private FirebaseAuth mAuth;private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState){super.onCreate(savedInstanceState);setContentView(R.layout.activity_main);setUpToolbar();showProgressDialog();initializeFirebase();findViewById(R.id.fab_add_post).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){startActivity(new Intent(MainActivity.this, NewPostActivity.class));}});showPosts();}
    private void initializeFirebase(){mAuth = getFirebaseAuth();final FirebaseUser user = mAuth.getCurrentUser();mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser(); if (user == null){startActivity(new Intent(MainActivity.this, LoginActivity.class)); finish();}}};mDatabase = FirebaseDatabase.getInstance().getReference();}
    private void setUpToolbar(){Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);toolbar.setTitle(getString(R.string.app_name));setSupportActionBar(toolbar);}
    private void showPosts(){setUpRecyclerView();Query postsQuery = mDatabase.child(POSTS).limitToFirst(POSTS_QUERY_LIMIT);mDatabase.keepSynced(true);Handler handler = new Handler();handler.postDelayed(new Runnable(){
            @Override
            public void run(){hideProgressDialog();if( !isPopulateViewHolderCalled) showPostsList(false);}}, HANDLER_DELAY);FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>() .setQuery(postsQuery, Post.class) .build();mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options){
            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);return new PostViewHolder(view);}
            @Override
            public void onBindViewHolder(@NonNull PostViewHolder holder, int position){super.onBindViewHolder(holder, position);}@Override
            protected void onBindViewHolder(@NonNull PostViewHolder viewHolder, final int position, @NonNull final Post model){final DatabaseReference postRef = getRef(position);final String postKey = postRef.getKey();viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){Intent intent = new Intent(getApplicationContext(), PostDetailActivity.class);intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);startActivity(intent);}});if (model.likes.containsKey(getUid())){viewHolder.likesView.setImageResource(R.drawable.ic_toggle_star_24);}else{viewHolder.likesView.setImageResource(R.drawable.ic_toggle_star_outline_24); }viewHolder.videoThumb.setImageBitmap(null);String videoSource = model.body;if (videoSource != null && videoSource.startsWith("https://firebasestorage.googleapis.com")){Glide.with(MainActivity.this).load(videoSource) .centerCrop() .into(viewHolder.videoThumb);}viewHolder.bindToPost(model, new View.OnClickListener(){
                    @Override
                    public void onClick(View view){int id = view.getId();if(id == R.id.post_like){DatabaseReference globalPostRef = mDatabase.child(POSTS).child(postRef.getKey());DatabaseReference userPostRef = mDatabase.child(USER_POSTS).child(model.uid).child(postRef.getKey());onLikeClicked(globalPostRef);onLikeClicked(userPostRef);}else if(id == R.id.post_share){onShareClicked(model.body);}}});isPopulateViewHolderCalled = true;hideProgressDialog();showPostsList(true);}
            @Override
            public void onDataChanged(){super.onDataChanged();if (mAdapter.getItemCount() > 0) showPostsList(true);}
            @Override
            public void onError(@NonNull DatabaseError error){super.onError(error);}};mPostsList.setAdapter(mAdapter);}
    private void showPostsList(boolean showPosts){findViewById(R.id.empty_list_layout).setVisibility(showPosts ? View.GONE : View.VISIBLE);findViewById(R.id.list_layout).setVisibility(showPosts ? View.VISIBLE : View.GONE);}
    private void setUpRecyclerView(){mPostsList = (RecyclerView) findViewById(R.id.main_posts_list);mPostsList.setLayoutManager(getLinLayoutManager());}
    private LinearLayoutManager getLinLayoutManager(){LinearLayoutManager manager = new LinearLayoutManager(this);manager.setReverseLayout(true);manager.setStackFromEnd(true);return manager;}
    private void onLikeClicked(DatabaseReference postRef){postRef.runTransaction(new Transaction.Handler(){
            @Override
            public Transaction.Result doTransaction(MutableData mutableData){Post p = mutableData.getValue(Post.class);if (p == null){return Transaction.success(mutableData);}if(p.likes.containsKey(getUid())){p.likesCount = p.likesCount - ONE_LIKE;p.likes.remove(getUid());}else{p.likesCount = p.likesCount + ONE_LIKE;p.likes.put(getUid(), true);}mutableData.setValue(p);return Transaction.success(mutableData);}
            @Override
            public void onComplete(DatabaseError databaseError, boolean b,DataSnapshot dataSnapshot){}});}
    private void onShareClicked(String videoLink){Intent shareIntent = new Intent(Intent.ACTION_SEND);shareIntent.setType(SHARE_INTENT_TYPE);shareIntent.putExtra(Intent.EXTRA_TEXT, videoLink);shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_intent_subject));startActivity(Intent.createChooser(shareIntent, getString(R.string.share_intent_title)));}
    public String getUid(){if(mAuth.getCurrentUser() != null){return mAuth.getCurrentUser().getUid();}return null;}
    @Override
    public void onStart(){super.onStart();
        if (mAuthListener != null) mAuth.addAuthStateListener(mAuthListener);
        if (mAdapter != null) mAdapter.startListening();}
    @Override
    public void onStop(){super.onStop();
        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
        if (mAdapter != null) mAdapter.stopListening();}
    @Override
    public boolean onCreateOptionsMenu(Menu menu){getMenuInflater().inflate(R.menu.menu_main, menu);return true;}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {int i = item.getItemId();if (i == R.id.action_logout){mAuth.signOut(); return true;}else if (i == R.id.action_user_info){showInfoDialog(mAuth.getCurrentUser());return true;}else {return super.onOptionsItemSelected(item);}}
    private void showInfoDialog(FirebaseUser user){ String userEmailText = getString(R.string.user_email_text)+ user.getEmail();String userIdText = getString(R.string.user_id_text)+ user.getUid();String userInfo = userEmailText + "\n" + userIdText;new AlertDialog.Builder(this) .setTitle(R.string.dialog_info_title) .setMessage(userInfo) .create(). show();}}