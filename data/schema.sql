
CREATE TABLE Players (
    PlayerID   INTEGER PRIMARY KEY AUTOINCREMENT,
    PlayerName VARCHAR(100) NOT NULL UNIQUE,
    Ranking    INTEGER
);



CREATE TABLE Tournaments (
    TournamentID   INTEGER PRIMARY KEY AUTOINCREMENT,
    TournamentName VARCHAR(150) NOT NULL,
    Series         VARCHAR(50)  NOT NULL,
    Surface        VARCHAR(20)  NOT NULL,
    Court          VARCHAR(20)  NOT NULL
);



CREATE TABLE Matches (
    MatchID      INTEGER PRIMARY KEY AUTOINCREMENT,
    TournamentID INTEGER,
    Date         DATE    NOT NULL,
    WinnerID     INTEGER,
    LoserID      INTEGER,
    WinnerRank   INTEGER,
    LoserRank    INTEGER,
    Round        VARCHAR(20) NOT NULL,
    FOREIGN KEY (TournamentID) REFERENCES Tournaments(TournamentID),
    FOREIGN KEY (WinnerID)     REFERENCES Players(PlayerID),
    FOREIGN KEY (LoserID)      REFERENCES Players(PlayerID)
);



CREATE INDEX idx_player_names       ON Players     (PlayerName);
CREATE INDEX idx_tournament_names   ON Tournaments (TournamentName);
CREATE INDEX idx_match_date         ON Matches     (Date);
