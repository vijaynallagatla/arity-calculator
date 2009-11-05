// Copyright (C) 2009 Mihai Preda

package calculator;

class Data {
    float[] xs = new float[4];
    float[] ys = new float[4];
    int size = 0;
    int allocSize = 4;

    void push(float x, float y) {
        if (size >= allocSize) {
            makeSpace(size+1);
        }
        xs[size] = x;
        ys[size] = y;
        ++size;
    }

    private void makeSpace(int sizeNeeded) {
        int oldAllocSize = allocSize;
        while (sizeNeeded > allocSize) {
            allocSize += allocSize;
        }
        if (oldAllocSize != allocSize) {
            float[] a = new float[allocSize];
            System.arraycopy(xs, 0, a, 0, size);
            xs = a;
            a = new float[allocSize];
            System.arraycopy(ys, 0, a, 0, size);
            ys = a;
        }
    }

    float topX() {
        return xs[size-1];
    }

    float topY() {
        return ys[size-1];
    }

    float firstX() {
        return xs[0];
    }

    float firstY() {
        return ys[0];
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

    void eraseBefore(float x) {
        int pos = 0;
        while (pos < size && xs[pos] < x) {
            ++pos;
        }
        --pos;
        if (pos > 0) {
            size -= pos;
            System.arraycopy(xs, pos, xs, 0, size);
            System.arraycopy(ys, pos, ys, 0, size);
        }
    }

    void eraseAfter(float x) {
        int pos = size-1;
        while (pos >= 0 && x < xs[pos]) {
            --pos;
        }
        ++pos;
        if (pos < size-1) {
            size = pos+1;
        }
    }

    void append(Data d) {
        makeSpace(size + d.size);
        int pos = 0;
        float last = xs[size-1];
        while (pos < d.size && d.xs[pos] <= last) {
            ++pos;
        }
        System.arraycopy(d.xs, pos, xs, size, d.size-pos);
        System.arraycopy(d.ys, pos, ys, size, d.size-pos);
        size += d.size-pos;
    }
}
