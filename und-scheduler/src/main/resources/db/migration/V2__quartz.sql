CREATE TABLE IF NOT EXISTS RECURRENCE_RULE
(
  id                 SERIAL  NOT NULL  CONSTRAINT "recurrence_rule_pkey"
  PRIMARY KEY,
  clientid           INTEGER NOT NULL,
  rid                INTEGER NOT NULL,
  rrule              VARCHAR NOT NULL,
  start_date         TIMESTAMP WITH TIME ZONE,
  next_creation_date TIMESTAMP WITH TIME ZONE,
  next_start_date    TIMESTAMP WITH TIME ZONE,
  status             VARCHAR,
  deleted            BOOLEAN                  DEFAULT FALSE,
  date_created       TIMESTAMP WITH TIME ZONE DEFAULT now(),
  date_modified      TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX ON RECURRENCE_RULE (clientid)
  WHERE deleted = FALSE;
CREATE UNIQUE INDEX unq_cid_rid
  ON RECURRENCE_RULE (clientid, rid)
  WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS RECURRING_DATES
(
  id             SERIAL  NOT NULL
    CONSTRAINT recurring_date_pkey
    PRIMARY KEY,
  clientid       INTEGER NOT NULL,
  occurance_date TIMESTAMP WITH TIME ZONE,
  recurrence_rid INTEGER
    CONSTRAINT recurrence_rid_
    REFERENCES RECURRENCE_RULE (id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  status         VARCHAR,
  deleted        BOOLEAN                  DEFAULT FALSE,
  date_created   TIMESTAMP WITH TIME ZONE DEFAULT now(),
  date_modified  TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX IF NOT EXISTS frecurrence_rid_fkey
  ON RECURRING_DATES (recurrence_rid);

