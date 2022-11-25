CREATE TABLE IF NOT EXISTS settings(
  user_id VARCHAR NOT NULL PRIMARY KEY,
  blocked BOOLEAN,
  billing_card_name VARCHAR,
  billing_card_num VARCHAR,
  billing_card_exp_date VARCHAR,
  billing_card_sec_code INTEGER,
  dark_theme BOOLEAN,
  marketing_emails BOOLEAN,
  display_name VARCHAR
);

CREATE TABLE IF NOT EXISTS cats(
  id VARCHAR NOT NULL PRIMARY KEY,
  name VARCHAR NOT NULL,
  sex BOOLEAN,
  age INTEGER
);
