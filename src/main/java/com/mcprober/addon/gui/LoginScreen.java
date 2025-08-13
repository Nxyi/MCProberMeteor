package com.mcprober.addon.gui;

import com.google.gson.JsonParser;
import com.mcprober.addon.Main;
import com.mcprober.addon.system.MCProberSystem;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.network.Http;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class LoginScreen extends WindowScreen {

    WindowScreen parent;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<String> tokenSetting = sg.add(new StringSetting.Builder()
        .name("token")
        .description("The token to use for the API.")
        .defaultValue("")
        .build()
    );

    public LoginScreen(WindowScreen parent) {
        super(GuiThemes.get(), "Login with Token");
        this.parent = parent;
    }
    WContainer settingsContainer;

    @Override
    public void initWidgets() {
        WContainer settingsContainer = add(theme.verticalList()).expandX().widget();
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        add(theme.button("Submit")).expandX().widget().action = () -> {
            reload();
            CompletableFuture.supplyAsync(() -> {
                if (tokenSetting.get().isEmpty()){
                    add(theme.label("Please enter a token to login."));

                    return null;
                }

                String response = Http.get(Main.mainEndpoint).header("X-API-KEY", tokenSetting.get()).sendString();

                if (response == null || Objects.equals(JsonParser.parseString(response).getAsJsonObject().get("message").getAsString(), "Invalid API Key")){
                    add(theme.label("Invalid token."));
                    return null;
                }

                return true;
            }).thenAccept(response -> {
                Main.mc.execute(() -> {
                    if (response == null) return;

                    MCProberSystem.get().setToken(tokenSetting.get());

                    mc.setScreen(this.parent);
                    this.parent.reload();
                });
            });
        };
    }
}
