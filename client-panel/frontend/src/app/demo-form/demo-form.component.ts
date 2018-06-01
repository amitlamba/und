import {Component, Input, OnInit} from '@angular/core';
import {RegistrationRequest} from "../_models/client";
import {AuthenticationService} from "../_services/authentication.service";
import {ContactUs, ContactUsComponent} from "../contact-us/contact-us.component";
import {RegisterService} from "../_services/register.service";

@Component({
  selector: 'app-demo-form',
  templateUrl: './demo-form.component.html',
  styleUrls: ['./demo-form.component.scss']
})
export class DemoFormComponent implements OnInit {
  visitorName: string;
  preferredCountries = ['in', 'us', 'ru', 'gb'];
  @Input() modalDemoButton;
  contactUs: ContactUs = new ContactUs();
  recaptchaToken: string = null;

  constructor(private registerService : RegisterService) {
  }

  ngOnInit() {
  }

  submitDemoForm(f) {
    console.log(f);
    // Fix Me (Below fields should not be filled before submitting)
    this.registerService.submitContactForm(this.contactUs, this.recaptchaToken)
      .subscribe(
        (response) => {
          console.log(response);
          this.contactUs = new ContactUs();
          this.recaptchaToken = null;
        }
      )
  }

  handleCorrectCaptcha(event) {
    this.recaptchaToken = event;
  }
}
