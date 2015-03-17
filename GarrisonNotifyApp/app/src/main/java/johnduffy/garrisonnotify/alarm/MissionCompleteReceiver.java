package johnduffy.garrisonnotify.alarm;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import johnduffy.garrisonnotify.GcmIntentService;
import johnduffy.garrisonnotify.MainActivity;
import johnduffy.garrisonnotify.R;

public class MissionCompleteReceiver extends BroadcastReceiver {
    public static String ACTION = "com.garrisonnotify.MISSION_COMPLETE";
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent showMissionsIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, showMissionsIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.app_icon)
                        .setAutoCancel(true)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setVibrate(new long[]{0, 500, 0})
                        .setContentTitle("Garrison Mission")
                        .setStyle(
                                new NotificationCompat
                                        .BigTextStyle()
                                        .bigText("Mission Complete"))
                        .setContentText("Mission Complete");

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }
}