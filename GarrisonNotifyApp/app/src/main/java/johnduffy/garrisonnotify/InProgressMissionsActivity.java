package johnduffy.garrisonnotify;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import johnduffy.garrisonnotify.event.MissionUpdates;
import johnduffy.garrisonnotify.model.Account;
import johnduffy.garrisonnotify.model.GarrisonMission;

/**
 * Created by Jay on 1/10/2015.
 */
public class InProgressMissionsActivity extends Activity {

    protected ViewGroup list;

    private List<Account> data;
    private TickRunnable tickRunnable;
    private BroadcastReceiver newMissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            requestData();
        }
    };

    private List<TickWorker> updateOnTick = new ArrayList<>();

    private static String getCountdownString(long endTime) {
        long secondsRemaining = endTime - System.currentTimeMillis() / 1000;

        if (secondsRemaining <= 0) {
            return "COMPLETE";
        }

        int hours = (int) (secondsRemaining / 3600);
        int mins = (int) ((secondsRemaining % 3600) / 60);
        int secs = (int) (secondsRemaining % 60);

        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.missions_list);

        list = (LinearLayout) findViewById(R.id.list);
        tickRunnable = new TickRunnable(list);
    }

    private void requestData() {
        Request<List<Account>> getDataRequest = new Request<List<Account>>(
                Request.Method.GET,
                "http://gnpushserver-jayd.rhcloud.com/app/missions/jayd/",
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }
        ) {
            @Override
            protected Response<List<Account>> parseNetworkResponse(NetworkResponse networkResponse) {
                try {
                    List<Account> respObject =
                            new Gson().fromJson(new String(networkResponse.data, "UTF-8"), new TypeToken<List<Account>>() {
                            }.getType());
                    return Response.success(
                            respObject,
                            HttpHeaderParser.parseCacheHeaders(networkResponse)
                    );
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return Response.error(new VolleyError(networkResponse));
            }

            @Override
            protected void deliverResponse(List<Account> resp) {
                data = resp;
                updateUI();
            }
        };


        GarrisonNotifyApplication.VOLLEY_REQUEST_QUEUE.add(getDataRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();
        list.post(tickRunnable);
        MissionUpdates.registerReceiver(this, newMissionReceiver);
        requestData();
    }

    private void updateUI() {
        updateOnTick.clear();
        list.removeAllViews();
        for (Account account : data) {
            View accView = LayoutInflater.from(this).inflate(R.layout.account_name, list, false);
            ((TextView) accView.findViewById(R.id.name)).setText(account.name);
            list.addView(accView);
            for (GarrisonMission mission : account.missionData.values()) {
                View v = LayoutInflater.from(this).inflate(R.layout.mission, list, false);
                ((TextView) v.findViewById(R.id.name)).setText(mission.name);

                TextView endTimeView = ((TextView) v.findViewById(R.id.endTime));
                updateOnTick.add(new TickWorker(endTimeView, mission.endTime));

                final NetworkImageView rewardImage = (NetworkImageView) v.findViewById(R.id.rewardImg);
                for (GarrisonMission.Reward reward : mission.rewards.values()) {
                    if (reward.quantity > 1) {
                        String s = String.valueOf(reward.quantity);
                        if (reward.quantity >= 1000) {
                            s = String.format("%.1fk", reward.quantity / 1000.0d);
                        }
                        ((TextView) v.findViewById(R.id.quantity)).setText(s);
                    }
                    if ("Interface\\Icons\\XPBonus_Icon".equals(reward.icon)) {
                        rewardImage.setDefaultImageResId(R.drawable.bonus_xp);
                    } else if (reward.imageUrl != null) {
                        rewardImage.setImageUrl(reward.imageUrl, ((GarrisonNotifyApplication) getApplication()).VOLLEY_IMAGE_LOADER);
                    }
                    if (reward.itemID != null) {
                        final int itemId = Integer.parseInt(reward.itemID);
                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://www.wowhead.com/item=%d", itemId))));
                            }
                        });
                    }
                }


                list.addView(v);
            }
        }
    }

    private void onTick() {

        for (Iterator<TickWorker> iter = updateOnTick.iterator(); iter.hasNext(); ) {
            boolean completed = iter.next().onTick();
            if (completed) {
                iter.remove();
            }
        }
    }

    @Override
    protected void onPause() {
        MissionUpdates.unregisterReceiver(this, newMissionReceiver);
        list.removeCallbacks(tickRunnable);
        super.onPause();
    }

    private static class TickWorker {
        private final TextView tv;
        private final long endTime;

        private TickWorker(TextView tv, long endTime) {
            this.tv = tv;
            this.endTime = endTime;
        }

        public boolean onTick() {
            tv.setText(getCountdownString(endTime));
            boolean complete = endTime < System.currentTimeMillis() / 1000;
            if (complete) {
                tv.setTextColor(Color.GREEN);
            }
            return complete;
        }
    }

    private class TickRunnable implements Runnable {
        private final View view;

        private TickRunnable(View view) {
            this.view = view;
        }

        @Override
        public void run() {
            view.postDelayed(this, 1000);
            onTick();
        }
    }

}
