import {Component, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {
  userProfile;
  eventsList ={
    eventName:''
  };
  @ViewChild('radioButton') radioButton;

  constructor() { }

  ngOnInit() {
    this.userProfile = 'modeOfCommunication';
    // console.log(this.radioButton.value);
  }
  myFunc() {
    console.log(this.userProfile)
  }


}
