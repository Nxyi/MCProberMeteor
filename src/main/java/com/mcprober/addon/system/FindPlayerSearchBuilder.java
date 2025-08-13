package com.mcprober.addon.system;

import com.google.gson.JsonObject;

import java.util.UUID;

public class FindPlayerSearchBuilder {

    public static String create(String player){
        boolean isUUID = true;

        try {
            UUID.fromString(player);
        } catch (Exception e) {
            isUUID = false;
        }

        return (isUUID ? "uuid" : "username") + "=" + player;
    }

}
