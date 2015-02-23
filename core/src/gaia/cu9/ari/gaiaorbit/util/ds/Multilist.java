package gaia.cu9.ari.gaiaorbit.util.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A multiple list, holding a number of indexed lists. Useful for threaded applications.
 * Not all methods of {@link java.util.List} are implemented, check the comments.
 * @author Toni Sagrista
 *
 * @param <T>
 */
public class Multilist<T> implements List<T> {

    private final List<T>[] lists;
    private Integer size;
    private List<T> tolist;

    /**
     * Creates a multiple list with the given number of lists and an
     * initial capacity of each list of ten.
     * @param numLists The number of lists.
     */
    public Multilist(int numLists) {
	this(numLists, 10);
    }

    /**
     * Creates a multiple list with the given number of lists and initial
     * capacity for each list.
     * @param numLists The number of lists.
     * @param initialCapacity The initial capacity for each list.
     */
    public Multilist(int numLists, int initialCapacity) {
	super();
	lists = new List[numLists];
	for (int i = 0; i < lists.length; i++) {
	    lists[i] = new ArrayList<T>(initialCapacity);
	}
	this.size = 0;
	tolist = new ArrayList<T>(initialCapacity * numLists);
    }

    /**
     * Converts this multilist to a simple list
     * @return
     */
    public List<T> toList() {
	int size = lists.length;
	for (int i = 0; i < size; i++)
	    tolist.addAll(lists[i]);
	return tolist;
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
    public boolean contains(Object o) {
	int size = lists.length;
	for (int i = 0; i < size; i++) {
	    if (lists[i].contains(o))
		return true;
	}
	return false;
    }

    /**
     * Returns true if this collection contains the specified element in the list identified
     * by the given index.
     * More formally, returns true if and only if the list contains
     * at least one element e such that (o==null ? e==null : o.equals(e)).
     * @param o element whose presence in this collection is to be tested
     * @param listIndex <tt>true</tt> if this collection contains the specified element.
     * @return
     */
    public boolean contains(Object o, int listIndex) {
	return lists[listIndex].contains(o);
    }

    @Override
    /** Not implemented **/
    public Iterator<T> iterator() {
	return new MultilistIterator<T>();
    }

    @Override
    public T[] toArray() {
	List<T> l = new ArrayList<T>(size);
	int size = lists.length;
	for (int i = 0; i < size; i++)
	    l.addAll(lists[i]);
	return (T[]) l.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
	if (a.length >= size) {
	    int k = 0;
	    // Use a
	    int size = lists.length;
	    for (int i = 0; i < size; i++) {
		int listSize = lists[i].size();
		for (int j = 0; j < listSize; j++) {
		    a[k] = (T) lists[i].get(j);
		    k++;
		}
	    }
	    return a;
	} else {
	    return (T[]) toArray();
	}
    }

    @Override
    public boolean add(T e) {
	incrementSize();
	return lists[0].add(e);
    }

    /**
     * Adds the element e in the list identified by the given index.
     * See {@link java.util.Collection#add(Object))}.
     * @param e The element to add.
     * @param index The index of the list to add the element to.
     * @return <tt>true</tt> if this collection changed as a result of the call
     */
    public boolean add(T e, int index) {
	incrementSize();
	return lists[index].add(e);
    }

    @Override
    public boolean remove(Object o) {
	for (int i = 0; i < lists.length; i++) {
	    if (lists[i].remove(o)) {
		decrementSize();
		return true;
	    }
	}
	return false;
    }

    /**
     * Removes the element e in the list identified by the given index. This version
     * is faster than the version without the index.
     * See {@link java.util.Collection#remove(Object))}.
     * @param e The element to remove.
     * @param index The index of the list to remove the element from.
     * @return <tt>true</tt> if the element was removed as a result of the call
     */
    public boolean remove(Object o, int index) {
	if (lists[index].remove(o)) {
	    decrementSize();
	    return true;
	}
	return false;
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
    public boolean addAll(Collection<? extends T> c) {
	if (lists[0].addAll(c)) {
	    incrementSize(c.size());
	    return true;
	}
	return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
	boolean result = false;
	for (Object o : c) {
	    boolean r = remove(o);
	    if (r) {
		decrementSize();
		result = true;
	    }
	}
	return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	boolean result = false;
	int newSize = 0;
	for (int i = 0; i < lists.length; i++) {
	    result = result || lists[i].retainAll(c);
	    newSize += lists[i].size();
	}
	setSize(newSize);
	return result;
    }

    @Override
    public void clear() {
	for (int i = 0; i < lists.length; i++)
	    lists[i].clear();
	tolist.clear();
	resetSize();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
	if (lists[0].addAll(index, c)) {
	    incrementSize(c.size());
	    return true;
	}
	return false;
    }

    @Override
    public T get(int index) {
	return lists[0].get(index);
    }

    /**
     * Gets the element of the given list index at the given index.
     * @param index The index of the element in the list.
     * @param listIndex The index of the list.
     * @return The element if exists, null otherwise.
     */
    public T get(int index, int listIndex) {
	return lists[listIndex].get(index);
    }

    @Override
    public T set(int index, T element) {
	return lists[0].set(index, element);
    }

    /**
     * Sets the element at the given index in the given list.
     * @param index The index of the element.
     * @param element The element.
     * @param listIndex The index of the list.
     * @return <tt>true</tt> if the list was modified.
     */
    public T set(int index, T element, int listIndex) {
	return lists[listIndex].set(index, element);
    }

    @Override
    public void add(int index, T element) {
	lists[0].add(index, element);
	incrementSize();
    }

    @Override
    public T remove(int index) {
	T t = lists[0].remove(index);
	decrementSize();
	return t;
    }

    /**
     * Removes the element of the given list index at the given index.
     * @param index The index of the element in the list.
     * @param listIndex The index of the list.
     * @return The element if it was removed, null otherwise.
     */
    public T remove(int index, int listIndex) {
	T t = lists[listIndex].remove(index);
	decrementSize();
	return t;
    }

    @Override
    public int indexOf(Object o) {
	return lists[0].indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
	return lists[0].lastIndexOf(o);
    }

    @Override
    /** Not implemented **/
    public ListIterator<T> listIterator() {
	return null;
    }

    @Override
    /** Not implemented **/
    public ListIterator<T> listIterator(int index) {
	return null;
    }

    @Override
    /** Not implemented **/
    public List<T> subList(int fromIndex, int toIndex) {
	return null;
    }

    private void incrementSize(int incr) {
	synchronized (size) {
	    size += incr;
	}
    }

    private void incrementSize() {
	synchronized (size) {
	    size++;
	}
    }

    private void decrementSize(int decr) {
	synchronized (size) {
	    size -= decr;
	}
    }

    private void decrementSize() {
	synchronized (size) {
	    size--;
	}
    }

    private void setSize(int newsize) {
	synchronized (size) {
	    size = newsize;
	}
    }

    private void resetSize() {
	synchronized (size) {
	    size = 0;
	}
    }

    private class MultilistIterator<T> implements Iterator<T> {
	/** The index of the list **/
	int listIndex;
	/** The index of the current element in the list **/
	int index;

	public MultilistIterator() {
	    super();
	    listIndex = 0;
	    index = 0;
	}

	@Override
	public boolean hasNext() {
	    return (index < lists[listIndex].size() - 1) || (listIndex < lists.length - 1 && !emptyFrom(listIndex + 1));
	}

	/** Are the lists from index li onwards empty? **/
	private boolean emptyFrom(int li) {
	    for (int i = li; i < lists.length; i++) {
		if (!lists[i].isEmpty())
		    return false;
	    }
	    return true;
	}

	@Override
	public T next() {
	    if (index == lists[listIndex].size() - 1) {
		index = 0;
		listIndex++;
	    } else {
		index++;
	    }
	    return (T) lists[listIndex].get(index);
	}

	@Override
	public void remove() {
	    lists[listIndex].remove(index);
	    if (index == lists[listIndex].size()) {
		index--;
	    }

	}

    }

}
