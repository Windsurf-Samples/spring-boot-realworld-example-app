package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.UserData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TestUserReadService extends UserReadService {
  @Override
  UserData findByUsername(@Param("username") String username);

  @Override
  UserData findById(@Param("id") String id);
}
