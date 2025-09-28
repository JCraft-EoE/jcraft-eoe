package net.arna.jcraft.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class CycleDeque<T> implements Deque<T> {

    protected final Deque<T> content = new LinkedList<>();

    protected final int maxSize;

    public CycleDeque(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize cannot be negative!");
        }
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(T t) {
        if (size() == maxSize) {
            content.removeFirst();
        }
        return content.add(t);
    }

    @Override
    public void addFirst(T t) {
        if (size() == maxSize) {
            throw new IllegalStateException("Cannot add at first position in full " + CycleDeque.class.getSimpleName() + "!");
        }
        content.addFirst(t);
    }

    @Override
    public void push(T t) {
        if (size() == maxSize) {
            throw new IllegalStateException(CycleDeque.class.getSimpleName() + " is full!");
        }
        content.addFirst(t);
    }

    @Override
    public T remove() {
        if (isEmpty()) {
            throw new NoSuchElementException("remove() called on empty " + CycleDeque.class.getSimpleName() + "!");
        }
        return content.remove();
    }

    @Override
    public T poll() {
        if (isEmpty()) {
            return null;
        }
        return content.removeFirst();
    }

    @Override
    public T element() {
        if (isEmpty()) {
            throw new NoSuchElementException("element() called on empty " + CycleDeque.class.getSimpleName() + "!");
        }
        return content.getFirst();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException("Cannot arbitrarily add elements to a " + CycleDeque.class.getSimpleName() + "!");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("Cannot arbitrarily remove elements from a " + CycleDeque.class.getSimpleName() + "!");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("Cannot arbitrarily retain elements in a " + CycleDeque.class.getSimpleName() + "!");
    }

    @Override
    public int size() {
        return content.size();
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return content.contains(o);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return content.iterator();
    }

    @Override
    public @NotNull Iterator<T> descendingIterator() {
        return content.descendingIterator();
    }

    @Override
    public @NotNull Object[] toArray() {
        return content.toArray();
    }

    @Override
    public @NotNull <S> S[] toArray(@NotNull S[] a) {
        return content.toArray(a);
    }

    @Override
    public void addLast(T t) {
        add(t);
    }

    @Override
    public boolean offerFirst(T t) {
        if (size() == maxSize) {
            return false;
        }
        addFirst(t);
        return true;
    }

    @Override
    public boolean offerLast(T t) {
        return false;
    }

    @Override
    public T removeFirst() {
        return content.removeFirst();
    }

    @Override
    public T removeLast() {
        return content.removeLast();
    }

    @Override
    public T pollFirst() {
        return content.pollFirst();
    }

    @Override
    public T pollLast() {
        return content.pollLast();
    }

    @Override
    public T getFirst() {
        return content.getFirst();
    }

    @Override
    public T getLast() {
        return content.getLast();
    }

    @Override
    public T peekFirst() {
        return content.peekFirst();
    }

    @Override
    public T peekLast() {
        return content.peekLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return content.removeFirstOccurrence(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return content.removeLastOccurrence(o);
    }

    @Override
    public boolean remove(Object o) {
        return content.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return content.contains(c);
    }

    @Override
    public T pop() {
        return content.removeFirst();
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public boolean offer(T t) {
        if (size() == maxSize) {
            return false;
        }
        return add(t);
    }

    @Override
    public T peek() {
        return content.peekFirst();
    }

}
