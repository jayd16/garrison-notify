package com.duffy.model.push;

import com.duffy.model.Account;
import com.duffy.model.InProgressMissionData;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Jay on 1/10/2015.
 */
public class InProgressMissionDataPushRequest extends PushRequest<List<Account>> {
    public InProgressMissionDataPushRequest(){

    }

    public InProgressMissionDataPushRequest(List<Account> data, String... regIds) {
        this.data = new DataWrapper();
        this.data.data = data;
        this.regIds = Arrays.asList(regIds);
    }
}
