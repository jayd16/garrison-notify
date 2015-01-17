package com.duffy.model;

import java.util.Map;

/**
 * Created by Jay on 12/26/2014.
 */
public class GarrisonMission {
    public String name;
    public long endTime;
    public Map<String, Reward> rewards;

    public static class Reward{
        String itemID;
        int quantity;
        String title;

        public static final String FOLLOWER_XP_ICON = "Interface\\\\Icons\\\\XPBonus_Icon";
        public static final String COIN_ICON = "Interface\\\\Icons\\\\inv_misc_coin_01";
    }
}
