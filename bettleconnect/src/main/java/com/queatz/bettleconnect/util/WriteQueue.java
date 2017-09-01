package com.queatz.bettleconnect.util;

import java.util.LinkedList;

/**
 * Created by jacob on 7/23/17.
 *
 * A queue that waits for each asynchronous item to report completion before moving to the next item.
 */
public abstract class WriteQueue<T> {
    private LinkedList<T> queue = new LinkedList<>();
    private boolean running;

    public void add(T item) {
        if (!queue.isEmpty() || running) {
            queue.add(item);
            return;
        }

        emit(item);
        running = true;
    }

    public void next() {
        if (queue.isEmpty()) {
            running = false;
            return;
        }

        emit(queue.removeFirst());
        running = true;
    }

    public abstract void emit(T item);
}
