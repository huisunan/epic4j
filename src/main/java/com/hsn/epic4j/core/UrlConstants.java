package com.hsn.epic4j.core;

public interface UrlConstants {
    /**
     * 是否登录判断url
     */
    String checkLoginUrl = "https://www.epicgames.com/account/v2/ajaxCheckLogin";
    /**
     * 获取用户信息url
     */
    String userInfoUrl = "https://www.epicgames.com/account/v2/personal/ajaxGet?sessionInvalidated=true";
    /**
     * 免费游戏url
     */
    String freeGameUrl = "https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions?locale={}&country={}&allowCountries={}";
    /**
     * 商店项url
     */
    String storeUrl = "https://store.epicgames.com/en-US/p/{}";
    /**
     * epic主页
     */
    String epicUrl = "https://www.epicgames.com/store/en-US/";
}
