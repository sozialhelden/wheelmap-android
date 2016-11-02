package com.google.atap.tango.ux;

import android.content.Context;
import android.util.AttributeSet;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class CustomTangoUxLayout extends TangoUxLayout {

    public enum ConnectionStatus {
        SHOW,
        HIDE
    }

    private BehaviorSubject<ConnectionStatus> connectionStatusBehaviorSubject = BehaviorSubject.create();

    public CustomTangoUxLayout(Context context) {
        super(context);
    }

    public CustomTangoUxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTangoUxLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    void hideConnectionLayout(boolean animate) {
        super.hideConnectionLayout(animate);
        connectionStatusBehaviorSubject.onNext(ConnectionStatus.HIDE);
    }

    @Override
    void showConnectionLayout(boolean bubbleLevelEnabled) {
        super.showConnectionLayout(bubbleLevelEnabled);
        connectionStatusBehaviorSubject.onNext(ConnectionStatus.SHOW);
    }

    public Observable<ConnectionStatus> connectionStatusObservable() {
        return connectionStatusBehaviorSubject;
    }

}
