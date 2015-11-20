package com.github.lzyzsd.rxexamples.operators;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by bruce on 11/20/15.
 * 展示如何使用lift自定义operator
 * 好处是可以让原有的Observable链式调用不被打断，继续保持链式调用的形式
 */
public class LiftDemo {
    /**
     * 这里展示了如何自定义一个operator，用来将每个发出来的字符串转换成一个整数
     */
    public static void createCustomOperator() {
        Observable.just("1", "2", "3")
                .lift(subscriber -> new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onNext(String s) {
                        subscriber.onNext(Integer.parseInt(s));
                    }
                })
                .map(i -> i)
                .subscribe(System.out::println);//1
    }
}
