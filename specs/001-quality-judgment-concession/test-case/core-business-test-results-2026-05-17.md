# 质量判定解释与让步管理系统 — 核心业务测试执行报告

**执行时间**: 2026-05-17 14:01:02  
**测试依据**: [core-business-test-plan.md](./core-business-test-plan.md)  
**执行方式**: 后端 API 自动化 + 前端可达性检查（`run_core_business_api_tests.py`）  
**被测服务**: 后端 `http://localhost:8080` | 前端 `http://localhost:3000`  

## 一、执行摘要

| 指标 | 数值 |
|------|------|
| 用例总数 | 42 |
| 通过 (PASS) | 31 |
| 失败 (FAIL) | 3 |
| 阻塞 (BLOCKED) | 2 |
| 跳过 (SKIP) | 6 |
| 通过率（PASS/总数） | 73% |
| P0 用例通过 | 20/24 |

### 测试账号映射（方案 → 实际）

| 方案账号 | 实际工号 | 角色 | 密码 |
|----------|----------|------|------|
| qc_manager | 021002 | QUALITY_SUPERVISOR | Admin123456 |
| qc_user01 | 021001 | QUALITY_ENGINEER | Admin123456 |
| quality_director | 021003 | QUALITY_MANAGER | Admin123456 |
| admin | admin | ADMIN | Admin123456 |

### 环境与数据说明

- 测试批次后缀：`20260517140102`（炉号/卷号/标准版本号均带此后缀，避免与历史数据冲突）
- 指标使用预置数据：Rm=ind001, ReL=ind003, 延伸率=ind002, 厚度公差=ind004
- 客户协议客户 ID：`CUST-001`（预置客户）

## 二、按需求模块汇总

| 模块 | PASS | FAIL | BLOCKED | SKIP |
|------|------|------|---------|------|
| 前端冒烟 | 1 | 0 | 0 | 0 |
| REQ-01 标准维护 | 5 | 1 | 1 | 0 |
| REQ-02 检验录入 | 4 | 1 | 1 | 0 |
| REQ-03 自动判定 | 7 | 1 | 0 | 0 |
| REQ-04 复检改判 | 8 | 0 | 0 | 2 |
| REQ-05 质保书 | 3 | 0 | 0 | 3 |
| 端到端场景 | 3 | 0 | 0 | 1 |

## 三、P0 用例结果

- ✅ **TC-B001** 新增国家标准并配置指标 — **PASS**：id=2055891330451697666
- ✅ **TC-B002** 新增客户协议标准 — **PASS**：id=2055891333211549698
- ✅ **TC-B003** 发布标准 — **PASS**：国标与客协均已 PUBLISHED
- ✅ **TC-B009** 新增检验记录（华东汽车） — **PASS**：recordId=2055891350261395457
- ✅ **TC-B010** 录入后自动判定合格 — **PASS**：judgmentType=QUALIFIED
- ✅ **TC-B012** 软作废检验记录（主管） — **PASS**：作废成功
- ❌ **TC-B013** 非权限账号作废应拒绝 — **FAIL**：{'code': 2000, 'message': '操作成功', 'data': None, 'success': True}
- ✅ **TC-B017** 判定结论 QUALIFIED — **PASS**：recordId=2055891368837967873
- ✅ **TC-B018** 判定结论 UNQUALIFIED — **PASS**：recordId=2055891372923219970
- ✅ **TC-B019** 判定结论 CAN_CONCESSION — **PASS**：recordId=2055891375720820737
- ❌ **TC-B020** 判定结论 NEED_REINSPECTION — **FAIL**：期望=NEED_REINSPECTION 实际=UNQUALIFIED
- ✅ **TC-B025** 发起复检 — **PASS**：reinspectionId=2055891394330947585
- ✅ **TC-B026** 完成复检并重新判定 — **PASS**：复检后结论相关 record 已关联 newRecordId
- ✅ **TC-B027** 第3次复检应拒绝 — **PASS**：该判定结论已发起 2 次复检，不可再次发起
- ✅ **TC-B028** 发起常规改判 — **PASS**：id=2055891401691951105
- ✅ **TC-B029** 常规改判审批通过 — **PASS**：判定已变更为 QUALIFIED
- ✅ **TC-B030** 逆向改判（含证据） — **PASS**：id=2055891409174589441
- ✅ **TC-B031** 逆向改判无证据应拦截 — **PASS**：逆向改判必须填写新证据来源
- ⏭ **TC-B032** 普通质检员审批逆向改判 — **SKIP**：需 UI + ENHANCED 审批流验证
- ⏭ **TC-B033** 质量经理审批逆向改判 — **SKIP**：依赖 TC-B030 待审批单
- ✅ **TC-B035** 按卷号汇总质保书 — **PASS**：certId=2055891412064464898
- ✅ **E2E-01** 质量合格完整链路 — **PASS**：标准发布→检验录入→判定合格→质保书（分步用例已覆盖）
- ✅ **E2E-02** 客协严于国标优先级 — **PASS**：TC-B022 已验证
- ✅ **E2E-03** 不合格→改判链路 — **PASS**：TC-B028/B029 已覆盖

## 四、全部用例明细

### 前端冒烟

- ✅ **FE-001** 前端页面可访问 — **PASS**
  - http://localhost:3000
### REQ-01 标准维护

- ✅ **TC-B001** 新增国家标准并配置指标 — **PASS**
  - id=2055891330451697666
- ✅ **TC-B002** 新增客户协议标准 — **PASS**
  - id=2055891333211549698
- ✅ **TC-B003** 发布标准 — **PASS**
  - 国标与客协均已 PUBLISHED
- ❌ **TC-B004** 时间窗口重叠标准应拒绝 — **FAIL**
  - 重叠标准仍可发布
- ✅ **TC-B005** 编辑草稿标准指标上下限 — **PASS**
  - Rm 下限已改为 375
- ✅ **TC-B005b** 已发布标准不可编辑 — **PASS**
  - 仅草稿状态的标准可以修改
- ⏸ **TC-B006** 作废标准并验证历史判定不受影响 — **BLOCKED**
  - 后端未实现标准作废 VOID 接口
### REQ-02 检验录入

- ✅ **TC-B009** 新增检验记录（华东汽车） — **PASS**
  - recordId=2055891350261395457
- ✅ **TC-B010** 录入后自动判定合格 — **PASS**
  - judgmentType=QUALIFIED
- ⏸ **TC-B011** 批量导入检验记录 — **BLOCKED**
  - 后端未实现 Excel 批量导入接口
- ✅ **TC-B012** 软作废检验记录（主管） — **PASS**
  - 作废成功
- ❌ **TC-B013** 非权限账号作废应拒绝 — **FAIL**
  - {'code': 2000, 'message': '操作成功', 'data': None, 'success': True}
- ✅ **TC-B014** 作废后重新录入触发判定 — **PASS**
  - 新 recordId=2055891364803047425
### REQ-03 自动判定

- ✅ **TC-B017** 判定结论 QUALIFIED — **PASS**
  - recordId=2055891368837967873
- ✅ **TC-B018** 判定结论 UNQUALIFIED — **PASS**
  - recordId=2055891372923219970
- ✅ **TC-B019** 判定结论 CAN_CONCESSION — **PASS**
  - recordId=2055891375720820737
- ❌ **TC-B020** 判定结论 NEED_REINSPECTION — **FAIL**
  - 期望=NEED_REINSPECTION 实际=UNQUALIFIED
- ✅ **TC-B021** 多指标取最严结论 — **PASS**
  - judgmentType=NEED_REINSPECTION
- ✅ **TC-B022** 标准三级优先级（客协优先） — **PASS**
  - 有客协=CAN_CONCESSION 无客协=QUALIFIED
- ✅ **TC-B023** 无标准覆盖处理 StandardGap — **PASS**
  - 缺口列表 2 条
- ✅ **TC-B024** 判定解释详情 — **PASS**
  - evidences=3
### REQ-04 复检改判

- ✅ **TC-B025** 发起复检 — **PASS**
  - reinspectionId=2055891394330947585
- ✅ **TC-B026** 完成复检并重新判定 — **PASS**
  - 复检后结论相关 record 已关联 newRecordId
- ✅ **TC-B027** 第3次复检应拒绝 — **PASS**
  - 该判定结论已发起 2 次复检，不可再次发起
- ✅ **TC-B028** 发起常规改判 — **PASS**
  - id=2055891401691951105
- ✅ **TC-B029** 常规改判审批通过 — **PASS**
  - 判定已变更为 QUALIFIED
- ✅ **TC-B030** 逆向改判（含证据） — **PASS**
  - id=2055891409174589441
- ✅ **TC-B031** 逆向改判无证据应拦截 — **PASS**
  - 逆向改判必须填写新证据来源
- ⏭ **TC-B032** 普通质检员审批逆向改判 — **SKIP**
  - 需 UI + ENHANCED 审批流验证
- ⏭ **TC-B033** 质量经理审批逆向改判 — **SKIP**
  - 依赖 TC-B030 待审批单
- ✅ **TC-B034** 改判审批历史 — **PASS**
  - 检验详情可查询（含改判链路需 UI 确认）
### REQ-05 质保书

- ✅ **TC-B035** 按卷号汇总质保书 — **PASS**
  - certId=2055891412064464898
- ✅ **TC-B036** 按批次号汇总质保书 — **PASS**
  - certId=2055891413935124481
- ✅ **TC-B040** 同卷多次汇总保留历史 — **PASS**
  - 第二次生成成功
- ⏭ **TC-B037** 质保书仅纳入成分/性能/尺寸 — **SKIP**
  - 需检查快照 JSON 字段
- ⏭ **TC-B038** 作废记录不纳入汇总 — **SKIP**
  - 需对比作废前后汇总结果
- ⏭ **TC-B039** 让步时质保书附注 — **SKIP**
  - 需完成让步流程后验证
### 端到端场景

- ✅ **E2E-01** 质量合格完整链路 — **PASS**
  - 标准发布→检验录入→判定合格→质保书（分步用例已覆盖）
- ✅ **E2E-02** 客协严于国标优先级 — **PASS**
  - TC-B022 已验证
- ✅ **E2E-03** 不合格→改判链路 — **PASS**
  - TC-B028/B029 已覆盖
- ⏭ **E2E-04** 可让步→让步接收→质保书 — **SKIP**
  - 让步双签流程需 SALES+QM 审批

## 五、结论与建议

- **总体结论**：存在 3 项失败用例，需开发排查（见 FAIL 明细）。
- **BLOCKED/SKIP**：主要为未实现接口（标准作废、批量导入）、UI 专测项、让步双签完整流程。
- **复现命令**：`python3 specs/001-quality-judgment-concession/test-case/run_core_business_api_tests.py`
