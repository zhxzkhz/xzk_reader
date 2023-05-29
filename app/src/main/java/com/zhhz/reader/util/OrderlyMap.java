package com.zhhz.reader.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mozilla.javascript.ConsString;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class OrderlyMap implements Map<String, String>, Cloneable, Serializable {

    /**
     * 一个可以添加重复key的有序Map
     */

    private String[] keys = new String[4096];


    private String[] values = new String[4096];


    // 表大小
    transient int size = 0;

    transient Set<String> keySet;
    transient Collection<String> _values;
    transient Set<Entry<String, String>> entrySet;

    transient Node[] table;

    /**
     * @param key key值
     * @return value值
     */
    public String get(String key) {
        return get(key, -1);
    }

    public String get(String key, int index) {
        String[] es = keys;
        String[] temp = new String[es.length];
        int i;
        int k = 0;
        if (key == null) {
            for (i = 0; i < es.length; ++i) {
                if (es[i] == null) {
                    temp[k++] = values[i];
                }
            }
        } else {
            for (i = 0; i < es.length; ++i) {
                if (key.equals(es[i])) {
                    temp[k++] = values[i];
                }
            }
        }
        if (index == -1 || index >= k) {
            return temp[k - 1];
        } else if (k != 0) {
            return temp[index];
        }
        return null;
    }


    public String put(Object key, ConsString value) {
        return put(String.valueOf(key),String.valueOf(value));
    }

    public String put(ConsString key, ConsString value) {
        return put(String.valueOf(key),String.valueOf(value));
    }

    @Nullable
    @Override
    public String put(String key, String value) {
        if (size >= keys.length) {
            String[] keys_1 = new String[keys.length + 1024];
            String[] value_1 = new String[values.length + 1024];
            System.arraycopy(keys, 0, keys_1, 0, keys.length);
            System.arraycopy(values, 0, value_1, 0, values.length);
            keys = keys_1;
            values = value_1;
        }
        keys[size] = key;
        values[size] = value;
        size++;
        return null;
    }

    public boolean isNotEmpty() {
        return size != 0;
    }

    public void set(@NonNull String key, @NonNull String value) {
        put(key, value);
    }

    public int indexOfKey(String o) {
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (keys[i] == null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(keys[i]))
                    return i;
        }
        return -1;
    }

    public int lastIndexOfKey(String o) {
        if (o == null) {
            for (int i = size - 1; i >= 0; i--)
                if (keys[i] == null)
                    return i;
        } else {
            for (int i = size - 1; i >= 0; i--)
                if (o.equals(keys[i]))
                    return i;
        }
        return -1;
    }

    public void putAll(@NonNull OrderlyMap it) {
        int i = 0;
        String s = it.keys[i];
        int index = lastIndexOfKey(s);

        if (index == -1 || index + it.size * 2 < size) {
            for (int i1 = 0; i1 < it.size; i1++) {
                put(it.keys[i1], it.values[i1]);
            }
        } else {

            if (index + it.size >= size) {
                if (values[index].equals(it.values[i])) {
                    for (int j = 0; j < it.size; j++) {
                        keys[index + j] = it.keys[j];
                        values[index + j] = it.values[j];
                        if (index + j >= size){
                            size++;
                        }
                    }

                } else {
                    for (int i1 = 0; i1 < it.size; i1++) {
                        put(it.keys[i1],it.values[i1]);
                    }
                }
                return;
            }

            for (int i1 = 0; i1 < it.size; i1++) {
                int b = containsKeys(it.keys[i1]);
                if (b == 0 || b > 1) {
                    put(it.keys[i1], it.values[i1]);
                } else {
                    int x = indexOfKey(it.keys[i1]);
                    values[x] = it.values[i1];
                }
            }

        }
    }


    @Override
    public void putAll(@NonNull Map<? extends String, ? extends String> m) {
        if (m instanceof OrderlyMap) {
            putAll((OrderlyMap)m);
            return;
        }
        if (m instanceof LinkedHashMap) {
            if (m.size() >= size) {
                int index = indexOfKey(m.entrySet().iterator().next().getKey());
                if (index == 0) {
                    clear();
                }
                for (Entry<? extends String, ? extends String> entry : m.entrySet()) {
                    put(entry.getKey(), entry.getValue());
                }
                return;
            }
        }
        for (Entry<? extends String, ? extends String> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        size = 0;
        for (int i = 0; i < keys.length; i++) {
            keys[i] = null;
            values[i] = null;
        }
    }

    public int size() {
        return size;
    }

    public boolean containsKey(@NonNull String key) {
        for (int i = 0; i < size; i++) {
            if (keys[i] !=null && keys[i].equals(key)) {
                return true;
            }
        }
        return false;
    }

    private int containsKeys(@NonNull String key) {
        int ii = 0;
        for (int i = 0; i < size; i++) {
            if (keys[i] !=null && keys[i].equals(key)) {
                ii++;
            }
        }
        return ii;
    }

    public boolean containsValue(@NonNull String value) {
        for (int i = 0; i < size; i++) {
            if (values[i] !=null && values[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    private int containsValues(@NonNull String value) {
        int ii = 0;
        for (int i = 0; i < size; i++) {
            if (values[i] !=null && values[i].equals(value)) {
                ii++;
            }
        }
        return ii;
    }

    @NonNull
    public Set<String> keySet() {
        Set<String> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    @NonNull
    public Collection<String> values() {
        Collection<String> vs = _values;
        if (vs == null) {
            vs = new Values();
            _values = vs;
        }
        return vs;
    }


    @NonNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }


    final class KeySet extends AbstractSet<String> {
        public int size() {
            return size;
        }

        public void clear() {
            OrderlyMap.this.clear();
        }

        @NonNull
        public Iterator<String> iterator() {
            return new KeyIterator();
        }
    }

    class KeyIterator implements Iterator<String> {
        String next;        // next entry to return
        String current;     // current entry
        int index;             // current slot

        KeyIterator() {
            current = next = null;
            index = 0;
            if (index < size) {
                next = keys[index];
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        public String next() {
            current = keys[index++];
            if (index < size) {
                next = keys[index];
            } else {
                next = null;
            }
            return current;
        }

    }


    final class Values extends AbstractCollection<String> {
        public int size() {
            return size;
        }

        public void clear() {
            OrderlyMap.this.clear();
        }

        @NonNull
        public Iterator<String> iterator() {
            return new ValueIterator();
        }
    }

    class ValueIterator implements Iterator<String> {
        String next;        // next entry to return
        String current;     // current entry
        int index;             // current slot

        ValueIterator() {
            current = next = null;
            index = 0;
            if (index < size) {
                next = keys[index];
            }
        }


        public final boolean hasNext() {
            return next != null;
        }

        public String next() {
            current = keys[index++];
            if (index < size) {
                next = keys[index];
            } else {
                next = null;
            }
            return current;
        }

    }

    final class EntrySet extends AbstractSet<Entry<String, String>> {
        public int size() {
            return size;
        }

        public void clear() {
            OrderlyMap.this.clear();
        }

        @NonNull
        public Iterator<Entry<String, String>> iterator() {
            return new EntryIterator();
        }
    }

    class EntryIterator implements Iterator<Entry<String, String>> {
        Node next;        // next entry to return
        Node current;     // current entry
        int index;             // current slot

        EntryIterator() {
            if (table == null || table.length != size) {
                table = new Node[OrderlyMap.this.size];
                for (int i = size - 1; i >= 0; i--) {
                    table[i] = new Node(i, size - 1 == i ? null : table[i + 1]);
                }
            }
            index = 0;
            next = table[index];
        }

        public final boolean hasNext() {
            return next != null;
        }

        public Entry<String, String> next() {
            current = table[index++];
            next = current.next;
            return current;
        }

    }

     class Node implements Entry<String, String> {
        int index;
         Node next;

        Node(int i, Node next) {
            this.index = i;
            this.next = next;
        }

        public final String getKey() {
            return OrderlyMap.this.keys[index];
        }

        public final String getValue() {
            return OrderlyMap.this.values[index];
        }

        public final int hashCode() {
            return Objects.hashCode(OrderlyMap.this.keys[index]) ^ Objects.hashCode(OrderlyMap.this.values[index]);
        }

        public final String setValue(String newValue) {
            String oldValue = OrderlyMap.this.values[index];
            OrderlyMap.this.values[index] = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Entry<?, ?> e = (Entry<?, ?>) o;
                return Objects.equals(OrderlyMap.this.keys[index], e.getKey()) &&
                        Objects.equals(OrderlyMap.this.values[index], e.getValue());
            }
            return false;
        }
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) return false;
        return containsKey(key.toString());
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) return false;
        return containsValue(value.toString());
    }

    @Override
    public String get(Object key) {
        if (key == null) return null;
        return get(key.toString());
    }

    @Override
    public String remove(Object key) {
        if (key == null) return null;
        String k = key.toString();
        String v = null;
        int index = lastIndexOfKey(k);
        if (index == -1) {
            return null;
        }
        String[] newArr_key = new String[size - 1];
        String[] newArr_value = new String[size - 1];
        for (int i = 0, j = 0; i < size; i++) {
            if (i != index) {
                newArr_key[j++] = keys[i];
                newArr_value[j++] = values[i];
            } else {
                v = values[i];
            }
        }

        System.arraycopy(newArr_key, 0, keys, 0, size - 1);
        System.arraycopy(newArr_value, 0, values, 0, size - 1);

        return v;
    }


    @NonNull
    @Override
    public OrderlyMap clone() {
        try {
            return (OrderlyMap) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < size; i++) {
            sb.append("\"").append(keys[i]).append("\"=\"").append(values[i]).append("\",");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append("}");
        return sb.toString();
    }

}
