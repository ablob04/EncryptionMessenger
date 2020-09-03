package com.example.messanger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    private static String Tag = "EmailPassword";

    Button Login, Logout;
    EditText Email, Password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Login = findViewById(R.id.Loginbtn);
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        Logout = findViewById(R.id.Logout);



        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth = FirebaseAuth.getInstance();
                String email = Email.getText().toString();
                String password = Password.getText().toString();
                signIn(email, password);
            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
            }
        });
    }



    private void signIn(String email,String password){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(Tag, "Sign in with Email: Success");
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    startActivity(new Intent(LoginActivity.this, ChatListActivity.class));
                }
                else{
                    Log.w(Tag, "Sign in with Email: Fail", task.getException());
                    Toast.makeText(LoginActivity.this, "Sign in Failed", Toast.LENGTH_LONG).show();
                    //UpdateUI(null);
                }

            }
        });
    }

}
