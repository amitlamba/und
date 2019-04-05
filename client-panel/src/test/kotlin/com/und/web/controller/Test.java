/*
package com.und.web.controller;

import com.und.report.web.model.UserCountByEvent;
import com.und.report.web.model.UserCountByEventForDate;

import java.util.*;

class Test1 {
     public static void main(String... args){
         UserCountByEventForDate data1= new UserCountByEventForDate("20", Arrays.asList(new UserCountByEvent(1,"view")));
         UserCountByEventForDate data2= new UserCountByEventForDate("19", Arrays.asList(new UserCountByEvent(1,"add to cart"),new UserCountByEvent(3,"view")));
         UserCountByEventForDate data3= new UserCountByEventForDate("21", Arrays.asList(new UserCountByEvent(3,"search")));
         List<UserCountByEventForDate> data= Arrays.asList(data1,data2,data3);

//         var dates=data.map { d->d.date }
         List dates=new ArrayList();
         for(int i=0;i<data.size();i++){
             dates.add(data.get(i));
         }
         int size=dates.size();

         Set cat=new HashSet<String>();

         Map map=new HashMap<String,ArrayList<Integer>>();

//         data.forEach{data->data.userCountData.forEach{data->cat.add(data.eventname)}};
         List<UserCountByEvent> user=data.get(1).getUserCountData();

         data.forEach{data->{

             var catCopy=cat;
             data.userCountData.forEach{data->{
                 if(map.containsKey(data.eventname)){
                     var list=map.get(data.eventname)!!
                             list.add(data.usercount);
                     map.set(data.eventname,list);
                     catCopy.remove(data.eventname);
                 }else{
                     var l= ArrayList<Int>();
                     l.add(data.usercount);
                     map.set(data.eventname,l);
                     catCopy.remove(data.eventname);
                 }
             }}
             catCopy.forEach{event->{
                 if(map.containsKey(event)){
                     var list=map.get(event)!!;
                     list.add(0);
                     map.set(event,list);
                 }else{
                     var l=ArrayList<Int>();
                     l.add(0);
                     map.set(event,l);
                 }
             }}
         }}
     }
}
*/
