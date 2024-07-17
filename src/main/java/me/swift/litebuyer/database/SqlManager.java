package me.swift.litebuyer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.bukkit.Bukkit;

public class SqlManager {
    private final String url;

    public SqlManager(String databasePath) {
        this.url = "jdbc:sqlite:" + databasePath;
        this.createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_multipliers (uuid TEXT PRIMARY KEY,multiplier REAL DEFAULT 1.0);";

        try {
            Connection conn = DriverManager.getConnection(this.url);

            try {
                PreparedStatement pstmt = conn.prepareStatement(sql);

                try {
                    pstmt.execute();
                } catch (Throwable var8) {
                    if (pstmt != null) {
                        try {
                            pstmt.close();
                        } catch (Throwable var7) {
                            var8.addSuppressed(var7);
                        }
                    }

                    throw var8;
                }

                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Throwable var9) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                    }
                }

                throw var9;
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException var10) {
            SQLException e = var10;
            Bukkit.getLogger().severe("Could not create table: " + e.getMessage());
        }

    }

    public double getMultiplier(UUID uuid) {
        String sql = "SELECT multiplier FROM player_multipliers WHERE uuid = ?;";

        try {
            Connection conn = DriverManager.getConnection(this.url);

            double var6;
            label114: {
                try {
                    PreparedStatement pstmt;
                    label106: {
                        pstmt = conn.prepareStatement(sql);

                        try {
                            pstmt.setString(1, uuid.toString());
                            ResultSet rs = pstmt.executeQuery();

                            label89: {
                                try {
                                    if (rs.next()) {
                                        var6 = rs.getDouble("multiplier");
                                        break label89;
                                    }
                                } catch (Throwable var11) {
                                    if (rs != null) {
                                        try {
                                            rs.close();
                                        } catch (Throwable var10) {
                                            var11.addSuppressed(var10);
                                        }
                                    }

                                    throw var11;
                                }

                                if (rs != null) {
                                    rs.close();
                                }
                                break label106;
                            }

                            if (rs != null) {
                                rs.close();
                            }
                        } catch (Throwable var12) {
                            if (pstmt != null) {
                                try {
                                    pstmt.close();
                                } catch (Throwable var9) {
                                    var12.addSuppressed(var9);
                                }
                            }

                            throw var12;
                        }

                        if (pstmt != null) {
                            pstmt.close();
                        }
                        break label114;
                    }

                    if (pstmt != null) {
                        pstmt.close();
                    }
                } catch (Throwable var13) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var8) {
                            var13.addSuppressed(var8);
                        }
                    }

                    throw var13;
                }

                if (conn != null) {
                    conn.close();
                }

                return 1.0;
            }

            if (conn != null) {
                conn.close();
            }

            return var6;
        } catch (SQLException var14) {
            SQLException e = var14;
            Bukkit.getLogger().severe("Could not get multiplier: " + e.getMessage());
            return 1.0;
        }
    }

    public void updateMultiplier(UUID uuid, double multiplier) {
        String sql = "INSERT INTO player_multipliers (uuid, multiplier) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET multiplier = excluded.multiplier;";

        try {
            Connection conn = DriverManager.getConnection(this.url);

            try {
                PreparedStatement pstmt = conn.prepareStatement(sql);

                try {
                    pstmt.setString(1, uuid.toString());
                    pstmt.setDouble(2, multiplier);
                    pstmt.executeUpdate();
                } catch (Throwable var11) {
                    if (pstmt != null) {
                        try {
                            pstmt.close();
                        } catch (Throwable var10) {
                            var11.addSuppressed(var10);
                        }
                    }

                    throw var11;
                }

                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Throwable var12) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                    }
                }

                throw var12;
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException var13) {
            SQLException e = var13;
            Bukkit.getLogger().severe("Could not update multiplier: " + e.getMessage());
        }

    }
}
