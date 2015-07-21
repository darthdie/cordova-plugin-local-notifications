/*
 * Copyright (c) 2013-2015 by appPlant UG. All rights reserved.
 *
 * @APPPLANT_LICENSE_HEADER_START@
 *
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apache License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://opensource.org/licenses/Apache-2.0/ and read it before using this
 * file.
 *
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 *
 * @APPPLANT_LICENSE_HEADER_END@
 */

package de.appplant.cordova.plugin.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.media.session.MediaSessionCompat.Callback;

import de.appplant.cordova.plugin.localnotification.MediaControlReceiver;

import android.R;

import org.json.JSONObject;

import java.util.Random;

/**
 * Builder class for local notifications. Build fully configured local
 * notification specified by JSON object passed from JS side.
 */
public class Builder {

    public static final String ACTION_PLAY = "de.appplant.action_play";
    public static final String ACTION_PAUSE = "de.appplant.action_pause";
    public static final String ACTION_REWIND = "de.appplant.action_rewind";
    public static final String ACTION_FAST_FORWARD = "de.appplant.action_fast_foward";
    public static final String ACTION_NEXT = "de.appplant.action_next";
    public static final String ACTION_PREVIOUS = "de.appplant.action_previous";
    public static final String ACTION_STOP = "de.appplant.action_stop";

    // Application context passed by constructor
    private final Context context;

    // Notification options passed by JS
    private final Options options;

    // Receiver to handle the trigger event
    private Class<?> triggerReceiver;

    // Receiver to handle the clear event
    private Class<?> clearReceiver = ClearReceiver.class;

    // Activity to handle the click event
    private Class<?> clickActivity = ClickActivity.class;

    private MediaSessionCompat mSession;

    /**
     * Constructor
     *
     * @param context
     *      Application context
     * @param options
     *      Notification options
     */
    public Builder(Context context, JSONObject options) {
        this.context = context;
        this.options = new Options(context).parse(options);
    }

    /**
     * Constructor
     *
     * @param options
     *      Notification options
     */
    public Builder(Options options) {
        this.context = options.getContext();
        this.options = options;
    }

    /**
     * Set trigger receiver.
     *
     * @param receiver
     *      Broadcast receiver
     */
    public Builder setTriggerReceiver(Class<?> receiver) {
        this.triggerReceiver = receiver;
        return this;
    }

    /**
     * Set clear receiver.
     *
     * @param receiver
     *      Broadcast receiver
     */
    public Builder setClearReceiver(Class<?> receiver) {
        this.clearReceiver = receiver;
        return this;
    }

    /**
     * Set click activity.
     *
     * @param activity
     *      Activity
     */
    public Builder setClickActivity(Class<?> activity) {
        this.clickActivity = activity;
        return this;
    }

    public static Notification update(Options options, Notification notification) {
        Uri sound = options.getSoundUri();
        NotificationCompat.BigTextStyle style;

        style = new NotificationCompat.BigTextStyle()
                .bigText(options.getText());

        NotificationCompat.Builder builder = notification.getBuilder();

        builder
            .setDefaults(0)
            .setContentTitle(options.getTitle())
            .setContentText(options.getText())
            .setNumber(options.getBadgeNumber())
            .setTicker(options.getText())
            .setSmallIcon(options.getSmallIcon())
            .setLargeIcon(options.getIconBitmap())
            .setAutoCancel(options.isAutoClear())
            .setOngoing(options.isOngoing())
            .setOnlyAlertOnce(options.isAlertOnlyOnce())
            .setStyle(style);


        String type = options.getType();

        if(type.equals("download")) {
            builder.setProgress(100, options.getProgress(), false);
        }
        else {
            builder.setLights(options.getLedColor(), 500, 500);
        }

        if (sound != null) {
            builder.setSound(sound);
        }

        //applyDeleteReceiver(builder);
        //applyContentReceiver(builder);

        return new Notification(notification.getContext(), options, builder, notification.getReceiver());
    }

    /**
     * Creates the notification with all its options passed through JS.
     */
    public Notification build() {
        Uri sound = options.getSoundUri();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setDefaults(0)
                .setContentTitle(options.getTitle())
                .setContentText(options.getText())
                .setNumber(options.getBadgeNumber())
                .setTicker(options.getText())
                .setSmallIcon(options.getSmallIcon())
                .setLargeIcon(options.getIconBitmap())
                .setAutoCancel(options.isAutoClear())
                .setOngoing(options.isOngoing())
                .setOnlyAlertOnce(options.isAlertOnlyOnce());

        String type = options.getType();

        if(type.equals("download")) {
            builder
                .setProgress(100, options.getProgress(), false)
                .setTicker(null);
        }
        else if(type.equals("media")) {
            /*PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY);
            stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);

            mSession = new MediaSessionCompat(context, "test");
            mSession.setPlaybackState(stateBuilder.build());
            mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

            mSession.setActive(true);

            style.setMediaSession(mSession.getSessionToken());*/
            builder
                .addAction(generateAction(android.R.drawable.ic_media_rew, "Rewind", ACTION_REWIND))
                .addAction(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY))
                .addAction(generateAction(android.R.drawable.ic_media_ff, "Fast Foward", ACTION_FAST_FORWARD))
                .setStyle(new android.support.v7.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1))
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }
        else {
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle().bigText(options.getText());
            
            builder
                .setStyle(style)
                .setLights(options.getLedColor(), 500, 500);
        }

        if (sound != null) {
            builder.setSound(sound);
        }

        applyDeleteReceiver(builder);
        applyContentReceiver(builder);

        return new Notification(context, options, builder, triggerReceiver);
    }

    private NotificationCompat.Action generateAction( int icon, String title, String intentAction ) {
        Intent intent = new Intent(context, MediaControlReceiver.class);
        intent.setAction(intentAction);
        intent.putExtra(Options.EXTRA, options.toString());
        //PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, 0);
        // (Context context, int requestCode, Intent intent, int flags)
        int requestCode = new Random().nextInt();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    /**
     * Set intent to handle the delete event. Will clean up some persisted
     * preferences.
     *
     * @param builder
     *      Local notification builder instance
     */
    private void applyDeleteReceiver(NotificationCompat.Builder builder) {

        if (clearReceiver == null)
            return;

        Intent deleteIntent = new Intent(context, clearReceiver)
                .setAction(options.getIdStr())
                .putExtra(Options.EXTRA, options.toString());

        PendingIntent dpi = PendingIntent.getBroadcast(
                context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setDeleteIntent(dpi);
    }

    /**
     * Set intent to handle the click event. Will bring the app to
     * foreground.
     *
     * @param builder
     *      Local notification builder instance
     */
    private void applyContentReceiver(NotificationCompat.Builder builder) {

        if (clickActivity == null)
            return;

        Intent intent = new Intent(context, clickActivity)
                .putExtra(Options.EXTRA, options.toString())
                .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        int requestCode = new Random().nextInt();

        PendingIntent contentIntent = PendingIntent.getActivity(
                context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentIntent(contentIntent);
    }

}
