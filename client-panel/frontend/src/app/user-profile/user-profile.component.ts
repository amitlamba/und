import {Component, OnInit, ViewChild} from '@angular/core';
import {SegmentService} from "../_services/segment.service";
import {Event, EventUser} from "../_models/user";
import {Router} from "@angular/router";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {
  userProfile: string;
  eventUser: EventUser = new EventUser();
  eventList: Event[] = [];
  showEventsList:boolean = false;
  @ViewChild('radioButton') radioButton;

  constructor(private segmentService: SegmentService,
              private router: Router) {
  }

  ngOnInit() {
    if (this.isEmptyObject(this.segmentService.eventUser)) {
      this.router.navigate(['/segment/find-users']);
    }
    else {
      this.eventUser = this.segmentService.eventUser;
      this.segmentService.getEventsListByUserId(this.eventUser.undId).subscribe(
        (response:Event[]) => {
          console.log(response);
          this.eventList = response;
          this.showEventsList = true;
        }, (error: HttpErrorResponse) => {
          console.log(error);
        }
      );
      // this.segmentService.eventUser = new EventUser();
    }
    this.userProfile = 'userDetails';
  }

  myValue($event) {
    console.log($event.target.value);
    this.userProfile = $event.target.value;
    console.log(this.userProfile);
  }

  isEmptyObject(obj) {
    for (var prop in obj) {
      if (obj.hasOwnProperty(prop)) {
        return false;
      }
    }
    return true;
  }
}
