package io.github.batch

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SampleRepository : JpaRepository<DummyEntity, Long> {

    @Query("select 1")
    fun findOne(): Int
}