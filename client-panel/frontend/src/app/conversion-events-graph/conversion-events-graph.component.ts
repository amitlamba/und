import {Component, OnInit} from '@angular/core';
import {Chart} from 'chart.js'
import {ReportsService} from "../_services/reports.service";


@Component({
  selector: 'app-conversion-events-graph',
  templateUrl: './conversion-events-graph.component.html',
  styleUrls: ['./conversion-events-graph.component.scss']
})
export class ConversionEventsGraphComponent implements OnInit {

  public conversionEventsChartData: Array<any> = [];
  public conversionEventsChartLabels: Array<any> = [];
  public conversionEventsChartOptions: any = {};
  public conversionEventsChartColors: Array<any> = [];
  public conversionEventsChartLegend: boolean;
  public conversionEventsChartType: string;

  constructor(private reportsService: ReportsService) {
  }

  ngOnInit() {
    this.conversionEventsChartData = this.reportsService.conversionEventsChartData;
    this.conversionEventsChartLabels = this.reportsService.lineChartLabels;
    this.conversionEventsChartOptions = this.reportsService.lineChartOptions.options;
    this.conversionEventsChartColors = this.reportsService.lineChartColors;
    this.conversionEventsChartLegend = this.reportsService.lineChartLegend;
    this.conversionEventsChartType = this.reportsService.lineChartType;
  }

  // https://valor-software.com/ng2-charts/ (Reference)
  // Make the below function reusable by specifying it in ReportsService instead of specifying it here in different components.

  getDailyOrWeeklyOrMonthlyReports($event) {
    if ($event == 'Daily') {
      let _lineChartData: Array<any> = new Array(this.conversionEventsChartData.length);
      _lineChartData[0] = {
        data: [98, 48, 40, 19, 86, 27, 90, 35, 15, 36, 75, 57, 30, 50, 11, 55, 92, 15, 19, 62, 84, 48, 78, 36],
        label: this.conversionEventsChartData[0].label
      };
      _lineChartData[1] = {
        data: [50, 11, 55, 92, 15, 19, 62, 84, 49, 90, 21, 58, 54, 44, 56, 55, 40, 65, 49, 90, 21, 58, 48, 76],
        label: this.conversionEventsChartData[1].label
      };
      _lineChartData[2] = {
        data: [85, 59, 80, 81, 56, 55, 40, 65, 49, 90, 21, 58, 54, 44, 96, 75, 54, 72, 49, 95, 41, 58, 38, 96],
        label: this.conversionEventsChartData[2].label
      };
      this.conversionEventsChartData = _lineChartData;
    }
    else if ($event == 'Weekly') {
      let _lineChartData: Array<any> = new Array(this.conversionEventsChartData.length);
      _lineChartData[0] = {
        data: [49, 47, 75, 61, 42, 90, 67, 20, 45, 89, 75, 57, 30, 50, 19, 62, 84, 11, 55, 92, 15, 28, 58, 76],
        label: this.conversionEventsChartData[0].label
      };
      _lineChartData[1] = {
        data: [65, 59, 80, 81, 56, 55, 40, 65, 49, 90, 21, 58, 54, 44, 56, 55, 40, 65, 49, 90, 21, 58, 48, 56],
        label: this.conversionEventsChartData[1].label
      };
      _lineChartData[2] = {
        data: [27, 90, 35, 91, 58, 48, 76, 70, 85, 70, 25, 62, 84, 11, 55, 92, 15, 28, 58, 76, 61, 42, 90, 67],
        label: this.conversionEventsChartData[2].label
      };

      this.conversionEventsChartData = _lineChartData;
    }
    else {
      let _lineChartData: Array<any> = new Array(this.conversionEventsChartData.length);
      _lineChartData[0] = {
        data: [28, 48, 40, 19, 86, 27, 90, 35, 91, 58, 48, 76, 70, 85, 70, 25, 95, 62, 84, 98, 32, 47, 62, 77],
        label: this.conversionEventsChartData[0].label
      };
      _lineChartData[1] = {
        data: [19, 35, 30, 71, 31, 87, 10, 50, 15, 36, 75, 57, 30, 50, 11, 55, 92, 15, 19, 62, 84, 48, 78, 36],
        label: this.conversionEventsChartData[1].label
      };
      _lineChartData[2] = {
        data: [49, 47, 75, 61, 42, 90, 67, 20, 45, 89, 75, 57, 30, 50, 19, 62, 84, 11, 55, 92, 15, 28, 58, 76],
        label: this.conversionEventsChartData[2].label
      };
      this.conversionEventsChartData = _lineChartData;
    }
  }
}
