package com.hsn.epic4j.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item {
    private String title;
    private String offerId;
    private String namespace;
    private String offerType;
    private String productSlug;
}
