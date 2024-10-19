package org.example;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TripletDeque<E> implements Deque<E>, Containerable {

    private static final int DEFAULT_TRIPLET_LENGTH = 5;
    private static final int DEFAULT_MAX_Q_SIZE = 1000;

    private int tripletLength;
    private int maxQSize;
    private int size;

    private Container<E> first;
    private Container<E> last;

    public TripletDeque(int tripletLength) {
        this(tripletLength, DEFAULT_MAX_Q_SIZE);
    }

    public TripletDeque() {
        this(DEFAULT_TRIPLET_LENGTH, DEFAULT_MAX_Q_SIZE);
    }

    public TripletDeque(int tripletLength, int maxQSize) {
        this.tripletLength = tripletLength;
        this.maxQSize = maxQSize;
        this.first = new Container<>(tripletLength);
        this.last = first;
    }


    @Override
    public Object[] getContainerByIndex(int cIndex) {
        Container<E> current = first;
        int currentIndex = 0;
        while (current != null) {
            if (currentIndex == cIndex) {
                return current.elements;
            }
            current = current.next;
            currentIndex++;
        }
        return null;
    }


    private static class Container<E> {
        E[] elements;
        Container<E> next;
        Container<E> prev;

        Container(int size) {
            elements = (E[]) new Object[size];
        }
    }

    @Override
    public void addFirst(E e) {
        if (e == null) {
            throw new NullPointerException("Cannot add null element");
        }
        if (size >= maxQSize) {
            throw new IllegalStateException("Deque is full");
        }
        if (isContainerFull(first)) {
            Container<E> newFirst = new Container<>(tripletLength);
            newFirst.next = first;
            first.prev = newFirst;
            first = newFirst;
        }
        for (int i = tripletLength - 1; i > 0; i--) {
            first.elements[i] = first.elements[i - 1];
        }
        first.elements[0] = e;
        size++;
    }

    @Override
    public void addLast(E e) {
        if (e == null) {
            throw new NullPointerException("Cannot add null element");
        }
        if (size >= maxQSize) {
            throw new IllegalStateException("Deque is full");
        }
        if (isContainerFull(last)) {
            Container<E> newLast = new Container<>(tripletLength);
            newLast.prev = last;
            last.next = newLast;
            last = newLast;
        }
        for (int i = 0; i < tripletLength; i++) {
            if (last.elements[i] == null) {
                last.elements[i] = e;
                size++;
                return;
            }
        }
    }

    private boolean isContainerFull(Container<E> container) {
        for (int i = 0; i < tripletLength; i++) {
            if (container.elements[i] == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean offerFirst(E e) {
        if (size >= maxQSize) {
            return false;
        }
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        if (size >= maxQSize) {
            return false;
        }
        addLast(e);
        return true;
    }

    @Override
    public E removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        E element = first.elements[0];
        for (int i = 0; i < tripletLength - 1; i++) {
            first.elements[i] = first.elements[i + 1];
        }
        first.elements[tripletLength - 1] = null;
        size--;

        if (first.elements[0] == null && first.next != null) {
            first = first.next;
            first.prev = null;
        }

        return element;
    }

    @Override
    public E removeLast() {
        if (size == 0) {
            throw new NoSuchElementException("Deque is empty");
        }

        int lastIndex = tripletLength - 1;
        while (lastIndex >= 0 && last.elements[lastIndex] == null) {
            lastIndex--;
        }

        if (lastIndex >= 0) {
            E element = last.elements[lastIndex];
            last.elements[lastIndex] = null;
            size--;
            //System.out.println("Removed element " + element + " from last container");

            if (lastIndex == 0 && last.prev != null) {
                last = last.prev;
                last.next = null;
                //System.out.println("Removed element " + element + " from last container");
            }

            return element;
        }

        if (last.prev != null) {
            last = last.prev;
            last.next = null;
            System.out.println("Moved to previous last container");
            return removeLast();
        }

        throw new NoSuchElementException("Deque is empty");
    }

    @Override
    public E pollFirst() {
        if (size == 0) {
            return null;
        }
        return removeFirst();
    }

    @Override
    public E pollLast() {
        if (size == 0) {
            return null;
        }
        return removeLast();
    }

    @Override
    public E getFirst() {
        if (size == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        return first.elements[0];
    }

    @Override
    public E getLast() {
        if (size == 0) {
            throw new NoSuchElementException("Deque is empty");
        }

        // Находим последний непустой элемент в последнем контейнере
        int lastIndex = tripletLength - 1;
        while (lastIndex >= 0 && last.elements[lastIndex] == null) {
            lastIndex--;
        }

        if (lastIndex >= 0) {
            return last.elements[lastIndex];
        }

        // Если последний контейнер пуст, переходим к предыдущему контейнеру
        if (last.prev != null) {
            last = last.prev;
            return getLast();
        }

        throw new NoSuchElementException("Deque is empty");
    }

    @Override
    public E peekFirst() {
        if (size == 0) {
            return null;
        }
        return first.elements[0];
    }

    @Override
    public E peekLast() {
        if (size == 0) {
            return null;
        }
        return last.elements[tripletLength - 1];
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        Container<E> current = first;
        while (current != null) {
            for (int i = 0; i < tripletLength; i++) {
                if (Objects.equals(o, current.elements[i])) {
                    // Сдвигаем элементы влево
                    for (int j = i; j < tripletLength - 1; j++) {
                        current.elements[j] = current.elements[j + 1];
                    }
                    current.elements[tripletLength - 1] = null;
                    size--;

                    // Если контейнер стал пустым, удаляем его
                    if (isContainerEmpty(current)) {
                        removeEmptyContainer(current);
                    }

                    return true;
                }
            }
            current = current.next;
        }
        return false;
    }
    private boolean isContainerEmpty(Container<E> container) {
        for (int i = 0; i < tripletLength; i++) {
            if (container.elements[i] != null) {
                return false;
            }
        }
        return true;
    }

    private void removeEmptyContainer(Container<E> container) {
        if (container.prev != null) {
            container.prev.next = container.next;
        } else {
            first = container.next;
        }
        if (container.next != null) {
            container.next.prev = container.prev;
        } else {
            last = container.prev;
        }
    }
    @Override
    public boolean removeLastOccurrence(Object o) {
        Container<E> current = last;
        while (current != null) {
            for (int i = tripletLength - 1; i >= 0; i--) {
                if (Objects.equals(o, current.elements[i])) {
                    System.arraycopy(current.elements, i + 1, current.elements, i, tripletLength - i - 1);
                    current.elements[tripletLength - 1] = null;
                    size--;
                    return true;
                }
            }
            current = current.prev;
        }
        return false;
    }

    @Override
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        for (E e : c) {
            addLast(e);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            while (remove(o)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        boolean modified = false;
        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            if (filter.test(iterator.next())) {
                iterator.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            if (!c.contains(iterator.next())) {
                iterator.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        first = new Container<>(tripletLength);
        last = first;
        size = 0;
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size, Spliterator.ORDERED | Spliterator.NONNULL);
    }

    @Override
    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        Container<E> current = first;
        while (current != null) {
            for (int i = 0; i < tripletLength; i++) {
                if (Objects.equals(o, current.elements[i])) {
                    return true;
                }
            }
            current = current.next;
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new TripletDequeIterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Container<E> current = first;
        while (current != null) {
            for (int i = 0; i < tripletLength; i++) {
                if (current.elements[i] != null) {
                    action.accept(current.elements[i]);
                }
            }
            current = current.next;
        }
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        int index = 0;
        Container<E> current = first;
        while (current != null) {
            for (int i = 0; i < tripletLength; i++) {
                if (current.elements[i] != null) {
                    array[index++] = current.elements[i];
                }
            }
            current = current.next;
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        int index = 0;
        Container<E> current = first;
        while (current != null) {
            for (int i = 0; i < tripletLength; i++) {
                if (current.elements[i] != null) {
                    a[index++] = (T) current.elements[i];
                }
            }
            current = current.next;
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return toArray(generator.apply(size));
    }

    @Override
    public Iterator<E> descendingIterator() {
        throw new UnsupportedOperationException("descendingIterator() is not implemented");
    }


    private class TripletDequeIterator implements Iterator<E> {
        private Container<E> currentContainer = first;
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            while (currentContainer != null) {
                if (currentIndex < tripletLength && currentContainer.elements[currentIndex] != null) {
                    return true;
                }
                currentContainer = currentContainer.next;
                currentIndex = 0;
            }
            return false;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E element = currentContainer.elements[currentIndex];
            currentIndex++;
            return element;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class TripletDequeDescendingIterator implements Iterator<E> {
        private Container<E> currentContainer = last;
        private int currentIndex = tripletLength - 1;

        @Override
        public boolean hasNext() {
            return currentContainer != null && (currentIndex >= 0 || currentContainer.prev != null);
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (currentIndex < 0) {
                currentContainer = currentContainer.prev;
                currentIndex = tripletLength - 1;
            }
            return currentContainer.elements[currentIndex--];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}