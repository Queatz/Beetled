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
    private boolean running;

    public synchronized void add(T item) {
        Log.w("BEETLE", "queue - add - " + queue.size() + " - " + running);
        if (!queue.isEmpty() || running) {
            queue.add(item);
            return;
        }

        emit(item);
        running = true;
    }

    public synchronized void next() {
        Log.w("BEETLE", "queue - next - " + queue.size() + " - " + running);
        if (queue.isEmpty()) {
            running = false;
            return;
        }

        emit(queue.removeFirst());
        running = true;
    }

    public abstract void emit(T item);
}
