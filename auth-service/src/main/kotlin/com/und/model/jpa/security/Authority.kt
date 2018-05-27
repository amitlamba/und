package com.und.model.jpa.security

import org.hibernate.annotations.DynamicUpdate
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "AUTHORITY")
@DynamicUpdate(true)
class Authority {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "authority_id_seq")
    @SequenceGenerator(name = "authority_id_seq", sequenceName = "authority_id_seq", allocationSize = 1)
    var id: Long? = null

    @Column(name = "NAME", length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    var name: AuthorityName = AuthorityName.ROLE_NONE

    @ManyToMany(mappedBy = "authorities", fetch = FetchType.LAZY)
    var users: List<User>? = null
}