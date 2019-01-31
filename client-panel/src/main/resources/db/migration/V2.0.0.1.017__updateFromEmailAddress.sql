update template SET template =
'<!DOCTYPE html>
<html>
<head><title>From EmailAddress Verification</title></head>
<body>
<div style="width:400px;margin:0 auto;padding:30px;">
    <a href="https://userndot.com" style="text-decoration: none;"><h2 style="text-align: center;">UserNDot</h2></a>
    <p>Hey ${name}, </p>
    <p> This email is in response to your request to add ${address}.</p>
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
 where id=11;