ALTER TABLE client_settings  ADD COLUMN IF NOT EXISTS android_app_ids VARCHAR(1024),ADD COLUMN IF NOT EXISTS ios_app_ids VARCHAR (1025);
ALTER TABLE appuser  ADD COLUMN IF NOT EXISTS ioskey VARCHAR(2048),ADD COLUMN IF NOT EXISTS androidkey VARCHAR (2048);
-- ALTER TABLE fcm_failure_audit_log ADD COLUMN IF NOT EXISTS type VARCHAR (50) DEFAULT NULL ;

update email_template set client_id=1, appuser_id=1, parent_id=null, from_user='admin@und.com', message_type='TRANSACTIONAL', tags=null, name='emailConnectionError', email_template_subject=10, email_template_body=9, editor_selected=false WHERE id=5;