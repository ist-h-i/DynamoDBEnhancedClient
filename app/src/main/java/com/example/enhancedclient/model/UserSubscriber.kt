package com.example.enhancedclient.model

import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import software.amazon.awssdk.enhanced.dynamodb.model.Page
import java.util.Objects
import java.util.concurrent.CountDownLatch
import java.util.stream.Collectors

class UserSubscriber: Subscriber<Page<User>> {
    // 読み込み完了まで待機するためのLatch
    private val latch = CountDownLatch(1)
    private val itemsFromAllPages: List<User> = ArrayList()
    private var subscription: Subscription? = null

    override fun onSubscribe(sub: Subscription?) {
        subscription = sub
        subscription!!.request(1L)
        try {
            // 待機開始
            latch.await()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    override fun onNext(userPage: Page<User>) {
        userPage.items()?.let {
            itemsFromAllPages.stream().filter { obj: User? ->
                Objects.nonNull(
                    obj
                )
            }.collect(Collectors.toList<Any>()).addAll(it)
        }
        subscription!!.request(1L)
    }

    override fun onError(throwable: Throwable?) {}

    override fun onComplete() {
        // 待機完了
        latch.countDown()
    }

    fun getSubscribedItems(): List<User> {
        return itemsFromAllPages
    }
}