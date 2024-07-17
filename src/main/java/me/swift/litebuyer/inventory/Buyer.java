package me.swift.litebuyer.inventory;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import me.swift.litebuyer.database.SqlManager;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Buyer implements Listener {
    private final Economy economy;
    private final FileConfiguration config;
    private final SqlManager sqlManager;

    public Buyer(Economy economy, FileConfiguration config, SqlManager sqlManager) {
        this.economy = economy;
        this.config = config;
        this.sqlManager = sqlManager;
    }

    public void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 54, ChatColor.GRAY + "Скупщик предметов");
        this.updateButton(inventory, 0.0, player);
        this.setPanelItems(inventory);
        this.setInfoButton(inventory, player);
        this.setHoneycombItem(inventory);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GRAY + "Скупщик предметов")) {
            int slot = event.getSlot();
            if (slot >= 45 && slot <= 53) {
                event.setCancelled(true);
                if (slot == 45) {
                    return;
                }

                if (slot == 53) {
                    this.processSale((Player)event.getWhoClicked());
                }
            } else {
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("LiteBuyer"), () -> {
                    this.updateButton(event.getInventory(), this.calculateTotalPrice(event.getInventory(), (Player)event.getWhoClicked()), (Player)event.getWhoClicked());
                }, 1L);
            }
        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GRAY + "Скупщик предметов")) {
            Inventory inventory = event.getInventory();

            for(int i = 0; i < 45; ++i) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    event.getPlayer().getInventory().addItem(new ItemStack[]{item});
                }
            }
        }

    }

    private void processSale(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        double total = this.calculateTotalPrice(inventory, player);
        if (total > 0.0) {
            for(int i = 0; i < 45; ++i) {
                inventory.setItem(i, (ItemStack)null);
            }

            this.economy.depositPlayer(player, total);
            double currentMultiplier = this.sqlManager.getMultiplier(player.getUniqueId());
            double newMultiplier = Math.min(currentMultiplier + 0.03, 10.0);  // Устанавливаем максимальный множитель в 10.0

            if (currentMultiplier >= 1.09) {
                newMultiplier = Math.min((double)Math.round((currentMultiplier + 0.1) * 10.0) / 10.0, 10.0);  // Устанавливаем максимальный множитель в 10.0
            }

            this.sqlManager.updateMultiplier(player.getUniqueId(), newMultiplier);
            String saleMessage = this.config.getString("messages.sall", "§x§F§B§6§5§0§8▶ §fВы §x§F§B§6§5§0§8успешно продали предметы (+3 поинтов к множителю) §fпредметов и заработали §x§F§B§C§D§0§8{money} монет. §fВаш множитель теперь составляет: §x§F§B§C§D§0§8{multiplier}.").replace("{money}", String.valueOf(total)).replace("{multiplier}", String.valueOf(newMultiplier));
            player.sendMessage(saleMessage);
            String successSound = this.config.getString("sounds.yes", "ENTITY_VILLAGER_YES");
            this.playSound(player, successSound);
        } else {
            String failureSound = this.config.getString("sounds.no", "ENTITY_VILLAGER_NO");
            this.playSound(player, failureSound);
        }

        this.updateButton(inventory, 0.0, player);
    }

    private void playSound(Player player, String soundName) {
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
        } catch (IllegalArgumentException var4) {
            Bukkit.getLogger().warning("Invalid sound name: " + soundName);
        }

    }

    private double calculateTotalPrice(Inventory inventory, Player player) {
        double total = 0.0;
        double multiplier = this.sqlManager.getMultiplier(player.getUniqueId());

        for(int i = 0; i < 45; ++i) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                String type = item.getType().toString();
                if (this.config.contains("buyer." + type)) {
                    int basePrice = this.config.getInt("buyer." + type + ".price", 0);
                    int adjustedPrice = basePrice + (int)(multiplier - 1.0) * 20;
                    total += (double)(adjustedPrice * item.getAmount());
                }
            }
        }

        return total;
    }

    private void updateButton(Inventory inventory, double totalPrice, Player player) {
        ItemStack buttonItem = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = buttonItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.config.getString("gui.button.info.name", "§f")));
            List<String> lore = (List)this.config.getStringList("gui.button.info.lore").stream().map((line) -> {
                return line.replace("{price}", String.valueOf(totalPrice));
            }).map((line) -> {
                return ChatColor.translateAlternateColorCodes('&', line);
            }).collect(Collectors.toList());
            meta.setLore(lore);
            buttonItem.setItemMeta(meta);
        }

        inventory.setItem(53, buttonItem);
    }

    private void setPanelItems(Inventory inventory) {
        ItemStack panelItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = panelItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.config.getString("gui.panel.displayName", " ")));
            panelItem.setItemMeta(meta);
        }

        Iterator var4 = this.config.getIntegerList("gui.panel.slot").iterator();

        while(var4.hasNext()) {
            int slot = (Integer)var4.next();
            inventory.setItem(slot, panelItem);
        }

    }

    private void setInfoButton(Inventory inventory, Player player) {
        ItemStack infoItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = infoItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.config.getString("main.buttons.info.displayName", " ")));
            double multiplier = this.sqlManager.getMultiplier(player.getUniqueId());
            List<String> lore = (List)this.config.getStringList("main.buttons.info.lore").stream().map((line) -> {
                return line.replace("{multiplier}", String.valueOf(multiplier));
            }).map((line) -> {
                return ChatColor.translateAlternateColorCodes('&', line);
            }).collect(Collectors.toList());
            meta.setLore(lore);
            infoItem.setItemMeta(meta);
        }

        inventory.setItem(45, infoItem);
    }

    private void setHoneycombItem(Inventory inventory) {
        ItemStack honeycombItem = new ItemStack(Material.HONEYCOMB);
        ItemMeta meta = honeycombItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.config.getString("ulta.button.info.name", " ")));
            List<String> lore = (List)this.config.getStringList("ulta.button.info.lore").stream().map((line) -> {
                return ChatColor.translateAlternateColorCodes('&', line);
            }).collect(Collectors.toList());
            meta.setLore(lore);
            honeycombItem.setItemMeta(meta);
        }

        inventory.setItem(49, honeycombItem);
    }
}
