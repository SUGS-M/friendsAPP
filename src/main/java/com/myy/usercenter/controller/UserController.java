package com.myy.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myy.usercenter.model.domain.User;
import com.myy.usercenter.model.request.UserLoginRequest;
import com.myy.usercenter.common.BaseResponse;
import com.myy.usercenter.common.ErrorCode;
import com.myy.usercenter.common.ResultUtils;
import com.myy.usercenter.exception.BusinessException;
import com.myy.usercenter.model.request.UserRegisterRequest;
import com.myy.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.myy.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.myy.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        System.out.println(userAccount + "+" + userPassword);
        User user = userService.userLogin(userAccount, userPassword, request);
        System.out.println(user);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**标签查询用户*/
    @PostMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request){
        //1、校验参数是否为空
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //User loginUser = request.getSession().
        User loginUser = userService.getCurrentUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }



    @GetMapping("/recommendByList")
    public BaseResponse<List<User>> recommendUsersByList(HttpServletRequest request){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userService.list(queryWrapper);
        //用户信息脱敏
        List<User> list = userList.stream().map(user->userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }
    @GetMapping("/recommendByPage")
    public BaseResponse<Page<User>> recommendUsersByPage(long pageSize, long pageNum, HttpServletRequest request){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userPage = userService.page(new Page<>(pageNum,pageSize),queryWrapper);
        //用户信息脱敏
        //Page<User> list = userPage.stream().map(user->userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(userPage);
    }
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){
        //1、redis的key设计
        User loginUser = userService.getCurrentUser(request);
        String redisKey = String.format("friends:user:recommend:%s", loginUser.getId());
        //2、读redis缓存
        ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
        //2.1、redis缓存有，直接输出
        Page<User> userPage = (Page<User>)valueOperations.get(redisKey);
        if(userPage != null){
            return ResultUtils.success(userPage);
        }
        //2.2、redis缓存没有，存入redis，mysql输出
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum,pageSize),queryWrapper);
        try{
            valueOperations.set(redisKey,userPage,3000, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            System.out.println("recommend"+e.getMessage());
        }
        return ResultUtils.success(userPage);
    }


    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getCurrentUser(request);
        return ResultUtils.success(userService.matchUsers(num, user));
    }











    /**是否为管理员*/
    private boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
