package com.und.service

import com.und.model.jpa.Cities
import com.und.model.jpa.Countries
import com.und.model.jpa.States
import com.und.repository.jpa.CitiesRepository
import com.und.repository.jpa.CountriesRepository
import com.und.repository.jpa.StatesRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LocationService {
    @Autowired
    private lateinit var countriesRepository: CountriesRepository
    @Autowired
    private lateinit var statesRepository: StatesRepository
    @Autowired
    private lateinit var citiesRepository: CitiesRepository

    fun getCountriesList(): List<Countries> {
        return countriesRepository.findAll()
    }

    fun getStatesByCountryId(countryId: Int): List<States> {
        return statesRepository.findByCountryId(countryId)
    }

    fun getCitiesByStateId(stateId: Int): List<Cities> {
        return citiesRepository.findByStateId(stateId)
    }
}