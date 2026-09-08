package com.edu.springboot.infrastructure.persistence.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.edu.springboot.infrastructure.persistence.mybatis")
public class MyBatisConfig {
}
