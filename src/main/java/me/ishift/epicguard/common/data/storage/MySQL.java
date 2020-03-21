/*
 * EpicGuard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EpicGuard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package me.ishift.epicguard.common.data.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.ishift.epicguard.api.EpicGuardAPI;
import me.ishift.epicguard.common.data.DataStorage;
import me.ishift.epicguard.common.data.StorageManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.StringJoiner;

public class MySQL extends DataStorage {
    private HikariDataSource dataSource;

    @Override
    public void load() {
        this.whitelist.add("188.146.103.125");
        this.whitelist.add("88.46.23.20");
        this.whitelist.add("98.7.53.122");
        this.initConnection();
        this.update();
        this.executeUpdate("CREATE TABLE IF NOT EXISTS epicguard_blacklist(`address` TEXT NOT NULL);");
        this.executeUpdate("CREATE TABLE IF NOT EXISTS epicguard_whitelist(`address` TEXT NOT NULL);");
    }

    @Override
    public void save() {
        this.executeUpdate("INSERT INTO `epicguard_whitelist` (`address`) VALUES " + this.fromList(this.whitelist));
        this.executeUpdate("INSERT INTO `epicguard_blacklist` (`address`) VALUES " + this.fromList(this.blacklist));
    }

    public void update() {
        //final ResultSet rs = this.executeQuery("SELECT * from `epicguard_whitelist`");
        StringBuilder update = new StringBuilder();

        update.append("INSTERT INTO `");
        update.append("epicguard_whitelist");
        update.append("` SET address = ");
        update.append(this.fromList(this.whitelist));

        this.executeUpdate(update.toString());
    }

    private void initConnection() {
        final HikariConfig config = new HikariConfig();
        config.addDataSourceProperty("dataSourceClassName", "com.mysql.jdbc.Driver");

        config.setMaximumPoolSize(StorageManager.poolSize);
        config.setConnectionTimeout(StorageManager.connectionTimeout);

        config.setJdbcUrl("jdbc:mysql://" + StorageManager.mysqlHost + ":" + StorageManager.mysqlPort + "/" + StorageManager.mysqlDatabase + "?useSSL=" + StorageManager.mysqlSSL);
        config.setUsername(StorageManager.mysqlUser);
        config.setPassword(StorageManager.mysqlPassword);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);

        this.dataSource = new HikariDataSource(config);
    }

    private ResultSet executeQuery(String query) {
        try {
            System.out.println("Executing query '" + query + "'.");
            final Connection connection = this.dataSource.getConnection();
            final Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException ex) {
            System.out.println("Could not execute query to the database '" + query + "'.");
        }
        return null;
    }

    private void executeUpdate(String query) {
        try {
            final Connection connection = this.dataSource.getConnection();
            final Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            EpicGuardAPI.getLogger().info("Could not execute update to the database '" + query + "'.");
        }
    }

    private String fromList(List<String> list) {
        final StringJoiner joiner = new StringJoiner(", ");
        for (String address : this.whitelist) {
            joiner.add("('" + address + "')");
        }
        return joiner.toString();
    }
}
