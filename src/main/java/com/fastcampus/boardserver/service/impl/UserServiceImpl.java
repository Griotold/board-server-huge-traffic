package com.fastcampus.boardserver.service.impl;

import com.fastcampus.boardserver.dto.UserDTO;
import com.fastcampus.boardserver.exception.DuplicateIdException;
import com.fastcampus.boardserver.mapper.UserProfileMapper;
import com.fastcampus.boardserver.service.UserService;
import com.fastcampus.boardserver.util.SHA256Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserProfileMapper userProfileMapper;

    @Override
    public void register(UserDTO userProfile) {
        boolean dupIdResult = isDuplicatedId(userProfile.getUserID());
        if (dupIdResult) {
            throw new DuplicateIdException("중복된 아이디입니다.");
        }
        userProfile.setCreateTime(new Date());
        userProfile.setPassword(SHA256Util.encryptionSHA256(userProfile.getPassword()));
        int insertCount = userProfileMapper.register(userProfile);

        if (insertCount != 1) {
            log.info("insertMember ERROR! {}", userProfile);
            throw new RuntimeException(
                    "insertUser ERROR! 회원가입 메서드를 확인해주세요\n"
            + " Params : " + userProfile);
        }

    }

    @Override
    public UserDTO login(String id, String password) {

        String cryptoPassword = SHA256Util.encryptionSHA256(password);
        UserDTO memberInfo = userProfileMapper.findByUserIdAndPassword(id, cryptoPassword);
        return memberInfo;
    }

    @Override
    public boolean isDuplicatedId(String id) {

        return userProfileMapper.idCheck(id) == 1;
    }

    @Override
    public UserDTO getUserInfo(String userId) {
        return null;
    }

    @Override
    public void updatePassword(String id, String beforePassword, String afterPassword) {
        String cryptoPassword = SHA256Util.encryptionSHA256(beforePassword);
        UserDTO memberInfo = userProfileMapper.findByIdAndPassword(id, cryptoPassword);

        if (memberInfo != null) {
            memberInfo.setPassword(SHA256Util.encryptionSHA256(afterPassword));
            int insertCount = userProfileMapper.updatePassword(memberInfo);
        } else {
            log.error("updatePasswrod ERROR! {}", memberInfo);
            throw new IllegalArgumentException("updatePasswrod ERROR! 비밀번호 변경 메서드를 확인해주세요\n"
                    + "Params : " + memberInfo);
        }
    }

    @Override
    public void deleteId(String id, String password) {
        String cryptoPassword = SHA256Util.encryptionSHA256(password);
        UserDTO memberInfo = userProfileMapper.findByIdAndPassword(id, cryptoPassword);

        if (memberInfo != null) {
            userProfileMapper.deleteUserProfile(memberInfo.getUserID());
        } else {
            log.error("deleteId ERROR! {}", memberInfo);
            throw new RuntimeException("deleteId ERROR! id 삭제 메서드를 확인해주세요\n" + "Params : " + memberInfo);
        }
    }
}
