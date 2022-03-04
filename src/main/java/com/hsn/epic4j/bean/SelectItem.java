package com.hsn.epic4j.bean;

import com.ruiyun.jvppeteer.core.page.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Predicate;

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
        boolean CONTINUE = true;
        boolean END = false;

        /**
         * @return 是否继续运行 true继续运行
         * @throws RuntimeException     RuntimeException
         * @throws InterruptedException InterruptedException
         */
        boolean run() throws RuntimeException, InterruptedException;
    }

    public interface SelectPredicate<P1, P2> {
        boolean test(P1 page, P2 selectItem);
    }
}
