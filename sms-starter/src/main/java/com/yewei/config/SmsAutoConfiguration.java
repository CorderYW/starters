package com.yewei.config;

import com.yewei.service.ChinaSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SmsChinaProperties.class)
@ConditionalOnProperty(name = "com.yewei.china.sms.enabled", havingValue = "true")
public class SmsAutoConfiguration {
    @Autowired
    private SmsChinaProperties smsChinaProperties;

    /**
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(ChinaSmsService.class)
    public ChinaSmsService ChinaSmsService() {
        ChinaSmsService chinaSmsService = new ChinaSmsService();
        chinaSmsService.setAccount(smsChinaProperties.getAccount());
        chinaSmsService.setPassword(smsChinaProperties.getPassword());
        chinaSmsService.setSmsCode(smsChinaProperties.getSmsCode());
        chinaSmsService.setUrl(smsChinaProperties.getUrl());
        return chinaSmsService;
    }

}
