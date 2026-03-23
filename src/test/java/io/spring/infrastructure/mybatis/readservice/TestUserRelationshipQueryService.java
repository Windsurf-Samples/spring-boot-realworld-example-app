package io.spring.infrastructure.mybatis.readservice;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TestUserRelationshipQueryService extends UserRelationshipQueryService {
  @Override
  boolean isUserFollowing(
      @Param("userId") String userId, @Param("anotherUserId") String anotherUserId);

  @Override
  Set<String> followingAuthors(@Param("userId") String userId, @Param("ids") List<String> ids);

  @Override
  List<String> followedUsers(@Param("userId") String userId);
}
