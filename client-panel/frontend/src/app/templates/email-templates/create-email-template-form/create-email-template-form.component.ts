import {Component, ElementRef, Input, OnChanges, OnInit, ViewChild} from '@angular/core';
import {EmailTemplate} from "../../../_models/email";
import {TemplatesService} from "../../../_services/templates.service";
import {isNullOrUndefined} from "util";
import {Router} from "@angular/router";
import {MessageService} from "../../../_services/message.service";
import {UserFields} from "../../../_settings/app-settings";
import {UserParams} from "../../../_models/user";
import {SettingsService} from "../../../_services/settings.service";
import {SendersInfo} from "../../../_models/client";

@Component({
  selector: 'app-create-email-template-form',
  templateUrl: './create-email-template-form.component.html',
  styleUrls: ['./create-email-template-form.component.scss']
})
export class CreateEmailTemplateFormComponent implements OnInit, OnChanges {
  showTinymceEditor: boolean = true;
  emailTemplate: EmailTemplate;
  sendersInfoList: SendersInfo[] = [];
  @ViewChild("f") form: any;

  userFields = UserFields.USER_DETAIILS;
  items = UserParams.params;

  constructor(private templatesService: TemplatesService, private messageService: MessageService,
              private settingsService: SettingsService) {
  }

  ngOnChanges() {
  }

  ngOnInit() {
    this.templatesService.castEmailTemplateForEdit.subscribe((emailTemplateForEdit) => {
      this.emailTemplate = emailTemplateForEdit;
    });
    this.settingsService.getSendersInfoList().subscribe(
      (sendersInfoList)=>{
        this.sendersInfoList = sendersInfoList;
      }
    )
  }

  onSave(form: FormData) {
    console.log(this.emailTemplate);
    if (this.form.valid) {
      if (this.emailTemplate.id) {
        this.templatesService.saveEmailTemplate(this.emailTemplate)
          .subscribe(
            response => {
              this.templatesService.editEmailTemplate(this.emailTemplate);
              this.messageService.addSuccessMessage("Email Template Edited Successfully");
            }
          );
      } else {
        this.templatesService.saveEmailTemplate(this.emailTemplate)
          .subscribe(
            response => {
              console.log(response);
              this.emailTemplate.id = response
              this.templatesService.addEmailTemplate(this.emailTemplate);
              this.messageService.addSuccessMessage("Email Template Created Successfully");
            }
          );
      }
    }
  }

  addUnsubscribeLink(event) {
    if (event.srcElement.textContent === 'Add Unsubscribe') {
      document.querySelector('textarea').value = document.querySelector('textarea').value + '<a href="#">Unsubscribe</a>';
      event.srcElement.textContent = 'Remove Unsubscribe';
    }
    else {
      document.querySelector('textarea').value = document.querySelector('textarea').value.replace('<a href="#">Unsubscribe</a>', '');
      event.srcElement.textContent = 'Add Unsubscribe';
    }
  }
}
