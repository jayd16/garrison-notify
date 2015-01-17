package com.duffy.model.push;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Jay on 1/10/2015.
 */
public class PushRequest<T> {
    @com.fasterxml.jackson.annotation.JsonProperty("registration_ids")
    @SerializedName("registration_ids")
    public List<String> regIds;
    public DataWrapper data;

    protected static class DataWrapper<T>{
        public T data;
    }

}
