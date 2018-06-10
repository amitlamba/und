import {Component, Input, OnInit} from '@angular/core';
import {Segment} from "../_models/segment";
import {SegmentService} from "../_services/segment.service";
import {HttpErrorResponse} from "@angular/common/http";
import {Campaign} from "../_models/campaign";
import cronstrue from 'cronstrue';

@Component({
  selector: 'app-campaigns-info',
  templateUrl: './campaigns-info.component.html',
  styleUrls: ['./campaigns-info.component.scss']
})
export class CampaignsInfoComponent implements OnInit {
  @Input('campaignInfoObject') campaignInfoObject: Campaign;
  segmentsList: Segment[] = [];
  segment: Segment;
  showCampaignsModalBody: boolean = false;
  cronExpressionSummary:string;

  constructor(private segmentsService: SegmentService) {
  }

  ngOnInit() {
  }

  ngOnChanges() {
    console.log(this.campaignInfoObject.segmentationID);
    this.segmentsService.getSegments().subscribe(
      (segmentsList) => {
        this.segmentsService.segments = segmentsList;
        this.segmentsList = this.segmentsService.segments;
        this.segment = this.segmentsList.find((segment) => {
          return segment.id === this.campaignInfoObject.segmentationID;
        });
        this.showCampaignsModalBody = true;
      },
      (error: HttpErrorResponse) => {
        console.log(error);
      }
    );
    console.log(this.campaignInfoObject);
    if(this.campaignInfoObject.schedule.recurring){
      console.log(this.campaignInfoObject.schedule)
      console.log(this.campaignInfoObject.schedule.recurring.cronExpression);
      this.cronExpressionSummary = cronstrue.toString(this.campaignInfoObject.schedule.recurring.cronExpression);
    }
  }
}
