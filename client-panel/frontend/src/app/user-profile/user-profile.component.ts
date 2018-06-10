import {Component, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {
  userProfile:string;
  @ViewChild('radioButton') radioButton;

  constructor() { }

  ngOnInit() {
    this.userProfile = 'userDetails';
  }
  myValue($event){
    console.log($event.target.value);
    this.userProfile=$event.target.value;
    console.log(this.userProfile);
  }
}
