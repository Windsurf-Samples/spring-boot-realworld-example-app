package io.spring.auth.infrastructure.mybatis.mapper;

import io.spring.auth.core.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
  void insert(@Param("user") User user);

  User findByUsername(@Param("username") String username);

  User findByEmail(@Param("email") String email);

  User findById(@Param("id") String id);

  void update(@Param("user") User user);
}
