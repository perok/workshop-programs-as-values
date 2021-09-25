CREATE TABLE IF NOT EXISTS user_account
(
  id       SERIAL      NOT NULL,
  email    VARCHAR(80) NOT NULL,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(60) NOT NULL,

  created  TIMESTAMP   NOT NULL DEFAULT NOW(),

  PRIMARY KEY (id),
  UNIQUE (email, username)
);

CREATE TABLE IF NOT EXISTS troop
(
  id   SERIAL,
  name VARCHAR(80) NOT NULL,

  created  TIMESTAMP   NOT NULL DEFAULT NOW(),

  PRIMARY KEY (id),
  UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS movie
(
  id    SERIAL, -- TheMovieDB ID
  title VARCHAR(80) NOT NULL,
--  data  JSONB       NOT NULL,

  PRIMARY KEY (id)
);

-- Relations
CREATE TABLE IF NOT EXISTS user_is_in_troop
(
  id       SERIAL,
  created  TIMESTAMP   NOT NULL DEFAULT NOW(),

  user_id  INT NOT NULL REFERENCES user_account (id) ON DELETE CASCADE,
  troop_id INT NOT NULL REFERENCES troop (id) ON DELETE CASCADE,
  is_admin BOOLEAN DEFAULT FALSE,

  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS troop_has_movie
(
  id        SERIAL,
  created   TIMESTAMP   NOT NULL DEFAULT NOW(),

  troop_id  INT NOT NULL REFERENCES troop (id) ON DELETE CASCADE,
  movie_id  INT NOT NULL REFERENCES movie (id),

  is_seen   BOOLEAN DEFAULT FALSE,
  date_seen DATE, -- TODO timestamp??

  PRIMARY KEY (id),
  -- A troop can only contain one of the same movie
  UNIQUE (troop_id, movie_id)
);

-- Can also be used as users settings on one movie in a particular troop
CREATE TABLE IF NOT EXISTS movie_in_troop_has_user_vote
(
  id             SERIAL,
  created        TIMESTAMP   NOT NULL DEFAULT NOW(),

  troop_movie_id INT NOT NULL REFERENCES troop_has_movie (id) ON DELETE CASCADE,
  user_id        INT NOT NULL REFERENCES user_account (id) ON DELETE CASCADE,

  -- TODO should this be boolean or tristate? (not checked, true, false)
  positive       BOOLEAN DEFAULT FALSE,

  PRIMARY KEY (id)
  UNIQUE(troop_movie_id, user_id)
);
