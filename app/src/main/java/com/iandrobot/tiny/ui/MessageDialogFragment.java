package com.iandrobot.tiny.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iandrobot.tiny.R;

import java.util.zip.Inflater;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by surajbhattarai on 8/4/15.
 */
public class MessageDialogFragment extends DialogFragment {

    @Bind(R.id.messageArea)
    TextView mMessageBox;

    static MessageDialogFragment newInstance() {
        return new MessageDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view  = inflater.inflate(R.layout.message_dialog, container);
        ButterKnife.bind(this, view);
        getDialog().setTitle("Your Message..");
        return view;
    }

    @OnClick (R.id.send_message_btn)
    public void onSendMessage() {
        //on click
        MainActivity activity = (MainActivity) getActivity();
        activity.onMessageReturn(mMessageBox.getText().toString());
        dismiss();
    }


    public interface MessageListener {
        void onMessageReturn(String message);
    }
}
