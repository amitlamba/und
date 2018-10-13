alter table  email_failure_audit_log drop constraint email_failure_audit_log_campaign_id_fkey;
alter table  email_failure_audit_log add  foreign key  (client_setting_email_id)references service_provider_credentials;
