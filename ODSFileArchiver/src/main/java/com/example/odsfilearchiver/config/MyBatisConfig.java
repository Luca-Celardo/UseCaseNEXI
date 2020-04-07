package com.example.odsfilearchiver.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.odsfilearchiver.persistence")
public class MyBatisConfig {
}
