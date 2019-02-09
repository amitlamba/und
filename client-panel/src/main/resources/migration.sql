--- migration code ---
select id, "from_user" from email_template;
select * from email_campaign;

select id, client_id, campaign_type, service_provider_id, from_user from campaign where campaign_type='EMAIL';

delete from campaign_audit_log where campaign_id in (select id  from campaign where service_provider_id is null and campaign.from_user is not null);
delete from campaign where service_provider_id is null and campaign.from_user is not null;

select id, client_id from service_provider_credentials where service_provider_type='Email Service Provider' and isdefault;

select id, client_id, service_provider_id from client_setting_email;

select  csm.id, csm.client_id, spc.client_id,csm.service_provider_id,  spc.id
from client_setting_email csm
INNER JOIN service_provider_credentials spc on spc.client_id = csm.client_id
WHERE
 service_provider_type='Email Service Provider' and isdefault;
select * from client_setting_email;
select * from campaign where (campaign_type = 'EMAIL') AND (from_user IS  NULL OR service_provider_id is   NULL);
--------------migration code ends --------

alter table client_setting_email add column service_provider_id BIGINT REFERENCES service_provider_credentials(id);
alter table email_template drop COLUMN  from_user;
alter table campaign add column from_user varchar(200) DEFAULT NULL;


alter table campaign add
CONSTRAINT if_email_then_from_user_is_not_null
CHECK (NOT (campaign_type = 'EMAIL' AND   from_user is  NULL));

alter table campaign add
 CONSTRAINT if_email_then_service_provider_is_not_null
 CHECK ( NOT (campaign_type = 'EMAIL' AND service_provider_id IS  NULL ));


update client_setting_email
set  service_provider_id = sps.id
FROM service_provider_credentials sps
inner join client_setting_email csm on  sps.client_id = csm.client_id and sps.isdefault
where client_setting_email.client_id = sps.client_id and
sps.service_provider_type='Email Service Provider' and sps.isdefault;
;

delete from client_setting_email where client_setting_email.service_provider_id is null;
update campaign
set  service_provider_id = csm.service_provider_id,
 from_user = csm.email
FROM client_setting_email csm

where campaign.client_id = csm.client_id
and campaign_type='EMAIL'
;

update client_setting_email
set  service_provider_id = null;

select * from campaign where campaign_type='EMAIL';

/*
1014	1
1044	3
1110	1020
1105	1018
1113	1022

 */

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
 (2, 1, 'Userndot', 'userndot19@gmail.com', true, false, now(), now(), 1),
 (3, 1, 'Userndot', 'userndot19@gmail.com', true, false, now(), now(), 1),
 (4, 1, 'Userndot', 'admin@userndot.com', true, false, now(), now(), 1),
 (5, 1, 'Userndot', 'admin@und.com', true, false, now(), now(), 1),
 (6, 1, 'Userndot', 'admin@userndot.com', true, false, now(), now(), 1)
;

insert into system_email (id, name, email_setting_id, email_template_id)
VALUES (1 ,'forgotpassword', 1, 1),
 (2 ,'verificationemail', 2, 2),
 (3 ,'contactus', 3, 3),
 (4 ,'support', 4, 4),
 (5 ,'emailConnectionError', 5, 5),
 (6 ,'fromEmailVerification', 6, 6);


/*

    ,
 (2 'verification', 1, 2),
 (3 'contactus', 1, 3),
 (4 'support', 1, 4);
        const val forgotPasswordTemplate = 1L
        const val verificationTemplate = 2L
        const val contactusTemplate = 3L
        const val supportTemplate=4L

userndot19@gmail.com,forgotpassword,1
userndot19@gmail.com,verificationemail,2
userndot19@gmail.com,contactus,3
admin@userndot.com,support,4
admin@und.com,emailConnectionError,5
admin@userndot.com,fromEmailVerification,6

INSERT INTO public.email_template (id, from_user) VALUES (1, 'userndot19@gmail.com');
INSERT INTO public.email_template (id, from_user) VALUES (2, 'userndot19@gmail.com');
INSERT INTO public.email_template (id, from_user) VALUES (3, 'userndot19@gmail.com');
INSERT INTO public.email_template (id, from_user) VALUES (4, 'admin@userndot.com');
INSERT INTO public.email_template (id, from_user) VALUES (5, 'admin@und.com');
INSERT INTO public.email_template (id, from_user) VALUES (6, 'admin@userndot.com'); */

select * from client_setting_email where client_id = 1;