package com.ruoyi.common.loadbalance.core;

import java.util.*;

public class CollectionUtils {
    private static final Integer INTEGER_ONE = 1;

    public CollectionUtils() {
    }

    public static Collection subtract(Collection a, Collection b) {
        ArrayList list = new ArrayList(a);
        Iterator it = b.iterator();

        while(it.hasNext()) {
            list.remove(it.next());
        }

        return list;
    }

    public static Map getCardinalityMap(Collection coll) {
        Map count = new HashMap(coll.size());
        Iterator it = coll.iterator();

        while(it.hasNext()) {
            Object obj = it.next();
            Integer c = (Integer)((Integer)count.get(obj));
            if (c == null) {
                count.put(obj, INTEGER_ONE);
            } else {
                count.put(obj, c + 1);
            }
        }

        return count;
    }

    public static boolean isEqualCollection(Collection a, Collection b) {
        if (a.size() != b.size()) {
            return false;
        } else {
            Map mapa = getCardinalityMap(a);
            Map mapb = getCardinalityMap(b);
            if (mapa.size() != mapb.size()) {
                return false;
            } else {
                Iterator it = mapa.keySet().iterator();

                Object obj;
                do {
                    if (!it.hasNext()) {
                        return true;
                    }

                    obj = it.next();
                } while(getFreq(obj, mapa) == getFreq(obj, mapb));

                return false;
            }
        }
    }

    public static boolean isEmpty(Collection coll) {
        return coll == null || coll.isEmpty();
    }

    private static int getFreq(Object obj, Map freqMap) {
        Integer count = (Integer)freqMap.get(obj);
        return count != null ? count : 0;
    }
}
