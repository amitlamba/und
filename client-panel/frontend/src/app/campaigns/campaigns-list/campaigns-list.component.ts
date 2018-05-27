import {Component, OnInit} from '@angular/core';
import {IMyDrpOptions} from "mydaterangepicker";
import {CampaignService} from "../../_services/campaign.service";
import {Campaign} from "../../_models/campaign";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
  selector: 'app-campaigns-list',
  templateUrl: './campaigns-list.component.html',
  styleUrls: ['./campaigns-list.component.scss']
})
export class CampaignsListComponent implements OnInit {

  campaigns: Campaign[];

  constructor(private campaignService: CampaignService) {
  }

  ngOnInit() {
    this.getCampaignsList();
  }

  getCampaignsList() {
    this.campaignService.getCampaignList().subscribe((campaigns) => {
      this.campaigns = campaigns;
      // console.log(campaigns);
    });
  }
  // Date Range Input Code
  myDateRangePickerOptions: IMyDrpOptions = {
    dateFormat: 'dd.mm.yyyy',
    height: '30px'
  };
  public model: any = {
    beginDate: {year: 2018, month: 10, day: 9},
    endDate: {year: 2018, month: 10, day: 19}
  };

  resumeCampaignFunction(campaignId) {
    console.log(campaignId);
    this.campaignService.resumeCampaign(campaignId)
      .subscribe(
        (campaignId) => {
          console.log(campaignId);
        },
        (error: HttpErrorResponse) => {
          console.log("Error from resume Campaign" + error);
        }
      );
    this.getCampaignsList();
  }

  pauseCampaignFunction(campaignId) {
    console.log(campaignId);
    this.campaignService.pauseCampaign(campaignId)
      .subscribe(
        (campaignId) => {
          console.log(campaignId);
        },
        (error: HttpErrorResponse) => {
          console.log("Error from Pause Campaign Function" + error);
        }
      );
    this.getCampaignsList();
  }

  stopCampaignFunction(campaignId) {
    console.log(campaignId);
    this.campaignService.stopCampaign(campaignId)
      .subscribe(
        (campaignId) => {
          console.log(campaignId);
        },
        (error: HttpErrorResponse) => {
          console.log("Error from Stop Campaign Function" + error);
        }
      );
    this.getCampaignsList();
  }

  deleteCampaignFunction(campaignId) {
    console.log(campaignId);
    this.campaignService.deleteCampaign(campaignId)
      .subscribe(
        (campaignId) => {
          console.log(campaignId);
        },
        (error: HttpErrorResponse) => {
          console.log("Error from Delete Campaign Function" + error);
        }
      );
    this.getCampaignsList();
  }

}
