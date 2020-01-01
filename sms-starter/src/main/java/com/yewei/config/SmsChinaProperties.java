package com.yewei.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.yewei.china.sms")
@Data
public class SmsChinaProperties {
    private boolean enabled;
    private String url;
    private String account;
    private String password;
    private String smsCode;
}
