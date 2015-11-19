package com.github.lzyzsd.rxexamples;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity {

    RxSharedPreferences rxPreferences;

    @OnClick(R.id.btn_run_concat)
    public void onRunConcatClick(View view) {
        testConcat();
    }

    @OnClick(R.id.btn_run_merge)
    public void onRunMergeClick(View view) {
        testMerge();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        rxPreferences = RxSharedPreferences.create(preferences);

        testThrottle();

        Observable.just("hello")
                //指定被观察者在新的线程中生产数据
                .subscribeOn(Schedulers.newThread())
                //指定观察者在UI主线程接收数据
                .observeOn(AndroidSchedulers.mainThread())
                //因为上面指定了在UI线程接收数据，所以这里可以做更新UI的事情
                .subscribe(System.out::println);

        Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("a");
                subscriber.onNext("b");
                subscriber.onNext("c");
                subscriber.onCompleted();
            }
        });

        observable.subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Object o) {
            }
        });
    }

    //拼接两个Observable的输出，保证顺序，按照Observable在concat中的顺序，依次将每个Observable产生的事件传递给订阅者
    //只有当前面的Observable结束了，才会执行后面的
    private void testConcat() {
        Observable observable1 = createObservable1().subscribeOn(Schedulers.newThread());
        Observable observable2 = createObservable2().subscribeOn(Schedulers.newThread());

        Observable.concat(observable1, observable2)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println(s);
                    }
                });
    }

    //拼接两个Observable的输出，不保证顺序，按照事件产生的顺序发送给订阅者
    private void testMerge() {
        Observable.merge(createObservable1().subscribeOn(Schedulers.newThread()), createObservable2().subscribeOn(Schedulers.newThread()))
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println(s);
                    }
                });
    }

    //嵌套有依赖
    private void testNestedDependency() {
        NetworkService.getToken("username", "password")
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        return NetworkService.getMessage(s);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("message: " + s);
                    }
                });
    }

    private void testThrottle() {
        RxView.clicks(findViewById(R.id.btn_throttle))
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        System.out.println("click");
                    }
                });
    }

    private String memoryCache = null;
    @OnClick(R.id.btn_fetch_data)
    public void testCache(View view) {
        final Observable<String> memory = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (memoryCache != null) {
                    subscriber.onNext(memoryCache);
                } else {
                    subscriber.onCompleted();
                }
            }
        });
        Observable<String> disk = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String cachePref = rxPreferences.getString("cache").get();
                if (!TextUtils.isEmpty(cachePref)) {
                    subscriber.onNext(cachePref);
                } else {
                    subscriber.onCompleted();
                }
            }
        });

        Observable<String> network = Observable.just("network");

        Observable.concat(memory, disk, network)
        .first()
        .subscribeOn(Schedulers.newThread())
        .subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                memoryCache = "memory";
                System.out.println("--------------subscribe: " + s);
            }
        });
    }

    @OnClick(R.id.btn_test_tranform)
    public void testMap() {
        Observable.just("1", "2", "2", "3", "4", "5")
                .map(Integer::parseInt)
                .filter(s -> s > 1)
                .distinct()
                .take(3)
                .reduce((integer, integer2) -> integer.intValue() + integer2.intValue())
                .subscribe(System.out::println);//9

        Observable.just("a")
            .lift(subscriber -> {
                return new Subscriber<String>() {
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
                        subscriber.onNext(1);
                    }
                };
            })
            .map(i -> i)
            .subscribe(System.out::println);//1
    }

    private int random() {
        return (int)(2000 * Math.random());
    }

    private Observable createObservable1() {
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

    private Observable createObservable2() {
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

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
