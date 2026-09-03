package com.moroccoflow.traffic.congestion;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CongestionProperties.class)
public class CongestionConfiguration {
}
