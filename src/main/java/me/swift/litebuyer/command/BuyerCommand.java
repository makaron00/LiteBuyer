package me.swift.litebuyer.command;

import me.swift.litebuyer.inventory.Buyer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuyerCommand implements CommandExecutor {
    private final Buyer buyer;

    public BuyerCommand(Buyer buyer) {
        this.buyer = buyer;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            this.buyer.openInventory((Player)sender);
            return true;
        } else {
            sender.sendMessage("");
            return false;
        }
    }
}
