package com.hsn.epic4j.core.bean;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Item {
    //基础游戏
    public static String BASE_GAME = "BASE_GAME";
    public static String DLC = "DLC";


    private String title;
    private String offerId;
    private String namespace;
    private String offerType;
    private String productSlug;
    private String urlSlug;

    private CatalogNs catalogNs;
    private List<PageSlug> offerMappings;

    public boolean isDLC() {
        return DLC.equals(offerType);
    }
}
