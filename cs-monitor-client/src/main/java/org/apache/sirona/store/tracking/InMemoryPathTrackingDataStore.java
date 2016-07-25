package org.apache.sirona.store.tracking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.sirona.store.tracking.AbstractPathTrackingDataStore;
import org.apache.sirona.store.tracking.CollectorPathTrackingDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.tracking.PathCallInformation;
import org.apache.sirona.tracking.PathTrackingEntry;
import org.apache.sirona.tracking.PathTrackingEntryComparator;
import org.apache.sirona.util.SerializeUtils;
import org.apache.sirona.util.UnsafeUtils;
/**
 * Created by kong on 2016/1/24.
 */
public class InMemoryPathTrackingDataStore extends AbstractPathTrackingDataStore implements PathTrackingDataStore, CollectorPathTrackingDataStore {
    private ConcurrentMap<String, List<InMemoryPathTrackingDataStore.Pointer>> pathTrackingEntries = new ConcurrentHashMap(50);

    public InMemoryPathTrackingDataStore() {
    }

    public void store(PathTrackingEntry pathTrackingEntry) {
        this.store((Collection)Collections.singletonList(pathTrackingEntry));
    }

    public void store(Collection<PathTrackingEntry> pathTrackingEntries) {
        if(pathTrackingEntries != null) {
            HashMap entries = new HashMap();
            Iterator i$ = pathTrackingEntries.iterator();

            Object entriesList;
            while(i$.hasNext()) {
                PathTrackingEntry entry = (PathTrackingEntry)i$.next();
                entriesList = (Set)entries.get(entry.getTrackingId());
                if(entriesList == null) {
                    entriesList = new HashSet();
                }

                ((Set)entriesList).add(entry);
                entries.put(entry.getTrackingId(), entriesList);
            }

            i$ = entries.entrySet().iterator();

            while(i$.hasNext()) {
                Entry entry1 = (Entry)i$.next();
                entriesList = (List)this.pathTrackingEntries.get(entry1.getKey());
                if(entriesList == null) {
                    entriesList = new CopyOnWriteArrayList();
                }

                ((List)entriesList).addAll(this.serialize((Collection)entry1.getValue()));
                this.pathTrackingEntries.put(entry1.getKey(), entriesList);
            }

        }
    }

    public Collection<PathTrackingEntry> retrieve(String trackingId) {
        List buffers = (List)this.pathTrackingEntries.get(trackingId);
        return this.deserialize(buffers);
    }

    public Collection<PathCallInformation> retrieveTrackingIds(Date startTime, Date endTime) {
        TreeSet trackingIds = new TreeSet(PathCallInformation.COMPARATOR);
        Iterator i$ = this.pathTrackingEntries.values().iterator();

        while(i$.hasNext()) {
            List buffers = (List)i$.next();
            if(!this.pathTrackingEntries.isEmpty()) {
                PathTrackingEntry first = (PathTrackingEntry)SerializeUtils.deserialize(this.readBytes((InMemoryPathTrackingDataStore.Pointer)buffers.iterator().next()), PathTrackingEntry.class);
                if(first.getStartTime() / 1000000L > startTime.getTime() && first.getStartTime() / 1000000L < endTime.getTime()) {
                    trackingIds.add(new PathCallInformation(first.getTrackingId(), new Date(startTime.getTime() / 1000000L)));
                }
            }
        }

        return trackingIds;
    }

    private Collection<PathTrackingEntry> deserialize(List<InMemoryPathTrackingDataStore.Pointer> buffers) {
        ArrayList entries = new ArrayList(buffers.size());
        Iterator i$ = buffers.iterator();

        while(i$.hasNext()) {
            InMemoryPathTrackingDataStore.Pointer pointer = (InMemoryPathTrackingDataStore.Pointer)i$.next();
            byte[] bytes = this.readBytes(pointer);
            PathTrackingEntry entry = (PathTrackingEntry)SerializeUtils.deserialize(bytes, PathTrackingEntry.class);
            if(entry != null) {
                entries.add(entry);
            }
        }

        return entries;
    }

    public byte[] readBytes(InMemoryPathTrackingDataStore.Pointer pointer) {
        byte[] bytes = new byte[pointer.size];
        int length = pointer.size;
        long offset = pointer.offheapPointer;

        for(int pos = 0; pos < length; ++pos) {
            bytes[pos] = UnsafeUtils.getUnsafe().getByte((long)pos + offset);
        }

        return bytes;
    }

    private List<InMemoryPathTrackingDataStore.Pointer> serialize(Collection<PathTrackingEntry> entries) {
        ArrayList buffers = new ArrayList(entries.size());
        Iterator i$ = entries.iterator();

        while(true) {
            byte[] bytes;
            do {
                if(!i$.hasNext()) {
                    return buffers;
                }

                PathTrackingEntry entry = (PathTrackingEntry)i$.next();
                bytes = SerializeUtils.serialize(entry);
            } while(bytes == null);

            long offheapPointer = UnsafeUtils.getUnsafe().allocateMemory((long)bytes.length);
            InMemoryPathTrackingDataStore.Pointer pointer = new InMemoryPathTrackingDataStore.Pointer();
            pointer.offheapPointer = offheapPointer;
            pointer.size = bytes.length;
            int i = 0;

            for(int size = bytes.length; i < size; ++i) {
                UnsafeUtils.getUnsafe().putByte(offheapPointer + (long)i, bytes[i]);
            }

            buffers.add(pointer);
        }
    }

    public void clearEntries() {
        ArrayList entriesToRemove = new ArrayList();
        Iterator i$ = this.pathTrackingEntries.entrySet().iterator();

        while(i$.hasNext()) {
            Entry key = (Entry)i$.next();
            boolean allFree = true;
            Iterator i$1 = ((List)key.getValue()).iterator();

            while(i$1.hasNext()) {
                InMemoryPathTrackingDataStore.Pointer pointer = (InMemoryPathTrackingDataStore.Pointer)i$1.next();
                if(!pointer.isFree()) {
                    allFree = false;
                }
            }

            if(allFree) {
                entriesToRemove.add(key.getKey());
            }
        }

        i$ = entriesToRemove.iterator();

        while(i$.hasNext()) {
            String key1 = (String)i$.next();
            this.pathTrackingEntries.remove(key1);
        }

    }

    protected Map<String, Set<PathTrackingEntry>> getPathTrackingEntries() {
        HashMap entries = new HashMap(this.pathTrackingEntries.size());
        Iterator i$ = this.pathTrackingEntries.entrySet().iterator();

        while(i$.hasNext()) {
            Entry entry = (Entry)i$.next();
            TreeSet pathTrackingEntries = new TreeSet(PathTrackingEntryComparator.INSTANCE);
            pathTrackingEntries.addAll(this.deserialize((List)entry.getValue()));
            entries.put(entry.getKey(), pathTrackingEntries);
        }

        return entries;
    }

    protected Map<String, List<InMemoryPathTrackingDataStore.Pointer>> getPointers() {
        return this.pathTrackingEntries;
    }

    public static class Pointer {
        int size;
        long offheapPointer;
        boolean free;

        public Pointer() {
        }

        public int getSize() {
            return this.size;
        }

        public long getOffheapPointer() {
            return this.offheapPointer;
        }

        public void freeMemory() {
            if(!this.free) {
                UnsafeUtils.getUnsafe().freeMemory(this.offheapPointer);
                this.free = true;
            }

        }

        public boolean isFree() {
            return this.free;
        }
    }
}