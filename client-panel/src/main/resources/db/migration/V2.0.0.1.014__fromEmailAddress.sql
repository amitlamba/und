insert into template VALUES
(11,1,1,'<!DOCTYPE html>
<html>
<head><title>From EmailAddress Verification</title></head>
<body>
<div style="width:400px;margin:0 auto;padding:30px;">
    <a href="https://userndot.com" style="text-decoration: none;"><h2 style="text-align: center;">UserNDot</h2></a>
    <p>Hey ${name}, </p>
    <p> This email is in response to your request to add "From Email Address".</p>
    <p>To verify the email <a href="${emailVerificationLink}"style="color: blue;">click here</a>, or copy paste the below link in the browser.</p>
    <p>${emailVerificationLink}</p>
    <p>If you did not make this request,you can safely ignore this email.This is an auto generated email. Please do not
        reply to this email.</p>
    <p style="margin-bottom: 0">Best Regards , </p>
    <p style="margin-top: 0">Team UserNDot</p><br><br>
    <p style="font-size: 12px">This is an auto generated email. Please do not reply to this email.</p>
    <p style="font-size: 10px;">IMPORTANT: Please do not reply to this message or mail address. For any queries, please
        call
        our 24 Hrs Customer Contact Centre at 18602662666(local call rates apply) or you can go to Contact section.</p>
    <p style="font-size: 10px;">DISCLAIMER: This communication is confidential and privileged and is directed to and for
        the
        use of the addressee only. The recipient if not the addressee should not use this message if erroneously
        received,
        and access and use of this e-mail in any manner by anyone other than the addressee is unauthorized.</p></div>
</body>
</html>'
    ,'6:fromEmailVerification:body'),
(12,1,1,'from emailaddress verification'
    ,'6:fromEmailVerification:subject');


INSERT INTO email_template (id, client_id, appuser_id, parent_id, from_user, message_type, tags, name, email_template_subject, email_template_body, editor_selected) VALUES
(6,1,1,null,'admin@userndot.com','TRANSACTIONAL',NULL,'fromEmailVerification',12,11,FALSE);


update email_template set from_user = 'admin@userndot.com' ,email_template_subject = 10,  email_template_body=9 where client_id=1 and name='emailConnectionError';
update email_template set from_user = 'admin@userndot.com' where client_id=1 and name='support';


ALTER TABLE campaign ADD COLUMN if NOT EXISTS conversion_event VARCHAR (255) DEFAULT NULL ;
ALTER TABLE service_provider_credentials ADD COLUMN if NOT EXISTS isDefault boolean not NULL DEFAULT FALSE ;
ALTER TABLE campaign ADD COLUMN if NOT EXISTS service_provider_id bigint;