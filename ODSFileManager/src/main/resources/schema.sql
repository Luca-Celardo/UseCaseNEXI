DROP TABLE IF EXISTS readyOds;

CREATE TABLE IF NOT EXISTS readyOds (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(250) NOT NULL,
    type VARCHAR(250) NOT NULL
);