package mans.abdullah.abdullah_mansour.wasalny;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {
    Button register,sign_in;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    DatabaseReference mDatabase;

    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        register = (Button) findViewById(R.id.register_btn);
        sign_in = (Button) findViewById(R.id.sign_in_btn);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        progressBar.setVisibility(View.GONE);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterDialog();
            }
        });

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInDialog();
            }
        });
    }

    @Override
    public void onBackPressed() { }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            UpdateUI();
        }
    }

    public void SignUp(final String email, final String password, final String name, final String phone)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            writeNewUser(email,password,name,phone);
                            FirebaseUser user = mAuth.getCurrentUser();
                            UpdateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "You already have an account",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            SignInDialog();
                        }
                    }
                });
    }

    public void SignIn (final String email,final String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            UpdateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Wrong email or password.",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            SignInDialog();
                        }
                    }
                });
    }

    private void writeNewUser(String email, String password, String name, String phone)
    {
        UserData user = new UserData(email, password, name, phone);

        mDatabase.child("users").child(name).setValue(user);
    }

    public void UpdateUI()
    {
        Intent n = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(n);
    }

    public void startPhoneNumberVerification(String phoneNumber)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                // Phone number to verify
                60,                              // Timeout duration
                TimeUnit.SECONDS,        // Unit of timeout
                this,                // Activity (for callback binding)
                mCallbacks);                 // OnVerificationStateChangedCallbacks
    }

    public void OnVerificationStateChanged ()
    {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                codeSent = s;
            }
        };
    }

    public PhoneAuthCredential SignIn (String code)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);

        return credential;
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(getApplicationContext(), "Sign In Success", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);

                            Intent n = new Intent(getApplicationContext(), MapsActivity.class);
                            startActivity(n);

                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void RegisterDialog()
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.sign_up_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final Button register = (Button) dialog.findViewById(R.id.register_btn2);
        final Button back = (Button) dialog.findViewById(R.id.back_btn);

        final MaterialEditText email = (MaterialEditText) dialog.findViewById(R.id.email_field);
        final MaterialEditText password = (MaterialEditText) dialog.findViewById(R.id.password_field);
        final MaterialEditText name = (MaterialEditText) dialog.findViewById(R.id.name_field);
        final MaterialEditText phone = (MaterialEditText) dialog.findViewById(R.id.phone_field);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String email_address = email.getText().toString();
                String pass = password.getText().toString();
                String username = name.getText().toString();
                String num = phone.getText().toString();

                if (email_address.length() == 0 | pass.length() == 0 | username.length() == 0 | num.length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Please enter a valid data", Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        dialog.dismiss();

                        SignUp(email_address,pass,username,num);
                    }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void SignInDialog()
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.sign_in_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final Button sign_in = (Button) dialog.findViewById(R.id.sign_in_btn2);
        final Button back = (Button) dialog.findViewById(R.id.back_btn);

        final MaterialEditText email = (MaterialEditText) dialog.findViewById(R.id.email_field);
        final MaterialEditText password = (MaterialEditText) dialog.findViewById(R.id.password_field);


        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String email_address = email.getText().toString();
                String pass = password.getText().toString();

                if (email_address.length() == 0 | pass.length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Please enter a valid data", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressBar.setVisibility(View.VISIBLE);
                    dialog.dismiss();

                    SignIn(email_address,pass);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


}
