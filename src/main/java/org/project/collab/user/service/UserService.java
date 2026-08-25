package org.project.collab.user.service;
import org.project.collab.common.error.*; import org.project.collab.user.domain.User; import org.project.collab.user.repository.UserRepository; import org.springframework.dao.DataIntegrityViolationException; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service public class UserService { private final UserRepository users; public UserService(UserRepository u){users=u;}
 @Transactional public User create(String email,String name){if(users.existsByEmail(email))throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);try{return users.save(new User(email,name));}catch(DataIntegrityViolationException e){throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);}}
 @Transactional(readOnly=true) public User requireUser(Long id){return users.findById(id).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));}}
