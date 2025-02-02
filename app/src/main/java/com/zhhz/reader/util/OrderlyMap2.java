package com.zhhz.reader.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mozilla.javascript.ConsString;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class OrderlyMap2 implements Map<String, String>, Cloneable, Serializable {

    /**
     * 自定义 Map，允许键重复，并保持插入顺序。
     */

    private static final int DEFAULT_CAPACITY = 4096; // 默认容量
    private static final int CAPACITY_INCREMENT = 1024; // 扩容增量

    private String[] keys; // 存储键的数组
    private String[] values; // 存储值的数组

    // 表大小
    private int size = 0; // 当前键值对数量

    // 键到索引的映射，用于高效的 get() 操作
    private Map<String, List<Integer>> keyIndexMap = new HashMap<>();

    private transient Set<String> keySet; // 键集合
    private transient Collection<String> valueCollection; // 值集合
    private transient Set<Entry<String, String>> entrySet; // 键值对集合

    private transient Node[] table; // 用于迭代的表

    public OrderlyMap2() {
        keys = new String[4096];
        values = new String[4096];
    }

    /**
     * 获取与给定键关联的值。
     *
     * @param key 键
     * @return 值，如果未找到则返回 null。
     */
    public String get(String key) {
        return get(key, -1); // 默认获取最后一个值
    }

    /**
     * 获取与给定键关联的值，并指定索引。
     *
     * @param key   键
     * @param index 索引，-1 表示获取最后一个值。
     * @return 值，如果未找到则返回 null。
     */
    public String get(String key, int index) {
        if (key == null) {
            return null; // 空键直接返回 null
        }

        List<Integer> indices = keyIndexMap.get(key); // 获取键对应的索引列表
        if (indices == null) {
            return null; // 没有找到索引列表
        }

        if (index == -1) {
            // 返回最后一个值
            return values[indices.get(indices.size() - 1)];
        } else if (index >= indices.size()) {
            return null; // 索引超出范围
        } else {
            return values[indices.get(index)]; // 返回指定索引的值
        }
    }

    public String put(Object key, ConsString value) {
        return put(String.valueOf(key), String.valueOf(value));
    }

    public String put(ConsString key, ConsString value) {
        return put(String.valueOf(key), String.valueOf(value));
    }

    /**
     * 将键值对放入 Map 中。
     *
     * @param key   键
     * @param value 值
     * @return 如果键已存在，则返回旧值，否则返回 null。
     */
    @Nullable
    @Override
    public String put(String key, String value) {
        if (size >= keys.length) {
            resize(); // 扩容
        }
        // 更新 keyIndexMap
        List<Integer> indices = keyIndexMap.get(key); // 获取键对应的索引列表
        if (indices == null) {
            indices = new ArrayList<>(); // 创建新的索引列表
        }
        indices.add(size); // 添加新的索引
        keyIndexMap.put(key, indices); // 更新索引列表

        keys[size] = key; // 存储键
        values[size] = value; // 存储值
        size++; // 增加键值对数量
        return null; // 返回 null，表示没有旧值
    }

    /**
     * 调整内部数组大小。
     */
    private void resize() {
        int newCapacity = keys.length + CAPACITY_INCREMENT; // 新容量
        String[] newKeys = new String[newCapacity]; // 新的键数组
        String[] newValues = new String[newCapacity]; // 新的值数组
        System.arraycopy(keys, 0, newKeys, 0, size); // 复制键
        System.arraycopy(values, 0, newValues, 0, size); // 复制值
        keys = newKeys; // 更新键数组
        values = newValues; // 更新值数组
    }

    public boolean isNotEmpty() {
        return size != 0; // 判断是否为空
    }

    public void set(@NonNull String key, @NonNull String value) {
        put(key, value); // 设置键值对
    }

    /**
     * 查找指定键的首次出现的索引。
     *
     * @param o 要查找的键
     * @return 键的首次出现的索引，如果未找到则返回 -1。
     */
    public int indexOfKey(String o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (keys[i] == null) {
                    return i; // 找到 null 键
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(keys[i])) {
                    return i; // 找到匹配的键
                }
            }
        }
        return -1; // 未找到
    }

    /**
     * 查找指定键的最后一次出现的索引。
     *
     * @param o 要查找的键
     * @return 键的最后一次出现的索引，如果未找到则返回 -1。
     */
    public int lastIndexOfKey(String o) {
        if (o == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (keys[i] == null) {
                    return i; // 找到 null 键
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (o.equals(keys[i])) {
                    return i; // 找到匹配的键
                }
            }
        }
        return -1; // 未找到
    }

    /**
     * 将另一个 OrderlyMap2 中的所有键值对放入此 Map 中。
     *
     * @param it 要复制的 OrderlyMap2
     */
    public void putAll(@NonNull OrderlyMap2 it) {
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
                        if (index + j >= size) {
                            size++;
                        }
                    }

                } else {
                    for (int i1 = 0; i1 < it.size; i1++) {
                        put(it.keys[i1], it.values[i1]);
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

    /**
     * 将另一个 Map 中的所有键值对放入此 Map 中。
     *
     * @param m 要复制的 Map
     */
    @Override
    public void putAll(@NonNull Map<? extends String, ? extends String> m) {
        if (m instanceof OrderlyMap2) {
            putAll((OrderlyMap2) m);
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

    /**
     * 清空 Map。
     */
    public void clear() {
        size = 0;
        keyIndexMap.clear(); // 清空索引映射
        for (int i = 0; i < keys.length; i++) {
            keys[i] = null;
            values[i] = null;
        }
    }

    /**
     * 返回 Map 的大小。
     *
     * @return Map 的大小
     */
    public int size() {
        return size;
    }

    /**
     * 判断 Map 是否包含指定的键。
     *
     * @param key 要查找的键
     * @return 如果包含则返回 true，否则返回 false。
     */
    public boolean containsKey(@NonNull String key) {
        return keyIndexMap.containsKey(key);
    }

    /**
     * 判断 Map 是否包含指定的键。
     *
     * @param key 要查找的键
     * @return 如果包含则返回 true，否则返回 false。
     */
    private int containsKeys(@NonNull String key) {
        int ii = 0;
        for (int i = 0; i < size; i++) {
            if (keys[i] != null && keys[i].equals(key)) {
                ii++;
            }
        }
        return ii;
    }

    /**
     * 判断 Map 是否包含指定的值。
     *
     * @param value 要查找的值
     * @return 如果包含则返回 true，否则返回 false。
     */
    public boolean containsValue(@NonNull String value) {
        for (int i = 0; i < size; i++) {
            if (values[i] != null && values[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断 Map 是否包含指定的值。
     *
     * @param value 要查找的值
     * @return 如果包含则返回 true，否则返回 false。
     */
    private int containsValues(@NonNull String value) {
        int ii = 0;
        for (int is = 0; is < size; is++) {
            if (values[is] != null && values[is].equals(value)) {
                ii++;
            }
        }
        return ii;
    }

    /**
     * 返回键的集合。
     *
     * @return 键的集合
     */
    @NonNull
    public Set<String> keySet() {
        Set<String> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    /**
     * 返回值的集合。
     *
     * @return 值的集合
     */
    @NonNull
    public Collection<String> values() {
        Collection<String> vs = valueCollection;
        if (vs == null) {
            vs = new Values();
            valueCollection = vs;
        }
        return vs;
    }

    /**
     * 返回键值对的集合。
     *
     * @return 键值对的集合
     */
    @NonNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    /**
     * 键集合的内部类。
     */
    final class KeySet extends AbstractSet<String> {
        public int size() {
            return size;
        }

        public void clear() {
            OrderlyMap2.this.clear();
        }

        @NonNull
        public Iterator<String> iterator() {
            return new KeyIterator();
        }
    }

    /**
     * 键迭代器。
     */
    class KeyIterator implements Iterator<String> {
        String next;        // 下一个要返回的键
        String current;     // 当前键
        int index;             // 当前索引

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
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            current = keys[index++];
            if (index < size) {
                next = keys[index];
            } else {
                next = null;
            }
            return current;
        }

    }


    /**
     * 值集合的内部类。
     */
    final class Values extends AbstractCollection<String> {
        public int size() {
            return size;
        }

        public void clear() {
            OrderlyMap2.this.clear();
        }

        @NonNull
        public Iterator<String> iterator() {
            return new ValueIterator();
        }
    }

    /**
     * 值迭代器。
     */
    class ValueIterator implements Iterator<String> {
        String next;        // 下一个要返回的值
        String current;     // 当前值
        int index;             // 当前索引

        ValueIterator() {
            current = next = null;
            index = 0;
            if (index < size) {
                next = values[index];
            }
        }


        public final boolean hasNext() {
            return next != null;
        }

        public String next() {if (!hasNext()) {
            throw new NoSuchElementException();
        }
            current = values[index++];
            if (index < size) {
                next = values[index];
            } else {
                next = null;
            }
            return current;
        }

    }

    /**
     * 键值对集合的内部类。
     */
    final class EntrySet extends AbstractSet<Entry<String, String>> {
        public int size() {
            return size;
        }

        public void clear() {
            OrderlyMap2.this.clear();
        }

        @NonNull
        public Iterator<Entry<String, String>> iterator() {
            return new EntryIterator();
        }
    }

    /**
     * 键值对迭代器。
     */
    class EntryIterator implements Iterator<Entry<String, String>> {
        Node next;        // 下一个要返回的键值对
        Node current;     // 当前键值对
        int index;             // 当前索引

        EntryIterator() {
            if (table == null || table.length != size) {
                table = new Node[OrderlyMap2.this.size];
                for (int i = size - 1; i >= 0; i--) {
                    table[i] = new Node(i, size - 1 == i ? null : table[i + 1]);
                }
            }
            index = 0;
            if (table.length > 0) next = table[index];
        }

        public final boolean hasNext() {
            return next != null;
        }

        public Entry<String, String> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            current = table[index++];
            next = current.next;
            return current;
        }

    }

    /**
     * 键值对节点。
     */
    class Node implements Entry<String, String> {
        int index;
        Node next;

        Node(int i, Node next) {
            this.index = i;
            this.next = next;
        }

        public final String getKey() {
            return OrderlyMap2.this.keys[index];
        }

        public final String getValue() {
            return OrderlyMap2.this.values[index];
        }

        public final int hashCode() {
            return Objects.hashCode(OrderlyMap2.this.keys[index]) ^ Objects.hashCode(OrderlyMap2.this.values[index]);
        }

        public final String setValue(String newValue) {
            String oldValue = OrderlyMap2.this.values[index];
            OrderlyMap2.this.values[index] = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Entry<?, ?> e = (Entry<?, ?>) o;
                return Objects.equals(OrderlyMap2.this.keys[index], e.getKey()) &&
                        Objects.equals(OrderlyMap2.this.values[index], e.getValue());
            }
            return false;
        }
    }

    /**
     * 判断 Map 是否为空。
     *
     * @return 如果为空则返回 true，否则返回 false。
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 判断 Map 是否包含指定的键。
     *
     * @param key 要查找的键
     * @return 如果包含则返回 true，否则返回 false。
     */
    @Override
    public boolean containsKey(Object key) {
        if (key == null) return false;
        return containsKey(key.toString());
    }

    /**
     * 判断 Map 是否包含指定的值。
     *
     * @param value 要查找的值
     * @return 如果包含则返回 true，否则返回 false。
     */
    @Override
    public boolean containsValue(Object value) {
        if (value == null) return false;
        return containsValue(value.toString());
    }

    /**
     * 获取指定键的值。
     *
     * @param key 要查找的键
     * @return 如果找到则返回值，否则返回 null。
     */
    @Override
    public String get(Object key) {
        if (key == null) return null;
        return get(key.toString());
    }



    /**
     * 克隆当前 Map。
     *
     * @return 克隆后的 Map
     */
    @NonNull
    @Override
    public OrderlyMap2 clone() {
        try {
            OrderlyMap2 clone = (OrderlyMap2) super.clone();
            clone.keys = keys.clone();
            clone.values = values.clone();
            clone.keyIndexMap = new HashMap<>(keyIndexMap);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * 返回 Map 的字符串表示形式。
     *
     * @return Map 的字符串表示形式
     */
    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < size; i++) {
            sb.append("\"").append(keys[i]).append("\"=\"").append(values[i]).append("\",");
        }
        if (sb.length() > 1) {
            sb.delete(sb.length() - 1, sb.length());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderlyMap2)) return false;
        OrderlyMap2 that = (OrderlyMap2) o;
        return Arrays.equals(keys, that.keys) &&
                Arrays.equals(values, that.values) &&
                Objects.equals(keyIndexMap, that.keyIndexMap) &&
                size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(keys), Arrays.hashCode(values), keyIndexMap, size);
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
                newArr_key[j] = keys[i];
                newArr_value[j] = values[i];
                j++;
            } else {
                v = values[i];
            }
        }

        System.arraycopy(newArr_key, 0, keys, 0, size - 1);
        System.arraycopy(newArr_value, 0, values, 0, size - 1);
        keyIndexMap.remove(k);
        size--;
        return v;
    }


}