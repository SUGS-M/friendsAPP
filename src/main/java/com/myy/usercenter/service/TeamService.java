package com.myy.usercenter.service;

import com.myy.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myy.usercenter.model.domain.User;
import com.myy.usercenter.model.dto.TeamQuery;
import com.myy.usercenter.model.request.TeamUpdateRequest;
import com.myy.usercenter.model.vo.TeamUserVO;

import java.util.List;

/**
* @author 18599
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-06-08 15:24:55
*/
public interface TeamService extends IService<Team> {


    Long addTeam(Team team, User loginUser);

    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);
}
