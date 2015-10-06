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
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by surajbhattarai on 7/26/15.
 */
public class LoginActivity extends AppCompatActivity {

    @Bind(R.id.userNameField)
    EditText mUsername;
    @Bind(R.id.passwordField)
    EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        ButterKnife.bind(this);
    }

    @OnClick(R.id.signup_text)
    public void openSignUp() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.login_btn)
    public void login() {
        String username = mUsername.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                    .setMessage(getString(R.string.login_screen_error))
                    .setTitle(getString(R.string.signup_error_title))
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            setProgressBarIndeterminateVisibility(true);
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    setProgressBarIndeterminateVisibility(false);
                    if (e==null) {
                        //Success
                        TinyApplication.updateParseInstallation(ParseUser.getCurrentUser());
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
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
