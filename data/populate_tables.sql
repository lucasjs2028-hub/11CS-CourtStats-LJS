-- CourtStats: Populate normalized tables from raw `data` table
-- Run this in DataGrip against data.db

-- Clear existing data (safe to re-run)
DELETE FROM Matches;
DELETE FROM Tournaments;
DELETE FROM Players;
DELETE FROM sqlite_sequence WHERE name IN ('Matches','Tournaments','Players');

-- 1. Populate Players (all unique player names from both columns)
INSERT INTO Players (PlayerName)
SELECT p FROM (
    SELECT Player_1 AS p FROM data
    UNION
    SELECT Player_2 FROM data
)
ORDER BY p;

-- 2. Populate Tournaments (unique by name+series+surface+court)
INSERT INTO Tournaments (TournamentName, Series, Surface, Court)
SELECT DISTINCT Tournament, Series, Surface, Court
FROM data
ORDER BY Tournament;

-- 3. Populate Matches (link to Players and Tournaments via FK)
INSERT INTO Matches (TournamentID, Date, WinnerID, LoserID, WinnerRank, LoserRank, Round)
SELECT
    t.TournamentID,
    d.Date,
    w.PlayerID,
    l.PlayerID,
    CASE WHEN CAST(d.Rank_1 AS INTEGER) < 0 THEN NULL ELSE CAST(d.Rank_1 AS INTEGER) END,
    CASE WHEN CAST(d.Rank_2 AS INTEGER) < 0 THEN NULL ELSE CAST(d.Rank_2 AS INTEGER) END,
    d.Round
FROM data d
JOIN Tournaments t
    ON t.TournamentName = d.Tournament
    AND t.Series       = d.Series
    AND t.Surface      = d.Surface
    AND t.Court        = d.Court
JOIN Players w ON w.PlayerName = d.Winner
JOIN Players l ON l.PlayerName = CASE
    WHEN d.Winner = d.Player_1 THEN d.Player_2
    ELSE d.Player_1
END;

-- Verify counts
SELECT 'Players'     AS tbl, COUNT(*) AS rows FROM Players
UNION ALL
SELECT 'Tournaments', COUNT(*) FROM Tournaments
UNION ALL
SELECT 'Matches',     COUNT(*) FROM Matches;
