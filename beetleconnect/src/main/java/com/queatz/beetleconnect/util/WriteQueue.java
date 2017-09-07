package com.queatz.beetleconnect.util;

import android.util.Log;

import java.util.LinkedList;

/**
 * Created by jacob on 7/23/17.
 *
 * A queue that waits for each asynchronous item to report completion before moving to the next item.
 */
public abstract class WriteQueue<T> {
    private LinkedList<T> queue = new LinkedList<>();
    private T current = null;

    public synchronized void add(T item) {
        Log.w("BEETLE", "queue - add - " + queue.size() + " - " + running());
        if (running()) {
            queue.add(item);
            return;
        }

        emit(item);
        current = item;
    }

    public synchronized void next() {
        Log.w("BEETLE", "queue - next - " + queue.size() + " - " + running());
        if (queue.isEmpty()) {
            current = null;
            return;
        }

        current = queue.removeFirst();
        emit(current);
    }

    public synchronized boolean running() {
        return current != null;
    }

    public synchronized T current() {
        return current;
    }

    public synchronized void retryCurrent() {
        if (!running()) {
            return;
        }

        emit(current);
    }

    public abstract void emit(T item);
}
