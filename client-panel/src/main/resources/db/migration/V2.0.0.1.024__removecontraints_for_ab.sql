
-- alter table email_campaign alter column email_template_id bigint constraint email_campaign_email_template_id_fkey references email_template;
alter table email_campaign alter column email_template_id drop not null ;
alter table sms_campaign alter column sms_template_id drop not null ;
alter table android_campaign alter column template_id drop not null ;
alter table webpush_campaign_table alter column template_id drop not null ;

