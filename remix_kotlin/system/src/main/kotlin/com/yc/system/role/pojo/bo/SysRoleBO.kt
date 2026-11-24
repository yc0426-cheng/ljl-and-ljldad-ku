package com.yc.system.role.pojo.bo

import org.apache.fesod.sheet.annotation.ExcelProperty
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth
import java.util.Date

/**
 * <p><b>系统服务-角色导出类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/19 19:16
 */
@ColumnWidth(20)
data class SysRoleBO(
    @ExcelProperty("角色ID") var roleId: Long,
    @ExcelProperty("角色名称") var roleName: String,
    @ExcelProperty("是否启用") var enabled: Int,
    @ExcelProperty("创建时间") var createTime: Date,
    @ExcelProperty("更新时间") var updateTime: Date
)
