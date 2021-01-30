package com.atguigu.gulimall.member.service.impl;

import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) throws PhoneExistException, UsernameExistException {
        MemberDao memberDao = this.baseMapper;
        //要保存的大对象
        MemberEntity entity = new MemberEntity();

        //异常机制
        //检查用户名 和 手机号是否唯一 为了让controller感知异常，异常机制
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());

        entity.setUsername(vo.getUserName());
        entity.setMobile(vo.getPhone());

        //密码需要加密蹲存储 MD5 密码加密处理
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        //初始化默认数据，会员等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        if (levelEntity != null) {
            entity.setLevelId(levelEntity.getId());
        }

        //其他默认信息

        //把这个大对象保存到数据库MemberEntity表中
        memberDao.insert(entity);
    }

    //异常机制
    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            //说明数据库有这个手机号
            throw new PhoneExistException();
        }
        //否则什么都不做 检查通过 业务继续进行注册
    }

    //异常机制
    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            //说明数据库有这个用户名
            throw new UsernameExistException();
        }
        //否则什么都不做 检查通过 业务继续进行注册
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {

        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();//123456

        //1 去数据库查询 根据登录账号查
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (entity == null) {
            //登录失败，数据库没有这个用户
            return null;
        }else {
            //数据库有这个用户
            //1 获取到数据库中的password
            String passwordDb = entity.getPassword();
            //2 进行密码比对
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches) {
                //密码比对成功，登录成功
                return entity;
            }else {
                //用户存在，密码不对，登录失败
                return null;
            }
        }
    }
}