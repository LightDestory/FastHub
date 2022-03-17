package com.fastaccess.ui.modules.main.premium

import io.reactivex.SingleEmitter
import com.google.android.gms.tasks.OnCompleteListener
import io.reactivex.CompletableEmitter
import java.lang.AssertionError

class GmsTaskListeners private constructor() {
    companion object {
        fun <T> listener(emitter: SingleEmitter<T>): OnCompleteListener<T> {
            return OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    if (!emitter.isDisposed) {
                        emitter.onError(task.exception!!)
                    }
                    return@OnCompleteListener
                }
                if (!emitter.isDisposed) {
                    emitter.onSuccess(task.result)
                }
            }
        }

        fun listener(emitter: CompletableEmitter): OnCompleteListener<Void> {
            return OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    if (!emitter.isDisposed) {
                        emitter.onError(task.exception!!)
                    }
                    return@OnCompleteListener
                }
                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }
        }
    }

    init {
        throw AssertionError("No instances")
    }
}