package com.arash.basemodule.tools.collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Data is loaded through segments, so you can manipulate each segment separately
 *
 * @param <T>
 */
public class SegmentedList<T> {
    // types --------------------------------------------------------
    public interface Collector<T> {
        List<T> collect(int start, int end);
    }

    public interface Emitter<T> {
        void setValue(int index, T value);
    }

    private class Data {
        long age;
        private final List<T> lst;

        public Data(List<T> lst) {
            this.lst = lst;
        }

        public T get(int index) {
            return lst.get(index);
        }

        public void set(int index, T value) {
            lst.set(index, value);
            if (emitter != null)
                emitter.setValue(index, value);
        }
    }

    // fields -------------------------------------------------------
    private final int segmentSize;
    private int totalCount = -1;
    private final HashMap<Integer, Data> dataCollection = new HashMap<>();
    private final Collector<T> collector;
    private Emitter<T> emitter;
    private int dismissAge;
    private long currentAge;
    private int workingSegment = -1;

    // private methods ----------------------------------------------

    /**
     * fill a segment
     *
     * @param upperIndex
     */
    private void fill(int upperIndex) {
        int start = upperIndex * segmentSize;
        dataCollection.put(upperIndex, new Data(collector.collect(start, start + segmentSize)));
    }

    /**
     * age of each segment
     *
     * @return
     */
    private long getCurrentAge() {
        return ++currentAge;
    }

    /**
     * check if old segment must be removed
     *
     * @param segmentId
     */
    private void checkWorkingSegment(int segmentId) {
        if (dismissAge > 0 && workingSegment != segmentId) {
            workingSegment = segmentId;
            Data data = dataCollection.get(segmentId);
            data.age = getCurrentAge();
            Iterator<Integer> itr = dataCollection.keySet().iterator();
            long minAge = data.age - dismissAge;
            while (itr.hasNext()) {
                Integer i = itr.next();
                if (dataCollection.get(i).age < minAge)
                    dataCollection.remove(i);
            }
        }
    }

    // public methods -----------------------------------------------
    public SegmentedList(Collector<T> collector) {
        this(50, collector);
    }

    public SegmentedList(int segmentSize, Collector<T> collector) {
        assert segmentSize > 0;
        assert collector != null;
        this.segmentSize = segmentSize;
        this.collector = collector;
    }

    /**
     * set aging > 0 to remove old segments by their age. For example, if your current age is 10 and aging is set 3, segments older than 7 will be removed<br>
     * age changes when you work on some other segment not the current segment
     *
     * @param dismissAge remove segments older than [currentAge - dismissAge]
     */
    public void setAging(int dismissAge) {
        this.dismissAge = dismissAge;
    }

    /**
     * @return the size of whole list
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * @param totalCount the size of whole list
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * @param index index of of item in the whole list
     * @return catch item by its index
     */
    public T get(int index) {
        int segmentId = index / segmentSize;
        if (!dataCollection.containsKey(segmentId)) {
            fill(segmentId);
        }
        checkWorkingSegment(segmentId);
        return dataCollection.get(segmentId).get(index % segmentSize);
    }

    /**
     * this method updates an item by its index. Please notice that the update is still in RAM and the segment reloads, the update will be lost.
     * To maintain new data, set emitter, then you can store it is the data source
     *
     * @param index
     * @param value
     */
    public void set(int index, T value) {
        int upperIndex = index / segmentSize;
        if (dataCollection.containsKey(upperIndex)) {
            dataCollection.get(upperIndex).set(index % segmentSize, value);
        }
    }

    /**
     * To propagate changes to the data source on call set(index, value)
     *
     * @param emitter
     */
    public void setEmitter(Emitter<T> emitter) {
        this.emitter = emitter;
    }
}
