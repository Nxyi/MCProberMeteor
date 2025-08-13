package com.mcprober.addon.system;

import javax.annotation.Nullable;

public record ServerStorage(String ip, String version, @Nullable String lastSeen) {

}
