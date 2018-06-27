CREATE TABLE IF NOT EXISTS campaign_job_map
(
  id                 SERIAL  NOT NULL  CONSTRAINT "campaign_job_map_pkey"
  PRIMARY KEY,
  clientid           INTEGER NOT NULL,
  campaignid         INTEGER NOT NULL,
  campaignName       VARCHAR NOT NULL,
  cron              VARCHAR NOT NULL,
  start_date         TIMESTAMP WITH TIME ZONE,
  next_creation_date TIMESTAMP WITH TIME ZONE,
  next_start_date    TIMESTAMP WITH TIME ZONE,
  status             VARCHAR,
  deleted            BOOLEAN                  DEFAULT FALSE,
  date_created       TIMESTAMP WITH TIME ZONE DEFAULT now(),
  date_modified      TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX ON campaign_job_map (clientid)
  WHERE deleted = FALSE;
CREATE INDEX ON campaign_job_map (campaignid)
  WHERE deleted = FALSE;

