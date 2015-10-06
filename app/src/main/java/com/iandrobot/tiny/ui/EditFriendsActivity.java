package com.iandrobot.tiny.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.iandrobot.tiny.adapters.UserAdapter;
import com.iandrobot.tiny.utils.ParseConstants;
import com.iandrobot.tiny.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by surajbhattarai on 7/27/15.
 */
public class EditFriendsActivity extends AppCompatActivity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    @Bind (R.id.friends_grid)
    GridView mFriendsGridView;
    @Bind(android.R.id.empty)
    TextView mEmptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid);
        setupActionBar();
        ButterKnife.bind(this);
        mFriendsGridView.setEmptyView(mEmptyTextView);
        mFriendsGridView.setOnItemClickListener(mOnItemClickListener);
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setSupportProgressBarIndeterminate(true);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                setSupportProgressBarIndeterminate(false);
                if (e==null) {
                    //Success
                    mUsers = list;
                    String[] userNames = new String[list.size()];
                    int i = 0;
                    for (ParseUser user : mUsers) {
                        userNames[i] = user.getUsername();
                        i ++;
                    }
                    mFriendsGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);

                    if (mFriendsGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(EditFriendsActivity.this, mUsers);
                        mFriendsGridView.setAdapter(adapter);
                    } else {
                        ((UserAdapter)mFriendsGridView.getAdapter()).reFill(mUsers);
                    }

                    addFriendCheckMarks();

                } else {
                    //Error
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this)
                            .setMessage(e.getMessage())
                            .setTitle(getString(R.string.signup_error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

    }

    private void addFriendCheckMarks() {
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e==null) {
                    // look for a match
                    for (int i =0; i<mUsers.size(); i++) {
                        ParseUser user = mUsers.get(i);
                        for (ParseUser friend : list) {
                            if (friend.getObjectId().equals(user.getObjectId())) {
                                mFriendsGridView.setItemChecked(i, true);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ImageView checkImageView = (ImageView)view.findViewById(R.id.checkImageView);

            if (mFriendsGridView.isItemChecked(position)) {
                // Add friend
                mFriendsRelation.add(mUsers.get(position));
                checkImageView.setVisibility(View.VISIBLE);
            } else {
                // Remove friend
                mFriendsRelation.remove(mUsers.get(position));
                checkImageView.setVisibility(View.INVISIBLE);
            }
            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        //success
                    } else {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        }
    };
}
