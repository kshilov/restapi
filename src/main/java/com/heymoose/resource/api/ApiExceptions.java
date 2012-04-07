package com.heymoose.resource.api;

import com.heymoose.domain.Role;
import com.heymoose.domain.base.IdEntity;

public class ApiExceptions {

  public static ApiRequestException illegalState(String reason) {
    return new ApiRequestException(409, reason);
  }
  
  public static ApiRequestException notFound(Class<? extends IdEntity> clazz, Object id) {
    return new ApiRequestException(404, clazz.getSimpleName() + " with id " + id + " was not found");
  }

  public static ApiRequestException nullParam(String paramName) {
    return new ApiRequestException(400, "param '" + paramName + "' is null");
  }

  public static ApiRequestException badValue(String paramName, Object value) {
    return new ApiRequestException(400, "'" + value.toString() + "' is bad value for '" + paramName+ "'");
  }

  public static ApiRequestException badSignature(String signature) {
    return new ApiRequestException(401, "bad signature: " + signature);
  }

  public static ApiRequestException appNotFound(long appId) {
    return new ApiRequestException(401, "App with id " + appId + " was not found");
  }

  public static ApiRequestException customerNotFound(long customerId) {
    return new ApiRequestException(401, "Customer with id" + customerId + " was not found");
  }

  public static ApiRequestException notInRole(long userId,  Role role) {
    return new ApiRequestException(401, "User with id " + userId + " has no role " + role.name());
  }
}
