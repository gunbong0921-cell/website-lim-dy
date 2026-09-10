package com.edu.springboot.infrastructure.persistence.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: MyBatisConfig
 * 책임: 스프링 설정. 구현 빈을 포트에 꽂음
 * 문서: [docs/features/07-boards.md](../../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../../docs/technical/01-architecture.md)
 */
@Configuration
@MapperScan("com.edu.springboot.infrastructure.persistence.mybatis")
public class MyBatisConfig {
}
