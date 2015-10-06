package com.iandrobot.tiny;

import android.app.Application;

import com.iandrobot.tiny.ui.MainActivity;
import com.iandrobot.tiny.utils.ParseConstants;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;

/**
 * Created by surajbhattarai on 7/26/15.
 */
public class TinyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "EuOvpWoUMsIDzziDOI0ywfZAIAocApKlqb5Q5Abc", "ID4lQ8haJpuNz5v8jdHkeK9KrFK5GJOYB2piottL");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static void updateParseInstallation(ParseUser user) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USERID, user.getObjectId());
        installation.saveInBackground();
    }
}
