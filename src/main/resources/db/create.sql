create type color as enum ('fawn', 'black', 'other');

CREATE TABLE pug (id UUID PRIMARY KEY,
                  name TEXT NOT NULL,
                  nicknames TEXT[] NOT NULL,
                  picture_url TEXT,
                  color color NOT NULL
);

