package com.zz.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.system.user.entity.SysUserMisc;
import com.zz.system.user.service.SysUserMiscService;
import com.zz.system.user.mapper.SysUserMiscMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 针对表【sys_user_misc(用户信息杂项表)】的数据库操作Service实现
 *
 * @author yangcheng
 * @since 2026-09-15 14:26:04
 */
@Service
@RequiredArgsConstructor
public class SysUserMiscServiceImpl extends ServiceImpl<SysUserMiscMapper, SysUserMisc>
        implements SysUserMiscService {

    @Override
    public void insertOrUpdate(String key) {
        SysUserMisc sysUserMisc = new SysUserMisc();

        // 正则匹配
        String regex = "^avatar/(.+)_[0-9a-f]{32}\\.(\\w+)$";
        Matcher m = Pattern.compile(regex).matcher(key);
        if (m.matches()) {
            Long userId = Long.parseLong(m.group(1));   // 贪婪匹配到最后一个 _ 之前
            sysUserMisc.setUserId(userId);
            sysUserMisc.setAvatar(key);
        }

        // 判断是否有此用户头像
        LambdaQueryWrapper<SysUserMisc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserMisc::getUserId, sysUserMisc.getUserId());
        List<SysUserMisc> list = super.list(queryWrapper);
        if (list.isEmpty()) {
            super.save(sysUserMisc);
        } else {
            SysUserMisc entity = new SysUserMisc();
            // 将之前的头像放入历史头像内
            if (entity.getHistoryAvatars() == null) {
                entity.setHistoryAvatars(entity.getAvatar());
            } else {
                entity.setHistoryAvatars("," + entity.getAvatar());
            }
            // 更新key值
            Long miscId = list.get(0).getMiscId();
            entity.setMiscId(miscId);
            entity.setAvatar(key);
            super.updateById(entity);
        }
    }

    @Override
    public String getAvatar(Long userId) {
        // 根据用户id查询
        LambdaQueryWrapper<SysUserMisc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserMisc::getUserId, userId);
        SysUserMisc sysUserMisc = super.getOne(queryWrapper);
        return sysUserMisc.getAvatar();
    }

    @Override
    public String getHistoryAvatars(Long userId) {
        // 根据用户id查询
        LambdaQueryWrapper<SysUserMisc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserMisc::getUserId, userId);
        SysUserMisc sysUserMisc = super.getOne(queryWrapper);
        return sysUserMisc.getHistoryAvatars();
    }
}




