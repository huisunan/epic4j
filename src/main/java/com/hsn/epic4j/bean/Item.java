package com.hsn.epic4j.bean;

import lombok.Builder;
import lombok.Data;

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

    public boolean isDLC() {
        return DLC.equals(offerType);
    }
}
