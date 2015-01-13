package com.duffy.model;

import com.duffy.InProgressMissionData;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jay on 1/10/2015.
 */
public class InProgressMissionDataPushRequest extends PushRequest<InProgressMissionData> {
    public InProgressMissionDataPushRequest(){

    }

    public InProgressMissionDataPushRequest(InProgressMissionData data, String... regIds) {
        this.data = new DataWrapper();
        this.data.data = data;
        this.regIds = Arrays.asList(regIds);
    }
}
