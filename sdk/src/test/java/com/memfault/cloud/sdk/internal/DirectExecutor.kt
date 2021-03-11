package com.memfault.cloud.sdk.internal

import java.util.concurrent.Executor

class DirectExecutor : Executor {
    override fun execute(command: Runnable?) {
        command?.run()
    }
}
