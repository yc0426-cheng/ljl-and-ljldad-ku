package com.zz.system.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.system.operation.entity.SysUserOperationLog;
import com.zz.system.operation.entity.SysUserOperationStepLog;
import com.zz.system.operation.mapper.SysUserOperationLogMapper;
import com.zz.system.operation.mapper.SysUserOperationStepLogMapper;
import com.zz.system.operation.service.SysUserOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

/**
 * <p><b>系统服务-用户操作记录业务实现（真正落库方）</b></p>
 *
 * <p>本类是 sys_user_operation_log / sys_user_operation_step_log 两张表的唯一落库实现，
 * 供两类调用方使用：</p>
 * <ul>
 *     <li>system 模块自身的埋点记录器（SystemOperationLogRecorder，直接注入本接口）；</li>
 *     <li>其它模块（auth 等）经 OperationLogFeignClient → SysUserOperationLogController 转发到本接口。</li>
 * </ul>
 *
 * <h3>status 约定（与 SQL 注释一致）</h3>
 * <p>主表、步骤：0=失败，1=成功；步骤额外支持 2=执行中（start 写入的中间态，正常结束必回写 0/1）。</p>
 *
 * <h3>description 生成规则</h3>
 * <p>finishRequest 时查询该 log_id 下全部步骤，按 step_no 升序（先序），把每步拼成
 * methodName(moduleName)，用 -&gt; 连接。例：login(auth)-&gt;getUserInfoByAccount(feign)-&gt;getUserInfoByAccount(system)。</p>
 *
 * <h3>根步骤解析</h3>
 * <p>startStep 的 parentStepId 为 null 时：若该 log 还没有根步骤（parent_step_id IS NULL），
 * 本步骤即根（step_no=1）；若已有根，则自动挂到根下面，避免出现"无父游离步骤"。</p>
 *
 * <h3>已知边界</h3>
 * <ul>
 *     <li>step_no 用 MAX+1 分配：同一次请求的步骤基本由单线程串行产生，冲突概率极低；
 *         生产可给 (log_id, step_no) 加唯一索引兜底。</li>
 *     <li>入口模块在 finishRequest 前崩溃，主表会停在"执行中"，可另加定时任务把超时记录置为失败。</li>
 *     <li>错误信息超长时按列宽截断：主表 error_message 200、步骤表 500、description 1000。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserOperationLogServiceImpl
        extends ServiceImpl<SysUserOperationLogMapper, SysUserOperationLog>
        implements SysUserOperationLogService {

    /** 状态：失败 */
    private static final int STATUS_FAIL = 0;
    /** 状态：成功 */
    private static final int STATUS_SUCCESS = 1;
    /** 状态：执行中（中间态） */
    private static final int STATUS_RUNNING = 2;

    /** 主表 error_message 列宽 */
    private static final int MAIN_ERROR_MAX = 200;
    /** 步骤表 error_message 列宽 */
    private static final int STEP_ERROR_MAX = 500;
    /** 主表 description 列宽 */
    private static final int DESCRIPTION_MAX = 1000;

    /** 主表 Mapper */
    private final SysUserOperationLogMapper logMapper;
    /** 步骤表 Mapper */
    private final SysUserOperationStepLogMapper stepLogMapper;

    /**
     * {@inheritDoc}
     * <p>主键由 MyBatis-Plus 全局 id-type=assign_id（雪花）自动生成，insert 后回填实体。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startRequest(String moduleName, String methodName, String callType,
                             String operationMethod, String operationIp, Long userId) {
        SysUserOperationLog logRecord = new SysUserOperationLog();
        logRecord.setUserId(userId);
        logRecord.setOperationDate(new Date());
        logRecord.setOperationIp(operationIp);
        logRecord.setOperationMethod(operationMethod);
        logRecord.setStatus(STATUS_RUNNING);
        logMapper.insert(logRecord);
        log.info("操作记录-开始请求 log_id={} module={} method={}", logRecord.getLogId(), moduleName, methodName);
        return logRecord.getLogId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startStep(Long logId, Long parentStepId, String moduleName, String methodName, String callType,
                          String targetDb, String targetTable) {
        // 1) 分配 step_no：该 log 下现有最大 step_no + 1（首次为 1）
        int stepNo = 1;
        List<SysUserOperationStepLog> last = stepLogMapper.selectList(new QueryWrapper<SysUserOperationStepLog>()
                .eq("log_id", logId)
                .orderByDesc("step_no")
                .last("LIMIT 1"));
        if (!last.isEmpty()) {
            stepNo = last.get(0).getStepNo() + 1;
        }

        // 2) 根步骤解析：parent 为空时，无根则本步为根；有根则挂到根下
        if (parentStepId == null) {
            List<SysUserOperationStepLog> roots = stepLogMapper.selectList(new QueryWrapper<SysUserOperationStepLog>()
                    .eq("log_id", logId)
                    .isNull("parent_step_id")
                    .last("LIMIT 1"));
            if (!roots.isEmpty()) {
                parentStepId = roots.get(0).getStepId();
            }
        }

        SysUserOperationStepLog step = new SysUserOperationStepLog();
        step.setLogId(logId);
        step.setParentStepId(parentStepId);
        step.setStepNo(stepNo);
        step.setModuleName(moduleName);
        step.setMethodName(methodName);
        step.setCallType(callType);
        step.setTargetDb(blankToNull(targetDb));
        step.setTargetTable(blankToNull(targetTable));
        step.setStatus(STATUS_RUNNING);
        step.setStartTime(new Date());
        stepLogMapper.insert(step);
        log.info("操作记录-开始步骤 step_id={} log_id={} parent={} {}({})",
                step.getStepId(), logId, parentStepId, methodName, moduleName);
        return step.getStepId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishStep(Long logId, Long stepId, Integer status, String errorMessage) {
        SysUserOperationStepLog step = stepLogMapper.selectById(stepId);
        if (step == null || !java.util.Objects.equals(step.getLogId(), logId)) {
            log.warn("操作记录-结束步骤跳过：step_id={} 不存在或不属于 log_id={}", stepId, logId);
            return;
        }
        Date endTime = new Date();
        long start = step.getStartTime() == null ? endTime.getTime() : step.getStartTime().getTime();
        step.setEndTime(endTime);
        step.setCostMs(endTime.getTime() - start);
        step.setStatus(status);
        step.setErrorMessage(truncate(errorMessage, STEP_ERROR_MAX));
        stepLogMapper.updateById(step);
        log.info("操作记录-结束步骤 step_id={} status={} cost={}ms", stepId, status, step.getCostMs());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishRequest(Long logId, Integer status, String errorMessage, Long userId) {
        SysUserOperationLog logRecord = logMapper.selectById(logId);
        if (logRecord == null) {
            log.warn("操作记录-结束请求跳过：log_id={} 不存在", logId);
            return;
        }
        logRecord.setStatus(status);
        logRecord.setErrorMessage(truncate(errorMessage, MAIN_ERROR_MAX));
        if (userId != null) {
            logRecord.setUserId(userId);
        }
        // 按 step_no 先序拼接 description：method(module)->method(module)...
        List<SysUserOperationStepLog> steps = stepLogMapper.selectList(new QueryWrapper<SysUserOperationStepLog>()
                .eq("log_id", logId)
                .orderByAsc("step_no"));
        StringJoiner joiner = new StringJoiner("->");
        for (SysUserOperationStepLog step : steps) {
            joiner.add(step.getMethodName() + "(" + step.getModuleName() + ")");
        }
        logRecord.setDescription(truncate(joiner.toString(), DESCRIPTION_MAX));
        logMapper.updateById(logRecord);
        log.info("操作记录-结束请求 log_id={} status={} description={}", logId, status, logRecord.getDescription());
    }

    /**
     * 超长截断
     *
     * @param text 原文，可为 null
     * @param max  最大长度
     * @return 截断后的字符串；null 原样返回
     */
    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    /**
     * 空串转 null
     *
     * @param value 字符串
     * @return 空白字符串返回 null
     */
    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
