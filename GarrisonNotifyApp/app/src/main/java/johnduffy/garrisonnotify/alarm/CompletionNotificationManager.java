package johnduffy.garrisonnotify.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

import johnduffy.garrisonnotify.model.Account;
import johnduffy.garrisonnotify.model.GarrisonMission;

/**
 * Created by johnduffy on 3/16/2015.
 */
public class CompletionNotificationManager {

    public static void setAlarmForNextMission(Context context, List<Account> accountList){
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(MissionCompleteReceiver.ACTION);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        GarrisonMission nextMission = nextMission(accountList);

        alarmMgr.cancel(alarmIntent);
        if(nextMission != null) {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, nextMission.endTime * 1000, alarmIntent);
        }
    }

    private static GarrisonMission nextMission(List<Account> accounts){
        GarrisonMission nextMission = null;
        for(Account account : accounts){
            for(String missionId: account.missionData.keySet()){

                GarrisonMission mission = account.missionData.get(missionId);

                if((nextMission == null || mission.endTime < nextMission.endTime) && mission.endTime > System.currentTimeMillis()/1000){
                    nextMission = mission;
                }
            }
        }
        return nextMission;
    }
}
