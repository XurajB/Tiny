package com.iandrobot.tiny.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.iandrobot.tiny.adapters.MessageAdapter;
import com.iandrobot.tiny.utils.ParseConstants;
import com.iandrobot.tiny.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by surajbhattarai on 7/27/15.
 */
public class InboxFragment extends ListFragment {

    protected List<ParseObject> mMessages;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);
        ButterKnife.bind(this, rootView);

        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.swipe_refresh_1, R.color.swipe_refresh_2, R.color.swipe_refresh_3, R.color.swipe_refresh_4);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        retrieveMessages();
    }

    private void retrieveMessages() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {

                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (e == null) {
                    //success
                    mMessages = messages;
                    String[] userNames = new String[mMessages.size()];
                    int i = 0;
                    for (ParseObject message : mMessages) {
                        userNames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }
                    if (getListView().getAdapter() == null) {
                        MessageAdapter adapter = new MessageAdapter(getListView().getContext(), messages);
                        setListAdapter(adapter);
                    } else {
                        //refill the adapter
                        ((MessageAdapter) getListView().getAdapter()).reFill(messages);
                    }
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);

        if (messageType.equals(ParseConstants.KEY_MESSAGE)) {
            String clientMessage = message.getString(ParseConstants.KEY_MESSAGE);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(clientMessage);
            builder.setTitle("Message From " + message.getString(ParseConstants.KEY_SENDER_NAME));
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {

            ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
            Uri fileUri = Uri.parse(file.getUrl());

            if (messageType.equals(ParseConstants.TYPE_IMAGE)) {
                // view image
                Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                intent.setData(fileUri);
                startActivity(intent);
            } else {
                // view video
                Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
                intent.setDataAndType(fileUri, "video/*");
                startActivity(intent);
            }

            //Delete it
            List<String> ids = message.getList(ParseConstants.KEY_RECIPIENT_IDS);
            if (ids.size() == 1) {
                //last recipient, delete message
                message.deleteInBackground();
            } else {
                //just delete the recipient
                ids.remove(ParseUser.getCurrentUser().getObjectId());
                ArrayList<String> idsToRemove = new ArrayList<>();
                idsToRemove.add(ParseUser.getCurrentUser().getObjectId());
                message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
                message.saveInBackground();
            }
        }
    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrieveMessages();
        }
    };
}
