// Copyright (C) 2009 Mihai Preda

package calculator;

class Data {
    float[] xs = new float[4];
    float[] ys = new float[4];
    int size = 0;
    int allocSize = 4;

    void push(float x, float y) {
        if (size >= allocSize) {
            allocSize += allocSize;
            float[] a = new float[allocSize];
            System.arraycopy(xs, 0, a, 0, size);
            xs = a;
            a = new float[allocSize];
            System.arraycopy(ys, 0, a, 0, size);
            ys = a;
        }
        xs[size] = x;
        ys[size] = y;
        ++size;
    }

    float topX() {
        return xs[size-1];
    }

    float topY() {
        return ys[size-1];
    }

    void pop() {
        --size;
    }

    boolean empty() {
        return size == 0;
    }

    void clear() {
        size = 0;
    }
}
