package me.swift.litebuyer;

import me.swift.litebuyer.command.BuyerCommand;
import me.swift.litebuyer.database.SqlManager;
import me.swift.litebuyer.inventory.Buyer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

public class LiteBuyer extends JavaPlugin {
    private Economy economy;
    private SqlManager sqlManager;

    public LiteBuyer() {
    }

    public void onEnable() {
        if (!this.setupEconomy()) {
            this.getLogger().severe("Disabled due to no Vault dependency found!");
            this.getServer().getPluginManager().disablePlugin(this);
        } else {
            this.saveDefaultConfig();
            this.sqlManager = new SqlManager(this.getDataFolder().getPath() + "/multipliers.db");
            Buyer buyer = new Buyer(this.economy, this.getConfig(), this.sqlManager);
            this.getServer().getPluginManager().registerEvents(buyer, this);
            this.getCommand("buyer").setExecutor(new BuyerCommand(buyer));
        }
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            Economy econ = (Economy)this.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
            if (econ == null) {
                return false;
            } else {
                this.economy = econ;
                return true;
            }
        }
    }
}
