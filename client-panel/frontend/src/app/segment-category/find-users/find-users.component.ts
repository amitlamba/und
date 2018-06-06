import { Component, OnInit } from '@angular/core';
import {SegmentService} from "../../_services/segment.service";
import {Segment} from "../../_models/segment";
import {Router} from "@angular/router";

@Component({
  selector: 'app-find-users',
  templateUrl: './find-users.component.html',
  styleUrls: ['./find-users.component.scss']
})
export class FindUsersComponent implements OnInit {

  newSegment: Segment;

  constructor(private segmentService: SegmentService,
              private router:Router) {
    this.newSegment = new Segment();
    // this.newSegment.didEvents = new DidEvents();
    // this.newSegment.didNotEvents = new DidEvents();
    // this.newSegment.globalFilters = [];
    // this.newSegment.geographyFilters = new Array<Geography>();
    this.segmentService.initSegment(this.newSegment);
    this.newSegment.type = "Behaviour";
    this.segmentService.editSegment = this.newSegment;
  }

  ngOnInit() {
  }

  findUserByIdentity() {
      this.router.navigate(['/segment/find-users/user-profile']);
    // this.router.navigate(["segment","find-users","user-profile"]);
  }
}
