import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormGroup} from "@angular/forms";
import {GlobalFilter, GlobalFilterType, StringOperator} from "../../../../_models/segment";
import {SegmentService} from "../../../../_services/segment.service";

@Component({
  selector: 'app-global-reactive',
  templateUrl: './global-reactive.component.html',
  styleUrls: ['./global-reactive.component.scss']
})
export class GlobalReactiveComponent implements OnInit {

  @Input() globalFilterForm:FormGroup;
  @Input() globalFilterFormIndex:number;
  @Output() remove=new EventEmitter<number>();

  globalFiltersMetadata: any;
  firstDropDown: string[];
  secondDropDown: any[];
  firstFilterSelected: string;
  secondFilterSelected: string;
  secondFilterDataType: string;
  options: any[];
  maxOrder: number = 0;

  globalFilter:GlobalFilter;

  private _operator:string;
  private _values:any[];


  get operator(): string {
    return this._operator;
  }

  set operator(value: string) {
    this._operator = value;
    this.globalFilterForm.get('operator').setValue(this._operator);
  }

  get values(): any[] {
    return this._values;
  }

  set values(value: any[]) {
    this._values = value;
    this.globalFilterForm.get('values').setValue(this._values);
  }


  constructor(private segmentService:SegmentService) {
    this.globalFilter=new GlobalFilter();
    // this.globalFiltersMetadata = this.segmentService.cachedUserProperties.reduce(
    //   (ac, p) => ({...ac, [p.name]: p.properties}), {}
    // );
    this.globalFiltersMetadata=segmentService.globalFiltersMetadata;
  }

  ngOnInit() {

    // console.log(this.globalFilterForm);
    // console.log(this.globalFilterForm.get('operator').value);
    // console.log(this.globalFilterForm.get('values').value);
    this.firstDropDown = Object.keys(this.globalFiltersMetadata);
    console.log(this.globalFilterForm);
    if(this.globalFilterForm.get('operator').value){
      this.operator=this.globalFilterForm.get('operator').value;
    }
    if(this.globalFilterForm.get('values').value){
      this.values=this.globalFilterForm.get('values').value;
    }
    if(this.globalFilterForm.get('globalFilterType').value){
      this.maxOrder=1;
      this.firstFilterChanged(this.globalFilterForm.get('globalFilterType').value)
    }else {
      this.globalFilterForm.get('globalFilterType').setValue(this.firstDropDown[0]);
      this.maxOrder=1;
      this.firstFilterChanged(this.firstDropDown[0]);
      this.operator=StringOperator.Equals;
    }

    this.globalFilterForm.valueChanges.subscribe(data=>console.log(data));
  }

  firstFilterChanged(name: string) {
    this.firstFilterSelected = name;
    this.globalFilter.globalFilterType = GlobalFilterType[name];
    this.globalFilterForm.get('globalFilterType').setValue(this.globalFilter.globalFilterType);
    this.secondDropDown = this.getDropdownList(1);
    this.secondFilterSelected = this.secondDropDown[0]['name'];
    this.globalFilter.name = this.secondFilterSelected;
    this.secondFilterChanged(this.globalFilter.name);
    this.maxOrder = 1;
  }

  secondFilterChanged(name: string) {
    this.secondFilterSelected = name;
    this.globalFilter.name = name;
    this.globalFilterForm.get('name').setValue(this.globalFilter.name);
    this.globalFilter.values = [];
    // console.log(this.globalFilter.values);
    for(let filter of this.globalFiltersMetadata[this.firstFilterSelected]) {
      if(filter["name"] == this.secondFilterSelected) {
        this.secondFilterDataType = filter["dataType"];
        this.options = filter["options"];
        this.globalFilter.type = this.secondFilterDataType;
        this.globalFilterForm.get('type').setValue(this.globalFilter.type);
      }
    }
    // this.maxOrder = 2;
  }


  getDropdownList(order: number): any[] {
    switch (order) {
      case 0:
        return Object.keys(this.globalFiltersMetadata);
      case 1:
        // if(this.firstFilterSelected == "Geography")
        //   return this.segmentService.getCountries().subscribe();
        var filters = this.globalFiltersMetadata[this.firstFilterSelected];
        console.log(filters);
        return filters;
    }
  }

  removeFilter(){
    this.remove.emit(this.globalFilterFormIndex);
  }
}
