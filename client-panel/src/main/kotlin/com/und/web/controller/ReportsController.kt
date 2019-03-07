package com.und.web.controller

import com.und.common.utils.CompareDate
import com.und.service.ReportsService
import com.und.web.controller.exception.InputUserDateFormatException
import com.und.web.model.EventCount
import com.und.web.model.UserDateInput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import org.springframework.beans.propertyeditors.StringTrimmerEditor
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.InitBinder


@CrossOrigin
@RestController
@RequestMapping("/reports")
class ReportsController {

    @Autowired
    private lateinit var reportsService: ReportsService

    @InitBinder
    fun dataBinder(binder: WebDataBinder) {
        val editor = StringTrimmerEditor(true)
        binder.registerCustomEditor(String::class.java, editor)

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/events")
    fun getEventsCount(@Valid @RequestBody userDateInput: UserDateInput,result:BindingResult):List<EventCount>{

        if(result.hasErrors()){
            throw InputUserDateFormatException("Date Format must be 'yyyy-MM-dd'")
        }
        var userDateInput=CompareDate.getDate(userDateInput)
        var fromDate=userDateInput.fromDate!!
        var toDate=userDateInput.toDate!!

       // println("********************************"+fromDate)
        //print("********************************"+toDate)
        return reportsService.getEventsCount(fromDate,toDate)

    }






}