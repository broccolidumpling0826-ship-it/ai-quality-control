# Quickstart: 集成测试场景

**Feature**: 001-quality-judgment-concession
**Date**: 2026-05-15

本文档定义端到端集成验证场景，用于确认各用户故事可独立测试。

---

## 场景 1：标准维护 + 自动判定（US1 MVP 验证）

**前提**: 数据库已有初始化数据（指标项目）

**步骤**:

```
1. POST /api/v1/auth/login
   → 获取 token（quality_engineer 账号）

2. POST /api/v1/standards
   Body: 新增客户协议标准（Q235B，抗拉强度 370–510 MPa，协议 C2025-001）
   → status=DRAFT, id=STANDARD-001

3. PUT /api/v1/standards/STANDARD-001/publish
   → status=PUBLISHED

4. POST /api/v1/inspections
   Body: coilNo=Z001, 抗拉强度=360（低于下限 370）
   → 自动判定结果: UNQUALIFIED

5. GET /api/v1/judgments/JUDGMENT-001/explanation
   → 查看判定解释：触发规则="实测值360 < 客户协议下限370（协议C2025-001）"

6. POST /api/v1/inspections
   Body: coilNo=Z002, 抗拉强度=420（在合格范围内）
   → 自动判定结果: QUALIFIED

7. POST /api/v1/inspections
   Body: coilNo=Z003, 抗拉强度=365（低于合格下限但高于让步下限）
   → 自动判定结果: CAN_CONCESSION（需已配置 concessionLower=360）

✅ 验收标准: Steps 4-7 各自返回正确 judgmentType，Step 5 返回触发规则描述
```

---

## 场景 2：复检 → 改判流程（US2 MVP 验证）

**前提**: 场景 1 的 Z001（UNQUALIFIED）数据存在

**步骤**:

```
1. POST /api/v1/reinspections
   Body: originalJudgmentId=JUDGMENT-Z001, reason="取样偏差，建议复检"
   → reinspectionId=RI-001

2. POST /api/v1/inspections
   Body: coilNo=Z001（复检），抗拉强度=390（合格）
   → 新建 InspectionRecord RECORD-Z001-2, 自动判定 QUALIFIED

3. PUT /api/v1/reinspections/RI-001/complete
   Body: { "newRecordId": "RECORD-Z001-2" }
   → 复检完成，新判定 QUALIFIED

4. POST /api/v1/rejudgments
   Body: originalJudgmentId=JUDGMENT-Z001（原UNQUALIFIED）, target=QUALIFIED
   → requestId=RJ-001, approvalLevel=NORMAL（非逆向改判）

5. PUT /api/v1/rejudgments/RJ-001/approve
   Body: { "action": "APPROVED", "comment": "复检合格，同意改判" }
   → 原判定 isFinal=0，新判定（QUALIFIED）isFinal=1

✅ 验收标准: Step 5 后 GET /judgment/record/RECORD-Z001 返回 judgmentType=QUALIFIED, isFinal=true
```

---

## 场景 3：逆向改判（US2 逆向改判验证）

**前提**: 场景 1 的 Z002（QUALIFIED）数据存在

**步骤**:

```
1. POST /api/v1/rejudgments
   Body: {
     originalJudgmentId=JUDGMENT-Z002（QUALIFIED）,
     targetJudgmentType: "UNQUALIFIED",
     newEvidenceSource: "后续工序缺陷",
     evidenceAttachmentUrl: "/files/evidence/proof.jpg"
   }
   → isReverse=true, approvalLevel=ENHANCED

2. PUT /api/v1/rejudgments/RJ-002/approve（quality_engineer 角色）
   → 403 FORBIDDEN（需 quality_manager）

3. PUT /api/v1/rejudgments/RJ-002/approve（quality_manager 角色）
   → 审批通过，原判定失效，新判定 UNQUALIFIED 生效

✅ 验收标准: Step 2 返回 4003，Step 3 成功
```

---

## 场景 4：让步接收完整流程（US3 验证）

**前提**: 场景 1 的 Z003（CAN_CONCESSION）数据存在

**步骤**:

```
1. POST /api/v1/concessions
   Body: { judgmentId=JUDGMENT-Z003, scope="...", riskDescription="...", expiryDate="2025-06-15" }
   → concessionId=CA-001

2. PUT /api/v1/concessions/CA-001/confirm（multipart）
   File: customer_email_screenshot.png
   confirmNote: "客户采购经理张三通过邮件确认"
   → confirm_status=CONFIRMED

3. PUT /api/v1/concessions/CA-001/confirm（再次尝试）
   → 4000: 客户确认附件已上传，如需更新请作废本让步申请

4. PUT /api/v1/concessions/CA-001/approve
   → approval_status=APPROVED

✅ 验收标准: Step 3 返回 4000 错误（附件不可替换），Step 4 成功
```

---

## 场景 5：质保书数据汇总（US4 验证）

**前提**: Z002 有最终判定结论 QUALIFIED

**步骤**:

```
1. POST /api/v1/cert-data/generate
   Body: { "queryType": "COIL", "coilNo": "Z002" }
   → 返回该卷所有 status=NORMAL 的检验值汇总 + 最终判定结论

2. GET /api/v1/statistics/overview?timeStart=2025-01-01&timeEnd=2025-12-31
   → 返回不合格率、复检率、让步率

✅ 验收标准: Step 1 返回完整检验指标快照（含 ≥1 个指标的实测值），Step 2 统计数据合理
```

---

## 快速启动命令

```bash
# 1. 建立 SSH 隧道（本地开发，连接远程云服务器的 MySQL 和 Redis）
ssh -L 3307:127.0.0.1:3306 -L 6380:127.0.0.1:6379 -N -f ubuntu@<YOUR_SERVER_IP>

# 2. 创建文件上传目录
mkdir -p /opt/quality-control/uploads/concession
mkdir -p /opt/quality-control/uploads/rejudgment
mkdir -p /opt/quality-control/uploads/temp

# 3. 后端启动（Spring Boot 单体）
cd backend
mvn spring-boot:run -Dspring.profiles.active=dev

# 4. 前端启动
cd frontend
npm install && npm run dev

# 5. 初始化测试数据
mysql -u root -p -h 127.0.0.1 -P 3307 quality_control < scripts/init-test-data.sql
```

## 关键配置参考（application-dev.yml）

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3307/quality_control?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&tinyInt1isBit=false
    username: ${DB_USER:root}
    password: ${DB_PASSWORD}
  redis:
    host: 127.0.0.1
    port: 6380
    password: ${REDIS_PASSWORD:}
    database: 0

# 本地文件存储配置
app:
  upload:
    base-path: /opt/quality-control/uploads
    url-prefix: /api/v1/files
    max-size: 20MB          # 单文件最大 20MB
```
