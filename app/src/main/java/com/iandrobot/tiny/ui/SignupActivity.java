package com.iandrobot.tiny.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.EditText;

import com.iandrobot.tiny.R;
import com.iandrobot.tiny.TinyApplication;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by surajbhattarai on 7/26/15.
 */
public class SignupActivity extends AppCompatActivity {

    @Bind(R.id.signup_email_textview)
    EditText mSignUpEmail;
    @Bind(R.id.signup_username_textview)
    EditText mSignupUsername;
    @Bind(R.id.signup_password_textview)
    EditText mSignupPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_signup);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        ButterKnife.bind(this);
    }

    @OnClick(R.id.cancel_btn)
    public void cancelSignUp() {
        finish();
    }

    @OnClick(R.id.signup_signup_btn)
    public void createAccount() {
        String username = mSignupUsername.getText().toString().trim();
        String password = mSignupPassword.getText().toString().trim();
        String email = mSignUpEmail.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this)
                .setMessage(getString(R.string.signup_error_message))
                .setTitle(getString(R.string.signup_error_title))
                .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            setProgressBarIndeterminateVisibility(true);
            ParseUser newUser = new ParseUser();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPassword(password);

            newUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    setProgressBarIndeterminateVisibility(false);
                    if (e==null) {
                        // Success;
                        TinyApplication.updateParseInstallation(ParseUser.getCurrentUser());
                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this)
                                .setMessage(e.getMessage())
                                .setTitle(getString(R.string.signup_error_title))
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        }
    }
}
