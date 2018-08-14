insert into email_template(id, client_id, appuser_id, parent_id, from_user, message_type, tags, name, email_template_subject, email_template_body, editor_selected) VALUES
(4, 1,1,null,'userndot19@gmail.com', 'TRANSACTIONAL', null, 'support', 8, 7, false );

insert into template (id, client_id, appuser_id, template,name) VALUES
(7,1,1,'<!DOCTYPE html>
<html>
<head>
    <title>Support</title>
</head>
<body>
<div>
<p>Message from ${name} from ${companyname}.</p>
<p>Message:</p>
<p>${message}</p>
<p>Contacts Details:</p>
<p>Email : ${email}</p>
<p>Mobile No : ${mobile}</p>
</div>
</body>
</html>','4:support:body'),
(8,1,1,
   'support'
    ,'4:support:subject');

update email_template set email_template_subject = 8,  email_template_body=7 where client_id=1 and name='support';