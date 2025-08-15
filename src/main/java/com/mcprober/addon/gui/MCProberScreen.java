package com.mcprober.addon.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcprober.addon.Main;
import com.mcprober.addon.MultiplayerScreenUtils;
import com.mcprober.addon.system.MCProberSystem;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;


public class MCProberScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;

    public MCProberScreen(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "MCProber");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        String authToken = MCProberSystem.get().getToken();

        if (authToken.isEmpty()) {
            this.client.setScreen(new LoginScreen(this));
            return;
        }

        WTable accountList = add(theme.table()).expandX().widget();
        CompletableFuture.supplyAsync(() -> {

            HttpResponse<String> response = Http.get(
                    Main.mainEndpoint + "stats"
                )
                .header(
                    "X-API-KEY",
                    MCProberSystem.get().getToken()
                )
                .sendStringResponse();

            return response.body();
        }).thenAccept(notice -> {
            JsonObject object = JsonParser.parseString(notice).getAsJsonObject();

            if (object.has("message")){
                MCProberSystem.get().setToken("");
                reload();
                return;
            }

            MCProberSystem.get().total_servers = object.get("total_servers").getAsInt();
            MCProberSystem.get().total_players = object.get("total_players").getAsInt();
            MCProberSystem.get().api_uptime = object.get("api_uptime").getAsString();
            MCProberSystem.get().last_scan = object.get("last_scan").getAsString();
            MCProberSystem.get().alive_servers = object.get("alive_servers").getAsInt();

            accountList.row();
            accountList.add(theme.label("total_servers: " + MCProberSystem.get().total_servers)).expandX();
            accountList.row();
            accountList.add(theme.label("total_players: " + MCProberSystem.get().total_players)).expandX();
            accountList.row();
            accountList.add(theme.label("alive_servers: " + MCProberSystem.get().alive_servers)).expandX();
        }); // fix freezing that can happen when opening screen, no longer waits for notice before adding other components


        WButton logoutButton = accountList.add(theme.button("Logout")).widget();
        logoutButton.action = () -> {
            MCProberSystem.get().setToken("");
            reload();
        };

        WHorizontalList widgetList = add(theme.horizontalList()).expandX().widget();
        WButton newServersButton = widgetList.add(this.theme.button("Find new servers")).expandX().widget();
        WButton findPlayersButton = widgetList.add(this.theme.button("Search players")).expandX().widget();
        WButton recentServersButton = widgetList.add(this.theme.button("Recent Servers")).expandX().widget();
        WButton removeServersButton = widgetList.add(this.theme.button("Remove Servers")).expandX().widget();
        WButton tickedIdScreenButton = widgetList.add(this.theme.button("TicketID search")).expandX().widget();
        WButton StreamSnipeScreenButton = widgetList.add(this.theme.button("StreamSnipe")).expandX().widget();


        newServersButton.action = () -> {
            this.client.setScreen(FindNewServersScreen.instance(this.multiplayerScreen, this));
        };

        findPlayersButton.action = () -> {
            this.client.setScreen(FindPlayerScreen.instance(this.multiplayerScreen, this));
        };

        recentServersButton.action = () -> {
            this.client.setScreen(new RecentServersScreen(this.multiplayerScreen));
        };

        removeServersButton.action = () -> {
            for (int i = 0; i < this.multiplayerScreen.getServerList().size(); i++) {
                if (this.multiplayerScreen.getServerList().get(i).name.startsWith("MCProber")) {
                    this.multiplayerScreen.getServerList().remove(this.multiplayerScreen.getServerList().get(i));
                    i--;
                }
            }

            MultiplayerScreenUtils.save(this.multiplayerScreen);
            MultiplayerScreenUtils.reload(this.multiplayerScreen);
        };

        tickedIdScreenButton.action = () -> {
            this.client.setScreen(TicketIDScreen.instance(this.multiplayerScreen, this));
        };

        StreamSnipeScreenButton.action = () -> {
            this.client.setScreen(StreamSnipeScreen.instance(this.multiplayerScreen, this));
        };

//        findPlayersButton.action = () -> {
//            if (this.client == null) return;
//            this.client.setScreen(new FindPlayerScreen(this.multiplayerScreen));
//        };
    }

    @Override
    public void close() {
        super.close();
        MultiplayerScreenUtils.reload(this.multiplayerScreen);
        this.client.setScreen(this.multiplayerScreen);
    }
}
