--lucas spec check

--1
SELECT Date, Tournament, Series, Round, Surface, Court,
       Player_1, Player_2, Winner, Score, Rank_1, Rank_2
FROM data
WHERE LOWER(TRIM(Player_1)) LIKE 'djokovic %'
   OR LOWER(TRIM(Player_2)) LIKE 'djokovic %'
ORDER BY Date DESC;

--2
SELECT Date, Tournament, Round,
       Player_1, Rank_1, Player_2, Rank_2, Winner,
       ABS(CAST(Rank_1 AS INTEGER) - CAST(Rank_2 AS INTEGER)) AS RankGap,
       Score
FROM data
WHERE LOWER(Tournament) LIKE '%wimbledon%'
  AND Rank_1 NOT IN ('-1', '') AND Rank_2 NOT IN ('-1', '')
  AND (
    (Winner = Player_1 AND CAST(Rank_1 AS INTEGER) > CAST(Rank_2 AS INTEGER))
    OR
    (Winner = Player_2 AND CAST(Rank_2 AS INTEGER) > CAST(Rank_1 AS INTEGER))
  )
ORDER BY RankGap DESC
LIMIT 20;


--3
SELECT Date, Tournament, Round, Surface, Court,
       Player_1, Player_2, Winner, Score
FROM data
WHERE (LOWER(TRIM(Player_1)) LIKE 'djokovic %' AND LOWER(TRIM(Player_2)) LIKE 'federer %')
   OR (LOWER(TRIM(Player_1)) LIKE 'federer %'  AND LOWER(TRIM(Player_2)) LIKE 'djokovic %')
ORDER BY Date DESC;


--4
SELECT
    SUM(CASE WHEN LOWER(TRIM(Winner)) LIKE 'djokovic %' THEN 1 ELSE 0 END) AS Djokovic_Wins,
    SUM(CASE WHEN LOWER(TRIM(Winner)) LIKE 'federer %'  THEN 1 ELSE 0 END) AS Federer_Wins,
    COUNT(*) AS Total_Matches
FROM data
WHERE (LOWER(TRIM(Player_1)) LIKE 'djokovic %' AND LOWER(TRIM(Player_2)) LIKE 'federer %')
   OR (LOWER(TRIM(Player_1)) LIKE 'federer %'  AND LOWER(TRIM(Player_2)) LIKE 'djokovic %');


--5
SELECT
    ROUND(
        100.0 * SUM(CASE WHEN LOWER(TRIM(Winner)) LIKE 'nadal %' THEN 1 ELSE 0 END)
        / COUNT(*), 2
    ) AS Win_Pct,
    SUM(CASE WHEN LOWER(TRIM(Winner)) LIKE 'nadal %' THEN 1 ELSE 0 END) AS Wins,
    COUNT(*) AS Total_Matches
FROM data
WHERE (LOWER(TRIM(Player_1)) LIKE 'nadal %' OR LOWER(TRIM(Player_2)) LIKE 'nadal %')
  AND LOWER(Surface) = 'clay';


--6
SELECT
    ROUND(
        100.0 * SUM(CASE WHEN LOWER(TRIM(Winner)) LIKE 'djokovic %' THEN 1 ELSE 0 END)
        / COUNT(*), 2
    ) AS Win_Pct,
    SUM(CASE WHEN LOWER(TRIM(Winner)) LIKE 'djokovic %' THEN 1 ELSE 0 END) AS Wins,
    COUNT(*) AS Total_Matches
FROM data
WHERE (LOWER(TRIM(Player_1)) LIKE 'djokovic %' OR LOWER(TRIM(Player_2)) LIKE 'djokovic %')
  AND LOWER(Court) = 'indoor';


--7
SELECT Date, Tournament, Series, Round, Surface, Court,
       Player_1, Player_2, Winner, Score
FROM data
WHERE Date BETWEEN '2012-06-01' AND '2015-11-04'
ORDER BY Date ASC;


--8
SELECT Tournament, Series, Date, Winner, Score
FROM data
WHERE Round IN ('The Final', 'Finals', 'Final')
  AND strftime('%Y', Date) = '2023'
ORDER BY Date ASC;


--9
SELECT Date, Tournament, Round, Surface,
       Player_1, Rank_1, Player_2, Rank_2, Winner, Score
FROM data
WHERE Series = 'Grand Slam'
  AND (LOWER(TRIM(Player_1)) LIKE 'federer %' OR LOWER(TRIM(Player_2)) LIKE 'federer %')
ORDER BY Date DESC;


--10
SELECT COUNT(*) AS Times_Reached
FROM data
WHERE LOWER(Round) = 'the final'
  AND (LOWER(TRIM(Player_1)) LIKE 'djokovic %' OR LOWER(TRIM(Player_2)) LIKE 'djokovic %');


--11
SELECT Date, Tournament, Series, Round, Surface, Court,
       Player_1, Rank_1, Player_2, Rank_2, Winner, Score
FROM data
WHERE LOWER(Series) LIKE '%masters%'
  AND (LOWER(TRIM(Player_1)) LIKE 'nadal %' OR LOWER(TRIM(Player_2)) LIKE 'nadal %')
ORDER BY Date DESC;
