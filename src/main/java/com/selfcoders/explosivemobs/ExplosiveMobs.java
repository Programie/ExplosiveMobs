package com.selfcoders.explosivemobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ExplosiveMobs extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spawnexplosivemob")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command must be executed as a player!");
                return true;
            }

            if (args.length == 0) {
                return false;
            }

            spawnExplosiveMobCommand((Player) sender, args);
            return true;
        }

        return false;
    }

    private void spawnExplosiveMobCommand(Player player, String[] args) {
        if (!player.hasPermission("explosivemobs.spawn")) {
            player.sendMessage(ChatColor.RED + "You do not have the required permissions for this command!");
            return;
        }

        Player target = player;
        String subType = null;
        int amount = 1;

        String[] typeParts = args[0].split(":");
        EntityType type = EntityType.fromName(typeParts[0]);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Invalid mob type!");
            return;
        }

        if (typeParts.length >= 2) {
            subType = typeParts[1];
        }

        // Amount specified
        if (args.length >= 2) {
            amount = Integer.parseInt(args[1]);
            if (amount < 1) {
                player.sendMessage(ChatColor.RED + "Amount must be 1 or greater!");
                return;
            }

            int maxSpawnAmount = getConfig().getInt("maxSpawnAmount", 0);
            if (maxSpawnAmount > 0 && amount > maxSpawnAmount) {
                amount = maxSpawnAmount;
                player.sendMessage("Mob spawning limit of " + maxSpawnAmount + " reached!");
            }
        }

        // Target player specified
        if (args.length >= 3) {
            if (!player.hasPermission("explosivemobs.spawn.targetplayer")) {
                player.sendMessage(ChatColor.RED + "You do not have the required permissions for this command!");
                return;
            }

            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Target player not found!");
                return;
            }
        }

        for (int number = 0; number < amount; number++) {
            try {
                spawnExplosiveMob(target.getWorld(), target.getLocation(), type, subType);
            } catch (IllegalArgumentException exception) {
                player.sendMessage(ChatColor.RED + "Invalid sub type: " + subType);
                return;
            }
        }

        player.sendMessage("Spawned " + amount + " " + type.getName() + "(s) fed with TNT");
    }

    private void spawnExplosiveMob(World world, Location location, EntityType type, String subType) {
        Entity entity = world.spawnEntity(location, type);
        if (entity != null) {
            switch (type) {
                case CREEPER:
                    if (subType != null && subType.toLowerCase().equals("powered")) {
                        ((Creeper) entity).setPowered(true);
                    }
                    break;
                case HORSE:
                    if (subType != null && !subType.isEmpty()) {
                        ((Horse) entity).setVariant(Horse.Variant.valueOf(subType.toUpperCase()));
                    }
                    break;
            }

            entity.setMetadata("isPlayerSpawnedExplosiveMob", new FixedMetadataValue(this, true));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        String configPath = "mobs." + entity.getType().getName();
        boolean isPlayerSpawnedExplosiveMob = entity.hasMetadata("isPlayerSpawnedExplosiveMob");

        if (entity.getKiller() == null && !isPlayerSpawnedExplosiveMob && !getConfig().getBoolean(configPath + ".triggeredByAny", getConfig().getBoolean("defaults.triggeredByAny"))) {
            return;
        }

        if (isPlayerSpawnedExplosiveMob || getConfig().getBoolean(configPath + ".enabled", getConfig().getBoolean("defaults.enabled"))) {
            Location entityLocation = entity.getLocation();
            double x = entityLocation.getX();
            double y = entityLocation.getY();
            double z = entityLocation.getZ();
            float power = (float) getConfig().getDouble(configPath + ".power", getConfig().getDouble("defaults.power"));
            boolean setFire = getConfig().getBoolean(configPath + ".setFire", getConfig().getBoolean("defaults.setFire"));
            boolean breakBlocks = getConfig().getBoolean(configPath + ".breakBlocks", getConfig().getBoolean("defaults.breakBlocks"));
            entity.getWorld().createExplosion(x, y, z, power, setFire, breakBlocks);
        }
    }
}
