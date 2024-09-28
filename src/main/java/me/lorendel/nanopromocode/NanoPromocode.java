package me.lorendel.nanopromocode;
import me.lorendel.nanopromocode.Commands.PromocodeCommand;
import me.lorendel.nanopromocode.Db.Database;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.sql.SQLException;
import java.util.Objects;

public final class NanoPromocode extends JavaPlugin {

    private Database database;
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;


    @Override
    public void onEnable() {

        System.out.println("NanoPromocode plugin has started!");
        Objects.requireNonNull(getCommand("promo")).setExecutor(new PromocodeCommand(this));
        Objects.requireNonNull(getCommand("promo")).setTabCompleter(new PromocodeCommand(this));
        saveDefaultConfig();
        loadCustomConfig();

        try {
            this.database = new Database(this);
            database.initializeDatabase();
        }catch (SQLException e){
            System.out.println("Unable to connect to the database and create tables!");
            e.printStackTrace();
        }
    }
    public Database getDatabase() {
        return database;
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }
    public void loadCustomConfig() {
        if (customConfigFile == null){
            customConfigFile = new File(getDataFolder(), "language.yml");
        }

        if (!customConfigFile.exists()){
            saveResource("language.yml", false);
        }

        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

    }
    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            loadCustomConfig();
        }
        return customConfig;
    }
}