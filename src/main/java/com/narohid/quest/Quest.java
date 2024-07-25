package com.narohid.quest;

import org.bukkit.plugin.java.JavaPlugin;

public class Quest extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("¡Quest ha sido habilitado!");
        this.getCommand("quest").setExecutor(new QuestCommand());
        this.getCommand("questadmin").setExecutor(new QuestAdminCommand());
        getServer().getPluginManager().registerEvents(new QuestEventListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("¡Quest ha sido deshabilitado!");
    }
}
