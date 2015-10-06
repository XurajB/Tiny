package com.iandrobot.tiny.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iandrobot.tiny.adapters.UserAdapter;
import com.iandrobot.tiny.utils.FileHelper;
import com.iandrobot.tiny.utils.ParseConstants;
import com.iandrobot.tiny.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class RecipientsActivity extends AppCompatActivity {

    public static final String TAG = RecipientsActivity.class.getSimpleName();

    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    protected MenuItem mSendMenuItem;
    protected Uri mMdeiaUri;
    protected String mFileType;
    protected String mMessage=null;

    @Bind(R.id.friends_grid)
    GridView mRecipientGridView;
    @Bind(android.R.id.empty)
    TextView mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_grid);
        setupActionBar();
        ButterKnife.bind(this);

        mMdeiaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
        if (mFileType.equals(ParseConstants.TYPE_MESSAGE)) {
            mMessage = getIntent().getExtras().getString(ParseConstants.KEY_MESSAGE);
        }
        mRecipientGridView.setEmptyView(mEmptyView);
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e == null) {

                    mFriends = list;

                    String[] userNames = new String[mFriends.size()];
                    int i = 0;
                    for (ParseUser user : mFriends) {
                        userNames[i] = user.getUsername();
                        i++;
                    }
                    mRecipientGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                    if (mRecipientGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(RecipientsActivity.this, mFriends);
                        mRecipientGridView.setAdapter(adapter);
                    } else {
                        ((UserAdapter) mRecipientGridView.getAdapter()).reFill(mFriends);
                    }


                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this)
                            .setMessage(e.getMessage())
                            .setTitle(getString(R.string.signup_error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        mRecipientGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
                if (mRecipientGridView.getCheckedItemCount() > 0) {
                    mSendMenuItem.setVisible(true);
                } else {
                    mSendMenuItem.setVisible(false);
                }

                if (mRecipientGridView.isItemChecked(position)) {
                    // Add recipient
                    checkImageView.setVisibility(View.VISIBLE);
                } else {
                    // Remove recipient
                    checkImageView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);
        mSendMenuItem = menu.getItem(0); //only one item
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            ParseObject message = createMessage();
            if (message == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.error_selecting_file));
                builder.setTitle(getString(R.string.error_selecting_file_title));
                builder.setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                send(message);
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ParseObject createMessage() {

        if (mFileType.equals(ParseConstants.TYPE_MESSAGE)) {
            ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
            message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
            message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
            message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
            message.put(ParseConstants.KEY_FILE_TYPE, mFileType);
            message.put(ParseConstants.KEY_MESSAGE, mMessage);
            return message;

        } else {
            ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
            message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
            message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
            message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
            message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

            byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMdeiaUri);
            if (fileBytes == null) {
                return null;
            } else {
                if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                    fileBytes = FileHelper.reduceImageForUpload(fileBytes);
                }
                String fileName = FileHelper.getFileName(this, mMdeiaUri, mFileType);
                ParseFile file = new ParseFile(fileName, fileBytes);
                message.put(ParseConstants.KEY_FILE, file);
                return message;
            }
        }
    }

    private void send(ParseObject message) {
        // Saving to backend
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null) {
                    //success
                    Toast.makeText(RecipientsActivity.this,
                            getString(R.string.message_sent_success), Toast.LENGTH_LONG).show();
                    sendPushNotification();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(getString(R.string.error_selecting_file));
                    builder.setTitle(getString(R.string.error_sending_message));
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientsIds = new ArrayList();
        for (int i = 0; i< mRecipientGridView.getCount(); i++) {
            if (mRecipientGridView.isItemChecked(i)) {
                recipientsIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientsIds;
    }

    protected void sendPushNotification() {
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USERID, getRecipientIds());

        //send push notification
        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(getString(R.string.push_message, ParseUser.getCurrentUser().getUsername()));
        push.sendInBackground();
    }

}
