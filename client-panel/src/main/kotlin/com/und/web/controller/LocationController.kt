package com.und.web.controller

import com.und.model.jpa.Cities
import com.und.model.jpa.Countries
import com.und.model.jpa.States
import com.und.service.LocationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController("location")
@RequestMapping("/location")
class LocationController {

    @Autowired
    private lateinit var locationService: LocationService

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/countries"])
    fun getCountriesList(): List<Countries> {
        return locationService.getCountriesList()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/states/{countryId}"])
    fun getStatesByCountryId(@PathVariable countryId: Int): List<States> {
        return locationService.getStatesByCountryId(countryId)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/cities/{stateId}"])
    fun getCitiesByStateId(@PathVariable stateId: Int): List<Cities> {
        return locationService.getCitiesByStateId(stateId)
    }
}