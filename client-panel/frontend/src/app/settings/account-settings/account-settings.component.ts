import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {AccountSettings, UnSubscribeLink} from "../../_models/client";
import {SettingsService} from "../../_services/settings.service";


@Component({
  selector: 'app-account-settings',
  templateUrl: './account-settings.component.html',
  styleUrls: ['./account-settings.component.css']
})
export class AccountSettingsComponent implements OnInit {
  showCodeBlock: boolean = false;
  accountSettings: AccountSettings = new AccountSettings();
  unSubscribeLink: UnSubscribeLink = new UnSubscribeLink();
  protocol: string = 'https://';
  websiteURL: string;
  unsubscribeLink: string;
  protocolsArray: string[] = ['http://', 'https://'];
  codeSnippet: string;
  tokenValue: string;

  // ng2-timezone-picker is used from https://samuelnygaard.github.io/ng2-timezone-selector/docs/
  placeholderString = 'Select timezone';

  constructor(private settingsService: SettingsService) {
  }

  ngOnInit() {
    this.accountSettings.timezone = "Asia/Kolkata";
    this.accountSettings.urls = [];
    this.settingsService.getAccountSettings()
      .subscribe(
        (accountSettings) => {
          this.accountSettings.id = accountSettings.id;

          if (typeof accountSettings.urls[0] !== "undefined") {

            if (accountSettings.urls[0].substring(0, accountSettings.urls[0].indexOf("/") + 2 == 'https://')) {
              this.protocol = accountSettings.urls[0].substring(0, accountSettings.urls[0].indexOf("/") + 2);
            }

            else {
              this.protocol = accountSettings.urls[0].substring(0, accountSettings.urls[0].indexOf("/") + 2)
            }

            this.websiteURL = accountSettings.urls[0].substring(accountSettings.urls[0].indexOf("/") + 2);
          }
        }
      );
    this.settingsService.refreshToken().subscribe(
      (response) => {
        console.log(response.data.value.token);
        this.tokenValue = response.data.value.token;
        this.codeSnippet = "var _und = _und || {event: [], profile: [], account: [], onUserLogin: [], notifications: []};\n" +
          "    // replace with the UND_ACCOUNT_ID with the actual ACCOUNT ID value from your Dashboard -> Settings page\n" +
          "    _und.account.push({\"id\":" + this.tokenValue + "});\n" +
          "    (function () {\n" +
          "        var dsft = document.createElement('script');\n" +
          "        dsft.type = 'text/javascript';\n" +
          "        dsft.async = true;\n" +
          "//                dsft.src =  ('https:' == document.location.protocol ? 'https://' : 'http://') + 'stats.g.doubleclick.net/dc.js';\n" +
          "        dsft.src = 'build/main.min.js';\n" +
          "        var s = document.getElementsByTagName('script')[0];\n" +
          "        s.parentNode.insertBefore(dsft, s);\n" +
          "    })();"
      }
    );
    this.settingsService.getUnSubscribeLink()
      .subscribe(
        (unSubscribeLink) => {
          console.log(unSubscribeLink);
          this.unSubscribeLink = unSubscribeLink;
        }
      )
  }


  getJSIntegrationCode() {
    this.showCodeBlock = true;
    this.accountSettings.urls.push(this.protocol + this.websiteURL);
    this.settingsService.saveAccountSettings(this.accountSettings)
      .subscribe(
        (response) => {
          this.accountSettings.urls = [];
        }
      );
  }

  changeTimezone(timezone) {
    this.accountSettings.timezone = timezone;
  }

  addUnSubscribeLink() {
    console.log(this.unSubscribeLink);
    this.settingsService.saveUnSubscribeLink(this.unSubscribeLink)
      .subscribe(
        (response) => {
          console.log(response);
        }
      )
  }
}
