INSERT INTO authority (id, name, date_created, date_modified) VALUES (4, 'ROLE_SYSTEM', now(), now());

insert into client (id, name, state, email, phone, email_verified, phone_verified, date_created, date_modified, address, firstname, lastname, country)
VALUES
  (1, 'und', 1, 'admin@und.com', '7838540240', true, true, now(), now(), 'Gurgaon', 'UserNDot', 'Private Ltd',
   'India');

insert into appuser (id, client_id, secret, email, enabled, firstname, key, lastpasswordresetdate, lastname, password, user_type, phone, username)
VALUES
  (1,1,'6Agq1z240W5igRjLToZb8gUdNknXJR16pnGazh8N28wkC3KSpyueJxc2e3wYYsUnpopuqwWf1oN7KM7NgCY9b19zsB0nAZm1iXpKD3dLN0zdTcejwDxcCZoPx0N7vNXq',
     'admin@und.com',  true, 'UserNDot',
     'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzeXN0ZW11bmQiLCJ1c2VySWQiOiIxIiwiY2xpZW50SWQiOiIxIiwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfRVZFTlQiLCJST0xFX1VTRVIiLCJST0xFX1NZU1RFTSJdLCJjcmVhdGVkIjoxNTI4NzMxOTcwNTUyLCJleHAiOjkyMjMzNzIwMzY4NTQ3NzV9.hcoZvUz5XwHXPW54TaW-FmLL50wr44WdoOjgOJ0LOYgwaF2EdJzKYsKIJVYIycHSTXXojzGmZFgoCMSrKMtuRQ',
     now(), 'private ltd.', '$2a$10$OcT.BEyC2jlBSeA0.uvoCuXNn2NAZYGJ16WPw1NrEUDnDm5XAKEQq', 3, '7838540240', 'systemund');

insert into user_authority (user_id, authority_id) VALUES
  (1, 1),
  (1, 2),
  (1, 3),
  (1, 4);

insert into email_template(id, client_id, appuser_id, parent_id, from_user, message_type, tags, name, email_template_subject, email_template_body, editor_selected) VALUES
  (1, 1,1,null,'admin@und.com', 'TRANSACTIONAL', null, 'forgotpassword', 1, 2, false ),
  (2, 1,1,null,'admin@und.com', 'TRANSACTIONAL', null, 'verificationemail', 3, 4, false ),
  (3, 1,1,null,'admin@und.com', 'TRANSACTIONAL', null, 'contactus', 5, 6, false );

insert into template(id, client_id, appuser_id, template, name) VALUES
  (1,1,1,
   '<!DOCTYPE html><html><head><title>Forgot Password</title></head><body><div style="width:400px;margin:0 auto;padding:30px;""><a href="https://userndot.com" style="text-decoration: none;"><h2 style="text-align: center;">UserNDot</h2></a><p>Hey ${user.standardInfo.firstname}, </p><p> This email is in response to your request to reset your password.</p><p>Please follow the <a href="" style="color: white;">link</a> below for to reset your password.</p><a href="${url}" style="color: white;">${url}"</a><p>If you did not make this request,you can safely ignore this email.This is an auto generated email. Please do not reply to this email.</p><p style="margin-bottom: 0">Best Regards , </p><p style="margin-top: 0">Team UserNDot</p><br><br><p style="font-size: 12px">This is an auto generated email. Please do not reply to this email.</p><p style="font-size: 10px;">IMPORTANT: Please do not reply to this message or mail address. For any queries, please call our 24 Hrs Customer Contact Centre at 18602662666(local call rates apply) or you can go to Contact section.</p><p style="font-size: 10px;">DISCLAIMER: This communication is confidential and privileged and is directed to and for the use of the addressee only. The recipient if not the addressee should not use this message if erroneously received, and access and use of this e-mail in any manner by anyone other than the addressee is unauthorized.</p></div></body></html>'
    ,'1:forgotpassword:body'),
  (2,1,1,
   'Reset Password'
    ,'1:forgotpassword:subject'),
  (3,1,1,
   '<!DOCTYPE html><html><head><title>Verification Email</title></head><body><div style="width:400px;margin:0 auto;padding:30px;""><a href="https://userndot.com" style="text-decoration: none;"><h2 style="text-align: center;">UserNDot</h2></a><p>Hey ${user.standardInfo.firstname} , </p><p> Thank You for registering with us.Pleas <a href="${url}">click</a> to activate your account.</p><a href="${url}">${url}</a><p style="margin-bottom: 0">Best Regards , </p><p style="margin-top: 0">Team UserNDot</p><br><br><p style="font-size: 12px">This is an auto generated email. Please do not reply to this email.</p><p style="font-size: 10px;">IMPORTANT: Please do not reply to this message or mail address. For any queries, please call our 24 Hrs Customer Contact Centre at 18602662666(local call rates apply) or you can go to Contact section.</p><p style="font-size: 10px;">DISCLAIMER: This communication is confidential and privileged and is directed to and for the use of the addressee only. The recipient if not the addressee should not use this message if erroneously received, and access and use of this e-mail in any manner by anyone other than the addressee is unauthorized.</p></div></body></html>'
    ,'2:verificationemail:body'),
  (4,1,1,
   'Verify Email'
    ,'2:verificationemail:subject'),
  (5,1,1
    ,'<!DOCTYPE html><html><head><title>Thanks</title></head><body><div style="width:400px;margin:0 auto;padding:30px;""><a href="https://userndot.com" style="text-decoration: none;"><h2 style="text-align: center;">UserNDot</h2></a><p>Hey ${user.standardInfo.firstname} , </p><p> Thanks for contacting us. Someone from our team will call you shortly.</p><p>If you did not make this request,you can safely ignore this email.This is an auto generated email. Please do not reply to this email.</p><p style="margin-bottom: 0">Best Regards , </p><p style="margin-top: 0">Team UserNDot</p><br><br><p style="font-size: 12px">This is an auto generated email. Please do not reply to this email.</p><p style="font-size: 10px;">IMPORTANT: Please do not reply to this message or mail address. For any queries, please call our 24 Hrs Customer Contact Centre at 18602662666(local call rates apply) or you can go to Contact section.</p><p style="font-size: 10px;">DISCLAIMER: This communication is confidential and privileged and is directed to and for the use of the addressee only. The recipient if not the addressee should not use this message if erroneously received, and access and use of this e-mail in any manner by anyone other than the addressee is unauthorized.</p></div></body></html>'
    ,'3:contactus:body'),
  (6,1,1,
   'Thanks For contacting!'
    ,'3:contactus:subject');

/* set sequences after adding default data to appropriate place say 1000*/

alter sequence  authority_id_seq INCREMENT 1000;
alter sequence  client_id_seq INCREMENT 1000;
alter sequence  appuser_id_seq INCREMENT 1000;
alter sequence  email_template_id_seq INCREMENT 1000;
alter sequence  template_id_seq INCREMENT 1000;
