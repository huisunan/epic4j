package com.hsn.epic4j.core.bean;

import com.ruiyun.jvppeteer.core.page.Page;
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
    private SelectPredicate<Page, SelectItem> pagePredicate;
    private SelectCallBack callback;

    public SelectItem(String selectors, SelectCallBack callback) {
        this(selectors, (page, item) -> page.$(item.getSelectors()) != null, callback);
    }


    public interface SelectCallBack {
        boolean CONTINUE = false;
        boolean END = true;

        /**
         * @return 是否终端运行 true终端
         * @throws RuntimeException     RuntimeException
         * @throws InterruptedException InterruptedException
         */
        boolean run() throws RuntimeException, InterruptedException;
    }

    public interface SelectPredicate<P1, P2> {
        boolean test(P1 page, P2 selectItem);
    }
}
