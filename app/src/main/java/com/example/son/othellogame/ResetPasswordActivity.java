package com.example.son.othellogame;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.son.othellogame.firebase.FirebaseModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

public class ResetPasswordActivity extends AppCompatActivity {

    private Toolbar toolBar;
    private MaterialEditText sendEmail;
    private Button resetBtn;
    private FirebaseModel firebaseModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        firebaseModel = new FirebaseModel(this);

        toolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Reset password");
        // Show < icon to go back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sendEmail = (MaterialEditText) findViewById(R.id.sendEmail);
        resetBtn = (Button) findViewById(R.id.resetBtn);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String txtEmail = sendEmail.getText().toString();

                if (TextUtils.isEmpty(txtEmail)) {
                    Toast.makeText(ResetPasswordActivity.this, "Please fill your email", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseModel.sendPasswordResetEmail(txtEmail);
                }
            }
        });
    }

    public void redirectToLoginActivity() {
        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
    }
}
