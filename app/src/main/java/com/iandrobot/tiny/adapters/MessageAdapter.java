package com.iandrobot.tiny.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iandrobot.tiny.utils.ParseConstants;
import com.iandrobot.tiny.R;
import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by surajbhattarai on 7/31/15.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {

    protected Context mContext;
    protected List<ParseObject> mMessages;

    public MessageAdapter(Context context, List<ParseObject> messages) {
        super(context, R.layout.message_item, messages);
        this.mContext = context;
        this.mMessages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        ParseObject message = mMessages.get(position);
        Date createdAt = message.getCreatedAt();
        Long now = new Date().getTime();
        String convertedDate = DateUtils.getRelativeTimeSpanString(
                createdAt.getTime(), now, DateUtils.SECOND_IN_MILLIS).toString();

        if (message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
            holder.messageIcon.setImageResource(R.drawable.ic_picture);
        }
        else if (message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.KEY_MESSAGE)) {
            holder.messageIcon.setImageResource(R.drawable.ic_message);
        }
        else {
            holder.messageIcon.setImageResource(R.drawable.ic_video);
        }

        holder.senderLabel.setText(message.getString(ParseConstants.KEY_SENDER_NAME));
        holder.timeLabel.setText(convertedDate);

        return convertView;
    }

    static class ViewHolder {

        @Bind(R.id.messageIcon)
        ImageView messageIcon;
        @Bind(R.id.senderLabel)
        TextView senderLabel;
        @Bind(R.id.timeLabel)
        TextView timeLabel;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void reFill(List<ParseObject> messages) {
        mMessages.clear();
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }
}
