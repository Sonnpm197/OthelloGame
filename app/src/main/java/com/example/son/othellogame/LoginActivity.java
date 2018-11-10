package com.example.son.othellogame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.son.othellogame.firebase.FirebaseModel;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolBar;
    private MaterialEditText loginEmail, loginPassword;
    private Button loginBtn;
    private TextView forgotPassword;
    private FirebaseModel firebaseModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseModel = new FirebaseModel(this);

        toolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginEmail = (MaterialEditText) findViewById(R.id.loginEmail);
        loginPassword = (MaterialEditText) findViewById(R.id.loginPassword);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtEmail = loginEmail.getText().toString();
                String txtPassword = loginPassword.getText().toString();

                if (TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)) {
                    Toast.makeText(LoginActivity.this, "Fields cannot be null/empty", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseModel.signInWithEmailAndPassword(txtEmail, txtPassword);
                }
            }
        });
    }

    /**
     * This function will be called in firebaseModel after login successfully
     */
    public void redirectToMainActivity() {
        Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainActivityIntent);
    }
}