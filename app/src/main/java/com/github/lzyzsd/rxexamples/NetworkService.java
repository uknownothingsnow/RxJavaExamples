package com.github.lzyzsd.rxexamples;

import rx.Observable;

/**
 * Created by bruce on 11/18/15.
 */
public class NetworkService {
    public static Observable<String> getToken(String userName, String pwd) {
        return Observable.just("token");
    }

    public static Observable<String> getMessage(String token) {
        return Observable.just("message");
    }
}
