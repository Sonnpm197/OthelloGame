package com.example.son.othellogame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.son.othellogame.firebase.FirebaseModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.rengwuxian.materialedittext.MaterialEditText;

public class RegisterActivity extends AppCompatActivity {

    private MaterialEditText registerUserName, registerEmail, registerPassword;
    private Button registerBtn;
    private Toolbar toolBar;
    private FirebaseModel firebaseModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseModel = new FirebaseModel(this);

        registerUserName = (MaterialEditText) findViewById(R.id.registerUserName);
        registerEmail = (MaterialEditText) findViewById(R.id.registerEmail);
        registerPassword = (MaterialEditText) findViewById(R.id.registerPassword);
        registerBtn = (Button) findViewById(R.id.registerBtn);

        toolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtUserName = registerUserName.getText().toString();
                String txtEmail = registerEmail.getText().toString();
                String txtPassword = registerPassword.getText().toString();

                if (TextUtils.isEmpty(txtUserName) || TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)) {
                    Toast.makeText(RegisterActivity.this, "Fields cannot be null/empty", Toast.LENGTH_SHORT).show();
                } else if (txtPassword.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password's length cannot less than 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseModel.createUserWithEmailAndPassword(txtUserName, txtEmail, txtPassword);
                }
            }
        });
    }

    public void redirectToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
