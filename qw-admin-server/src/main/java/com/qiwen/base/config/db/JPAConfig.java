package com.qiwen.base.config.db;

import com.qiwen.base.util.SystemUtil;
import com.qiwen.base.vo.LoginUserVO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.EntityManager;
import java.util.Objects;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "usernameAuditor")
public class JPAConfig {

    @Bean
    public JPAQueryFactory JPAQueryFactory(EntityManager entityManager) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory;
    }

    @Bean
    public AuditorAware usernameAuditor() {
        return () -> {
            LoginUserVO user = SystemUtil.currentLoginUser();
            if(Objects.isNull(user)) {
                return Optional.of("anonymous");
            }
            return Optional.of(user.getUsername());
        };
    }
}
