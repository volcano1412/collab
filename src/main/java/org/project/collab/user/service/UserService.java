package org.project.collab.user.service;

import org.project.collab.common.error.*;
import org.project.collab.user.domain.User;
import org.project.collab.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserRepository users;

  public UserService(UserRepository u) {
    users = u;
  }

  @Transactional
  public User create(String email, String name) {
    if (users.existsByEmail(email)) throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
    try {
      return users.save(new User(email, name));
    } catch (DataIntegrityViolationException e) {
      throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
    }
  }

  @Transactional(readOnly = true)
  public User requireUser(Long id) {
    return users.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  /**
   * 이메일로 사용자를 찾는다.
   *
   * <p>인증이 없어 클라이언트는 자기 자신을 이메일로 식별하고, 그렇게 얻은 id를 이후 요청의 X-User-Id 로 쓴다.
   * 없는 이메일은 id 조회와 같은 USER_NOT_FOUND 로 답한다.
   */
  @Transactional(readOnly = true)
  public User requireByEmail(String email) {
    return users
        .findByEmail(email == null ? "" : email.trim())
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }
}
