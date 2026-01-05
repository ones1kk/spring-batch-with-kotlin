package io.github.batch

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class DummyEntity(
    @Id
    val id: Long,
)