package org.black_ixx.playerpoints.conversion.converter;

import dev.rosewood.rosegarden.RosePlugin;
import org.black_ixx.playerpoints.conversion.CurrencyConverter;
import org.black_ixx.playerpoints.manager.DataManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SuperCreditsConverter extends CurrencyConverter {

    public SuperCreditsConverter(RosePlugin rosePlugin) {
        super(rosePlugin, "SuperCredits");
    }

    // === CONFIGURATION ===
    private static final String SUPER_CREDITS_DB_URL = "jdbc:mysql://10.20.0.198:3306/db_84671_global";
    private static final String SUPER_CREDITS_DB_USER = "db_84671_global";
    private static final String SUPER_CREDITS_DB_PASSWORD = "2cP7jpsD!";
    private static final String SUPER_CREDITS_TABLE = "supercredits"; // Replace with your actual table name

    @Override
    @SuppressWarnings("unchecked")
    public void convert(String currencyId) {
        if (!Objects.equals(currencyId, "KCT")) return;

        Map<UUID, Integer> points = new HashMap<>();
        Map<UUID, String> usernames = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(
                SUPER_CREDITS_DB_URL, SUPER_CREDITS_DB_USER, SUPER_CREDITS_DB_PASSWORD)) {

            String query = "SELECT uuid, name, value FROM " + SUPER_CREDITS_TABLE;
            try (PreparedStatement statement = conn.prepareStatement(query);
                 ResultSet result = statement.executeQuery()) {

                this.rosePlugin.getLogger().warning("Converting data from SuperCredits, this may take a while if you have a lot of data...");


                while (result.next()) {
                    try {
                        UUID uuid = UUID.fromString(result.getString("uuid"));
                        String name = result.getString("name");
                        int credits = result.getInt("value");

                        points.put(uuid, credits);
                        usernames.put(uuid, name);

                    } catch (Exception e) {
                        // Handle bad UUIDs or other formatting issues
                        plugin.getLogger().warning("Skipping row due to error: " + e.getMessage());
                    }
                }
            }

        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to connect or read SuperCredits data: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        DataManager dataManager = this.rosePlugin.getManager(DataManager.class);
        dataManager.importData(points, usernames);
        plugin.getLogger().info("✅ SuperCredits migration completed. Imported " + points.size() + " players.");
    }
}
