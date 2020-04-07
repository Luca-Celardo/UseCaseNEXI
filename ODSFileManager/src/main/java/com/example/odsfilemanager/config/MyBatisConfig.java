package com.example.odsfilemanager.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.odsfilemanager.persistence")
public class MyBatisConfig {
}
