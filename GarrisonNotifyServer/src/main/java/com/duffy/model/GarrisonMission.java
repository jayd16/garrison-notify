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
        public String itemID;
        public int currencyID = -1;
        public Integer quantity;
        public Integer followerXP;
        public String title;
        public String icon;
        public String imageUrl;

        public static final String FOLLOWER_XP_ICON = "Interface\\\\Icons\\\\XPBonus_Icon";
    }
}
