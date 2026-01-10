package io.github.batch.sample

import io.github.batch.BatchApplication
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(
    classes = [BatchApplication::class],
)
@Import(SampleJobExecutionTest.SampleJobConfig::class)
class SampleJobExecutionTest(
    private val jobLauncher: JobLauncher,
    @Qualifier("sampleJob") private val sampleJob: Job,

    ) {

    @Test
//    @Transactional
    fun test() {
        val params = JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters()

        jobLauncher.run(sampleJob, params)
    }

    @Configuration
    class SampleJobConfig(
        private val jobRepository: JobRepository,
        private val transactionManager: PlatformTransactionManager,
    ) {

        @Bean
        fun sampleJob(sampleStep: Step): Job = JobBuilder("sampleJob", jobRepository)
            .start(sampleStep)
            .build()

        @Bean
        fun sampleStep(sampleTasklet: Tasklet): Step = StepBuilder("sampleStep", jobRepository)
            .tasklet(sampleTasklet, transactionManager)
            .transactionManager(transactionManager)
            .build()

        @Bean
        fun sampleTasklet(): Tasklet = Tasklet { _, _ ->
            RepeatStatus.FINISHED
        }
    }
}
