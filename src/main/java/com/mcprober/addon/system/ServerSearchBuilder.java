package com.mcprober.addon.system;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerSearchBuilder {
    public static class SearchParams {
        Boolean online, cracked, secureChat;
        String motd,maxOnline, currentlyOnline, port, version, countryCode;

        public SearchParams(String version, Boolean online, Boolean cracked, String motd, String maxOnline, String currentlyOnline, String port, Boolean secureChat, String countryCode) {
            this.version = version;
            this.online = online;
            this.cracked = cracked;
            this.motd = motd;
            this.maxOnline = maxOnline;
            this.currentlyOnline = currentlyOnline;
            this.port = port;
            this.secureChat = secureChat;
            this.countryCode = countryCode;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            if (version != null) map.put("version", version);
            if (online != null) map.put("online", online);
            if (cracked != null) map.put("cracked", cracked);
            if (motd != null && !motd.isEmpty()) map.put("motd", motd);
            if (maxOnline != null && !maxOnline.isEmpty()) map.put("max_players", maxOnline);
            if (currentlyOnline != null && !currentlyOnline.isEmpty()) map.put("online_players", currentlyOnline);
            if (port != null) map.put("port", port);
            if (secureChat != null) map.put("enforces_secure_chat", secureChat);
            if (countryCode != null) map.put("country", countryCode);
            return map;
        }
    }

    public static class Search {
        SearchParams flags;
        public Search(SearchParams flags) {
            this.flags = flags;
        }
    }

    public static String createHttpParams(Search search) {
        List<String> keyValuePairs = new ArrayList<>();

        if (search.flags != null) {
            Map<String, Object> flagsMap = search.flags.toMap();
            for (Map.Entry<String, Object> entry : flagsMap.entrySet()) {
                Object value = entry.getValue();
                if (value != null && !value.toString().isEmpty()) {
                    String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                    String encodedValue = URLEncoder.encode(value.toString(), StandardCharsets.UTF_8);
                    keyValuePairs.add(encodedKey + "=" + encodedValue);
                }
            }
        }

        return keyValuePairs.isEmpty() ? "" : "?" + String.join("&", keyValuePairs);
    }
}

