package io.github.batch.tx

import org.springframework.transaction.annotation.Transactional

open class SampleTxServiceImpl : SampleTxService {
    @Transactional
    override fun doWork(): String = "ok"
}