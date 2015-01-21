package johnduffy.garrisonnotify.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import johnduffy.garrisonnotify.model.Account;
import johnduffy.garrisonnotify.model.InProgressMissionData;

/**
 * Created by Jay on 1/10/2015.
 */
public class MissionUtil {

    private static String PREF_NAME = MissionUtil.class.getName();

    private static String MISSION_KEY = "MISSION_KEY";
    private static String ACCOUNT_KEY = "ACCOUNT_KEY";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void setMissions(Context context, String missions) {
        getPreferences(context).edit().putString(ACCOUNT_KEY, missions).apply();
    }

    public static void setMissions(Context context, List<Account> missions) {
        getPreferences(context).edit().putString(ACCOUNT_KEY, new Gson().toJson(missions)).apply();
    }

    public static List<Account> getMissions(Context context) {
        try {
            return new Gson().fromJson(getPreferences(context).getString(ACCOUNT_KEY, "[]"), new TypeToken<List<Account>>() {
            }.getType());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

}
