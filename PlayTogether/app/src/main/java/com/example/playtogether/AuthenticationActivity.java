package com.example.playtogether;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";
    EditText email_ed, password_ed;
    public ProgressDialog mProgressDialog;
    FirebaseFirestore db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication);


        email_ed = findViewById(R.id.email);
        password_ed = findViewById(R.id.password);
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnRegister).setOnClickListener(this);

        //Инициализация Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //Получение текущего пользователя и проверка
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            Toasty.success(getApplicationContext(), "Добро пожаловать " +
                    user.getEmail(), Toast.LENGTH_SHORT, false).show();

            Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //Registration
    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }

        if (password_ed.length() >= 6) {

            showProgressDialog();

            // [START create_user_with_email]
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            user = mAuth.getCurrentUser();
                            create_user();

                            Toasty.success(getApplicationContext(), "Аккаунт " +
                                    email + " создан", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toasty.error(getApplicationContext(), "Ошибка регистрации, данный email уже существует или пароль не соответствует требованиям (должен содержать строчные буквы и цифры)",
                                    Toast.LENGTH_LONG).show();

                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();

                        // [END_EXCLUDE]
                    });
        } else
            Toasty.info(getApplicationContext(), "Пароль должен содержать больше 6 символов",
                    Toast.LENGTH_SHORT).show();
        // [END create_user_with_email]
    }

    //Authorization
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        user = mAuth.getCurrentUser();

                        Toasty.success(getApplicationContext(), "Добро пожаловать " +
                                email, Toast.LENGTH_SHORT, false).show();

                        Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toasty.error(getApplicationContext(), "Логин или пароль введены не верно.",
                                Toast.LENGTH_SHORT).show();
                    }

                    // [START_EXCLUDE]
                    if (!task.isSuccessful()) {
                        //mStatusTextView.setText(R.string.auth_failed);
                    }
                    hideProgressDialog();
                    // [END_EXCLUDE]
                });
        // [END sign_in_with_email]
    }


    private boolean validateForm() {
        boolean valid = true;

        String email = email_ed.getText().toString();
        if (TextUtils.isEmpty(email)) {
            email_ed.setError("Введите email");
            valid = false;
        } else {
            email_ed.setError(null);
        }

        String password = password_ed.getText().toString();
        if (TextUtils.isEmpty(password)) {
            password_ed.setError("Введите пароль");
            valid = false;
        } else {
            password_ed.setError(null);
        }

        return valid;
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnRegister) {
            createAccount(email_ed.getText().toString(), password_ed.getText().toString());
        } else if (i == R.id.btnLogin) {
            signIn(email_ed.getText().toString(), password_ed.getText().toString());
        }
    }

    public void create_user(){
        String uid = user.getUid();
        String email = user.getEmail();

        Map<String, Object> user = new HashMap<>();
        user.put("email",email);

        db.collection("User").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });

    }
}
