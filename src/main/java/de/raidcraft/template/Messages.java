package de.raidcraft.template;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;

public final class Messages {

    private Messages() {}

    public static void send(UUID playerId, Component message) {
        if (PluginTemplate.isTesting()) return;
        BukkitAudiences.create(PluginTemplate.instance())
                .player(playerId)
                .sendMessage(message);
    }

    public static void send(UUID playerId, Consumer<TextComponent.Builder> message) {

        TextComponent.Builder builder = text();
        message.accept(builder);
        send(playerId, builder.build());
    }

    public static void send(Player player, Component message) {
        send(player.getUniqueId(), message);
    }
}
