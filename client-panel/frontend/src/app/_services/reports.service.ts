import {Injectable} from "@angular/core";

@Injectable()
export class ReportsService {

  // ng2-charts https://valor-software.com/ng2-charts/
  // lineChart
  public newUsersChartData: Array<any> = [
    {
      data: [19, 35, 30, 71, 31, 87, 10, 50, 15, 36, 75, 57, 30, 50, 11, 55, 92, 15, 19, 62, 84, 48, 78, 36],
      label: 'Users Acquired'
    },
    {
      data: [45, 89, 60, 41, 96, 55, 40, 65, 49, 90, 21, 58, 54, 44, 56, 55, 40, 65, 49, 90, 21, 58, 48, 76],
      label: 'Conversion Event'
    }
  ];
  public eventsChartData: Array<any> = [
    {
      data: [19, 35, 30, 71, 31, 87, 10, 50, 15, 36, 75, 57, 30, 50, 11, 55, 92, 15, 19, 62, 84, 48, 78, 36],
      label: 'Event 1'
    },
    {
      data: [67, 46, 61, 8, 66, 3, 76, 81, 52, 46, 81, 80, 34, 96, 81, 28, 19, 4, 16, 11, 56, 85, 84, 17],
      label: 'Event 2'
    },
    {
      data: [98, 48, 40, 19, 86, 27, 90, 35, 91, 58, 48, 76, 70, 85, 70, 25, 95, 62, 84, 98, 32, 47, 62, 77],
      label: 'Event 3'
    },
    {
      data: [85, 59, 80, 81, 56, 55, 40, 65, 49, 90, 21, 58, 54, 44, 56, 55, 40, 65, 49, 90, 21, 58, 48, 76],
      label: 'Event 4'
    }
  ];
  public conversionEventsChartData: Array<any> = [
    {
      data: [98, 48, 40, 19, 86, 27, 90, 35, 15, 36, 75, 57, 30, 50, 11, 55, 92, 15, 19, 62, 84, 48, 78, 36],
      label: 'Conversion Rate'
    },
    {
      data: [50, 11, 55, 92, 15, 19, 62, 84, 49, 90, 21, 58, 54, 44, 56, 55, 40, 65, 49, 90, 21, 58, 48, 76],
      label: 'Revenue'
    },
    {
      data: [85, 59, 80, 81, 56, 55, 40, 65, 49, 90, 21, 58, 54, 44, 96, 75, 54, 72, 49, 95, 41, 58, 38, 96],
      label: 'Conversion Event'
    }
  ];
  public lineChartLabels: Array<any> = ["00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
    "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
    "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00",
    "22:00", "23:00"];
  public lineChartOptions: any = {
    responsive: true,
    options: {
      legend: {
        position: 'right',
        labels: {
          padding: 30
        }
      },
      scales: {
        yAxes: [{
          ticks: {
            beginAtZero: true,
            stepSize: 20,
            min: 0,
            max: 100
          },
          scaleLabel: {
            display: true,
            labelString: 'Number of Users  (in thousands)'
          }
        }],
        xAxes: [{
          scaleLabel: {
            display: true,
            labelString: 'Time  (24 hrs)'
          }
        }]
      },
    }
  };
  public lineChartColors: Array<any> = [
    { // grey
      backgroundColor: 'rgba(148,159,177,0.2)',
      borderColor: 'rgba(148,159,177,1)',
      pointBackgroundColor: 'rgba(148,159,177,1)',
      pointBorderColor: '#fff',
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: 'rgba(148,159,177,0.8)'
    },
    { // dark grey
      backgroundColor: 'rgba(77,83,96,0.2)',
      borderColor: 'rgba(77,83,96,1)',
      pointBackgroundColor: 'rgba(77,83,96,1)',
      pointBorderColor: '#fff',
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: 'rgba(77,83,96,1)'
    },
    { // grey
      backgroundColor: 'rgba(148,159,177,0.2)',
      borderColor: 'rgba(148,159,177,1)',
      pointBackgroundColor: 'rgba(148,159,177,1)',
      pointBorderColor: '#fff',
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: 'rgba(148,159,177,0.8)'
    }
  ];
  public lineChartLegend: boolean = true;
  public lineChartType: string = 'line';
}
