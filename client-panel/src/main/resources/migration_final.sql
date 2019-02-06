--- migration code ---
UPDATE client_setting_email
SET service_provider_id = sps.id
FROM service_provider_credentials sps
  INNER JOIN client_setting_email csm ON sps.client_id = csm.client_id AND sps.isdefault
WHERE client_setting_email.client_id = sps.client_id AND
      sps.service_provider_type = 'Email Service Provider' AND sps.isdefault;
;

DELETE FROM client_setting_email
WHERE service_provider_id IS NULL;
DELETE FROM campaign_audit_log
WHERE campaign_id IN (SELECT id
                      FROM campaign c
                      WHERE c.service_provider_id IS NULL AND c.from_user IS NOT NULL);
DELETE FROM campaign
WHERE service_provider_id IS NULL AND from_user IS NOT NULL;




UPDATE email_campaign
SET client_setting_email_id = csm.id
FROM client_setting_email csm

WHERE email_campaign.client_id = csm.client_id;
--------------migration code ends --------

ALTER TABLE client_setting_email
  ADD COLUMN service_provider_id BIGINT REFERENCES service_provider_credentials (id);
ALTER TABLE email_template
  DROP COLUMN from_user;

ALTER TABLE email_campaign
  ADD COLUMN client_setting_email_id BIGINT REFERENCES client_setting_email (id);

ALTER TABLE
email_campaign
  ALTER COLUMN client_setting_email_id SET
  NOT NULL;


CREATE TABLE system_email
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

INSERT INTO service_provider_credentials
(id, client_id, appuser_id, service_provider_type,
 service_provider, date_created, date_modified, status, credentials, isdefault, name)
VALUES (1, 1, 1, 'Email Service Provider', 'SMTP', now(), now(), 'ACTIVE', '{
  "password" : "Userndot1@",
  "port" : "465",
  "url" : "smtp.gmail.com",
  "username" : "userndot19@gmail.com"
}', true, 'default');

INSERT INTO client_setting_email (id, client_id, address, email, verified,
                                  deleted, date_created, date_modified, service_provider_id)
VALUES (1, 1, 'Userndot', 'userndot19@gmail.com', true, false, now(), now(), 1),
  (4, 1, 'Userndot', 'admin@userndot.com', true, false, now(), now(), 1),
  (5, 1, 'Userndot', 'admin@und.com', true, false, now(), now(), 1)

;

insert into system_email (id, name, email_setting_id, email_template_id)
VALUES (1 ,'forgotpassword', 1, 1),
  (2 ,'verificationemail', 2, 2),
  (3 ,'contactus', 1, 3),
  (4 ,'support', 4, 4),
  (5 ,'emailConnectionError', 5, 5),
  (6 ,'fromEmailVerification', 6, 4);