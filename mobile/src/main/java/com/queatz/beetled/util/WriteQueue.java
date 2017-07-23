package com.queatz.beetled.util;

import java.util.LinkedList;

/**
 * Created by jacob on 7/23/17.
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
