create table if not exists email_failure_audit_log
(
  id bigserial not null
    constraint email_failure_audit_log_pkey
    primary key,
  client_id bigint not null
    constraint email_failure_audit_log_client_id_fkey
    references client,
  client_setting_email_id bigint not null
    constraint email_failure_audit_log_campaign_id_fkey
    references campaign,
  status varchar(20),
  message text,
  date_created timestamp with time zone default now()
);



insert into email_template(id, client_id, appuser_id, parent_id, from_user, message_type, tags, name, email_template_subject, email_template_body, editor_selected) VALUES
  (5, 1,1,null,'admin@und.com', 'TRANSACTIONAL', null, 'emailConnectionError', 1, 2, false );

insert into template(id, client_id, appuser_id, template, name) VALUES
  (9,1,1,
   '<!DOCTYPE html><html><head><title>Email Connection Failure</title></head><body><div style="width:400px;margin:0 auto;padding:30px;""><a href="https://userndot.com" style="text-decoration: none;"><h2 style="text-align: center;">UserNDot</h2></a><p>Hey ${user.standardInfo.firstname}, </p><p> Email server connection failed while using settings please correct them.</p>If you did not make this request,you can safely ignore this email.This is an auto generated email. Please do not reply to this email.</p><p style="margin-bottom: 0">Best Regards , </p><p style="margin-top: 0">Team UserNDot</p><br><br><p style="font-size: 12px">This is an auto generated email. Please do not reply to this email.</p><p style="font-size: 10px;">IMPORTANT: Please do not reply to this message or mail address. For any queries, please call our 24 Hrs Customer Contact Centre at 18602662666(local call rates apply) or you can go to Contact section.</p><p style="font-size: 10px;">DISCLAIMER: This communication is confidential and privileged and is directed to and for the use of the addressee only. The recipient if not the addressee should not use this message if erroneously received, and access and use of this e-mail in any manner by anyone other than the addressee is unauthorized.</p></div></body></html>'
    ,'5:emailConnectionError:body'),
  (10,1,1,
   'Email setting are not correct'
    ,'5:emailConnectionError:subject');