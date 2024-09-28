package me.lorendel.nanopromocode.Db;
import me.lorendel.nanopromocode.Models.PlayerPromocodeStats;
import me.lorendel.nanopromocode.Models.Promocodes;
import me.lorendel.nanopromocode.NanoPromocode;
import org.bukkit.entity.Player;

import java.sql.*;

public class Database {

    private Connection connection;
    private final NanoPromocode plugin;

    public Database(NanoPromocode plugin) {
        this.plugin = plugin;
    }

    public Connection getConnection() throws SQLException {

        if (connection != null) {
            return connection;

        }

        String dbIp = this.plugin.getConfig().getString("Database.Address");
        String port = this.plugin.getConfig().getString("Database.Port");
        String db = this.plugin.getConfig().getString("Database.Database");

        String url = "jdbc:mysql://" + dbIp + ":" + port + "/" + db;
        String user = this.plugin.getConfig().getString("Database.Username");
        String password = this.plugin.getConfig().getString("Database.Password");

        this.connection = DriverManager.getConnection(url, user, password);
        System.out.println("Connected to the practice_code database!");

        return this.connection;

    }

    public void initializeDatabase() throws SQLException {

        Statement statement = getConnection().createStatement();
        String sql1 = "CREATE TABLE IF NOT EXISTS promocode_data(uuid varchar(36) primary key, date_used DATE, promocode varchar(255))";
        statement.execute(sql1);
        String sql2 = " CREATE TABLE IF NOT EXISTS promocodes(promocodes varchar(255) primary key, date_created DATE, count_used int)";
        statement.execute(sql2);
        System.out.println("Created the promocode_data and promocodes tables in the database!");
        statement.close();
    }

    public PlayerPromocodeStats findPromocodePlayerStatsByUUID(String uuid) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM promocode_data WHERE uuid = ?");
        statement.setString(1, uuid);
        ResultSet results = statement.executeQuery();

        if (results.next()) {

            Date dateUsed = results.getDate("date_used");
            String promocode = results.getString("promocode");

            PlayerPromocodeStats playerPromocodeStats = new PlayerPromocodeStats(uuid, dateUsed, promocode);
            statement.close();
            return playerPromocodeStats;

        }

        statement.close();
        return null;

    }

    public Promocodes findPromocodeByName(String promocode) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM promocodes WHERE promocodes = ?");
        statement.setString(1, promocode);
        ResultSet results = statement.executeQuery();

        if (results.next()) {

            Date date_used = results.getDate("date_created");
            Integer count_used = results.getInt("count_used");
            Promocodes promocodes = new Promocodes(promocode, date_used, count_used);
            statement.close();
            return promocodes;

        }

        statement.close();
        return null;

    }


    public void createPlayerPromocodeStats(PlayerPromocodeStats stats) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("INSERT INTO promocode_data(uuid, date_used, promocode) VALUES (?, ?, ?)");
        statement.setString(1, stats.getUuid());
        statement.setDate(2, new Date(stats.getDateUsed().getTime()));
        statement.setString(3, stats.getPromocode());

        statement.executeUpdate();
        statement.close();

    }

    public void deletePlayerPromocodeStats(Player p) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM promocode_data WHERE uuid = ?");
        statement.setString(1, String.valueOf(p.getUniqueId()));
        statement.executeUpdate();
        statement.close();

    }

    public void createPromocode(Promocodes promocodes) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("INSERT INTO promocodes(promocodes, date_created, count_used) VALUES (?, ?, ?)");
        statement.setString(1, promocodes.getPromocodes());
        statement.setDate(2, new Date(promocodes.getDateCreated().getTime()));
        statement.setInt(3, promocodes.getCountUsed());
        statement.executeUpdate();
        statement.close();
    }

    public void updatePromocode(Promocodes promocodes) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "UPDATE promocodes SET count_used = ? WHERE promocodes = ?")) {
            statement.setInt(1, promocodes.getCountUsed());
            statement.setString(2, promocodes.getPromocodes());
            statement.executeUpdate();
        }
    }

}