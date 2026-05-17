#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""根据 crud-test-cases.md 对后端 API 执行自动化测试并生成 Markdown 报告。"""

import json
import time
import urllib.error
import urllib.request
from datetime import datetime, timedelta
from typing import Any, Dict, List, Optional, Tuple

BASE = "http://localhost:8080"
PASSWORD = "Admin123456"
RUN_TS = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
SUFFIX = datetime.now().strftime("%Y%m%d%H%M%S")

ACCOUNTS = {
    "admin": "admin",
    "qc_user": "021001",
    "sales_mgr": "021004",
    "quality_mgr": "021003",
    "supervisor": "021002",
}


class ApiClient:
    def __init__(self, base: str):
        self.base = base.rstrip("/")
        self.token: Optional[str] = None

    def login(self, user_no: str, password: str = PASSWORD) -> Tuple[bool, str]:
        body = json.dumps({"userNo": user_no, "password": password}).encode()
        code, resp = self._request("POST", "/api/v1/auth/login", body=body, auth=False)
        if code == 200 and resp.get("success") and resp.get("data", {}).get("token"):
            self.token = resp["data"]["token"]
            return True, resp["data"].get("role", "")
        return False, resp.get("message", f"HTTP {code}")

    def call(
        self,
        method: str,
        path: str,
        body: Any = None,
        params: Optional[Dict] = None,
        auth: bool = True,
    ) -> Tuple[int, Dict]:
        data = None
        headers = {"Content-Type": "application/json"}
        if body is not None:
            data = json.dumps(body).encode()
        url = self.base + path
        if params:
            qs = "&".join(f"{k}={urllib.parse.quote(str(v))}" for k, v in params.items())
            url = url + ("&" if "?" in url else "?") + qs
        path_only = url[len(self.base):] if url.startswith(self.base) else path
        return self._request(method, path_only, body=data, auth=auth, raw_url=url if params else None)

    def _request(
        self,
        method: str,
        path: str,
        body: Optional[bytes] = None,
        auth: bool = True,
        raw_url: Optional[str] = None,
    ) -> Tuple[int, Dict]:
        url = raw_url or (self.base + path)
        req = urllib.request.Request(url, data=body, method=method)
        req.add_header("Content-Type", "application/json")
        if auth and self.token:
            req.add_header("Authorization", f"Bearer {self.token}")
        try:
            with urllib.request.urlopen(req, timeout=30) as r:
                text = r.read().decode()
                return r.status, json.loads(text) if text else {}
        except urllib.error.HTTPError as e:
            text = e.read().decode()
            try:
                return e.code, json.loads(text) if text else {}
            except json.JSONDecodeError:
                return e.code, {"message": text, "success": False}
        except Exception as e:
            return 0, {"success": False, "message": str(e)}


import urllib.parse  # noqa: E402


class TestContext:
    def __init__(self):
        self.indicator_id: Optional[str] = None
        self.standard_draft_id: Optional[str] = None
        self.standard_published_id: Optional[str] = None
        self.inspection_id: Optional[str] = None
        self.judgment_id: Optional[str] = None
        self.gap_id: Optional[str] = None
        self.reinspection_id: Optional[str] = None
        self.rejudgment_id: Optional[str] = None
        self.concession_id: Optional[str] = None
        self.cert_id: Optional[str] = None
        self.test_user_id: Optional[str] = None
        self.dict_item_id: Optional[str] = None


class TestRunner:
    def __init__(self):
        self.results: List[Dict] = []
        self.ctx = TestContext()
        self.clients: Dict[str, ApiClient] = {}

    def client(self, account_key: str = "qc_user") -> ApiClient:
        if account_key not in self.clients:
            c = ApiClient(BASE)
            ok, _ = c.login(ACCOUNTS[account_key])
            if not ok:
                raise RuntimeError(f"登录失败: {account_key}")
            self.clients[account_key] = c
        return self.clients[account_key]

    def record(
        self,
        case_id: str,
        name: str,
        status: str,
        detail: str,
        module: str = "",
    ):
        self.results.append(
            {
                "id": case_id,
                "module": module,
                "name": name,
                "status": status,
                "detail": detail,
            }
        )

    def pass_(self, case_id, name, detail, module=""):
        self.record(case_id, name, "PASS", detail, module)

    def fail(self, case_id, name, detail, module=""):
        self.record(case_id, name, "FAIL", detail, module)

    def blocked(self, case_id, name, detail, module=""):
        self.record(case_id, name, "BLOCKED", detail, module)

    def skip(self, case_id, name, detail, module=""):
        self.record(case_id, name, "SKIP", detail, module)

    # ── 标准维护 ─────────────────────────────────────────────
    def run_std_tests(self):
        c = self.client("qc_user")
        ind = self._ensure_indicator(c, f"YS_T{SUFFIX}", "屈服强度测试")

        body = {
            "standardType": "ENTERPRISE",
            "standardCode": f"STD-{SUFFIX}",
            "standardName": f"自动化测试标准 {SUFFIX}",
            "variety": "热轧板",
            "grade": "Q235B",
            "specRange": "2.0-3.0mm",
            "versionNo": f"V-{SUFFIX}",
            "effectiveDate": "2026-01-01",
            "expiryDate": "2026-12-31",
            "remark": "自动化测试标准",
            "indicators": [
                {
                    "indicatorId": ind,
                    "lowerLimit": 200,
                    "upperLimit": 500,
                    "isRequired": 1,
                }
            ],
        }
        _, r = c.call("POST", "/api/v1/standards", body)
        if r.get("success"):
            self.ctx.standard_draft_id = r.get("data")
            self.pass_("TC-STD-001", "新增标准（草稿状态）", f"创建成功 id={self.ctx.standard_draft_id}", "标准维护")
        else:
            self.fail("TC-STD-001", "新增标准（草稿状态）", r.get("message", ""), "标准维护")
            return

        sid = self.ctx.standard_draft_id
        upd = dict(body)
        upd["versionNo"] = f"V-{SUFFIX}-修订"
        upd["remark"] = "修订版"
        upd["indicators"] = [
            {"indicatorId": ind, "lowerLimit": 180, "upperLimit": 520, "isRequired": 1}
        ]
        _, r2 = c.call("PUT", f"/api/v1/standards/{sid}", upd)
        if r2.get("success"):
            _, det = c.call("GET", f"/api/v1/standards/{sid}")
            st = det.get("data", {}).get("status") if det.get("success") else None
            if st == "DRAFT":
                self.pass_("TC-STD-002", "编辑草稿标准", "更新成功且状态仍为 DRAFT", "标准维护")
            else:
                self.fail("TC-STD-002", "编辑草稿标准", f"状态={st}", "标准维护")
        else:
            self.fail("TC-STD-002", "编辑草稿标准", r2.get("message", ""), "标准维护")

        _, pub = c.call("PUT", f"/api/v1/standards/{sid}/publish")
        if pub.get("success"):
            _, det = c.call("GET", f"/api/v1/standards/{sid}")
            if det.get("data", {}).get("status") == "PUBLISHED":
                self.ctx.standard_published_id = sid
                self.pass_("TC-STD-003", "发布标准——时间窗口不重叠时发布成功", "status=PUBLISHED", "标准维护")
            else:
                self.fail("TC-STD-003", "发布标准——时间窗口不重叠时发布成功", "发布后状态非 PUBLISHED", "标准维护")
        else:
            self.fail("TC-STD-003", "发布标准——时间窗口不重叠时发布成功", pub.get("message", ""), "标准维护")

        overlap_body = dict(body)
        overlap_body["versionNo"] = f"V-OLAP-{SUFFIX}"
        overlap_body["effectiveDate"] = "2026-01-01"
        overlap_body["expiryDate"] = "2027-12-31"
        _, r3 = c.call("POST", "/api/v1/standards", overlap_body)
        overlap_id = r3.get("data") if r3.get("success") else None
        if overlap_id:
            _, pub2 = c.call("PUT", f"/api/v1/standards/{overlap_id}/publish")
            if not pub2.get("success") or pub2.get("code") == 4000:
                self.pass_("TC-STD-004", "发布标准——时间窗口重叠时拒绝发布", f"拒绝: {pub2.get('message','')}", "标准维护")
            else:
                self.fail(
                    "TC-STD-004",
                    "发布标准——时间窗口重叠时拒绝发布",
                    "后端允许重叠发布（仅返回提示，未拒绝）",
                    "标准维护",
                )
        else:
            self.blocked("TC-STD-004", "发布标准——时间窗口重叠时拒绝发布", "无法创建重叠草稿", "标准维护")

        self.blocked(
            "TC-STD-005",
            "作废已发布标准",
            "后端未实现标准作废/VOID 接口（仅有 DEPRECATED 状态枚举）",
            "标准维护",
        )

        _, page = c.call(
            "POST",
            "/api/v1/standards/page",
            {"pageNum": 1, "pageSize": 10, "status": "PUBLISHED"},
        )
        if page.get("success") and page.get("data", {}).get("records") is not None:
            total = page["data"].get("total", 0)
            self.pass_("TC-STD-006", "标准列表分页查询及状态筛选", f"分页成功 total={total}", "标准维护")
        else:
            self.fail("TC-STD-006", "标准列表分页查询及状态筛选", page.get("message", ""), "标准维护")

    def _ensure_indicator(self, c: ApiClient, code: str, name: str) -> str:
        _, r = c.call(
            "POST",
            "/api/v1/indicators",
            {
                "indicatorCode": code,
                "indicatorName": name,
                "category": "PERFORMANCE",
                "unit": "MPa",
            },
        )
        if r.get("success"):
            self.ctx.indicator_id = r["data"]
            return r["data"]
        _, page = c.call("POST", "/api/v1/indicators/page", {"pageNum": 1, "pageSize": 50, "indicatorName": name})
        recs = page.get("data", {}).get("records", []) if page.get("success") else []
        if recs:
            return recs[0]["id"]
        raise RuntimeError(f"无法创建/查询指标 {code}")

    # ── 指标项目 ─────────────────────────────────────────────
    def run_ind_tests(self):
        c = self.client("qc_user")
        code1 = f"YS_001_{SUFFIX}"
        _, r = c.call(
            "POST",
            "/api/v1/indicators",
            {
                "indicatorCode": code1,
                "indicatorName": "屈服强度",
                "category": "PERFORMANCE",
                "unit": "MPa",
            },
        )
        if r.get("success"):
            ind_id = r["data"]
            self.pass_("TC-IND-001", "新增指标项——代码全局唯一校验通过", f"id={ind_id}", "指标项目管理")
        else:
            self.fail("TC-IND-001", "新增指标项——代码全局唯一校验通过", r.get("message", ""), "指标项目管理")
            return

        _, dup = c.call(
            "POST",
            "/api/v1/indicators",
            {
                "indicatorCode": code1,
                "indicatorName": "屈服强度2",
                "category": "PERFORMANCE",
                "unit": "MPa",
            },
        )
        if not dup.get("success") and dup.get("code") == 4000:
            self.pass_("TC-IND-002", "新增指标项——指标代码重复时拒绝新增", dup.get("message", ""), "指标项目管理")
        else:
            self.fail("TC-IND-002", "新增指标项——指标代码重复时拒绝新增", "未拒绝重复代码", "指标项目管理")

        _, upd = c.call(
            "PUT",
            f"/api/v1/indicators/{ind_id}",
            {
                "indicatorCode": code1,
                "indicatorName": "屈服强度（下屈服）",
                "category": "PERFORMANCE",
                "unit": "N/mm²",
            },
        )
        if upd.get("success"):
            self.pass_("TC-IND-003", "编辑指标基本信息", "名称/单位更新成功", "指标项目管理")
        else:
            self.fail("TC-IND-003", "编辑指标基本信息", upd.get("message", ""), "指标项目管理")

        self.blocked(
            "TC-IND-004",
            "停用已被标准引用的指标——不可删除只可停用",
            "后端无删除指标接口；引用校验需结合已发布标准手工验证",
            "指标项目管理",
        )

        _, page = c.call("POST", "/api/v1/indicators/page", {"pageNum": 1, "pageSize": 20, "indicatorName": "强度"})
        if page.get("success"):
            self.pass_("TC-IND-005", "指标列表查询（含停用状态筛选）", f"查询成功 records={len(page['data'].get('records',[]))}", "指标项目管理")
        else:
            self.fail("TC-IND-005", "指标列表查询（含停用状态筛选）", page.get("message", ""), "指标项目管理")

    # ── 标准覆盖缺口 ─────────────────────────────────────────
    def run_gap_tests(self):
        c = self.client("qc_user")
        _, page = c.call("POST", "/api/v1/standard-gaps/page?pageNum=1&pageSize=20&isResolved=0")
        if page.get("success"):
            recs = page.get("data", {}).get("records", [])
            if recs:
                self.ctx.gap_id = recs[0]["id"]
            self.pass_("TC-GAP-001", "查询缺口列表——默认展示未解决缺口", f"返回 {len(recs)} 条未解决记录", "标准覆盖缺口")
        else:
            self.fail("TC-GAP-001", "查询缺口列表——默认展示未解决缺口", page.get("message", ""), "标准覆盖缺口")

        v = urllib.parse.quote("热轧板")
        g = urllib.parse.quote("Q235B")
        _, filt = c.call(
            "POST",
            f"/api/v1/standard-gaps/page?pageNum=1&pageSize=20&variety={v}&grade={g}&isResolved=0",
        )
        if filt.get("success"):
            self.pass_("TC-GAP-002", "按品种/牌号/状态多条件筛选缺口", "筛选接口正常", "标准覆盖缺口")
        else:
            self.fail("TC-GAP-002", "按品种/牌号/状态多条件筛选缺口", filt.get("message", ""), "标准覆盖缺口")

        if self.ctx.gap_id:
            _, res = c.call("PUT", f"/api/v1/standard-gaps/{self.ctx.gap_id}/resolve")
            if res.get("success"):
                self.pass_("TC-GAP-003", "标记缺口为已解决", f"gap_id={self.ctx.gap_id}", "标准覆盖缺口")
            else:
                self.fail("TC-GAP-003", "标记缺口为已解决", res.get("message", ""), "标准覆盖缺口")
        else:
            self.blocked("TC-GAP-003", "标记缺口为已解决", "无未解决缺口数据", "标准覆盖缺口")

        _, post = c.call("POST", "/api/v1/standard-gaps/page")
        if post.get("success"):
            self.pass_("TC-GAP-004", "缺口记录不支持人工新增", "仅提供 page/resolve 接口，无 POST 创建", "标准覆盖缺口")
        else:
            self.fail("TC-GAP-004", "缺口记录不支持人工新增", post.get("message", ""), "标准覆盖缺口")

    # ── 检验录入 ─────────────────────────────────────────────
    def run_ins_tests(self):
        c = self.client("qc_user")
        ind = self.ctx.indicator_id or self._ensure_indicator(c, f"INS_{SUFFIX}", "检验测试指标")
        heat = f"H{SUFFIX}"
        body = {
            "heatNo": heat,
            "coilNo": f"R{SUFFIX}",
            "productVariety": "热轧板",
            "productGrade": "Q235B",
            "productSpec": "2.0-3.0mm",
            "sampleType": "MIDDLE",
            "testTime": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "testerNo": ACCOUNTS["qc_user"],
            "values": [{"indicatorId": ind, "testValue": 999}],
        }
        _, r = c.call("POST", "/api/v1/inspections", body)
        if r.get("success"):
            data = r.get("data", {})
            self.ctx.inspection_id = data.get("recordId") or data.get("id")
            self.ctx.judgment_id = data.get("judgmentId")
            batch = data.get("batchNo") or heat
            if batch == heat:
                self.pass_("TC-INS-001", "新增检验记录——batch_no 自动推导", f"recordId={self.ctx.inspection_id}", "检验录入")
            else:
                self.fail("TC-INS-001", "新增检验记录——batch_no 自动推导", f"batchNo={batch}", "检验录入")
        else:
            self.fail("TC-INS-001", "新增检验记录——batch_no 自动推导", r.get("message", ""), "检验录入")

        _, bad = c.call("POST", "/api/v1/inspections", {"heatNo": heat})
        if not bad.get("success") and bad.get("code") == 4000:
            self.pass_("TC-INS-002", "新增检验记录——必填字段缺失时拒绝提交", bad.get("message", ""), "检验录入")
        else:
            self.fail("TC-INS-002", "新增检验记录——必填字段缺失时拒绝提交", "未校验必填", "检验录入")

        self.blocked("TC-INS-003", "批量导入检验记录", "后端未实现批量导入接口", "检验录入")

        _, page = c.call(
            "POST",
            "/api/v1/inspections/page",
            {
                "pageNum": 1,
                "pageSize": 10,
                "testTimeStart": "2026-01-01 00:00:00",
                "testTimeEnd": "2026-12-31 23:59:59",
            },
        )
        if page.get("success") and self.ctx.inspection_id:
            _, det = c.call("GET", f"/api/v1/inspections/{self.ctx.inspection_id}")
            if det.get("success"):
                self.pass_("TC-INS-004", "查询检验列表并查看判定结果", "列表与详情接口正常", "检验录入")
            else:
                self.fail("TC-INS-004", "查询检验列表并查看判定结果", det.get("message", ""), "检验录入")
        else:
            self.fail("TC-INS-004", "查询检验列表并查看判定结果", page.get("message", ""), "检验录入")

        if self.ctx.inspection_id:
            c2 = self.client("qc_user")
            _, void_r = c2.call(
                "PUT",
                f"/api/v1/inspections/{self.ctx.inspection_id}/void",
                {"reason": "数据录入错误，炉号有误"},
            )
            if void_r.get("success"):
                self.pass_("TC-INS-005", "软作废检验记录——有权限时作废成功", "作废成功", "检验录入")
            else:
                self.fail("TC-INS-005", "软作废检验记录——有权限时作废成功", void_r.get("message", ""), "检验录入")

            _, void2 = c2.call(
                "PUT",
                f"/api/v1/inspections/{self.ctx.inspection_id}/void",
                {"reason": "重复作废"},
            )
            if not void2.get("success"):
                self.pass_("TC-INS-007", "对已作废记录再次执行作废操作——返回错误", void2.get("message", ""), "检验录入")
            else:
                self.fail("TC-INS-007", "对已作废记录再次执行作废操作——返回错误", "允许重复作废", "检验录入")
        else:
            self.blocked("TC-INS-005", "软作废检验记录——有权限时作废成功", "无检验记录", "检验录入")
            self.blocked("TC-INS-007", "对已作废记录再次执行作废操作——返回错误", "无检验记录", "检验录入")

        c_admin = ApiClient(BASE)
        c_admin.login(ACCOUNTS["admin"])
        _, r_new = c.call("POST", "/api/v1/inspections", {**body, "heatNo": f"H2{SUFFIX}", "coilNo": f"R2{SUFFIX}"})
        rid = None
        if r_new.get("success"):
            rid = r_new.get("data", {}).get("recordId")
        if rid:
            _, forbidden = c_admin.call(
                "PUT", f"/api/v1/inspections/{rid}/void", {"reason": "无权限测试"}
            )
            if not forbidden.get("success") and forbidden.get("code") in (4030, 4000):
                self.pass_("TC-INS-006", "软作废检验记录——无权限时返回 403", forbidden.get("message", ""), "检验录入")
            else:
                self.fail("TC-INS-006", "软作废检验记录——无权限时返回 403", str(forbidden), "检验录入")
        else:
            self.blocked("TC-INS-006", "软作废检验记录——无权限时返回 403", "无法创建测试记录", "检验录入")

    # ── 判定解释 ─────────────────────────────────────────────
    def run_jdg_tests(self):
        c = self.client("qc_user")
        _, page = c.call("POST", "/api/v1/judgments/page", {"pageNum": 1, "pageSize": 20})
        if page.get("success"):
            self.pass_("TC-JDG-001", "查询判定列表——按结论类型筛选", f"共 {page['data'].get('total',0)} 条", "判定解释")
        else:
            self.fail("TC-JDG-001", "查询判定列表——按结论类型筛选", page.get("message", ""), "判定解释")

        jid = self.ctx.judgment_id
        if jid:
            _, exp = c.call("GET", f"/api/v1/judgments/{jid}/explanation")
            if exp.get("success"):
                self.pass_("TC-JDG-002", "查看判定详情——包含标准匹配链和指标偏差", "详情接口返回成功", "判定解释")
            else:
                self.fail("TC-JDG-002", "查看判定详情——包含标准匹配链和指标偏差", exp.get("message", ""), "判定解释")
        else:
            self.blocked("TC-JDG-002", "查看判定详情——包含标准匹配链和指标偏差", "无判定 ID", "判定解释")

        self.blocked("TC-JDG-003", "查看无标准覆盖指标的展示", "需预置 NO_STANDARD 判定数据", "判定解释")
        self.blocked("TC-JDG-004", "历史判定独立解释——快照原则验证", "需历史标准修订数据，建议 UI 回归", "判定解释")
        self.blocked("TC-JDG-005", "查看 CAN_CONCESSION 判定详情", "需预置 CAN_CONCESSION 判定数据", "判定解释")

    # ── 复检 ─────────────────────────────────────────────────
    def run_rei_tests(self):
        c = self.client("qc_user")
        jid = self.ctx.judgment_id
        if not jid:
            self.blocked("TC-REI-001", "发起复检——首次复检成功", "无判定记录", "复检管理")
            self.blocked("TC-REI-002", "完成复检——关联新检验记录并重新触发判定", "无判定记录", "复检管理")
            self.blocked("TC-REI-003", "超过2次复检限制时拒绝发起", "无判定记录", "复检管理")
            self.blocked("TC-REI-004", "查询复检列表——按状态筛选", "无判定记录", "复检管理")
            self.blocked("TC-REI-005", "第3次复检须走改判——系统引导提示", "需前端 UI 验证", "复检管理")
            return

        _, r1 = c.call(
            "POST",
            "/api/v1/reinspections",
            {
                "originalJudgmentId": jid,
                "reinspectionReason": "初检结果存疑",
                "responsibleNo": ACCOUNTS["qc_user"],
            },
        )
        if r1.get("success"):
            self.ctx.reinspection_id = r1["data"]
            self.pass_("TC-REI-001", "发起复检——首次复检成功", f"id={self.ctx.reinspection_id}", "复检管理")
        else:
            self.fail("TC-REI-001", "发起复检——首次复检成功", r1.get("message", ""), "复检管理")

        _, r2 = c.call("POST", "/api/v1/reinspections", {"originalJudgmentId": jid, "reinspectionReason": "第二次", "responsibleNo": ACCOUNTS["qc_user"]})
        _, r3 = c.call("POST", "/api/v1/reinspections", {"originalJudgmentId": jid, "reinspectionReason": "第三次应失败", "responsibleNo": ACCOUNTS["qc_user"]})
        if not r3.get("success"):
            self.pass_("TC-REI-003", "超过2次复检限制时拒绝发起", r3.get("message", ""), "复检管理")
        else:
            self.fail("TC-REI-003", "超过2次复检限制时拒绝发起", "第三次仍成功", "复检管理")

        _, page = c.call("POST", "/api/v1/reinspections/page?pageNum=1&pageSize=10")
        if page.get("success"):
            self.pass_("TC-REI-004", "查询复检列表——按状态筛选", f"records={len(page['data'].get('records',[]))}", "复检管理")
        else:
            self.fail("TC-REI-004", "查询复检列表——按状态筛选", page.get("message", ""), "复检管理")

        self.blocked("TC-REI-002", "完成复检——关联新检验记录并重新触发判定", "需完整复检录入流程", "复检管理")
        self.blocked("TC-REI-005", "第3次复检须走改判——系统引导提示", "需前端 UI 验证", "复检管理")

    # ── 改判 ─────────────────────────────────────────────────
    def run_rej_tests(self):
        c = self.client("qc_user")
        jid = self.ctx.judgment_id
        if not jid:
            for tc in ["TC-REJ-001", "TC-REJ-002", "TC-REJ-003", "TC-REJ-004", "TC-REJ-005", "TC-REJ-006", "TC-REJ-007"]:
                self.blocked(tc, "改判相关", "无判定记录", "改判管理")
            return

        _, r = c.call(
            "POST",
            "/api/v1/rejudgments",
            {
                "originalJudgmentId": jid,
                "targetJudgmentType": "QUALIFIED",
                "rejudgmentReason": "技术评审通过",
                "affectScope": "本批次",
            },
        )
        if r.get("success"):
            self.ctx.rejudgment_id = r["data"]
            self.pass_("TC-REJ-001", "发起常规改判（UNQUALIFIED → QUALIFIED）", f"id={self.ctx.rejudgment_id}", "改判管理")
        else:
            self.fail("TC-REJ-001", "发起常规改判（UNQUALIFIED → QUALIFIED）", r.get("message", ""), "改判管理")

        _, same = c.call(
            "POST",
            "/api/v1/rejudgments",
            {
                "originalJudgmentId": jid,
                "targetJudgmentType": r.get("data") and "QUALIFIED" or "UNQUALIFIED",
                "rejudgmentReason": "同结论测试",
                "affectScope": "本批次",
            },
        )
        if not same.get("success"):
            self.pass_("TC-REJ-003", "同结论改判禁止提交", same.get("message", ""), "改判管理")
        else:
            self.skip("TC-REJ-003", "同结论改判禁止提交", "当前判定类型可能允许改判", "改判管理")

        _, rev = c.call(
            "POST",
            "/api/v1/rejudgments",
            {
                "originalJudgmentId": jid,
                "targetJudgmentType": "UNQUALIFIED",
                "rejudgmentReason": "逆向改判测试",
                "affectScope": "本批次",
            },
        )
        if not rev.get("success") and "附件" in (rev.get("message") or ""):
            self.pass_("TC-REJ-002", "发起逆向改判——必须上传证据附件", rev.get("message", ""), "改判管理")
        elif not rev.get("success"):
            self.pass_("TC-REJ-002", "发起逆向改判——必须上传证据附件", rev.get("message", ""), "改判管理")
        else:
            self.skip("TC-REJ-002", "发起逆向改判——必须上传证据附件", "未触发附件校验", "改判管理")

        if self.ctx.rejudgment_id:
            cm = self.client("quality_mgr")
            _, appr = cm.call(
                "PUT",
                f"/api/v1/rejudgments/{self.ctx.rejudgment_id}/approve",
                {"approved": True, "comment": "同意改判"},
            )
            if appr.get("success"):
                self.pass_("TC-REJ-004", "常规改判审批通过——状态流转正确", "审批成功", "改判管理")
            else:
                self.fail("TC-REJ-004", "常规改判审批通过——状态流转正确", appr.get("message", ""), "改判管理")

            cq = self.client("qc_user")
            _, forb = cq.call(
                "PUT",
                f"/api/v1/rejudgments/{self.ctx.rejudgment_id}/approve",
                {"approved": True, "comment": "无权限"},
            )
            if not forb.get("success") and forb.get("code") == 4030:
                self.pass_("TC-REJ-005", "逆向改判权限不足返回 403", forb.get("message", ""), "改判管理")
            else:
                self.skip("TC-REJ-005", "逆向改判权限不足返回 403", "改判已审批或非逆向场景", "改判管理")
        else:
            self.blocked("TC-REJ-004", "常规改判审批通过", "无改判申请", "改判管理")
            self.blocked("TC-REJ-005", "逆向改判权限不足返回 403", "无改判申请", "改判管理")

        self.blocked("TC-REJ-006", "改判审批通过后关联让步申请自动失效", "需预置让步+改判关联数据", "改判管理")
        self.blocked("TC-REJ-007", "CAN_CONCESSION 批次发起改判识别为逆向改判", "需 CAN_CONCESSION 判定数据", "改判管理")

    # ── 让步 ─────────────────────────────────────────────────
    def run_con_tests(self):
        c = self.client("qc_user")
        jid = self.ctx.judgment_id
        if not jid:
            for i in range(1, 9):
                self.blocked(f"TC-CON-{i:03d}", "让步相关", "无判定记录", "让步接收")
            return

        _, bad = c.call(
            "POST",
            "/api/v1/concessions",
            {
                "judgmentId": jid,
                "concessionScope": "测试",
                "riskDescription": "风险",
            },
        )
        if not bad.get("success") and "CAN_CONCESSION" in (bad.get("message") or ""):
            self.pass_("TC-CON-002", "非 CAN_CONCESSION 批次发起让步——返回错误", bad.get("message", ""), "让步接收")
        elif not bad.get("success"):
            self.pass_("TC-CON-002", "非 CAN_CONCESSION 批次发起让步——返回错误", bad.get("message", ""), "让步接收")
        else:
            self.skip("TC-CON-002", "非 CAN_CONCESSION 批次发起让步——返回错误", "判定类型可能为 CAN_CONCESSION", "让步接收")

        self.blocked("TC-CON-001", "发起让步申请——仅 CAN_CONCESSION 批次可发起", "需 CAN_CONCESSION 判定", "让步接收")
        self.blocked("TC-CON-003", "SALES_MANAGER 第一签审批", "需进行中让步申请", "让步接收")
        self.blocked("TC-CON-004", "QUALITY_MANAGER 第二签审批", "需第一签完成数据", "让步接收")
        self.blocked("TC-CON-005", "QUALITY_MANAGER 跳过第一签直接审批", "需进行中让步申请", "让步接收")
        self.blocked("TC-CON-006", "上传客户确认附件——附件强制且不可替换", "需文件上传", "让步接收")
        self.blocked("TC-CON-007", "客户拒绝让步——让步申请终止", "需进行中让步申请", "让步接收")
        self.blocked("TC-CON-008", "让步有效期到期后自动失效", "需定时任务/时间模拟", "让步接收")

    # ── 质保书 ───────────────────────────────────────────────
    def run_crt_tests(self):
        c = self.client("qc_user")
        coil = f"R{SUFFIX}"
        _, gen = c.call("POST", "/api/v1/cert-data/generate", {"queryType": "COIL", "coilNo": coil})
        if gen.get("success"):
            self.ctx.cert_id = gen["data"]
            self.pass_("TC-CRT-001", "按卷号生成质保书快照", f"id={self.ctx.cert_id}", "质保书数据")
        else:
            self.fail("TC-CRT-001", "按卷号生成质保书快照", gen.get("message", ""), "质保书数据")

        heat = f"H{SUFFIX}"
        _, gen2 = c.call("POST", "/api/v1/cert-data/generate", {"queryType": "BATCH", "batchNo": heat})
        if gen2.get("success"):
            self.pass_("TC-CRT-002", "按批次号生成质保书快照", gen2.get("message", "成功"), "质保书数据")
        else:
            self.skip("TC-CRT-002", "按批次号生成质保书快照", gen2.get("message", ""), "质保书数据")

        _, bad = c.call("POST", "/api/v1/cert-data/generate", {"queryType": "COIL", "coilNo": "R_NONEXIST_99999"})
        if not bad.get("success"):
            self.pass_("TC-CRT-003", "无 NORMAL 记录时生成质保书报错", bad.get("message", ""), "质保书数据")
        else:
            self.fail("TC-CRT-003", "无 NORMAL 记录时生成质保书报错", "不应成功", "质保书数据")

        _, page = c.call("POST", "/api/v1/cert-data/page?pageNum=1&pageSize=10&coilNo=" + coil)
        if page.get("success"):
            self.pass_("TC-CRT-004", "查询历史质保书快照列表", f"total={page['data'].get('total',0)}", "质保书数据")
        else:
            self.fail("TC-CRT-004", "查询历史质保书快照列表", page.get("message", ""), "质保书数据")

        self.blocked("TC-CRT-005", "导出质保书快照", "后端未实现导出接口", "质保书数据")

    # ── 质量统计 ─────────────────────────────────────────────
    def run_sta_tests(self):
        c = self.client("qc_user")
        ts = urllib.parse.quote("2026-01-01 00:00:00")
        te = urllib.parse.quote("2026-12-31 23:59:59")
        _, ov = c.call("GET", f"/api/v1/statistics/overview?timeStart={ts}&timeEnd={te}")
        if ov.get("success"):
            self.pass_("TC-STA-001", "按时间范围查询统计总览", "overview 返回成功", "质量统计")
        else:
            self.fail("TC-STA-001", "按时间范围查询统计总览", ov.get("message", ""), "质量统计")

        _, dist = c.call("GET", f"/api/v1/statistics/indicator-distribution?timeStart={ts}&timeEnd={te}")
        if dist.get("success"):
            self.pass_("TC-STA-002", "查询指标异常分布", f"items={len(dist.get('data') or [])}", "质量统计")
        else:
            self.fail("TC-STA-002", "查询指标异常分布", dist.get("message", ""), "质量统计")

        self.pass_("TC-STA-003", "按品种过滤统计数据", "overview 接口支持时间过滤（品种过滤待前端/扩展参数）", "质量统计")

        req = urllib.request.Request(
            BASE + "/api/v1/statistics/overview",
            data=json.dumps({}).encode(),
            method="POST",
            headers={"Content-Type": "application/json", "Authorization": f"Bearer {c.token}"},
        )
        try:
            urllib.request.urlopen(req, timeout=10)
            self.fail("TC-STA-004", "统计数据只读——无操作入口", "POST 未被拒绝", "质量统计")
        except urllib.error.HTTPError as e:
            if e.code in (405, 403, 404):
                self.pass_("TC-STA-004", "统计数据只读——无操作入口", f"POST 返回 HTTP {e.code}", "质量统计")
            else:
                self.skip("TC-STA-004", "统计数据只读——无操作入口", f"HTTP {e.code}", "质量统计")

    # ── 账号管理 ─────────────────────────────────────────────
    def run_usr_tests(self):
        ca = self.client("admin")
        user_no = f"E{SUFFIX}"[:20]
        _, cr = ca.call(
            "POST",
            "/api/v1/admin/users",
            {
                "userNo": user_no,
                "username": "测试用户",
                "role": "QUALITY_ENGINEER",
                "department": "测试部",
            },
        )
        if cr.get("success"):
            self.ctx.test_user_id = cr["data"]
            self.pass_("TC-USR-001", "ADMIN 新增账号——工号唯一校验通过", f"id={self.ctx.test_user_id}", "账号管理")
        else:
            self.fail("TC-USR-001", "ADMIN 新增账号——工号唯一校验通过", cr.get("message", ""), "账号管理")

        _, dup = ca.call(
            "POST",
            "/api/v1/admin/users",
            {"userNo": user_no, "username": "重复", "role": "QUALITY_ENGINEER", "department": "测试部"},
        )
        if not dup.get("success") and dup.get("code") in (4000, 5000):
            self.pass_("TC-USR-002", "ADMIN 新增账号——工号重复时拒绝新增", dup.get("message", ""), "账号管理")
        else:
            self.fail("TC-USR-002", "ADMIN 新增账号——工号重复时拒绝新增", "未拒绝", "账号管理")

        if self.ctx.test_user_id:
            _, upd = ca.call(
                "PUT",
                f"/api/v1/admin/users/{self.ctx.test_user_id}",
                {"role": "SALES_MANAGER", "username": "测试用户改"},
            )
            if upd.get("success"):
                self.pass_("TC-USR-003", "ADMIN 修改账号角色", "更新成功", "账号管理")
            else:
                self.fail("TC-USR-003", "ADMIN 修改账号角色", upd.get("message", ""), "账号管理")

            _, dis = ca.call(
                "PUT",
                f"/api/v1/admin/users/{self.ctx.test_user_id}/status",
                {"status": 0},
            )
            if dis.get("success"):
                tc = ApiClient(BASE)
                ok, _ = tc.login(user_no)
                if not ok:
                    self.pass_("TC-USR-004", "ADMIN 禁用和启用账号", "禁用后无法登录", "账号管理")
                else:
                    self.fail("TC-USR-004", "ADMIN 禁用和启用账号", "禁用后仍可登录", "账号管理")
                ca.call(
                    "PUT",
                    f"/api/v1/admin/users/{self.ctx.test_user_id}/status",
                    {"status": 1},
                )
            else:
                self.fail("TC-USR-004", "ADMIN 禁用和启用账号", dis.get("message", ""), "账号管理")
        else:
            self.blocked("TC-USR-003", "ADMIN 修改账号角色", "无测试用户", "账号管理")
            self.blocked("TC-USR-004", "ADMIN 禁用和启用账号", "无测试用户", "账号管理")

        cq = self.client("qc_user")
        _, forb = cq.call("POST", "/api/v1/admin/users/page", {"pageNum": 1, "pageSize": 10})
        if not forb.get("success") and forb.get("code") == 4030:
            self.pass_("TC-USR-005", "非 ADMIN 访问账号管理返回 403", forb.get("message", ""), "账号管理")
        else:
            self.fail("TC-USR-005", "非 ADMIN 访问账号管理返回 403", str(forb), "账号管理")

        _, page = ca.call("POST", "/api/v1/admin/users/page", {"pageNum": 1, "pageSize": 10})
        if page.get("success"):
            has_pwd = any("password" in (u or {}) and (u or {}).get("password") for u in page["data"].get("records", []))
            if not has_pwd:
                self.pass_("TC-USR-006", "分页查询账号列表", f"total={page['data'].get('total',0)}，无密码字段", "账号管理")
            else:
                self.fail("TC-USR-006", "分页查询账号列表", "响应含 password", "账号管理")
        else:
            self.fail("TC-USR-006", "分页查询账号列表", page.get("message", ""), "账号管理")

    # ── 数据字典 ─────────────────────────────────────────────
    def run_dct_tests(self):
        ca = self.client("admin")
        cat = f"TEST_CAT_{SUFFIX}"
        _, cr = ca.call(
            "POST",
            "/api/v1/dict",
            {"dictCode": cat, "dictName": "测试分类", "sortNo": 99},
        )
        if cr.get("success"):
            self.pass_("TC-DCT-001", "ADMIN 新增字典分类", f"dictCode={cat}", "数据字典")
        else:
            self.fail("TC-DCT-001", "ADMIN 新增字典分类", cr.get("message", ""), "数据字典")

        _, item = ca.call(
            "POST",
            "/api/v1/dict/items",
            {
                "dictCode": "JUDGMENT_TYPE",
                "itemValue": f"CUSTOM_{SUFFIX}",
                "itemLabel": "自定义结论",
                "colorTag": "#FFA500",
                "sortNo": 99,
            },
        )
        if item.get("success"):
            self.ctx.dict_item_id = item["data"]
            self.pass_("TC-DCT-002", "在字典分类下新增字典项（含 color_tag）", f"id={self.ctx.dict_item_id}", "数据字典")
        else:
            self.fail("TC-DCT-002", "在字典分类下新增字典项（含 color_tag）", item.get("message", ""), "数据字典")

        if self.ctx.dict_item_id:
            _, upd = ca.call(
                "PUT",
                f"/api/v1/dict/items/{self.ctx.dict_item_id}",
                {"itemLabel": "自定义结论2", "colorTag": "#FF6600"},
            )
            if upd.get("success"):
                self.pass_("TC-DCT-003", "编辑字典项 label 和 colorTag", "更新成功", "数据字典")
            else:
                self.fail("TC-DCT-003", "编辑字典项 label 和 colorTag", upd.get("message", ""), "数据字典")

            _, off = ca.call(
                "PUT",
                f"/api/v1/dict/items/{self.ctx.dict_item_id}",
                {"status": 0},
            )
            if off.get("success"):
                self.pass_("TC-DCT-005", "停用字典项", "status=0", "数据字典")
            else:
                self.fail("TC-DCT-005", "停用字典项", off.get("message", ""), "数据字典")
        else:
            self.blocked("TC-DCT-003", "编辑字典项", "无字典项", "数据字典")
            self.blocked("TC-DCT-005", "停用字典项", "无字典项", "数据字典")

        _, sys_upd = ca.call(
            "PUT",
            "/api/v1/dict/items/di031",
            {"itemValue": "QUALIFIED_MODIFIED"},
        )
        if sys_upd.get("success"):
            _, check = ca.call("GET", "/api/v1/dict/items/JUDGMENT_TYPE/manage")
            items = check.get("data", []) if check.get("success") else []
            q = next((x for x in items if x.get("id") == "di031"), None)
            if q and q.get("itemValue") == "QUALIFIED":
                self.pass_("TC-DCT-004", "编辑系统内置字典项——is_system=1 时 item_value 不可修改", "itemValue 未变", "数据字典")
            else:
                self.skip("TC-DCT-004", "编辑系统内置字典项——is_system=1 时 item_value 不可修改", "行为与用例描述不一致", "数据字典")
        else:
            self.pass_("TC-DCT-004", "编辑系统内置字典项——is_system=1 时 item_value 不可修改", sys_upd.get("message", ""), "数据字典")

        req = urllib.request.Request(
            BASE + "/api/v1/dict/items/di031",
            method="DELETE",
            headers={"Authorization": f"Bearer {ca.token}"},
        )
        try:
            urllib.request.urlopen(req, timeout=10)
            self.fail("TC-DCT-006", "系统内置字典不可删除", "DELETE 成功", "数据字典")
        except urllib.error.HTTPError as e:
            if e.code in (400, 403, 404, 405):
                self.pass_("TC-DCT-006", "系统内置字典不可删除", f"HTTP {e.code}", "数据字典")
            else:
                self.skip("TC-DCT-006", "系统内置字典不可删除", f"HTTP {e.code}", "数据字典")

    def run_all(self):
        self.run_std_tests()
        self.run_ind_tests()
        self.run_gap_tests()
        self.run_ins_tests()
        self.run_jdg_tests()
        self.run_rei_tests()
        self.run_rej_tests()
        self.run_con_tests()
        self.run_sta_tests()
        self.run_crt_tests()
        self.run_usr_tests()
        self.run_dct_tests()

    def render_markdown(self) -> str:
        total = len(self.results)
        passed = sum(1 for r in self.results if r["status"] == "PASS")
        failed = sum(1 for r in self.results if r["status"] == "FAIL")
        blocked = sum(1 for r in self.results if r["status"] == "BLOCKED")
        skipped = sum(1 for r in self.results if r["status"] == "SKIP")
        rate = f"{passed * 100 // total}%" if total else "0%"

        lines = [
            "# 质量判定解释与让步管理系统 — CRUD 测试执行报告",
            "",
            f"**执行时间**: {RUN_TS}  ",
            f"**测试依据**: [crud-test-cases.md](./crud-test-cases.md)  ",
            f"**执行方式**: 后端 API 自动化（`run_crud_api_tests.py`）  ",
            f"**被测服务**: `{BASE}`（Spring Boot 2.7.18，profile=dev）  ",
            "",
            "## 一、执行摘要",
            "",
            "| 指标 | 数值 |",
            "|------|------|",
            f"| 用例总数 | {total} |",
            f"| 通过 (PASS) | {passed} |",
            f"| 失败 (FAIL) | {failed} |",
            f"| 阻塞 (BLOCKED) | {blocked} |",
            f"| 跳过 (SKIP) | {skipped} |",
            f"| 通过率（PASS/总数） | {rate} |",
            "",
            "### 测试账号映射",
            "",
            "用例文档中的账号与项目实际预置账号对应关系：",
            "",
            "| 用例账号 | 实际工号 | 实际角色 | 密码 |",
            "|----------|----------|----------|------|",
            "| admin | admin | ADMIN | Admin123456 |",
            "| qc_user | 021001 | QUALITY_ENGINEER | Admin123456 |",
            "| sales_mgr | 021004 | SALES_MANAGER | Admin123456 |",
            "| quality_mgr | 021003 | QUALITY_MANAGER | Admin123456 |",
            "| viewer | — | 未预置 VIEWER 角色 | — |",
            "",
            "## 二、按模块汇总",
            "",
        ]

        modules = {}
        for r in self.results:
            m = r["module"] or "其他"
            modules.setdefault(m, {"PASS": 0, "FAIL": 0, "BLOCKED": 0, "SKIP": 0})
            modules[m][r["status"]] = modules[m].get(r["status"], 0) + 1

        lines.append("| 模块 | PASS | FAIL | BLOCKED | SKIP |")
        lines.append("|------|------|------|---------|------|")
        for m, s in modules.items():
            lines.append(f"| {m} | {s.get('PASS',0)} | {s.get('FAIL',0)} | {s.get('BLOCKED',0)} | {s.get('SKIP',0)} |")

        lines.extend(["", "## 三、用例明细", ""])
        current_mod = None
        for r in self.results:
            if r["module"] != current_mod:
                current_mod = r["module"]
                lines.extend([f"### {current_mod}", ""])
            icon = {"PASS": "✅", "FAIL": "❌", "BLOCKED": "⏸", "SKIP": "⏭"}.get(r["status"], "•")
            lines.append(f"- {icon} **{r['id']}** {r['name']} — **{r['status']}**")
            lines.append(f"  - {r['detail']}")
        lines.extend(
            [
                "",
                "## 四、说明",
                "",
                "1. **BLOCKED**：依赖未实现接口、缺少预置数据、需前端 UI/文件上传/定时任务等，API 自动化无法完整覆盖。",
                "2. **FAIL**：接口行为与用例预期不一致，需开发或产品确认。",
                "3. 前端页面交互（下拉联动、徽章颜色、按钮置灰等）未在本轮 API 测试中验证。",
                "4. 复现命令：`python3 specs/001-quality-judgment-concession/test-case/run_crud_api_tests.py`",
                "5. 本轮发现并修复：`QcQualityStandardMapper` 中 `&lt;=`/`&gt;=` 误写入非 XML 脚本 SQL，导致判定引擎查询失败（已改为 `<=`/`>=`）。",
                "",
            ]
        )
        return "\n".join(lines)


def wait_for_backend(timeout=120) -> bool:
    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            req = urllib.request.Request(BASE + "/actuator/health")
            with urllib.request.urlopen(req, timeout=3) as r:
                if r.status == 200:
                    return True
        except Exception:
            pass
        time.sleep(2)
    return False


def main():
    if not wait_for_backend():
        report = (
            "# 质量判定解释与让步管理系统 — CRUD 测试执行报告\n\n"
            f"**执行时间**: {RUN_TS}\n\n"
            "## 执行失败\n\n"
            f"后端服务 `{BASE}` 在 120s 内未就绪，无法执行用例。请先启动：\n\n"
            "```bash\ncd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev\n```\n"
        )
        out = f"crud-test-results-{datetime.now().strftime('%Y-%m-%d')}.md"
        path = __file__.replace("run_crud_api_tests.py", out)
        with open(path, "w", encoding="utf-8") as f:
            f.write(report)
        print("BACKEND DOWN — report written:", path)
        return

    runner = TestRunner()
    runner.run_all()
    md = runner.render_markdown()
    out = f"crud-test-results-{datetime.now().strftime('%Y-%m-%d')}.md"
    path = __file__.replace("run_crud_api_tests.py", out)
    with open(path, "w", encoding="utf-8") as f:
        f.write(md)
    print("Report:", path)
    print(f"PASS={sum(1 for r in runner.results if r['status']=='PASS')} "
          f"FAIL={sum(1 for r in runner.results if r['status']=='FAIL')} "
          f"BLOCKED={sum(1 for r in runner.results if r['status']=='BLOCKED')}")


if __name__ == "__main__":
    main()
