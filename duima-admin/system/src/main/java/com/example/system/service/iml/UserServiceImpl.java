package com.example.system.service.iml;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.system.configuration.constants.Constants;
import com.example.system.domain.User;
import com.example.system.dto.UserDto;
import com.example.system.mapper.RoleMapper;
import com.example.system.mapper.UserMapper;
import com.example.system.service.UserService;
import com.example.system.configuration.utils.AppMd5Utils;
import com.example.system.vo.DataGridView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
* @Author:
*/

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;


    @Override
    public User queryUserByPhone(String phone) {
        QueryWrapper<User> qw=new QueryWrapper<>();
        qw.eq(User.COL_PHONE,phone);
        User user = this.userMapper.selectOne(qw);
        return user;
    }

    @Override
    public User getOne(Long userId) {
        return this.userMapper.selectById(userId);
    }

    @Override
    public DataGridView listUserForPage(UserDto userDto) {
        Page<User> page=new Page<>(userDto.getPageNum(), userDto.getPageSize());
        QueryWrapper<User> qw=new QueryWrapper<>();
        qw.like(StringUtils.isNotBlank(userDto.getUserName()), User.COL_USER_NAME, userDto.getUserName());
        qw.like(StringUtils.isNotBlank(userDto.getPhone()), User.COL_PHONE, userDto.getPhone());
        qw.eq(StringUtils.isNotBlank(userDto.getStatus()), User.COL_STATUS, userDto.getStatus());
        qw.eq(userDto.getDeptId()!=null, User.COL_DEPT_ID, userDto.getDeptId());

        qw.ge(null!= userDto.getBeginTime(), User.COL_CREATE_TIME, userDto.getBeginTime());
        qw.le(null!= userDto.getEndTime(), User.COL_CREATE_TIME, userDto.getEndTime());
        qw.orderByAsc(User.COL_USER_ID);
        this.userMapper.selectPage(page,qw);
        return new DataGridView(page.getTotal(), page.getRecords());
    }

    @Override
    public int addUser(UserDto userDto) {
        User user=new User();
        BeanUtil.copyProperties(userDto,user);
        user.setUserType(Constants.USER_NORMAL);//???????????????????????????
        String defaultPwd=user.getPhone().substring(5);
        user.setCreateBy(userDto.getSimpleUser().getUserName());
        user.setCreateTime(DateUtil.date());
        user.setSalt(AppMd5Utils.createSalt());
        user.setPassword(AppMd5Utils.md5(defaultPwd,user.getSalt(),2));
        return this.userMapper.insert(user);
    }

    @Override
    public int updateUser(UserDto userDto) {
        User user=this.userMapper.selectById(userDto.getUserId());
        if(null==user){
            return 0;
        }
        BeanUtil.copyProperties(userDto,user);
        //???????????????
        user.setUpdateBy(userDto.getSimpleUser().getUserName());
        return this.userMapper.updateById(user);
    }

    @Override
    public int deleteUserByIds(Long[] userIds) {
        List<Long> ids = Arrays.asList(userIds);
        //????????????ids??????sys_role_user???????????????
        this.roleMapper.deleteRoleUserByUserIds(ids);
        return this.userMapper.deleteBatchIds(ids);
    }

    @Override
    public void resetPassWord(Long[] userIds) {
        for (Long userId : userIds) {
            User user=this.userMapper.selectById(userId);
            String defaultPwd="";
            //??????????????? ?????????
            if (user.getUserType().equals(Constants.USER_ADMIN)){
                defaultPwd="123456";
            }else{
                defaultPwd=user.getPhone().substring(5);
            }
            user.setSalt(AppMd5Utils.createSalt());
            user.setPassword(AppMd5Utils.md5(defaultPwd,user.getSalt(),2));
            this.userMapper.updateById(user);
        }
    }

    @Override
    public List<User> getAllUsers() {
        QueryWrapper<User> qw=new QueryWrapper<>();
        qw.eq(User.COL_STATUS, Constants.STATUS_TRUE);
        qw.eq(User.COL_USER_TYPE,Constants.USER_NORMAL);
        qw.orderByAsc(User.COL_USER_ID);
        return this.userMapper.selectList(qw);
    }
}
