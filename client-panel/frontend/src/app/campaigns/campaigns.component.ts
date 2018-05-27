import {Component, OnInit} from '@angular/core';
import {IMyDrpOptions} from "mydaterangepicker";
import {Campaign} from "../_models/campaign";
import {CampaignService} from "../_services/campaign.service";

@Component({
  selector: 'app-campaigns',
  templateUrl: './campaigns.component.html',
  styleUrls: ['./campaigns.component.scss']
})
export class CampaignsComponent implements OnInit {
  constructor() {
  }

  ngOnInit() {

  }

}
