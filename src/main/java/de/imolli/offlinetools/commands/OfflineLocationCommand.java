package de.imolli.offlinetools.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.DoubleTag;
import net.querz.nbt.tag.ListTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class OfflineLocationCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }

        String playerName = args[0];
        Optional<UUID> uuid = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(offlinePlayer -> playerName.equalsIgnoreCase(offlinePlayer.getName()))
                .map(OfflinePlayer::getUniqueId)
                .findAny();

        if (uuid.isEmpty()) {
            sender.sendMessage("§cNo player with the name §e" + playerName + " §cwas found.");
            return true;
        }

        Optional<File> fileOptional = Optional.of(new File("world", "playerdata"))
                .filter(File::exists)
                .map(File::listFiles)
                .flatMap(a -> Arrays.stream(a).filter(b -> matchUUID(b, uuid.get())).findAny())
                .filter(File::exists);

        // Check if file is present
        if (fileOptional.isEmpty()) {
            sender.sendMessage("§7No playerdata was found. Perhaps the player has never played on this server?");
            return true;
        }

        try {
            //noinspection unchecked
            ListTag<DoubleTag> list = (ListTag<DoubleTag>) ((CompoundTag) NBTUtil.read(fileOptional.get()).getTag()).get("Pos");
            double x = list.get(0).asDouble();
            double y = list.get(1).asDouble();
            double z = list.get(2).asDouble();

            // if the command sender is a player. Build a Component set to give the player a better feedback.
            if (sender instanceof Player) {
                TextComponent base = new TextComponent(MessageFormat.format("§7The last known location of §e{0} §7is §e{1} {2} {3} ", playerName, x, y, z));
                TextComponent clickComponent = new TextComponent("§a[Teleport]");
                clickComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, MessageFormat.format("/tp {0} {1} {2}", x, y, z)));
                clickComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to teleport to the location")));

                sender.spigot().sendMessage(new ComponentBuilder(base).append(clickComponent).create());
            } else {
                sender.sendMessage("§7The player §e{0} §7last known location is §e{1} {2} {3}");
            }

            return true;
        } catch (IOException e) {
            sender.sendMessage("§cAn unknown error occurred while reading playerdata of player §e" + playerName);
        }
        return true;
    }

    private boolean matchUUID(File file, UUID uuid) {
        return Optional.of(file.getName())
                .map(fullName -> fullName.split("\\."))
                .map(nameSplits -> nameSplits[0])
                .map(name -> name.equalsIgnoreCase(uuid.toString()))
                .orElse(false);
    }

}
