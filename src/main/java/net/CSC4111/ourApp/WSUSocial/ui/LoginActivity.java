package net.CSC4111.ourApp.WSUSocial.ui;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import net.CSC4111.ourApp.WSUSocial.R;
public class LoginActivity extends AppCompatActivity{private EditText mInputEmail, mInputPassword;private FirebaseAuth mAuth;private ProgressBar mProgressBar;private Button mBtnRegister, mBtnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState){super.onCreate(savedInstanceState);initializeFirebase();setContentView(R.layout.activity_login);Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);setSupportActionBar(toolbar);findViews();setOnClickMethods();}
    private void initializeFirebase(){mAuth = FirebaseAuth.getInstance();if (mAuth.getCurrentUser() != null){startActivity(new Intent(LoginActivity.this, MainActivity.class));finish();}}
    private void findViews(){mInputEmail = (EditText) findViewById(R.id.email);mInputPassword = (EditText) findViewById(R.id.password);mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);mBtnRegister = (Button) findViewById(R.id.btn_signup);mBtnLogin = (Button) findViewById(R.id.btn_login);}private void setOnClickMethods(){mBtnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){startActivity(new Intent(LoginActivity.this, RegisterActivity.class));}});mBtnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){String email = mInputEmail.getText().toString();final String password = mInputPassword.getText().toString();if(TextUtils.isEmpty(email)){mInputEmail.setError(getString(R.string.error_toast_enter_email));Toast.makeText(getApplicationContext(), getString(R.string.error_toast_enter_email),Toast.LENGTH_SHORT).show();return;}if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mInputEmail.getText().toString()).matches()){mInputEmail.setError(getString(R.string.wrong_email_format));return;}if (TextUtils.isEmpty(password)){Toast.makeText(getApplicationContext(),getString(R.string.error_toast_enter_password),Toast.LENGTH_SHORT).show();return;}mProgressBar.setVisibility(View.VISIBLE);mBtnRegister.setEnabled(false);mBtnLogin.setEnabled(false);mAuth.signInWithEmailAndPassword(email, password) .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>(){
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task){mProgressBar.setVisibility(View.GONE);mBtnRegister.setClickable(true);mBtnLogin.setClickable(true);mBtnRegister.setEnabled(true);mBtnLogin.setEnabled(true);if (!task.isSuccessful()){if (password.length() < RegisterActivity.MIN_PASSWRD_LENGTH){mInputPassword.setError(getString(R.string.minimum_password));}else{Toast.makeText(LoginActivity.this,getString(R.string.auth_failed),Toast.LENGTH_LONG).show();}}else{Intent intent = new Intent(LoginActivity.this, MainActivity.class);startActivity(intent);finish();}}});}});}}
