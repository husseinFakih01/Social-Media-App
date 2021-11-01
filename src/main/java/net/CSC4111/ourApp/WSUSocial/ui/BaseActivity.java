package net.CSC4111.ourApp.WSUSocial.ui;
import android.app.ProgressDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import net.CSC4111.ourApp.WSUSocial.R;
public class BaseActivity extends AppCompatActivity{private ProgressDialog mProgressDialog;public void showProgressDialog(){if(mProgressDialog == null){mProgressDialog = new ProgressDialog(this);mProgressDialog.setCancelable(false);mProgressDialog.setMessage(getString(R.string.loading_text));}mProgressDialog.show();}public void hideProgressDialog(){if (mProgressDialog != null && mProgressDialog.isShowing()){mProgressDialog.dismiss();}}public FirebaseAuth getFirebaseAuth(){return FirebaseAuth.getInstance();}public String getUid(){return FirebaseAuth.getInstance().getCurrentUser().getUid();}}
