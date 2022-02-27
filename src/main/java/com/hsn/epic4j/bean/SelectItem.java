package com.hsn.epic4j.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hsn
 * 2022/1/14
 * SelectItem
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectItem {
    private String selectors;
    private boolean exist = true;

    public SelectItem(String selectors) {
        this.selectors = selectors;
    }
}
