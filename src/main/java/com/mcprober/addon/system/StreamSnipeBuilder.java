package com.mcprober.addon.system;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamSnipeBuilder {
    public static class SearchParams {
        String viewerCount,language, mods, tags;

        public SearchParams(String viewerCount, String language, String mods, String tags) {
            this.viewerCount = viewerCount;
            this.language = language;
            this.mods = mods;
            this.tags = tags;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            if (viewerCount != null) map.put("viewer_count", viewerCount);
            if (language != null) map.put("language", language);
            if (tags != null) map.put("tags", tags);
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

