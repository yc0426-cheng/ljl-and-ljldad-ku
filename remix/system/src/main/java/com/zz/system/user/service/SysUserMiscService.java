package com.zz.system.user.service;

import com.zz.system.user.entity.SysUserMisc;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* 针对表【sys_user_misc(用户信息杂项表)】的数据库操作Service
*
* @author yangcheng
* @since 2026-09-15 14:26:04
*/
public interface SysUserMiscService extends IService<SysUserMisc> {

    /**
     * 保存头像数据
     *
     * @param key ossKey
     */
    void insertOrUpdate(String key);

    /**
     * 获取头像数据
     *
     * @param userId 用户id
     * @return 头像地址
     */
    String getAvatar(Long userId);

    /**
     * 获取历史头像数据
     *
     * @param userId 用户id
     * @return 历史头像数据
     */
    String getHistoryAvatars(Long userId);
}
