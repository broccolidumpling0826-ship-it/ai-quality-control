# 模拟标准与多格式源文件测试包

本目录提供 **2 份模拟质量标准** 及 **配套源文件（PDF / Excel）**，用于验证「标准维护 → 上传源文件 → 发布入库 → RAG 引用」链路。

> 均为联调模拟数据，**不得用于生产判定**。

## 文件清单

| 文件 | 说明 |
| --- | --- |
| `mock-standard-A-q345b-enterprise.pdf` | 企业标准 Q345B 源 PDF |
| `mock-standard-B-q235b-customer.pdf` | 客户协议 Q235B 源 PDF |
| `mock-standard-table-q345b.xlsx` | 企业标准 Q345B 指标表格（Excel 源文件） |
| `mock-standard-scan-q235b.png` | 客户协议 Q235B 扫描件模拟（Vision OCR 源文件） |
| `../init-mock-standards-for-pdf-test.sql` | 预置两条 **DRAFT** 结构化标准 + 指标 |
| `generate-mock-standard-pdfs.py` | PDF 生成脚本（可重复执行） |
| `generate-mock-standard-table-xlsx.py` | Excel 生成脚本（可重复执行） |
| `generate-mock-standard-scan-png.py` | 扫描图片生成脚本（可重复执行） |

## 支持的上传格式

标准维护页「标准源文件」支持：`pdf`、`xlsx`、`xls`、`png`、`jpg`、`jpeg`。

- **PDF / Excel**：本地 POI/PDFBox 解析
- **图片**：硅基流动 `deepseek-ai/DeepSeek-OCR`（Vision API，配置见 `application-dev.yml` → `app.ai.model.vision`）

## 标准 A：企业标准 Q345B

| 字段 | 值 |
| --- | --- |
| 建议 ID（SQL 预置） | `stdmock001` |
| 标准类型 | 企业标准 `ENTERPRISE` |
| 标准编号 | `Q/ZX-STEEL-2026-Q345B` |
| 标准名称 | Q345B低合金高强度结构钢企业标准 |
| 品种 / 牌号 | 热轧板 / Q345B |
| 规格范围 | 厚度2.0-12.0mm，宽度900-1800mm |
| 版本 | V2026.1 |
| 生效 / 失效 | 2026-01-01 / 9999-12-31 |
| 配套 PDF | `mock-standard-A-q345b-enterprise.pdf` |

**结构化指标（与 PDF 条款一致）**

| 指标 | 合格范围 | 让步范围 |
| --- | --- | --- |
| Rm | 470 ~ 630 MPa | 460 ~ 470 MPa |
| ReL | 345 ~ 460 MPa | — |
| A | ≥ 20% | ≥ 18% |
| Δt | -0.20 ~ 0.20 mm | -0.25 ~ 0.25 mm |

**RAG 验证问题示例**：`Q345B 热轧板抗拉强度企业标准范围是多少？`

## 标准 B：客户协议 Q235B

| 字段 | 值 |
| --- | --- |
| 建议 ID（SQL 预置） | `stdmock002` |
| 标准类型 | 客户协议 `CUSTOMER` |
| 标准编号 | `协议D2026-001-v1` |
| 标准名称 | 西南建材集团Q235B冷轧板供货协议 |
| 关联客户 | `CUST-002` 西南建材集团 |
| 品种 / 牌号 | 冷轧板 / Q235B |
| 规格范围 | 厚度0.8-2.5mm，宽度800-1250mm |
| 版本 | 协议D2026-001-v1 |
| 生效 / 失效 | 2026-01-01 / 2026-12-31 |
| 配套 PDF | `mock-standard-B-q235b-customer.pdf` |

**结构化指标（与 PDF 条款一致）**

| 指标 | 合格范围 | 让步范围 |
| --- | --- | --- |
| Rm | 375 ~ 505 MPa | 370 ~ 375 MPa |
| A | ≥ 27% | ≥ 25% |
| ReL | 235 ~ 360 MPa | — |
| Δt | -0.10 ~ 0.10 mm | -0.13 ~ 0.13 mm |

**RAG 验证问题示例**：`西南建材 Q235B 冷轧板延伸率协议下限是多少？`

## 快速使用

### 1. 生成演示源文件（若尚未生成）

```bash
cd backend/scripts/demo-documents
python3 generate-mock-standard-pdfs.py
python3 generate-mock-standard-table-xlsx.py
python3 generate-mock-standard-scan-png.py
```

### 2. 导入结构化标准（可选，也可在 UI 手工新建）

```bash
mysql -h 127.0.0.1 -P 3307 -u root -p ai_quality_control \
  < backend/scripts/init-mock-standards-for-pdf-test.sql
```

### 3. UI 联调步骤

1. 登录 `admin / Admin123456`
2. 进入 **标准与协议 → 标准维护**
3. 编辑 `stdmock001` 或 `stdmock002`（或按上表手工新建）
4. 在「标准源文件」上传对应 PDF 或 Excel（`mock-standard-table-q345b.xlsx`）
5. 草稿阶段确认索引状态为 `PENDING`、未入 ES
6. 点击 **发布**，观察解析/索引变为 `INDEXED`
7. 在 **标准 RAG** 页用上方示例问题检索，应能引用源文件条款

**图片 OCR 联调**：上传扫描版 `png/jpg/jpeg` 后发布；需启用 `AI_VISION_ENABLED=true` 且配置有效的 `AI_MODEL_API_KEY`。

### 4. API 抽验

```bash
# 上传（替换 standardId 与 PDF 路径）
curl -X POST "http://localhost:8080/api/v1/standards/stdmock001/source-file" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@backend/scripts/demo-documents/mock-standard-A-q345b-enterprise.pdf"
```

## 重新生成

修改 `generate-mock-standard-pdfs.py` 中 `STANDARD_A` / `STANDARD_B` 文案后重新运行脚本即可覆盖 PDF。
