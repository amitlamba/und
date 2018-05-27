package com.und.repository.jpa

import com.und.model.jpa.Cities
import com.und.model.jpa.Countries
import com.und.model.jpa.States
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CountriesRepository : JpaRepository<Countries, Int> {
    fun findByName(name: String): Countries
}

@Repository
interface StatesRepository : JpaRepository<States, Int> {
    fun findByCountryId(countryId: Int): List<States>
}

@Repository
interface CitiesRepository : JpaRepository<Cities, Int> {
    fun findByStateId(stateId: Int): List<Cities>
}