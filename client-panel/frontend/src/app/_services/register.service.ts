import {Injectable} from '@angular/core';
import {Response} from '@angular/http';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {AppSettings} from "../_settings/app-settings";

@Injectable()
export class RegisterService {

  constructor(private http: HttpClient) {
  }

  getData() {
    return this.http.get('https://userndot-a528b.firebaseio.com/code.json')
      .map((res: Response) => res.json());
  }

  submitContactForm(contactUs): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json; charset=utf-8');
    return this.http.post(AppSettings.API_ENDPOINT_CONTACT_US_SAVE, contactUs, {headers: headers});
  }
}
