import {Component, Injectable, OnInit, ViewChild} from '@angular/core';
import {RegistrationRequest} from "../_models/client";
import {AuthenticationService} from "../_services/authentication.service";
import {Router} from "@angular/router";
import {ReCaptchaComponent} from "angular2-recaptcha";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  // Angular2 ReCaptcha Component used from https://github.com/xmaestro/angular2-recaptcha
  @ViewChild('ReCaptchaComponent') captcha: ReCaptchaComponent;
  recaptchaToken: string = null

  constructor(private authenticationService: AuthenticationService, private router: Router) {
  }

  model: RegistrationRequest = new RegistrationRequest();
  @ViewChild('f') form: any;
  loading = false;
  error = '';

  langs: string[] = [
    'English',
    'French',
    'German',
  ];

  onSubmit(form: FormData) {
    this.loading = true;
    if (this.form.valid) {
      console.log(this.form);
      console.log(this.model);
      this.authenticationService.register(this.model, this.recaptchaToken)
        .subscribe(
          response => {
            this.router.navigate(['/login']);
          },
          (error: Error) => {
            this.error = "Not Registered. Server Error: " + error.message;
            this.loading = false;
          }
        );
    }

  }


  ngOnInit() {
    this.captcha.reset();
  }

  handleCorrectCaptcha($event) {
    console.log("event daata \n" + $event);
    this.recaptchaToken = this.captcha.getResponse().toString();
    console.log("Token Data \n" + this.recaptchaToken);
  }
}
