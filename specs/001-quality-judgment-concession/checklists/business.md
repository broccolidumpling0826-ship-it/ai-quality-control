# Business Requirements Checklist: 质量判定解释与让步管理系统

**Purpose**: 验证规格书是否严格覆盖用户指定的 5 条核心业务需求，每项检查均为"需求质量单元测试"——测试需求写法是否完整、清晰、一致和可验证，而非测试系统实现是否正确。

**Created**: 2026-05-16
**Feature**: [spec.md](../spec.md)
**Focus Areas**: 标准维护 · 检验录入 · 自动判定 · 复检改判 · 质保书基础

---

## 1. 标准维护（国标 / 企标 / 客户协议标准）

> 用户要求：支持国标、企标、客户协议标准，包含品种、牌号、规格范围、指标上下限、版本、生效日期。

- [ ] CHK001 - 规格书是否明确定义了国标、企标、客户协议三类标准的业务区分标准，以及每类需独立维护的必填字段集合？[Completeness, Spec §FR-001]
- [ ] CHK002 - "规格范围"是否给出了格式规范（如"厚度 0.5–3.0mm，宽度 900–1250mm"），还是仅为自由文本，无法做程序化的规格匹配？[Clarity, Spec §FR-001]
- [ ] CHK003 - 指标上下限（upper_limit / lower_limit）是否明确区分了"合格限"和"让步限"的定义，以及两者为空时分别代表何含义（无上限 vs 无该指标）？[Clarity, Spec §FR-001]
- [ ] CHK004 - 版本号（version_no）的命名规范是否有明确示例或格式约束，使得两个版本可以比较新旧？[Clarity, Spec §FR-001]
- [ ] CHK005 - 生效日期（effective_date）与失效日期（expiry_date）的关系约束是否完整？失效日期填 9999-12-31 代表"永不失效"是否为具名常量而非魔法值？[Clarity, Spec §FR-001, Gap]
- [ ] CHK006 - 客户协议标准与国标/企标的"同时命中"情形下，规格书是否明确说明三者共存时如何合并结论（AND 逻辑），而非仅描述单一命中场景？[Completeness, Spec §FR-002]
- [ ] CHK007 - 同一体系下时间窗口不重叠约束是否有可操作的校验规则（如：新版本 effective_date 不得落入旧版本 [effective_date, expiry_date]）？[Measurability, Spec §FR-001]
- [ ] CHK008 - 客户协议标准"锁定引用旧版国标"的需求是否有记录载体（如备注字段），以及在判定引擎中如何生效，是否已在规格书中说明？[Completeness, Spec §FR-002]
- [ ] CHK009 - 标准作废（DEPRECATED）操作对已出具历史判定的影响是否明确？规格书是否说明历史判定使用快照，不受标准作废影响？[Coverage, Spec §FR-005]

---

## 2. 检验录入（炉号 / 卷号 / 检验项目 / 检验值等）

> 用户要求：维护炉号、卷号、检验项目、检验值、检验时间、检验人、样品类型。

- [ ] CHK010 - 规格书是否说明了炉号（heat_no）与卷号（coil_no）的业务关系（一炉多卷）以及批次号（batch_no）由炉号衍生的推导规则？[Completeness, Spec §Assumptions]
- [ ] CHK011 - 检验值（test_value）的数据类型和精度要求是否明确（如 DECIMAL(20,6)），文本型指标与数值型指标是否分别有处理规则？[Clarity, Spec §FR-004]
- [ ] CHK012 - 样品类型（sample_type）的枚举范围是否已在规格书或字典中穷举，还是允许业务扩展？HEAD/TAIL/MIDDLE 以外的样品类型如何处理？[Coverage, Spec §FR-004]
- [ ] CHK013 - 检验项目与标准指标的关联方式是否明确——检验时是否要求录入的指标集覆盖标准中的所有必检项，还是允许部分录入？[Completeness, Spec §FR-004]
- [ ] CHK014 - "检验记录提交后数据值永不修改"的约束是否明确区分了哪些字段受保护（检验值字段），哪些字段例外（status/void_reason/void_by/void_time 四字段）？[Clarity, Spec §FR-004b]
- [ ] CHK015 - 软作废操作（void）的权限规则是否清晰——"质检主管或原录入人"是 OR 关系，规格书是否处理了两者身份重叠的情形？[Clarity, Spec §FR-004b]
- [ ] CHK016 - 批量录入（batch import）时的部分成功策略是否明确：已成功记录不回滚但部分失败时，已成功记录是否立即触发判定，还是等全批完成？[Coverage, Spec §FR-004]
- [ ] CHK017 - 同一卷号、同一检验时间、同一指标允许录入多次的情形是否有处理规则？质保书汇总时如何取"最新值"是否已定义？[Edge Case, Spec §FR-010]

---

## 3. 自动判定（按客户/产品匹配标准，输出四种结论）

> 用户要求：按客户和产品匹配适用标准，输出合格、不合格、需复检、可让步。

- [ ] CHK018 - 四种判定结论（合格/不合格/需复检/可让步）的触发条件是否逐一在规格书中有可验证的定义？"可让步"的触发是否明确依赖让步范围（concession_upper/lower）的存在？[Clarity, Spec §FR-005]
- [ ] CHK019 - "需复检"结论的触发条件是否与"可让步"结论区分清晰？规格书是否说明同一偏差值同时满足两种条件时的优先级？[Clarity, Spec §FR-005, Ambiguity]
- [ ] CHK020 - 多指标同时命中时，最终结论的合并规则是否明确（任一不满足则为不合格）？合格指标与可让步指标同时存在时，整体结论是否定义？[Completeness, Spec §FR-002]
- [ ] CHK021 - 无标准覆盖的指标"跳过判定"规则是否明确——跳过是否影响整体合格/不合格布尔运算，还是仅从分子/分母中排除？[Clarity, Spec §FR-005]
- [ ] CHK022 - 判定依据快照（JudgmentEvidence）中冗余存储的字段范围是否明确，是否包含了足以独立重现判定过程的所有关键数值（上下限、让步范围、版本号）？[Completeness, Spec §FR-005]
- [ ] CHK023 - "按客户和产品匹配"中的"产品"匹配维度是否完整定义？品种（variety）、牌号（grade）、规格（product_spec）三者如何共同参与匹配，规格是精确匹配还是区间判断？[Clarity, Spec §FR-002]
- [ ] CHK024 - 判定以"检验时间"为准，当同一时间存在多个有效版本的标准时（如版本重叠约束被绕过），规格书是否定义了降级处理或错误处理行为？[Edge Case, Spec §FR-001]

---

## 4. 复检改判（发起复检 / 记录结果 / 改判原因 / 审批记录）

> 用户要求：支持发起复检、记录复检结果、改判原因和审批记录。

- [ ] CHK025 - 复检最多 2 次的限制是以"同一检验记录"为单位还是以"同一卷号"为单位？规格书是否明确了"含其所有作废/重录版本形成的判定链"的计数方式？[Clarity, Spec §FR-007]
- [ ] CHK026 - "完成复检后重新触发判定"——新判定是否基于复检新录入的检验记录，还是基于原检验记录的修订版？规格书是否明确说明新 InspectionRecord 与复检记录的关联机制？[Completeness, Spec §FR-007]
- [ ] CHK027 - 改判申请中"改判原因"是否有最小内容要求（如：必填文本域、字数下限）？是否区分常规改判和逆向改判各自的必填字段？[Completeness, Spec §FR-008]
- [ ] CHK028 - 改判类型矩阵（4×4 原结论×目标结论 = 常规/逆向/禁止）是否在规格书中有权威的完整表格定义，而非分散的散文描述？[Clarity, Spec §FR-008]
- [ ] CHK029 - 审批记录的不可删除性是否有明确的技术保障手段说明（如仅提供 INSERT 接口），还是仅作为业务约束声明？[Completeness, Spec §FR-008]
- [ ] CHK030 - 逆向改判必须上传的"证据附件"在上传后不可替换的约束，是否和让步接收确认附件的同类约束保持规格书层面的一致描述？[Consistency, Spec §FR-008, Spec §FR-009]
- [ ] CHK031 - 改判审批通过后联动让步失效的规则，是否覆盖了让步处于"待确认"、"已确认"、"已批准"三种子状态的全部情形，并分别说明处理方式？[Coverage, Spec §FR-008]

---

## 5. 质保书基础（按卷号或批次汇总关键检验结果）

> 用户要求：支持按卷号或批次汇总关键检验结果，为质保书出具预留数据。

- [ ] CHK032 - "关键检验结果"中"关键"的判断标准是否在规格书中有定义？是所有检验项目，还是仅特定类别（成分/性能/尺寸等），还是由标准配置决定？[Clarity, Spec §FR-010, Ambiguity]
- [ ] CHK033 - 同一指标存在多条检验记录时，"取最新检验记录的值"的"最新"定义是否明确（按 test_time 排序，还是按 create_date_time）？[Clarity, Spec §FR-010]
- [ ] CHK034 - 批次包含已作废检验记录时，质保书汇总是否明确仅取 status='NORMAL' 的记录？作废记录与有效记录对应同一指标时的去重规则是否说明？[Coverage, Spec §FR-004b, Spec §FR-010]
- [ ] CHK035 - "为质保书出具预留数据"中，snapshot_data 的 JSON 结构是否有字段级的规格定义（至少包含哪些字段，格式如何），还是完全由实现决定？[Completeness, Spec §FR-010, Gap]
- [ ] CHK036 - 同一卷号/批次可多次生成快照时，历史快照的保留策略是否明确（不覆盖、追加记录），以及如何区分多次快照之间的有效性？[Coverage, Spec §FR-010]
- [ ] CHK037 - 存在已批准让步（approval_status=APPROVED）的批次，质保书快照需附加让步说明的要求是否与让步接收模块的让步详情格式保持一致？[Consistency, Spec §FR-010, Spec §FR-009]
- [ ] CHK038 - 质保书汇总操作的性能要求（≤10 秒）是否明确了前提条件（如批次下检验记录条数上限），以便该指标可客观验证？[Measurability, Spec §SC-005]

---

## 跨领域一致性与追溯性

- [ ] CHK039 - 5 条核心业务需求（标准维护/检验录入/自动判定/复检改判/质保书）之间的数据流链路是否完整定义：检验时间 → 标准匹配 → 判定 → 复检 → 改判 → 质保书汇总？[Consistency, Gap]
- [ ] CHK040 - 各模块间"判定时间"、"检验时间"、"复检时间"、"改判时间"的语义是否有统一定义，避免跨模块引用时产生歧义？[Consistency, Ambiguity]

---

## 6. Session 2026-05-16 澄清项实现就绪性（D-015 ~ D-019）

> 以下各项检验 Session 2026-05-16 五条澄清（D-015 四段优先级 / D-016 规格下拉 / D-017 质保书指标范围 / D-018 可用性 / D-019 日志）的需求描述是否足够清晰，可直接指导 T106–T110 实现，无需再次澄清。

### D-015：判定四段优先级（FR-005）

- [ ] CHK041 - 当实测值**恰好等于**合格限边界值（testValue == upperLimit 或 testValue == lowerLimit）时，判定结论是 QUALIFIED 还是 UNQUALIFIED？规格书中边界是否为闭区间（≤/≥）？[Clarity, Spec §FR-005, Ambiguity]
- [ ] CHK042 - 当三级标准匹配全部返回空（客协/企标/国标均无匹配结果）时，判定引擎输出什么结论？规格书是否定义了"无匹配标准时的 fallback 结论"？[Coverage, Edge Case, Spec §FR-002, Gap]
- [ ] CHK043 - 当某批次所有检验项目均属于"无标准覆盖"（全部写入 StandardGap）时，整体判定结论是什么？是否定义了"无任何有效判定指标"时的特殊处理？[Coverage, Edge Case, Spec §FR-005]
- [ ] CHK044 - 当 standard_indicator 的 upper_limit 和 lower_limit 均为 NULL（指标无具体限值）时，该指标是否应视同"无标准覆盖"跳过，还是视为"无限制=自动通过"？规格书是否区分了这两种情形？[Clarity, Spec §FR-001, Ambiguity]
- [ ] CHK045 - 让步范围（concession_upper/lower）可否合法地设置为比合格限更宽（如 lower_limit=370 而 concession_lower=360）？若让步范围比合格限更严，判定引擎行为是否有定义？[Clarity, Spec §FR-005, Edge Case]

### D-016：product_spec 规格下拉约束（FR-004）

- [ ] CHK046 - 规格下拉列表为空（所选客户+品种+牌号下无有效标准，无 spec_range 可选）时，检验录入界面是否有明确的提示要求（如"请先维护对应标准"），还是允许跳过规格字段？[Coverage, Edge Case, Spec §FR-004]
- [ ] CHK047 - 当用户已选择 spec_range 后，再次更改品种或牌号时，是否要求 product_spec 字段自动清空并重新加载选项？规格书是否明确此交互规则？[Clarity, Spec §FR-004]
- [ ] CHK048 - product_spec 的选定值（即 spec_range 字符串）是否应完整存入检验快照，还是仅存 standard_id 外键？规格书是否明确快照字段的存储内容？[Completeness, Spec §FR-004, Spec §FR-005]

### D-017：质保书关键指标分类过滤（FR-010）

- [ ] CHK049 - 当某批次完全没有 COMPOSITION/PERFORMANCE/DIMENSION 三类指标的检验记录时（如仅录入了表面/外形检验），系统是否允许生成空快照？是否有失败/警告要求？[Coverage, Edge Case, Spec §FR-010]
- [ ] CHK050 - ADMIN 修改指标类别纳入配置后，是否对**历史**已生成的质保书快照产生影响？规格书是否明确历史快照的不变性？[Clarity, Spec §FR-010, Ambiguity]
- [ ] CHK051 - 质保书快照（snapshot_data JSON）的字段结构是否有任何规格定义（至少列出顶层必要字段），还是完全由实现决定？[Completeness, Spec §FR-010, Gap]

### D-019：结构化 JSON 日志（FR-019）

- [ ] CHK052 - 日志中的业务字段（coilId、grade、customerId、testerNo）是否属于需要脱敏处理的个人/业务敏感信息？规格书或安全需求中是否有日志数据分类要求？[Completeness, Spec §FR-019, Gap]
- [ ] CHK053 - File Appender 发生写入失败（磁盘满、权限不足）时，系统是否应继续正常运行，还是进入降级/告警状态？规格书是否定义了日志基础设施故障的处理策略？[Coverage, Edge Case, Spec §FR-019, Gap]
- [ ] CHK054 - MDC traceId 的传播要求是否涵盖异步场景（@Async 注解方法、Spring @Scheduled 定时任务）？MDC 默认不跨线程传播，规格书是否明确此类异步场景的 traceId 要求？[Completeness, Spec §FR-019, Gap]

---

## 7. 异常流与边界条件覆盖

- [ ] CHK055 - 当针对某判定结论发起复检（复检次数尚未达到上限），而该判定结论在复检进行中被改判时，复检记录的最终状态是否有明确规则？[Coverage, Edge Case, Spec §FR-007, Spec §FR-008]
- [ ] CHK056 - 同一批次并发提交两条改判申请时，哪条应被接受、哪条应被拒绝？规格书是否定义了并发改判的互斥或排队规则？[Coverage, Edge Case, Spec §FR-008, Gap]
- [ ] CHK057 - 当判定结论为 NEED_REINSPECTION 且 2 次复检次数已耗尽时，是否允许发起让步接收申请（结论非 CAN_CONCESSION）？还是强制走改判流程？规格书是否定义了此路径？[Coverage, Edge Case, Spec §FR-007, Spec §FR-009]
- [ ] CHK058 - 让步催促提醒阈值（app.concession.remind-days）变更时，是否立即对所有当前处于"待确认"状态的让步申请生效，还是仅对新发起的让步生效？规格书是否明确此实时性？[Clarity, Spec §FR-009]
- [ ] CHK059 - 当质保书数据生成时，批次中存在尚未完成客户确认的让步申请（confirm_status=PENDING）时，质保书快照是否附加让步说明，还是等待确认后再生成？[Coverage, Edge Case, Spec §FR-010, Spec §FR-009]
- [ ] CHK060 - 当让步接收附件（confirm_attachment_url）写入后，关联的改判申请同时被提交审批通过，让步自动置为 INVALID——此时是否应要求通知曾上传附件的销售人员（而非仅让步申请人）？[Coverage, Spec §FR-009]

---

## 8. 新旧需求一致性验证

- [ ] CHK061 - FR-005 中"四段优先级"最严重结论合并规则（UNQUALIFIED > NEED_REINSPECTION > CAN_CONCESSION > QUALIFIED）是否与 FR-002 中"任一不满足则结论为不合格"的 AND 逻辑完全一致？是否存在可能的语义冲突？[Consistency, Spec §FR-002, Spec §FR-005]
- [ ] CHK062 - FR-019（结构化日志）要求记录业务主键，FR-012（审计日志）要求记录操作人/时间/变更内容——当两者都被触发（如改判审批时），是否明确两套日志的写入顺序或事务关系？[Consistency, Spec §FR-012, Spec §FR-019]
