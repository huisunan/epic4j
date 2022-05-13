package com.hsn.epic4j.core.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hsn
 * 2022/1/6
 * UserInfo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    /**
     * 邮箱
     */
    private String email;
    /**
     * 密码
     */
    private String password;
}
