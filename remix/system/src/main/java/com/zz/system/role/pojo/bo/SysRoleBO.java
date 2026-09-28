package com.zz.system.role.pojo.bo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;

import java.util.Date;

/**
 * <p><b>系统服务-角色导出类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/28 19:16
 */
@ColumnWidth(20)
@Data
public class SysRoleBO {

    @ExcelProperty("角色ID")
    private Long roleId;

    @ExcelProperty("角色名称")
    private String roleName;

    @ExcelProperty("是否启用")
    private Integer enabled;

    @ExcelProperty("创建时间")
    private Date createTime;

    @ExcelProperty("更新时间")
    private Date updateTime;
}
