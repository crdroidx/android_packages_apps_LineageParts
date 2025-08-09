/*
 * SPDX-FileCopyrightText: 2012 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2019,2021,2023 The LineageOS project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.lineageparts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.UserManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import org.lineageos.lineageparts.contributors.ContributorsCloudFragment;
import org.lineageos.lineageparts.donate.DonateActivity;
import org.lineageos.lineageparts.gestures.TouchscreenGestureSettings;
import org.lineageos.lineageparts.input.ButtonSettings;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "PartsBootReceiver";
    private static final String ONE_TIME_TUNABLE_RESTORE = "hardware_tunable_restored";

    private static final String DONATE_CHANNEL_ID = "donation_channel";
    private static final String DONATE_LAST_CHECKED = "pref_donate_checked_in";
    private static final long COOLDOWN_MS = 2L * 60 * 1000; // Testing
    private static final int DONATE_NOTIFICATION_ID = 42;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (!ctx.getSystemService(UserManager.class).isPrimaryUser()) {
            Log.d(TAG, "Not running as the primary user, skipping tunable restoration.");
            return;
        }

        if (!hasRestoredTunable(ctx)) {
            /* Restore the hardware tunable values */
            /* ButtonSettings.restoreKeyDisabler(ctx); */
            setRestoredTunable(ctx);
        }

        /* ButtonSettings.restoreKeyDisabler(ctx); */
        ButtonSettings.restoreKeySwapper(ctx);
        TouchscreenGestureSettings.restoreTouchscreenGestureStates(ctx);

        // Extract the contributors database
        ContributorsCloudFragment.extractContributorsCloudDatabase(ctx);

        // Check for donate intent
        maybeShowDonateNotification(ctx);
    }

    private void maybeShowDonateNotification(Context ctx) {
        long now = System.currentTimeMillis();
        long last = PreferenceManager.getDefaultSharedPreferences(ctx)
                       .getLong(DONATE_LAST_CHECKED, 0L);
        if (now - last < COOLDOWN_MS) {
            Log.d(TAG, "Donate notification suppressed due to cooldown.");
            return;
        }

        createDonateChannelIfNeeded(ctx);

        PendingIntent contentPi = PendingIntent.getActivity(
                ctx, 0, new Intent(ctx, DonateActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, DONATE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_donate)
                .setContentTitle(ctx.getString(R.string.crdroid_donate_title))
                .setContentText(ctx.getString(R.string.crdroid_donate_notification_text))
                .setContentIntent(contentPi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(DONATE_NOTIFICATION_ID, b.build());
    }

    private void createDonateChannelIfNeeded(Context ctx) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel ch = new NotificationChannel(
                DONATE_CHANNEL_ID,
                ctx.getString(R.string.crdroid_donate_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        ch.setDescription(ctx.getString(R.string.crdroid_donate_channel_desc));
        nm.createNotificationChannel(ch);
    }

    private boolean hasRestoredTunable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(ONE_TIME_TUNABLE_RESTORE, false);
    }

    private void setRestoredTunable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(ONE_TIME_TUNABLE_RESTORE, true).apply();
    }
}
