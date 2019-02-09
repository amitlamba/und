-- client email setting table change

ALTER TABLE client_setting_email DROP COLUMN if EXISTS service_provider_id;

ALTER TABLE client_setting_email
  ADD COLUMN if not exists service_provider_id BIGINT NOT NULL REFERENCES service_provider_credentials (id);

--ALTER TABLE client_setting_email
  --ALTER COLUMN service_provider_id SET NOT NULL ;

-- drop the column from email template table

ALTER TABLE email_template
  DROP COLUMN from_user;

-- add a column in email campaign table client setting id that is not null.

ALTER TABLE email_campaign
  ADD COLUMN client_setting_email_id BIGINT REFERENCES client_setting_email (id);

ALTER TABLE
email_campaign
  ALTER COLUMN client_setting_email_id SET
  NOT NULL;

-- campaign table change

alter table campaign add column if not EXISTS from_user varchar(200) DEFAULT NULL;

alter table campaign add
CONSTRAINT if_email_then_from_user_is_not_null
CHECK (NOT (campaign_type = 'EMAIL' AND   from_user is  NULL));

alter table campaign add
 CONSTRAINT if_email_then_service_provider_is_not_null
 CHECK ( NOT (campaign_type = 'EMAIL' AND service_provider_id IS  NULL ));

-- create system email table

CREATE TABLE if not EXISTS system_email
(
  id                  INTEGER      NOT NULL
    CONSTRAINT system_email_pkey
    PRIMARY KEY,
  name                VARCHAR(256) NOT NULL,
  email_setting_id BIGINT NOT NULL,
  email_template_id BIGINT NOT NULL,
  date_created        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  date_modified       TIMESTAMP WITH TIME ZONE DEFAULT now()
);

