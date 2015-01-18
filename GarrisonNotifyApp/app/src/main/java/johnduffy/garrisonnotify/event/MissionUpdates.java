package johnduffy.garrisonnotify.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by Jay on 1/10/2015.
 */
public class MissionUpdates {

    public static String MISSION_UPDATE_ACTION = MissionUpdates.class.getName() + ".MISSION_UPDATE_ACTION";

    public static void sendBroadcast(Context context) {
        context.sendBroadcast(new Intent(MISSION_UPDATE_ACTION));
    }

    public static void registerReceiver(Context context, BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MISSION_UPDATE_ACTION);
        context.registerReceiver(receiver, filter);
    }

    public static void unregisterReceiver(Context context, BroadcastReceiver receiver){
        context.unregisterReceiver(receiver);
    }

}
