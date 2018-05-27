package com.und.model.jpa

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "countries")
class Countries {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "countries_id_seq")
    @SequenceGenerator(name = "countries_id_seq", sequenceName = "countries_id_seq", allocationSize = 1)
    var id: Int? = null

    @Column(name = "shortname")
    @NotNull
    var shortname: String? = null

    @Column(name = "name")
    @NotNull
    var name: String? = null

    @Column(name = "phonecode")
    @NotNull
    var phonecode: Int? = null
}

@Entity
@Table(name = "states")
class States {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "states_id_seq")
    @SequenceGenerator(name = "states_id_seq", sequenceName = "states_id_seq", allocationSize = 1)
    var id: Int? = null

    @Column(name = "name")
    @NotNull
    var name: String? = null

    @Column(name = "country_id")
    @NotNull
    var countryId: Int? = null
}

@Entity
@Table(name = "cities")
class Cities {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "cities_id_seq")
    @SequenceGenerator(name = "cities_id_seq", sequenceName = "cities_id_seq", allocationSize = 1)
    var id: Int? = null

    @Column(name = "name")
    @NotNull
    var name: String? = null

    @Column(name = "state_id")
    @NotNull
    var stateId: Int? = null
}