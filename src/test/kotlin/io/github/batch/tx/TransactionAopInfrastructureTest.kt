package io.github.batch.tx

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator
import org.springframework.aop.support.AopUtils
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor
import org.springframework.transaction.interceptor.TransactionInterceptor

class TransactionAopInfrastructureTest {

    @Test
    fun `트랜잭션 서비스가 프록시로 감싸지고 인터셉터에 연결되는지`() {
        AnnotationConfigApplicationContext(TestTxConfig::class.java).use { context ->
            val service = context.getBean(SampleTxService::class.java)

            assertThat(AopUtils.isAopProxy(service)).isTrue()

            val advisor = context.getBean(BeanFactoryTransactionAttributeSourceAdvisor::class.java)
            assertThat(advisor.advice).isInstanceOf(TransactionInterceptor::class.java)

            val interceptor = advisor.advice as TransactionInterceptor
            assertThat(resolveTransactionAttribute(interceptor)).isNotNull()

            val autoProxyCreatorName = "org.springframework.aop.config.internalAutoProxyCreator"
            val autoProxyCreator = context.getBean(autoProxyCreatorName)
            assertThat(autoProxyCreator).isInstanceOf(InfrastructureAdvisorAutoProxyCreator::class.java)
        }
    }

    private fun resolveTransactionAttribute(interceptor: TransactionInterceptor): Any? {
        val source = interceptor.transactionAttributeSource ?: return null
        val method = SampleTxServiceImpl::class.java.getMethod("doWork")
        return source.getTransactionAttribute(method, SampleTxServiceImpl::class.java)
    }
}


@Configuration
@EnableTransactionManagement
private class TestTxConfig {

    @Bean
    fun transactionManager(): PlatformTransactionManager = ResourcelessTransactionManager()

    @Bean
    fun sampleTxService(): SampleTxService = SampleTxServiceImpl()
}
