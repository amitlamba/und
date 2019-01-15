ALTER TABLE client_settings  ADD COLUMN IF NOT EXISTS android_app_ids VARCHAR(1024),ADD COLUMN IF NOT EXISTS ios_app_ids VARCHAR (1025);
ALTER TABLE appuser  ADD COLUMN IF NOT EXISTS ioskey VARCHAR(2048),ADD COLUMN IF NOT EXISTS androidkey VARCHAR (2048);
-- ALTER TABLE fcm_failure_audit_log ADD COLUMN IF NOT EXISTS type VARCHAR (50) DEFAULT NULL ;

insert into email_template (id, client_id, appuser_id, parent_id, from_user, message_type, tags, name, email_template_subject, email_template_body, editor_selected) VALUES
(5,1,1,null,'admin@und.com','TRANSACTIONAL',null,'emailConnectionError',10,9,false);