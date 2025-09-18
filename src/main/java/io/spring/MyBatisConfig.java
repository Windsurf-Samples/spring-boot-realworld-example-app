package io.spring;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan({"io.spring.infrastructure.mybatis.mapper", "io.spring.infrastructure.mybatis.readservice"})
public class MyBatisConfig {}
