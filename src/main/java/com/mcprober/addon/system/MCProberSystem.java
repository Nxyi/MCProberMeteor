package com.mcprober.addon.system;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MCProberSystem extends System<MCProberSystem> {
    private String token = "";
    public int total_servers = 0;
    public int total_players = 0;
    public String api_uptime = "";
    public String last_scan = "";
    public int alive_servers = 0;

    private String lastServer = "";

    private List<ServerStorage> recentServers = new ArrayList<>();

    public MCProberSystem() {
        super("MCProberSystem");
    }

    public static MCProberSystem get() {
        return Systems.get(MCProberSystem.class);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<ServerStorage> getRecentServers() {
        return recentServers;
    }

    public ServerStorage getRecentServerWithIp(String ip){
        for (ServerStorage server : recentServers){
            if (Objects.equals(server.ip(), ip)){
                return server;
            }
        }
        return null;
    }

    public String getLastServer() {
        return lastServer;
    }

    public void setLastServer(String lastServer) {
        this.lastServer = lastServer;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound compound = new NbtCompound();
        compound.putString("token", this.token);

        NbtList list = new NbtList();

        recentServers.forEach((server) -> {
            NbtCompound compound2 = new NbtCompound();
            compound2.putString("ip", server.ip());
            compound2.putString("version", server.version());
            list.add(compound2);
        });

        compound.put("recent", list);


        return compound;
    }

    @Override
    public MCProberSystem fromTag(NbtCompound tag) {
        this.token = tag.getString("token").get();

        NbtList list = tag.getList("recent").get();
        for (NbtElement element : list){
            NbtCompound compound = (NbtCompound) element;
            String ip = compound.getString("ip").get();
            String ver = compound.getString("version").get();

            recentServers.add(new ServerStorage(ip, ver, null));
        }

        // reverse servers to ensure they are in the correct order. or they would flip each time.
        Collections.reverse(recentServers);

        return super.fromTag(tag);
    }
}
