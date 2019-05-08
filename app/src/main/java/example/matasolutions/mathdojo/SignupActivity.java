package example.matasolutions.mathdojo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private com.rengwuxian.materialedittext.MaterialEditText email;
    private com.rengwuxian.materialedittext.MaterialEditText username;
    private com.rengwuxian.materialedittext.MaterialEditText password;
    private com.rengwuxian.materialedittext.MaterialEditText confirm_password;

    private Button button_register;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    boolean success_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email =  findViewById(R.id.signup_email_input);
        username = findViewById(R.id.signup_username_input);
        password = findViewById(R.id.signup_password_input);
        confirm_password = findViewById(R.id.signup_password_confirm_input);
        button_register = findViewById(R.id.button_register);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if(CheckIfPasswordsMatch()){
                        RegisterUser(getApplicationContext());

                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Passwords don't match. Please try again.", Toast.LENGTH_SHORT).show();

                    }

                }
        });
        }



    private boolean CheckIfPasswordsMatch(){

        String comp_password = password.getText().toString().trim();
        String comp_confirm = confirm_password.getText().toString().trim();

        return comp_password.equals(comp_confirm);

    }


    public void RegisterUser(final Context context){
        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();
        if (TextUtils.isEmpty(Email)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Password)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        try {
                            //check if successful
                            if (task.isSuccessful()) {
                                //User is successfully registered and logged in
                                //start Profile Activity here
                                database = FirebaseDatabase.getInstance();
                                myRef = database.getReference();
                                String id = mAuth.getUid();


                                myRef.child("user_data").child(mAuth.getUid()).setValue(new Profile(mAuth.getUid(),username.getText().toString()));

                                Intent intent = new Intent(context, WelcomeBackActivity.class);

                                startActivity(intent);

                                Toast.makeText(SignupActivity.this, "Registration successful",
                                        Toast.LENGTH_LONG).show();


                            }else{
                                //Toast.makeText(SignupActivity.this, "Couldn't register, try again",Toast.LENGTH_LONG).show();
                                FirebaseAuthException e = (FirebaseAuthException)task.getException();
                                Toast.makeText(SignupActivity.this, "Failed Registration: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                success_register =false;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
    }



}