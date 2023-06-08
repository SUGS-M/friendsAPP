package com.myy.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myy.usercenter.common.BaseResponse;
import com.myy.usercenter.common.ErrorCode;
import com.myy.usercenter.common.ResultUtils;
import com.myy.usercenter.exception.BusinessException;
import com.myy.usercenter.model.domain.Team;
import com.myy.usercenter.model.domain.User;
import com.myy.usercenter.model.dto.TeamQuery;
import com.myy.usercenter.model.request.TeamUpdateRequest;
import com.myy.usercenter.model.vo.TeamUserVO;
import com.myy.usercenter.service.TeamService;
import com.myy.usercenter.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.myy.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.myy.usercenter.contant.UserConstant.USER_LOGIN_STATE;


/**
 * 团队接口
 */
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;
    //@Resource
    //private RedisTemplate<String,Object> redisTemplate;//就不用redis
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamQuery teamQuery , HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Long result = teamService.addTeam(team,loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //boolean isAdmin = userService.isAdmin(request);
        boolean isAdmin = isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,isAdmin);
        return ResultUtils.success(teamList);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }















    @ApiOperation("已丢弃")
    @PostMapping("/addold")
    public BaseResponse<Long> addTeamOld(@RequestBody Team team){
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.save(team);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"插入失败");
        }
        return ResultUtils.success(team.getId());
    }
    @ApiOperation("已丢弃")
    @DeleteMapping("/deleteold/{id}")
    public BaseResponse<Boolean> deleteTeamOld(@PathVariable Long id){
        if(id < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(id);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(result);
    }
    @ApiOperation("已丢弃")
    @PostMapping("/updateold")
    public BaseResponse<Boolean> updateTeamOld(@RequestBody Team team){
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.updateById(team);//是team，不是team.getId()  ->需要测试时对准id
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(result);
    }
    @ApiOperation("已丢弃")
    @PostMapping("/getold")
    public BaseResponse<Team> getTeamOld(Long id){
        if(id < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查询失败");
        }
        return ResultUtils.success(team);
    }
    @PostMapping("/listold")
    public BaseResponse<List<Team>> listTeamold(TeamQuery teamQuery){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);//spring(原来的，目标值)   mybatis(目标值,原来的)
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        List<Team> teamList = teamService.list(queryWrapper);
        return ResultUtils.success(teamList);
    }
    @PostMapping("/listByPageold")
    public BaseResponse<Page<Team>> listByPageTeamold(TeamQuery teamQuery){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);//spring(原来的，目标值)   mybatis(目标值,原来的)
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());  //注意Page导入的是mybatisplus，而不是springboot自带的那个
        Page<Team> teamPage = teamService.page(page,queryWrapper);
        return ResultUtils.success(teamPage);
    }

    /**是否为管理员*/
    private boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
