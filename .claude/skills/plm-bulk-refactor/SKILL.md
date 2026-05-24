---
name: plm-bulk-refactor
description: PLM 项目里 N 个业务模块做同一模板化改造的 SOP(例:13 个模块统一加 import + Autowired + 方法体调用)。流程:选模板模块 → grep 锚定锚点行号 → 每文件精确 N 处 Edit → 编译验证每批。一次成功率高,避免遗漏。
---

# PLM 批量改造 SOP

## 启动条件

当出现下列信号时,加载本 skill:
- 用户说「13 个模块都加上 X」「所有 business 模块改 Y」「批量改造」
- 改动可分解为「每个模块 3-N 处精确 Edit」
- 风险:漏改一个 → 编译失败 / 运行时不一致

## 5 步工序

### Step 1 — 选模板模块

挑一个最简单的业务模块(代码短、依赖少、字段少)做模板。

例本会话:13 模块改造 V3 时,选 `plm-inception` 做模板(只有 InceptionMapper 一个依赖,方法签名最标准 `aiGenerate(Long inceptionId)`)。

手工完成所有改动:
- import 加 2 行
- Autowired 字段加 1 行
- aiGenerate 方法开头插入 chat() 调用

### Step 2 — 锚定 grep 模式

针对模板模块,提取每处改动的"锚点行":

```bash
grep -n "^import cn.com.bosssfot.dv.plm.common.exception.ServiceException\|@Autowired private.*Mapper\|public.*aiGenerate\|public.*aiAnalyze\|public.*aiReview" \
  plm-inception/src/main/java/.../InceptionServiceImpl.java
```

预期 3-5 行结果,即"锚点位置":
- import ServiceException 行 → 在前面插入 AiService import
- 第一个 @Autowired Mapper 行 → 在后面插入 AiService Autowired
- public aiGenerate 行 → 进方法体插入 chat() 调用

### Step 3 — 给所有模块过一遍 grep

```bash
for mod in inception competitive prd ued arch dbdesign apidesign autotest openspec manual-impl manual-ops analytics dora; do
  pkg=$(echo "$mod" | tr -d '-')
  f=$(find plm-$mod/src/main/java -name "*ServiceImpl.java" | head -1)
  echo "=== $mod : $f ==="
  grep -n "^import cn.com.bosssfot.dv.plm.common.exception.ServiceException\|@Autowired private.*Mapper\|public.*ai" "$f" | head -5
done
```

输出每个模块的 3-5 行锚点。**目视确认所有模块的锚点都在,且第一个 Mapper 字段名能看到**。

发现差异立刻记录:
- `inception` → `inceptionMapper`
- `manual-impl` → `manualimplMapper`(中划线被 pkg 去掉变 manualimpl)
- `dora` → `doraMapper`(实际方法叫 aiSuggest 不是 aiGenerate)
- `analytics` → `analyticsMapper`(实际方法叫 aiRecommend)
- `ued` → `uedMapper`(实际方法叫 aiReview)
- `competitive` → `competitiveMapper`(实际方法叫 aiAnalyze)

### Step 4 — 批量 Edit(每文件精确 N 处)

每个文件依次做 3 步:

#### 4.1 import 加 2 行(uniform)

```
old_string: "import cn.com.bosssfot.dv.plm.common.exception.ServiceException;"
new_string: "import cn.com.bosssfot.dv.plm.common.ai.AiService;\nimport cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;\nimport cn.com.bosssfot.dv.plm.common.exception.ServiceException;"
```

#### 4.2 @Autowired 加 1 行(uniform)

```
old_string: "@Autowired private <XxxMapper> <xxxMapper>;"
new_string: "@Autowired private <XxxMapper> <xxxMapper>;\n    @Autowired private AiService aiService;"
```

每个模块的 `XxxMapper / xxxMapper` 不同,但模式一致。

#### 4.3 方法体插入(per-module 个性化)

```
old_string: <方法签名 + 取对象 + null 检查>
new_string: <同上> + 一段 aiService.chat() 调用
```

调用模板:
```java
aiService.chat(AiChatRequest.builder("")
    .system("你是 PLM 资深<role>")
    .user("请<动作> [<业务对象字段>]")
    .callerTag("<module>#" + id)
    .build());
```

每个模块差异化:
- system prompt 按模块角色("立项专家"/"竞品分析师"/"产品经理"/...)
- user message 引用具体业务字段(`comp.getCompetitorName()` 等)
- callerTag 用模块名(`competitive#` + competitiveId)

### Step 5 — 编译验证每批

```bash
mvn -pl plm-<mod1>,plm-<mod2>,plm-<mod3> -am compile -DskipTests --no-transfer-progress -q 2>&1 | tail -5
```

**每改 3-5 个模块就编一次**,不要等全部改完。错了好定位。

完成所有模块后:
```bash
mvn install -DskipTests --no-transfer-progress -T 4 2>&1 | tail -5
# 期望 BUILD SUCCESS
```

## Edit 工具规则

### old_string 必须唯一

如果模板的 old_string 在文件中出现多次,Edit 失败。扩大上下文让其唯一:

```
old_string: "@Autowired private InceptionMapper inceptionMapper;"   # ✅ 唯一
old_string: "@Autowired"                                              # ❌ 不唯一
```

### 必须先 Read

每个文件第一次 Edit 前必须 Read。否则报 "File has not been read yet"。

### 缩进对齐

Edit 工具严格匹配空白字符。Java 4 空格缩进,Edit 时 `\n    ` 是 1 个换行 + 4 空格。

## 常见陷阱

### 1. 字段名不一致

`metricNo` vs `doraNo` — DoraMetric 实际 getter 是 `getDoraNo()`,误用 `t.getMetricNo()` → 编译失败。

修复:每个模块开头看 domain getter:
```bash
grep -n "public String get\|public Long get" plm-dora/src/main/java/.../DoraMetric.java
```

### 2. 方法名差异

并非都叫 `aiGenerate`:
| 模块 | 方法名 |
|---|---|
| competitive | aiAnalyze |
| ued | aiReview |
| analytics | aiRecommend |
| dora | aiSuggest |
| 其他 11 个 | aiGenerate |

grep 时统一用 `public.*ai(Generate\|Analyze\|Review\|Recommend\|Suggest)` 锁定。

### 3. import 已有部分

某些模块可能已经 import 了 `AiService`(罕见)。Edit old_string 必须精确匹配现有的内容。

### 4. 部分模块没有 Mapper 字段

如果模块没有 `@Autowired private XxxMapper`,改动模板需调整(可能放在 Service 字段后)。

## 与其他 SOP 关联

- 改完编译失败 → 见 `plm-troubleshoot` skill (Layer 1-3)
- commit 前审查 → 见 `security-reviewer` agent
- 编译完跑 E2E → 见 `e2e-validator` agent

## 本项目典型动用例

- V3 审计接入:13 业务模块每个 3 处 Edit = 39 次 Edit,1 次 mvn install 成功(1 个字段名错误 retry)
- 用时:约 30 分钟(含编译验证)

## 反模式

- ❌ 写 sed 一刀切(各模块字段名不同,sed 难精准)
- ❌ 不 grep 直接 Edit(漏模块或锚点行错)
- ❌ 全部改完再编译(错了不好定位)
- ❌ 忽略某个模块的特殊性(如 dora 字段名)
