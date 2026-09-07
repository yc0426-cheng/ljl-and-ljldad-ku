package com.zz.system.menu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 系统菜单表（sys_menu）
 *
 * <p>一行 = 一个菜单/路由节点：目录（menu_type=1，仅分组）或叶子菜单（menu_type=2，点击跳路由）。</p>
 * <ul>
 *     <li>route_path：叶子菜单的路由地址（如 /system/menu）；内置内容页约定 /progress-daily 等由前端特判；</li>
 *     <li>component： 对应 src/views 下组件路径（如 system/menu/index），供前端动态注册路由；</li>
 *     <li>icon：      前端图标名。</li>
 * </ul>
 *
 * <p>children 为树形接口组装用（非表字段），单表 CRUD 时忽略。</p>
 *
 * @TableName sys_menu
 * @author yangcheng
 * @since 2026-09-06
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    /** 主键：菜单 ID（雪花算法） */
    @TableId
    private Long menuId;

    /** 父菜单 ID：顶级为 NULL */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 菜单类型：1 = 目录（分组），2 = 菜单（叶子路由） */
    private Integer menuType;

    /** 路由地址（叶子有效），如 /system/menu */
    private String routePath;

    /** 组件路径（views 下相对路径），如 system/menu/index */
    private String component;

    /** 图标名 */
    private String icon;

    /** 排序号（小到大） */
    private Integer orderNo;

    /** 是否在菜单显示 */
    private Boolean visible;

    /** 状态：1 启用，0 停用 */
    private Integer status;

    /** 创建时间 */
    private Date createTime;

    /** 删除标记 */
    private Boolean delFlag;

    /** 子菜单（树形接口使用，非表字段） */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
