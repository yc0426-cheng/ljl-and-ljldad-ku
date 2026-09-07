-- 创建数据库（如果已存在则忽略）
CREATE DATABASE IF NOT EXISTS learn
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE learn;

-- 系统菜单表：一行 = 一个菜单/路由节点（目录 或 叶子菜单）
-- 作用：前端主页左侧菜单、以及"系统管理 → 菜单管理"维护的动态路由来源。
-- 说明：
--   menu_type 1 = 目录（仅分组，不跳转）；2 = 菜单（叶子，点击跳路由）
--   route_path：叶子菜单的路由地址，如 /system/menu；内置内容页约定 /progress-daily 等（前端特判）
--   component： 叶子菜单对应 src/views 下的组件路径，如 system/menu/index；目录/内置内容页为空
--   icon：      前端图标名（与主页内置图标映射一致），可为空
CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id     BIGINT PRIMARY KEY COMMENT '菜单ID',
    parent_id   BIGINT      DEFAULT NULL COMMENT '父菜单ID，顶级为 NULL，关联本表 menu_id',
    menu_name   varchar(50) NOT NULL COMMENT '菜单名称',
    menu_type   int         NOT NULL COMMENT '菜单类型：1 = 目录（分组），2 = 菜单（叶子路由）',
    route_path  varchar(200) DEFAULT NULL COMMENT '路由地址（叶子有效），如 /system/menu',
    component   varchar(200) DEFAULT NULL COMMENT '组件路径（views 下相对路径），如 system/menu/index',
    icon        varchar(50)  DEFAULT NULL COMMENT '图标名',
    order_no    int          DEFAULT 0 COMMENT '排序号（小到大）',
    visible     tinyint(1)   DEFAULT 1 COMMENT '是否在菜单显示：1 显示，0 隐藏',
    status      int          DEFAULT 1 COMMENT '状态：1 启用，0 停用',
    create_time datetime     DEFAULT NULL COMMENT '创建时间',
    del_flag    tinyint(1)   DEFAULT 0 COMMENT '删除标记：0 未删除，1 已删除',

    index(parent_id),
    index(parent_id, order_no)
) comment '系统菜单表';

-- 种子数据：主页内置模块目录（前端特判保留原内容）+ 系统管理/菜单管理
-- menu_id 1~4 为内置内容目录，5 为系统管理目录（可自行增删路由/菜单）
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, route_path, component, icon, order_no, visible, status, create_time, del_flag)
VALUES
    (1, NULL, '学习进度',  1, NULL, NULL, 'DataLine',  1, 1, 1, NOW(), 0),
    (2, NULL, '图书馆',    1, NULL, NULL, 'Reading',   2, 1, 1, NOW(), 0),
    (3, NULL, '学习题目',  1, NULL, NULL, 'EditPen',   3, 1, 1, NOW(), 0),
    (4, NULL, '用户管理',  1, NULL, NULL, 'UserFilled',4, 1, 1, NOW(), 0),
    (5, NULL, '系统管理',  1, NULL, NULL, 'Setting',   5, 1, 1, NOW(), 0),

    (11, 1, '每日记录', 2, '/progress-daily', NULL, NULL, 1, 1, 1, NOW(), 0),
    (21, 2, '我的书架', 2, '/library-shelf',  NULL, NULL, 1, 1, 1, NOW(), 0),
    (31, 3, '题库练习', 2, '/quiz-list',      NULL, NULL, 1, 1, 1, NOW(), 0),
    (41, 4, '用户列表', 2, '/users-list',     NULL, NULL, 1, 1, 1, NOW(), 0),
    (51, 5, '菜单管理', 2, '/system/menu', 'system/menu/index', NULL, 1, 1, 1, NOW(), 0);
