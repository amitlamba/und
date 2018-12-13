alter table segment ADD UNIQUE (client_id,name);
alter table campaign ADD UNIQUE (client_id,name);
alter table email_template ADD UNIQUE (client_id,name);
alter table sms_template ADD UNIQUE (client_id,name);
alter table notification_template_android ADD UNIQUE (client_id,name);
alter table notification_template_webpush ADD UNIQUE (client_id,name);