package com.duffy.model.push;

import com.duffy.model.Account;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Jay on 1/10/2015.
 */
public class ReadFromServerPushRequest extends PushRequest<String> {
    public ReadFromServerPushRequest(){

    }

    public ReadFromServerPushRequest(String... regIds) {
        this.data = new DataWrapper();
        this.data.data = "read_from_server";
        this.regIds = Arrays.asList(regIds);
    }
}
