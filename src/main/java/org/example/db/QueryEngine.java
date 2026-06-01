package org.example.db;

import java.sql.*;
import java.util.*;

/**
 * All SQL queries for CourtStats. Each method returns a list of String arrays
 * (rows), where index 0 is always the column-headers row.
 *
 * Player name matching uses word-boundary search (not substring):
 *   "tien" matches "Tien L." but NOT "Lestienne C." or "Patience O."
 */
public class QueryEngine {

    // ── Name-matching helpers ────────────────────────────────────────────────

    /**
     * SQL condition for word-boundary name match against one column.
     * Consumes 2 PreparedStatement params (see np()).
     *   param 1 → name + " %"   (last-name-first format: "Tien L.", "Djokovic N.")
     *   param 2 → name          (exact match for names with no initial)
     */
    private static String nm(String col) {
        String c = "LOWER(TRIM(" + col + "))";
        return "(" + c + " LIKE LOWER(?) OR " + c + " = LOWER(?))";
    }

    /**
     * Sets 2 params for one nm() condition. Returns the next free param index.
     */
    private static int np(PreparedStatement ps, int i, String name) throws SQLException {
        ps.setString(i,     name + " %");
        ps.setString(i + 1, name);
        return i + 2;
    }

    // ── Resolve a partial name to the real name(s) stored in the DB ─────────

    /**
     * Returns matching player names, sorted so full names come before initials
     * (e.g. "Novak Djokovic" before "N. Djokovic") then longest first.
     */
    public static List<String> resolvePlayerNames(String pattern) throws SQLException {
        // nm() uses 2 params per column; UNION needs it for both Player_1 and Player_2
        String sql =
            "SELECT DISTINCT TRIM(p) AS p FROM (" +
            "  SELECT Player_1 AS p FROM data WHERE " + nm("Player_1") +
            "  UNION" +
            "  SELECT Player_2 FROM data WHERE " + nm("Player_2") +
            ") ORDER BY" +
            "  CASE WHEN TRIM(p) NOT LIKE '%.' THEN 0 ELSE 1 END," + // full names first
            "  LENGTH(TRIM(p)) DESC," +                               // longer = more complete
            "  TRIM(p) ASC" +
            " LIMIT 10";

        List<String> names = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            int i = 1;
            i = np(ps, i, pattern); // Player_1
            i = np(ps, i, pattern); // Player_2
            ResultSet rs = ps.executeQuery();
            while (rs.next()) names.add(rs.getString(1));
        }
        return names;
    }

    // ── Design Spec 1 ── find all matches for a player ──────────────────────

    public static List<String[]> searchByPlayer(String name) throws SQLException {
        String sql =
            "SELECT d.Date, d.Tournament, d.Series, d.Round, d.Surface, d.Court," +
            "       d.Player_1, d.Player_2, d.Winner, d.Score, d.Rank_1, d.Rank_2" +
            " FROM data d" +
            " WHERE " + nm("d.Player_1") + " OR " + nm("d.Player_2") +
            " ORDER BY d.Date DESC";

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Date","Tournament","Series","Round","Surface",
                "Court","Player 1","Player 2","Winner","Score","Rank 1","Rank 2"});
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            int i = 1;
            i = np(ps, i, name); // Player_1
            i = np(ps, i, name); // Player_2
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString(1),  rs.getString(2),  rs.getString(3),
                    rs.getString(4),  rs.getString(5),  rs.getString(6),
                    rs.getString(7),  rs.getString(8),  rs.getString(9),
                    rs.getString(10), rs.getString(11), rs.getString(12)
                });
            }
        }
        return rows;
    }

    // ── Design Spec 2 ── biggest upset in a tournament ───────────────────────
    // Tournament uses substring search intentionally (partial tournament names are common)

    public static List<String[]> biggestUpset(String tournamentLike) throws SQLException {
        String sql =
            "SELECT d.Date, d.Tournament, d.Round," +
            "       d.Player_1, d.Rank_1, d.Player_2, d.Rank_2, d.Winner," +
            "       ABS(CAST(d.Rank_1 AS INTEGER) - CAST(d.Rank_2 AS INTEGER)) AS RankGap," +
            "       d.Score" +
            " FROM data d" +
            " WHERE LOWER(d.Tournament) LIKE LOWER(?)" +
            "   AND d.Rank_1 NOT IN ('-1','') AND d.Rank_2 NOT IN ('-1','')" +
            "   AND (" +
            "     (d.Winner = d.Player_1 AND CAST(d.Rank_1 AS INTEGER) > CAST(d.Rank_2 AS INTEGER))" +
            "     OR" +
            "     (d.Winner = d.Player_2 AND CAST(d.Rank_2 AS INTEGER) > CAST(d.Rank_1 AS INTEGER))" +
            "   )" +
            " ORDER BY RankGap DESC LIMIT 20";

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Date","Tournament","Round","Player 1","Rank 1",
                "Player 2","Rank 2","Winner","Rank Gap","Score"});
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1, "%" + tournamentLike + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6),
                    rs.getString(7), rs.getString(8), rs.getString(9),
                    rs.getString(10)
                });
            }
        }
        return rows;
    }

    // ── Design Spec 3 ── H2H between two players ─────────────────────────────

    public static List<String[]> headToHead(String p1, String p2) throws SQLException {
        // (Player_1=p1 AND Player_2=p2) OR (Player_1=p2 AND Player_2=p1)
        String sql =
            "SELECT d.Date, d.Tournament, d.Round, d.Surface, d.Court," +
            "       d.Player_1, d.Player_2, d.Winner, d.Score" +
            " FROM data d" +
            " WHERE (" + nm("d.Player_1") + " AND " + nm("d.Player_2") + ")" +
            "    OR (" + nm("d.Player_1") + " AND " + nm("d.Player_2") + ")" +
            " ORDER BY d.Date DESC";

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Date","Tournament","Round","Surface","Court",
                "Player 1","Player 2","Winner","Score"});
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            int i = 1;
            i = np(ps, i, p1); // first  branch: Player_1 = p1
            i = np(ps, i, p2); // first  branch: Player_2 = p2
            i = np(ps, i, p2); // second branch: Player_1 = p2
            i = np(ps, i, p1); // second branch: Player_2 = p1
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6),
                    rs.getString(7), rs.getString(8), rs.getString(9)
                });
            }
        }
        return rows;
    }

    // ── Design Spec 4 ── win % on a surface ──────────────────────────────────

    public static List<String[]> winRateBySurface(String player, String surface) throws SQLException {
        // nm() used twice in SELECT (both CASE WHEN) and twice in WHERE
        String sql =
            "SELECT" +
            "  ROUND(100.0 * SUM(CASE WHEN " + nm("d.Winner") + " THEN 1 ELSE 0 END) / COUNT(*), 2) AS WinPct," +
            "  SUM(CASE WHEN " + nm("d.Winner") + " THEN 1 ELSE 0 END) AS Wins," +
            "  COUNT(*) AS Total" +
            " FROM data d" +
            " WHERE (" + nm("d.Player_1") + " OR " + nm("d.Player_2") + ")" +
            "   AND LOWER(d.Surface) = LOWER(?)";

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Win %","Wins","Total Matches"});
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            int i = 1;
            i = np(ps, i, player); // Winner in WinPct CASE WHEN
            i = np(ps, i, player); // Winner in Wins CASE WHEN
            i = np(ps, i, player); // Player_1 in WHERE
            i = np(ps, i, player); // Player_2 in WHERE
            ps.setString(i, surface);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rows.add(new String[]{
                    rs.getString(1) + "%", rs.getString(2), rs.getString(3)
                });
            }
        }
        return rows;
    }

    // ── Design Spec 5 ── win % by court condition ────────────────────────────

    public static List<String[]> winRateByCourt(String player, String court) throws SQLException {
        String sql =
            "SELECT" +
            "  ROUND(100.0 * SUM(CASE WHEN " + nm("d.Winner") + " THEN 1 ELSE 0 END) / COUNT(*), 2) AS WinPct," +
            "  SUM(CASE WHEN " + nm("d.Winner") + " THEN 1 ELSE 0 END) AS Wins," +
            "  COUNT(*) AS Total" +
            " FROM data d" +
            " WHERE (" + nm("d.Player_1") + " OR " + nm("d.Player_2") + ")" +
            "   AND LOWER(d.Court) = LOWER(?)";

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Win %","Wins","Total Matches"});
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            int i = 1;
            i = np(ps, i, player); // Winner in WinPct CASE WHEN
            i = np(ps, i, player); // Winner in Wins CASE WHEN
            i = np(ps, i, player); // Player_1 in WHERE
            i = np(ps, i, player); // Player_2 in WHERE
            ps.setString(i, court);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rows.add(new String[]{
                    rs.getString(1) + "%", rs.getString(2), rs.getString(3)
                });
            }
        }
        return rows;
    }

    // ── Design Spec 6 ── filter by date range ────────────────────────────────
    // Date filter has no player search, no change needed.

    public static List<String[]> filterByDateRange(String from, String to) throws SQLException {
        String sql =
            "SELECT d.Date, d.Tournament, d.Series, d.Round, d.Surface, d.Court," +
            "       d.Player_1, d.Player_2, d.Winner, d.Score" +
            " FROM data d" +
            " WHERE d.Date BETWEEN ? AND ?" +
            " ORDER BY d.Date ASC";

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Date","Tournament","Series","Round","Surface",
                "Court","Player 1","Player 2","Winner","Score"});
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1, from);
            ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6),
                    rs.getString(7), rs.getString(8), rs.getString(9),
                    rs.getString(10)
                });
            }
        }
        return rows;
    }

    // ── Design Spec 7 ── tournament winners by year ───────────────────────────

    public static List<String[]> tournamentWinnersByYear(String year) throws SQLException {
        String sql =
            "SELECT d.Tournament, d.Series, d.Date, d.Winner, d.Score" +
            " FROM data d" +
            " WHERE d.Round IN ('The Final', 'Finals', 'Final')" +
            "   AND strftime('%Y', d.Date) = ?" +
            " ORDER BY d.Date ASC";

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Tournament","Series","Date","Champion","Score"});
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5)
                });
            }
        }
        return rows;
    }

    // ── Design Spec 8 ── filter grand slams only ─────────────────────────────

    public static List<String[]> grandSlamMatches(String playerFilter) throws SQLException {
        boolean fp = playerFilter != null && !playerFilter.isBlank();
        String sql =
            "SELECT d.Date, d.Tournament, d.Round, d.Surface," +
            "       d.Player_1, d.Rank_1, d.Player_2, d.Rank_2, d.Winner, d.Score" +
            " FROM data d" +
            " WHERE d.Series = 'Grand Slam'" +
            (fp ? " AND (" + nm("d.Player_1") + " OR " + nm("d.Player_2") + ")" : "") +
            " ORDER BY d.Date DESC";

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Date","Tournament","Round","Surface",
                "Player 1","Rank 1","Player 2","Rank 2","Winner","Score"});
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            if (fp) {
                int i = 1;
                i = np(ps, i, playerFilter); // Player_1
                i = np(ps, i, playerFilter); // Player_2
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6),
                    rs.getString(7), rs.getString(8), rs.getString(9),
                    rs.getString(10)
                });
            }
        }
        return rows;
    }

    // ── Design Spec 9 ── times player reached a certain round ────────────────

    public static List<String[]> timesReachedRound(String player, String round) throws SQLException {
        String sql =
            "SELECT COUNT(*) AS TimesReached" +
            " FROM data d" +
            " WHERE LOWER(d.Round) = LOWER(?)" +
            "   AND (" + nm("d.Player_1") + " OR " + nm("d.Player_2") + ")";

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Times Reached"});
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1, round);
            int i = 2;
            i = np(ps, i, player); // Player_1
            i = np(ps, i, player); // Player_2
            ResultSet rs = ps.executeQuery();
            if (rs.next()) rows.add(new String[]{rs.getString(1)});
        }
        return rows;
    }

    // ── Design Spec 10 ── filter by tournament level ─────────────────────────
    // Level uses substring search (e.g. "Masters" matches "Masters 1000")

    public static List<String[]> filterByLevel(String player, String level) throws SQLException {
        boolean fp = player != null && !player.isBlank();
        String sql =
            "SELECT d.Date, d.Tournament, d.Series, d.Round, d.Surface, d.Court," +
            "       d.Player_1, d.Rank_1, d.Player_2, d.Rank_2, d.Winner, d.Score" +
            " FROM data d" +
            " WHERE LOWER(d.Series) LIKE LOWER(?)" +
            (fp ? " AND (" + nm("d.Player_1") + " OR " + nm("d.Player_2") + ")" : "") +
            " ORDER BY d.Date DESC";

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Date","Tournament","Series","Round","Surface",
                "Court","Player 1","Rank 1","Player 2","Rank 2","Winner","Score"});
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1, "%" + level + "%");
            if (fp) {
                int i = 2;
                i = np(ps, i, player); // Player_1
                i = np(ps, i, player); // Player_2
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString(1),  rs.getString(2),  rs.getString(3),
                    rs.getString(4),  rs.getString(5),  rs.getString(6),
                    rs.getString(7),  rs.getString(8),  rs.getString(9),
                    rs.getString(10), rs.getString(11), rs.getString(12)
                });
            }
        }
        return rows;
    }
}
