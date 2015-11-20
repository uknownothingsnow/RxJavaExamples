package com.github.lzyzsd.rxexamples.operators;

import rx.Observable;

/**
 * Created by bruce on 11/20/15.
 */
public class TransformDemo {
    public static void complexTransform() {
        Observable.just("1", "2", "2", "3", "4", "5")
                .map(Integer::parseInt)
                .filter(s -> s > 1)
                .distinct()
                .take(3)
                .reduce((integer, integer2) -> integer.intValue() + integer2.intValue())
                .subscribe(System.out::println);//9
    }
}
