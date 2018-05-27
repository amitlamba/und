import {Component, OnInit, ViewChild} from '@angular/core';
import {RegisterService} from "../_services/register.service";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
  selector: 'app-contact-us',
  templateUrl: './contact-us.component.html',
  styleUrls: ['./contact-us.component.scss',
  '../landing-page-und/landing-page-und.component.scss']
})
export class ContactUsComponent implements OnInit {
  contactUs: ContactUs;
  loading: boolean = false;
  showSuccessMessage: boolean = false;
  showSubmitMessage: boolean = false;
  showErrorMessage: boolean = false;
  preferredCountries = ['in', 'us', 'ru', 'gb'];
  errorMessage: HttpErrorResponse;
  @ViewChild('contactUsForm') contactUsForm;

  constructor(private registerService: RegisterService) {
  }

  ngOnInit() {
    this.contactUs = new ContactUs();
  }

  submitContactUsForm() {
    this.loading = true;
    this.showSubmitMessage = true;
    this.registerService.submitContactForm(this.contactUs)
      .subscribe(
        (response) => {
          console.log(response);
          this.loading = false;
          this.showSuccessMessage = true;
          this.contactUs = new ContactUs();
          this.contactUsForm.reset();
          this.contactUsForm.mobileNo = "";
        },
        (error: HttpErrorResponse) => {
          this.loading = false;
          this.showErrorMessage = true;
          console.log(error);
          this.errorMessage = error;
        }
      );
  }
}

class ContactUs {
  name: string;
  email: string;
  mobileNo: string;
  message: string;
}
