import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../../_services/authentication.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent implements OnInit {
  user: any = {
    email: ''
  };

  recaptchaToken: string = null;

  constructor(private authenticationService: AuthenticationService,
              private router: Router) {
  }

  ngOnInit() {
  }

  emptyUserEmail() {
    this.user.email = "";
  }

  submitEmail() {
    this.authenticationService.forgotpassword(this.user.email + '/', this.recaptchaToken)
      .subscribe(
        (response) => {
          console.log(response);
          // this.router.navigate(['/login']);
        }
      );

  }

  handleCorrectCaptcha(event) {
    this.recaptchaToken = event;
  }

}
