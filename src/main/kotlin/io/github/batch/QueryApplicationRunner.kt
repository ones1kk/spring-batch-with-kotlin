package io.github.batch

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class QueryApplicationRunner(
    private val repository: SampleRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val one = repository.findOne()
//        println("one : $one")
    }

}