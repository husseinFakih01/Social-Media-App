package net.CSC4111.ourApp.WSUSocial.ui;
import android.content.Context;import android.media.MediaPlayer;import android.net.Uri;import android.os.Bundle;import androidx.recyclerview.widget.LinearLayoutManager;import androidx.recyclerview.widget.RecyclerView;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.EditText;import android.widget.MediaController;import android.widget.ProgressBar;import android.widget.TextView;import android.widget.Toast;import android.widget.VideoView;import com.google.firebase.database.ChildEventListener;import com.google.firebase.database.DataSnapshot;import com.google.firebase.database.DatabaseError;import com.google.firebase.database.DatabaseReference;import com.google.firebase.database.FirebaseDatabase;import com.google.firebase.database.ValueEventListener;import net.CSC4111.ourApp.WSUSocial.R;import net.CSC4111.ourApp.WSUSocial.models.Comment;import net.CSC4111.ourApp.WSUSocial.models.Post;import net.CSC4111.ourApp.WSUSocial.models.User;import java.util.ArrayList;import java.util.List;import static net.CSC4111.ourApp.WSUSocial.Const.POSTS;import static net.CSC4111.ourApp.WSUSocial.Const.USERS;
public class PostDetailActivity extends BaseActivity implements View.OnClickListener{public static final String EXTRA_POST_KEY = "post_key";private static final String POSTS_COMMENTS = "post-comments";private static final String HTTPS_TEXT = "https";private static final int REQUEST_FOCUS_DIRECTION = 0;private DatabaseReference mPostReference;private DatabaseReference mCommentsReference;private ValueEventListener mPostListener;private String mPostKey;private CommentAdapter mAdapter;private TextView mAuthorView;private TextView mTitleView;private TextView mBodyView;private EditText mCommentField;private RecyclerView mCommentsRecycler;private VideoView mVideoView;private ProgressBar mVideoLoadProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState){super.onCreate(savedInstanceState);setContentView(R.layout.activity_post_detail);mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);if(mPostKey == null){throw new IllegalArgumentException(getString(R.string.must_pass_extra_key));}initializeFirebase();findViews();}
    private void initializeFirebase(){mPostReference = FirebaseDatabase.getInstance().getReference().child(POSTS).child(mPostKey);mCommentsReference = FirebaseDatabase.getInstance().getReference().child(POSTS_COMMENTS).child(mPostKey);}
    private void findViews(){mAuthorView = (TextView) findViewById(R.id.post_author);mTitleView = (TextView) findViewById(R.id.post_title);mBodyView = (TextView) findViewById(R.id.post_body);mCommentField = (EditText) findViewById(R.id.field_comment_text);mCommentsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);mVideoView = (VideoView) findViewById(R.id.video_view);mVideoLoadProgress = (ProgressBar) findViewById(R.id.video_load_progressbar);findViewById(R.id.button_post_comment).setOnClickListener(this);mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));}
    @Override
    public void onStart(){super.onStart();ValueEventListener postListener = new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){Post post = dataSnapshot.getValue(Post.class);mAuthorView.setText(post.author);mTitleView.setText(post.title);mBodyView.setText(post.body);final String videoSource = post.body;if(videoSource != null && videoSource.contains(HTTPS_TEXT)){mVideoView.setVideoURI(Uri.parse(videoSource));mVideoView.setMediaController(new MediaController(PostDetailActivity.this));mVideoView.requestFocus(REQUEST_FOCUS_DIRECTION);mVideoLoadProgress.setVisibility(View.VISIBLE);mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
                        @Override
                        public void onPrepared(MediaPlayer mp){mVideoLoadProgress.setVisibility(View.GONE);mVideoView.start();}});}}
            @Override
            public void onCancelled(DatabaseError databaseError){Toast.makeText(PostDetailActivity.this,getString(R.string.failed_load_post) + databaseError.toException(),Toast.LENGTH_SHORT).show();}};mPostReference.addValueEventListener(postListener);mPostListener = postListener;mAdapter = new CommentAdapter(this, mCommentsReference);mCommentsRecycler.setAdapter(mAdapter);}
    @Override
    public void onStop(){super.onStop();if(mPostListener != null){mPostReference.removeEventListener(mPostListener);}mAdapter.cleanupListener();}
    @Override
    public void onClick(View v){int i = v.getId();if(i == R.id.button_post_comment){postComment();}}private void postComment(){final String uid = getUid();FirebaseDatabase.getInstance().getReference().child(USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot){User user = dataSnapshot.getValue(User.class);String authorName = user.username;String commentText = mCommentField.getText().toString();Comment comment = new Comment(uid, authorName, commentText);mCommentsReference.push().setValue(comment);mCommentField.setText(null);}
                    @Override
                    public void onCancelled(DatabaseError databaseError){Toast.makeText(PostDetailActivity.this,getString(R.string.on_cancelled_err) + databaseError.toException(),Toast.LENGTH_SHORT).show();}});}
    private static class CommentViewHolder extends RecyclerView.ViewHolder{
        public TextView authorView;
        public TextView bodyView;
        public CommentViewHolder(View itemView){super(itemView);authorView = (TextView) itemView.findViewById(R.id.comment_author);bodyView = (TextView) itemView.findViewById(R.id.comment_body);}}private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder>{private static final int WRONG_COMMENT_INDEX = -1;private static final int ONE_VALUE = 1;private Context mContext;private DatabaseReference mDatabaseReference;private ChildEventListener mChildEventListener;private List<String> mCommentIds = new ArrayList<>();private List<Comment> mComments = new ArrayList<>();
        public CommentAdapter(final Context context, DatabaseReference ref){mContext = context;mDatabaseReference = ref;ChildEventListener childEventListener = new ChildEventListener(){
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName){Comment comment = dataSnapshot.getValue(Comment.class);mCommentIds.add(dataSnapshot.getKey());mComments.add(comment);notifyItemInserted(mComments.size() - ONE_VALUE);}
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName){Comment newComment = dataSnapshot.getValue(Comment.class);String commentKey = dataSnapshot.getKey();int commentIndex = mCommentIds.indexOf(commentKey);if (commentIndex > WRONG_COMMENT_INDEX){mComments.set(commentIndex, newComment);notifyItemChanged(commentIndex);}}
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot){String commentKey = dataSnapshot.getKey();int commentIndex = mCommentIds.indexOf(commentKey);if (commentIndex > WRONG_COMMENT_INDEX){mCommentIds.remove(commentIndex);mComments.remove(commentIndex);notifyItemRemoved(commentIndex);}}
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName){Comment movedComment = dataSnapshot.getValue(Comment.class);String commentKey = dataSnapshot.getKey();}
                @Override
                public void onCancelled(DatabaseError databaseError){Toast.makeText(mContext, mContext.getString(R.string.failed_load_comments) + databaseError.toException(),Toast.LENGTH_SHORT).show();}};ref.addChildEventListener(childEventListener);mChildEventListener = childEventListener;}
        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType){LayoutInflater inflater = LayoutInflater.from(mContext);View view = inflater.inflate(R.layout.item_comment, parent, false);return new CommentViewHolder(view);}
        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position){Comment comment = mComments.get(position);holder.authorView.setText(comment.author);holder.bodyView.setText(comment.text);}
        @Override
        public int getItemCount(){return mComments.size();}
        public void cleanupListener(){if (mChildEventListener != null){mDatabaseReference.removeEventListener(mChildEventListener);}}}}
