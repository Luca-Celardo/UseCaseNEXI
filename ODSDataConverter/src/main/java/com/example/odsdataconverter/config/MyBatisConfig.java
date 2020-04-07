package com.example.odsdataconverter.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.odsdataconverter.persistence")
public class MyBatisConfig {
}
