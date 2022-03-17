package com.fastaccess.helper

import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by Kosh on 11 Nov 2016, 11:53 AM
 */
object RxHelper {
    @JvmStatic
    fun <T> getObservable(observable: Observable<T>): Observable<T> {
        return observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    @JvmStatic
    fun <T> safeObservable(observable: Observable<T>): Observable<T> {
        return getObservable(observable)
            .doOnError { obj: Throwable -> obj.printStackTrace() }
    }

    @JvmStatic
    fun <T> getSingle(single: Single<T>): Single<T> {
        return single
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}