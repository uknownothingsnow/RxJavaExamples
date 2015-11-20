package com.github.lzyzsd.rxexamples;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by bruce on 11/20/15.
 */
public class DemoUtils {
    private static int random() {
        return (int)(2000 * Math.random());
    }

    public static Observable createObservable1() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                System.out.println("observable1 produce a");
                subscriber.onNext("a");
                sleep(random());
                System.out.println("observable1 produce b");
                subscriber.onNext("b");
                sleep(random());
                System.out.println("observable1 produce c");
                subscriber.onNext("c");
                sleep(random());
                System.out.println("observable1 produce d");
                subscriber.onNext("d");
                subscriber.onCompleted();
            }
        });
    }

    public static Observable createObservable2() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                System.out.println("observable2 produce e");
                subscriber.onNext("e");
                sleep(random());
                System.out.println("observable2 produce f");
                subscriber.onNext("f");
                sleep(random());
                System.out.println("observable2 produce g");
                subscriber.onNext("g");
                sleep(random());
                System.out.println("observable2 produce h");
                subscriber.onNext("h");
                subscriber.onCompleted();
            }
        });
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
