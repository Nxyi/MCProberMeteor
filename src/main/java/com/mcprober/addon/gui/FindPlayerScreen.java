package com.mcprober.addon.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcprober.addon.Main;
import com.mcprober.addon.MultiplayerScreenUtils;
import com.mcprober.addon.system.FindPlayerSearchBuilder;
import com.mcprober.addon.system.MCProberSystem;
import com.mcprober.addon.system.ServerStorage;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FindPlayerScreen extends WindowScreen {
    private static FindPlayerScreen instance = null;
    private MultiplayerScreen multiplayerScreen;
    private Screen parent;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<String> playerSetting = sg.add(new StringSetting.Builder()
        .name("name/uuid")
        .description("")
        .defaultValue("popbob")
        .build()
    );

    public static FindPlayerScreen instance(MultiplayerScreen multiplayerScreen, Screen parent) {
        if (instance == null) {
            instance = new FindPlayerScreen();
        }
        instance.setMultiplayerScreen(multiplayerScreen);
        instance.setParent(parent);
        return instance;
    }

    public void setParent(Screen parent) {
        this.parent = parent;
    }

    public MultiplayerScreen setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        return this.multiplayerScreen = multiplayerScreen;
    }

    public FindPlayerScreen() {
        super(GuiThemes.get(), "Find Player");
    }
    WContainer settingsContainer;
    @Override
    public void initWidgets() {

        WContainer settingsContainer = add(theme.verticalList()).expandX().widget();
        settingsContainer.minWidth = 300;
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        add(theme.button("search")).expandX().widget().action = () -> {
            reload();
            if (playerSetting.get().isEmpty()){
                add(theme.label("Enter a name/uuid")).expandX();
            }

            CompletableFuture.supplyAsync(() -> {
                String toSearch = FindPlayerSearchBuilder.create(playerSetting.get());
                String string = String.format("%swhereis?%s", Main.mainEndpoint, toSearch);

                return Http.get(string).header("X-API-KEY", MCProberSystem.get().getToken()).sendStringResponse().body();
            }).thenAccept(response -> {
                List<ServerStorage> extractedServers = extractServerInfo(response);
                if (extractedServers.isEmpty() || response == null){
                    add(theme.label("No servers found."));
                    return;
                }

                add(theme.button("add all")).expandX().widget().action = () -> {
                    extractedServers.forEach((server) -> {
                        ServerInfo info = new ServerInfo("Mcsdc " + server.ip(), server.ip(), ServerInfo.ServerType.OTHER);
                        multiplayerScreen.getServerList().add(info, false);
                    });
                    MultiplayerScreenUtils.save(this.multiplayerScreen);
                    MultiplayerScreenUtils.reload(this.multiplayerScreen);
                };

                Main.mc.execute(() -> {
                    WTable table = add(theme.table()).widget();

                    table.add(theme.label("Server IP"));
                    table.row();
                    table.add(theme.horizontalSeparator()).expandX();
                    table.row();

                    // Iterate through the extracted server data
                    extractedServers.forEach((server) -> {
                        String serverIP = server.ip();

                        table.add(theme.label(serverIP));

                        WButton addServerButton = theme.button("Add Server");
                        addServerButton.action = () -> {
                            ServerInfo info = new ServerInfo("Mcsdc " + serverIP, serverIP, ServerInfo.ServerType.OTHER);
                            multiplayerScreen.getServerList().add(info, false);
                            MultiplayerScreenUtils.save(this.multiplayerScreen);
                            MultiplayerScreenUtils.reload(this.multiplayerScreen);
                            addServerButton.visible = false;
                        };

                        WButton joinServerButton = theme.button("Join Server");
                        joinServerButton.action = () ->
                            ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), Main.mc,
                                ServerAddress.parse(serverIP), new ServerInfo("", serverIP, ServerInfo.ServerType.OTHER), false, null);


                        WButton serverInfoButton = theme.button("Server Info");
                        serverInfoButton.action = () -> {
                            Main.mc.setScreen(new ServerInfoScreen(serverIP));
                        };

                        table.add(addServerButton);
                        table.add(joinServerButton);
                        table.add(serverInfoButton);
                        table.row();
                    });
                });
            });
        };
    }

    public static List<ServerStorage> extractServerInfo(String jsonResponse) {
        List<ServerStorage> serverStorageList = new ArrayList<>();
        JsonArray array = JsonParser.parseString(jsonResponse)
            .getAsJsonObject()
            .getAsJsonArray("servers");

        array.forEach(node -> {
            JsonObject obj = node.getAsJsonObject();
            String address = obj.get("server").getAsString();
            String version = "";

            // Parse the ISO 8601 string into an Instant and get epoch milliseconds
            String lastSeenStr = obj.get("last_seen").getAsString();

            serverStorageList.add(new ServerStorage(address, version, lastSeenStr));
        });

        return serverStorageList;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
