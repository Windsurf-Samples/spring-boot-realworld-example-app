package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.UserData;

public interface UserReadService {
  UserData findById(String id);

  UserData findByUsername(String username);
}
