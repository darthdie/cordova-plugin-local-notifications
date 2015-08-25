package de.appplant.cordova.plugin.localnotification;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import de.appplant.cordova.plugin.notification.Builder;
import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.Options;

public class MediaControlReceiver extends BroadcastReceiver {
    public static final String ACTION_PLAY = "de.appplant.action_play";
    public static final String ACTION_PAUSE = "de.appplant.action_pause";
    public static final String ACTION_REWIND = "de.appplant.action_rewind";
    public static final String ACTION_FAST_FORWARD = "de.appplant.action_fast_forward";
    public static final String ACTION_NEXT = "de.appplant.action_next";
    public static final String ACTION_PREVIOUS = "de.appplant.action_previous";
    public static final String ACTION_STOP = "de.appplant.action_stop";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle  = intent.getExtras();
        Options options;

        try {
            String data = bundle.getString("NOTIFICATION_OPTIONS");
            JSONObject dict = new JSONObject(data);

            options = new Options(context).parse(dict);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if (options == null) {
            return;
        }

        Builder builder = new Builder(options);
        Notification notification = buildNotification(builder);

        final String action = intent.getAction();

        if(action.equals(ACTION_PAUSE)) {
            LocalNotification.fireEvent("mediapause", notification);
        }
        else if(action.equals(ACTION_PLAY)) {
            LocalNotification.fireEvent("mediaplay", notification);
        }
        else if(action.equals(ACTION_REWIND)) {
            LocalNotification.fireEvent("mediarewind", notification);
        }
        else if(action.equals(ACTION_FAST_FORWARD)) {
            LocalNotification.fireEvent("mediafastforward", notification);
        }

        /*switch(action) {
            case ACTION_PAUSE:
                LocalNotification.fireEvent("mediapause", notification);
            break;

            case ACTION_PLAY:
                LocalNotification.fireEvent("mediaplay", notification);
            break;

            case ACTION_REWIND:
                LocalNotification.fireEvent("mediarewind", notification);
            break;

            case ACTION_FAST_FORWARD:
                LocalNotification.fireEvent("mediafastforward", notification);
            break;

            default:
            break;
        }*/
    }

    public Notification buildNotification (Builder builder) {
        return builder
                .setTriggerReceiver(TriggerReceiver.class)
                .setClickActivity(ClickActivity.class)
                .setClearReceiver(ClearReceiver.class)
                .build();
    }
}
