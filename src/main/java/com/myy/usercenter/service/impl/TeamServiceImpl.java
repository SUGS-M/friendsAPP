package com.myy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myy.usercenter.common.ErrorCode;
import com.myy.usercenter.exception.BusinessException;
import com.myy.usercenter.model.domain.Team;
import com.myy.usercenter.model.domain.User;
import com.myy.usercenter.model.domain.UserTeam;
import com.myy.usercenter.model.dto.TeamQuery;
import com.myy.usercenter.model.enums.TeamStatusEnum;
import com.myy.usercenter.model.request.TeamUpdateRequest;
import com.myy.usercenter.model.vo.TeamUserVO;
import com.myy.usercenter.model.vo.UserVO;
import com.myy.usercenter.service.TeamService;
import com.myy.usercenter.mapper.TeamMapper;
import com.myy.usercenter.service.UserService;
import com.myy.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.myy.usercenter.contant.UserConstant.ADMIN_ROLE;

/**
* @author 18599
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-06-08 15:24:55
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;


    @Override
    public Long addTeam(Team team, User loginUser) {
        //1、请求参数是否为空
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2、是否登录
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        final long userId = loginUser.getId();
        //3、检验信息
        //3.1、队伍人数（1-20）
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);    //先判断  为空 赋0
        if(maxNum < 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"人数不符合要求");
        }
        //3.2、队伍名
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length() > 20){           //为org.apache.commons.lang3.StringUtils
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名不符合要求");
        }
        //3.3、描述
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //4、status 是否公开  不转默认为0
        int status =  Optional.ofNullable(team.getMaxNum()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            //throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //5.如果status是加密状态，一定要密码 且密码<=32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(team.getStatus())){
            if(StringUtils.isBlank(password) || password.length() >32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //6.超出时间 > 当前时间
//        Date expireTime = team.getExpireTime();
//        if(new Date().after(expireTime)){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超出时间 > 当前时间");
//        }
        //7.校验用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
        //8.插入队伍消息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //9. 插入用户 ==> 队伍关系 到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;

    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("expireTime", searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            //查询最大人数相等
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxMum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            //根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            //根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }

        //不展示已过期的队伍
        //expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        //关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            //脱敏用户信息
            if (user!=null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }


    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //只有管理员或者队伍的创建者才可以修改
        //if (oldTeam.getUserId()!=loginUser.getId()&&!userService.isAdmin(loginUser)){
        if (oldTeam.getUserId()!=loginUser.getId()&&!isAdmin(loginUser)){
            //throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        return this.updateById(updateTeam);
    }



    private boolean isAdmin(User loginUser) {
        return loginUser.getUserRole() == ADMIN_ROLE;
    }
}




