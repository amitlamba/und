import {Component, Input, OnInit} from '@angular/core';
import {GlobalFilterType, RegisteredEvent, RegisteredEventProperties, Segment} from "../_models/segment";
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {FunnelOrder, FunnelReportFilter, FunnelStep, Step} from "../_models/reports";
import {SegmentService} from "../_services/segment.service";
import {ReportsService} from "../_services/reports.service";
import {ChartModel} from "../eventreport/eventreport-demographics/eventreport-demographics.component";

@Component({
  selector: 'app-funnel',
  templateUrl: './funnel.component.html',
  styleUrls: ['./funnel.component.scss']
})
export class FunnelComponent implements OnInit {

  technographics:string[]=['os','device','browser'];
  geography:string[]=['country','state','city'];
  registerdEventProperty:RegisteredEventProperties[];
  step1Event:RegisteredEvent;
  splitproperty:string = "None";
  splitPropertyType:string;

  data:ChartModel;

  @Input() funnel:FunnelReportFilter;
  error:string='';
  segmentId:number;
  segments:Segment[];
  days:number;
  funnelForm:FormGroup;
  showSplitProperty:boolean=false;

  registeredEvents: RegisteredEvent[];

  // globalFiltersMetadata:any;
  constructor(private fb:FormBuilder,
              private segmentService:SegmentService,
              private reportService:ReportsService) {

    // this.globalFiltersMetadata = this.segmentService.cachedUserProperties.reduce(
    //   (ac, p) => ({...ac, [p.name]: p.properties}), {}
    // );
    // if (!(this.globalFiltersMetadata && this.globalFiltersMetadata.length)) {
    //   this.globalFiltersMetadata = segmentService.globalFiltersMetadata;
    // }

    this.registeredEvents = this.segmentService.cachedRegisteredEvents;
  }

  ngOnInit() {

    this.data=new ChartModel();
    this.data.title='';
    this.data.subTitle='';
    this.data.graphType='column';
    this.data.xAxisTitle='';
    this.data.yAxisTitle='';

    this.segmentService.getSegments().subscribe(response=>{
      this.segments=response;
    });
    if(!this.funnel) {
      this.funnel = new FunnelReportFilter();
      this.funnel.segmentid=-1;
      this.funnel.steps = [];
      this.funnel.days = 30;
      this.funnel.conversionTime=5*24*60*60;
      this.funnel.funnelOrder=FunnelOrder.default;
      this.funnel.splitProperty="None";
    }

    this.createFunnelForm(this.funnel);

  }

  private createFunnelForm(funnel: FunnelReportFilter) {
    this.funnelForm = this.fb.group({
      segmentid: [funnel.segmentid, Validators.required],
      days: [funnel.days, Validators.required],
      steps: this.createSteps(funnel.steps),
      funnelOrder:[funnel.funnelOrder,Validators.required],
      conversionTime:[funnel.conversionTime,Validators.required]
    });
  }

  private createSteps(steps: Step[]): FormArray {
    if(!(steps && steps.length)) {
      let step=new Step();
      step.eventName = this.registeredEvents[0].name;
      steps.push(step);
    }
    return this.fb.array(steps.map<FormGroup>((v,i,a)=> {
      return this.createStep(v);
    })
    )
  }

  private createStep(step: Step): FormGroup {
    return this.fb.group({
      order: [step.order],
      eventName: [step.eventName]
    })
  }

  get stepArray():FormArray{
    return <FormArray>this.funnelForm.get('steps');
  }
  addStep(){
    let step=new Step();
    step.eventName = this.registeredEvents[0].name;
    this.stepArray.push(this.createStep(step));
  }
  removeStep(index:number){
    this.stepArray.removeAt(index);
  }


  viewFunnel(){
    console.log(this.funnelForm);
    Object.assign(this.funnel,this.funnelForm.value);
    this.reportService.getFunnelResult(this.funnel).subscribe(
      response => {
        this.initializeGraph(response);
        this.showSplitProperty = true;
      },
      error => {
        console.error("error occur in funnel report");
        console.error(error);
      }
    );



    this.step1Event=this.registeredEvents.find(events=> events.name===this.funnelForm.get('steps').value[0].eventName);
    console.log(this.step1Event);
    this.registerdEventProperty=this.step1Event.properties;

  }

  filterOnSplitByProperty(filter){
    this.showSplitProperty=false;
    console.log(filter.target.value);
    // this.splitproperty = filter.target.value;
    if(this.geography.indexOf(filter.target.value)>=0){
      console.log("Demographics");
      this.funnel.splitProperty=filter.target.value;
      this.funnel.splitPropertyType=GlobalFilterType.Demographics;
    }else if(this.technographics.indexOf(filter.target.value)>=0){
      console.log("Technographics");
      this.funnel.splitProperty=filter.target.value;
      this.funnel.splitPropertyType=GlobalFilterType.Technographics;
    }else{
      console.log("EventAtrributesProperty");
      this.funnel.splitProperty=filter.target.value;
      this.funnel.splitPropertyType=GlobalFilterType.EventAttributeProperties;
    }


    this.reportService.getFunnelResult(this.funnel).subscribe(
      response=>{
        console.log(response);
        this.initializeGraph(response);
        this.showSplitProperty=true;
      },
      error=>console.error("error occur in funnel report"+error)
    );

  }

  initializeGraph(data:FunnelStep[]){

    this.data.category=data.map(v=>v.property);
    console.log(this.data.category);

    this.data.dataSeries = [];

    data.forEach(v=> {
      this.data.dataSeries.push({
        showInLegend: true,
        seriesName: v.step.eventName,
        data: [v.count]
      });
    })
    // data.map(v=>v.count)
  }

  conversionTimeMetadata = {
      "5 minutes": 5 * 60,
      "10 minutes": 10 * 60,
      "30 minutes": 30 * 60,
      "1 hour": 60 * 60,
      "2 hours": 2 * 60 * 60,
      "6 hours": 5 * 60 * 60,
      "12 hours": 12 * 60 * 60,
      "18 hours": 18 * 60 * 60,
      "1 day": 24 * 60 * 60,
      "2 days": 2 * 24 * 60 * 60,
      "5 days": 5 * 24 * 60 * 60,
      "7 days": 7 * 24 * 60 * 60,
      "15 days": 15 * 24 * 60 * 60,
      "30 days": 30 * 24 * 60 * 60,
      "60 days": 60 * 24 * 60 * 60,
      "180 days": 180 * 24 * 60 * 60
  };

  conversionTimeMetadataKeys = Object.keys(this.conversionTimeMetadata);
}
