package com.example.odsdatamartextractor.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.odsdatamartextractor.persistence")
public class MyBatisConfig {
}
