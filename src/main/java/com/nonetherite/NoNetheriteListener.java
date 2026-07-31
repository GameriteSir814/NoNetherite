package com.nonetherite;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Blocks every vanilla path to obtaining netherite:
 * 1. Mining ancient debris (BlockBreakEvent)
 * 2. Bastion/other loot-table chests containing netherite items (LootGenerateEvent)
 * 3. Smelting ancient debris into scrap (FurnaceSmeltEvent)
 * 4. Crafting netherite scrap + gold -> netherite ingot (CraftItemEvent - fires
 *    when the result is actually taken, not PrepareItemCraftingEvent, which
 *    doesn't exist as a class in this Paper API build)
 * 5. Smithing table upgrade (diamond gear + ingot + template -> netherite gear) (PrepareSmithingEvent)
 * 6. Mob drops, e.g. piglin brutes dropping netherite axes (EntityDeathEvent)
 *
 * Uses a generic "material name contains NETHERITE" check throughout, so it
 * also catches netherite blocks and any future netherite items automatically
 * without needing to list every single one by hand.
 */
public class NoNetheriteListener implements Listener {

    private final NoNetherite plugin;

    public NoNetheriteListener(NoNetherite plugin) {
        this.plugin = plugin;
    }

    private boolean enabled() {
        return plugin.getConfig().getBoolean("enabled", true);
    }

    private boolean isNetheriteRelated(Material mat) {
        return mat != null && mat.name().contains("NETHERITE");
    }

    // 1. Mining ancient debris
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!enabled()) return;
        if (e.getBlock().getType() != Material.ANCIENT_DEBRIS) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        String msg = plugin.getConfig().getString("block-message", "&cAncient debris cannot be mined on this server.");
        p.sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&', msg)));
    }

    // 2. Loot-table chests (bastion remnants, etc.)
    @EventHandler
    public void onLootGenerate(LootGenerateEvent e) {
        if (!enabled()) return;
        List<ItemStack> filtered = new ArrayList<>();
        for (ItemStack item : e.getLoot()) {
            if (item != null && isNetheriteRelated(item.getType())) continue;
            filtered.add(item);
        }
        e.setLoot(filtered);
    }

    // 3. Smelting ancient debris -> netherite scrap
    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent e) {
        if (!enabled()) return;
        if (e.getSource().getType() == Material.ANCIENT_DEBRIS) {
            e.setCancelled(true);
        }
    }

    // 4. Crafting table: netherite scrap + gold -> netherite ingot.
    // Fires when the player actually takes the crafted result.
    @EventHandler
    public void onCraftItem(CraftItemEvent e) {
        if (!enabled()) return;
        ItemStack result = e.getRecipe().getResult();
        if (result != null && isNetheriteRelated(result.getType())) {
            e.setCancelled(true);
            if (e.getWhoClicked() instanceof Player p) {
                p.sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&', "&cThat recipe is disabled on this server.")));
            }
        }
    }

    // 5. Smithing table: diamond gear + netherite ingot + template -> netherite gear
    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent e) {
        if (!enabled()) return;
        ItemStack result = e.getResult();
        if (result != null && isNetheriteRelated(result.getType())) {
            e.setResult(null);
        }
    }

    // 6. Mob drops (e.g. piglin brutes dropping netherite axes)
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (!enabled()) return;
        e.getDrops().removeIf(item -> item != null && isNetheriteRelated(item.getType()));
    }
}
