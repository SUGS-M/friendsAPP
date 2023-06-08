package com.myy.usercenter.service;

import com.myy.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     */
    int userLogout(HttpServletRequest request);

    /**
     * 标签查询用户（Gson查询版）
     */
    List<User> searchUsersByTags(List<String> tagNameList);
    /**
     * 标签查询用户（SQL查询版）
     */
    List<User> searchUsersByTagsBySQL(List<String> tagNameList);

    User getCurrentUser(HttpServletRequest request);

    int updateUser(User user, User loginUser);

    List<User> matchUsers(long num, User user);

    // boolean isAdmin(User loginUser);
}
