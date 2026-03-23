package io.spring.infrastructure.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LocalUserCacheMapper {

  void upsertUser(
      @Param("id") String id,
      @Param("username") String username,
      @Param("email") String email,
      @Param("bio") String bio,
      @Param("image") String image);
}
