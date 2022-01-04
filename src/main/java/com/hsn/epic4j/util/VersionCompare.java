package com.hsn.epic4j.util;

import java.util.Comparator;

/**
 * @author hsn
 * 2022/1/4
 * VersionCompare
 */
public class VersionCompare implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        String[] split1 = o1.split("\\.");
        String[] split2 = o2.split("\\.");
        for (int i = 0; i < Math.max(split1.length, split2.length); i++) {
            //循环比较值
            Integer i1 = i < split1.length ? Integer.parseInt(split1[i]) : 0;
            Integer i2 = i < split2.length ? Integer.parseInt(split2[i]) : 0;
            int res = i1.compareTo(i2);
            if (res != 0) {
                return res;
            }
        }
        return 0;
    }
}
