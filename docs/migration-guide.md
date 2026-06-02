# 手机题库 Android 迁移开发文档

> 版本：1.0.0 | 更新日期：2026-06-03
> 源项目：TikuApp (Electron + Vue 3) → 目标：Android 原生 (Kotlin + Jetpack Compose)

---

## 一、迁移概述

### 1.1 源项目分析

TikuApp 是一个基于 Electron + Vue 3 的桌面题库应用，部署在 Windows 7 和银河麒麟 ARM64 环境。核心功能包括题库管理、刷题、小测试、模拟考试、错题本、统计、出卷等。

**源项目技术栈**：Electron 22 + Vue 3 + Element Plus + sql.js (SQLite WASM) + Pinia + ECharts + xlsx + docx

**迁移目标**：将核心功能迁移为 Android 原生应用，运行在 HarmonyOS 2.0 (兼容安卓) 系统的华为 Mate30/Mate40E 设备上。

### 1.2 迁移策略

| 策略 | 说明 |
|------|------|
| 技术路线 | Kotlin + Jetpack Compose 原生安卓开发 |
| 迁移范围 | 第一阶段仅迁移核心功能（题库管理+刷题+考试+错题本） |
| 数据库 | Room ORM，保持双库架构，表结构直接映射 |
| UI | 全部用 Compose 重写，不使用 WebView |
| 不迁移 | 出卷(docx)、xlsx导入、快捷键、ECharts |

### 1.3 迁移映射总表

| 源项目模块 | 安卓模块 | 迁移方式 |
|------------|----------|----------|
| electron/db/database.js | data/local/SystemDatabase + UserDatabase | Room 重建 |
| electron/services/* | data/repository/* | 逻辑移植 |
| src/views/home/ | ui/home/HomeScreen | Compose 重写 |
| src/views/bank/ | ui/bank/BankScreen + QuestionListScreen | Compose 重写 |
| src/views/practice/ | ui/practice/PracticeScreen | Compose 重写 |
| src/views/exam/ | ui/exam/ExamScreen + ExamResultScreen | Compose 重写 |
| src/views/wrong/ | ui/wrong/WrongQuestionsScreen | Compose 重写 |
| src/composables/use-timer.js | util/CountdownTimer | Kotlin 重写 |
| src/utils/question-utils.js | util/QuestionUtils | Kotlin 移植 |
| src/stores/app-store.js | 各 ViewModel StateFlow | 拆分到各 VM |
| Electron IPC | 直接函数调用 | 架构简化 |

---

## 二、技术架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────┐
│                  UI Layer                    │
│  Compose Screens + ViewModels (StateFlow)   │
├─────────────────────────────────────────────┤
│               Domain Layer                   │
│  Repository Interfaces + Use Cases          │
├─────────────────────────────────────────────┤
│                Data Layer                    │
│  Repository Impl + Room DAO + FileParser    │
└─────────────────────────────────────────────┘
```

### 2.2 依赖清单

```kotlin
// build.gradle.kts 核心依赖
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // DataStore (设置存储备选)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // CSV解析
    implementation("com.opencsv:opencsv:5.9")
}
```

### 2.3 导航设计

```kotlin
sealed class Route(val route: String) {
    object Home : Route("home")
    object Bank : Route("bank")
    object QuestionList : Route("bank/{bankId}") {
        fun createRoute(bankId: Long) = "bank/$bankId"
    }
    object Practice : Route("practice")
    object PracticeConfig : Route("practice/config/{bankId}") {
        fun createRoute(bankId: Long) = "practice/config/$bankId"
    }
    object Exam : Route("exam")
    object ExamConfig : Route("exam/config/{bankId}") {
        fun createRoute(bankId: Long) = "exam/config/$bankId"
    }
    object ExamResult : Route("exam/result/{sessionId}") {
        fun createRoute(sessionId: String) = "exam/result/$sessionId"
    }
    object WrongQuestions : Route("wrong")
}
```

---

## 三、数据库详细设计

### 3.1 SystemDatabase (question_bank + question)

```kotlin
@Database(
    entities = [QuestionBankEntity::class, QuestionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SystemDatabase : RoomDatabase() {
    abstract fun questionBankDao(): QuestionBankDao
    abstract fun questionDao(): QuestionDao
}
```

**QuestionBankEntity**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, autoGenerate | 主键 |
| name | String | NOT NULL | 题库名称 |
| description | String | DEFAULT "" | 描述 |
| questionCount | Int | DEFAULT 0 | 题目数量(冗余) |
| createdAt | String | | 创建时间 |
| updatedAt | String | | 更新时间 |

**QuestionEntity**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, autoGenerate | 主键 |
| bankId | Long | FK, CASCADE | 所属题库 |
| type | String | single/multiple/judge | 题型 |
| content | String | NOT NULL | 题目内容 |
| options | String | JSON DEFAULT "[]" | 选项 |
| answer | String | NOT NULL | 答案 |
| explanation | String | DEFAULT "" | 解析 |
| difficulty | Int | 1-5, DEFAULT 2 | 难度 |
| category | String | DEFAULT "" | 分类 |
| tags | String | JSON DEFAULT "[]" | 标签 |
| sortOrder | Int | DEFAULT 0 | 排序 |
| createdAt | String | | 创建时间 |
| updatedAt | String | | 更新时间 |

### 3.2 UserDatabase (exam_session + answer_record + question_flag + settings + practice_progress)

```kotlin
@Database(
    entities = [
        ExamSessionEntity::class,
        AnswerRecordEntity::class,
        QuestionFlagEntity::class,
        SettingsEntity::class,
        PracticeProgressEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun examSessionDao(): ExamSessionDao
    abstract fun answerRecordDao(): AnswerRecordDao
    abstract fun questionFlagDao(): QuestionFlagDao
    abstract fun settingsDao(): SettingsDao
    abstract fun practiceProgressDao(): PracticeProgressDao
}
```

**ExamSessionEntity**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | String | PK (UUID) | 会话ID |
| mode | String | practice/quiz/exam | 模式 |
| bankId | Long | | 题库ID |
| totalCount | Int | | 总题数 |
| correctCount | Int | DEFAULT 0 | 正确数 |
| config | String | JSON | 配置 |
| score | Float | DEFAULT 0 | 分数(正确率) |
| startedAt | String | | 开始时间 |
| finishedAt | String? | | 结束时间 |

**AnswerRecordEntity**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, autoGenerate | 主键 |
| questionId | Long | | 题目ID |
| sessionId | String | | 会话ID |
| mode | String | | 模式 |
| userAnswer | String | DEFAULT "" | 用户答案 |
| isCorrect | Boolean? | | 是否正确 |
| timeSpent | Int | DEFAULT 0 | 用时(秒) |
| answeredAt | String | | 答题时间 |

**QuestionFlagEntity**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, autoGenerate | 主键 |
| questionId | Long | | 题目ID |
| flagType | String | favorite/wrong/note | 标记类型 |
| masteryLevel | Int | 0-2, DEFAULT 0 | 掌握度 |
| note | String | DEFAULT "" | 笔记 |
| createdAt | String | | 创建时间 |
| updatedAt | String | | 更新时间 |

UNIQUE(questionId, flagType)

**SettingsEntity**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| key | String | PK | 设置键 |
| value | String | DEFAULT "" | 设置值 |
| updatedAt | String | | 更新时间 |

默认值：font_size=medium, theme=light, auto_backup=true, max_backups=10

**PracticeProgressEntity**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, autoGenerate | 主键 |
| sessionId | String | | 会话ID |
| bankId | Long | | 题库ID |
| bankName | String | DEFAULT "" | 题库名称 |
| currentIndex | Int | DEFAULT 0 | 当前题目索引 |
| questionIds | String | JSON | 题目ID列表 |
| userAnswers | String | JSON DEFAULT "{}" | 用户答案 |
| submittedMap | String | JSON DEFAULT "{}" | 已提交记录 |
| savedAt | String | | 保存时间 |

注意：此表始终只有一行（保存时 DELETE + INSERT）。

---

## 四、页面设计

### 4.1 HomeScreen (首页)

**功能**：
- 顶部：应用标题 + 统计概览（总题库数、总题目数、已答题数、正确率）
- 中部：题库卡片列表（显示名称、题目数量、分类数）
- 底部：快捷入口（刷题、考试、错题本）
- 右下角：导入题库按钮

**数据源**：
- 题库列表：QuestionBankDao.getAll() (Flow)
- 统计概览：组合查询（总题目数、答题记录数、正确率）

**交互**：
- 点击题库卡片 → QuestionListScreen
- 长按题库卡片 → 删除确认对话框
- 点击导入 → 文件选择器 → 预览 → 导入

### 4.2 BankScreen (题库管理)

**功能**：
- 题库卡片网格（名称、描述、题目数量、创建时间）
- 新建题库（名称+描述）
- 删除题库（级联删除题目）
- 导入题目到指定题库

### 4.3 QuestionListScreen (题目列表)

**功能**：
- 顶部筛选：题型(全部/单选/多选/判断)、分类、关键词搜索
- 题目列表：题号+题型标签+题目内容+答案
- 分页加载（每页20条）
- 添加题目（表单）
- 删除题目

### 4.4 PracticeScreen (刷题)

**功能**：
- 配置面板：选择题库、分类(可选)、题型(可选)、题目数量
- 续答提示：有进度时显示"继续上次"按钮
- 答题区域：
  - 单选题：圆形选项，点击即选即判
  - 多选题：方形选项，可切换选择，需点"确认"提交
  - 判断题：只显示2个选项(正确/错误)
- 底部导航：上一题/下一题/题号(当前/总数)
- 答题后显示：正确答案+解析
- 进度自动保存（切题时保存）

**数据流**：
```
进入刷题 → 检查续答进度 → 有进度显示续答按钮
答题 → 切题 → watch(currentIndex) → 保存进度
退出 → 立即保存进度 → 清空状态
```

### 4.5 ExamScreen (模拟考试)

**功能**：
- 配置面板：选择题库、分类、题型、题目数量(5-100)、时间(5-120分钟)
- 考试界面：
  - 顶部：倒计时 + 题号
  - 中部：题目+选项
  - 底部：答题卡按钮 + 上一题/下一题
- 答题卡弹窗：显示所有题号，已答/未答/当前标记
- 交卷：手动交卷 / 倒计时结束自动交卷
- 交卷后跳转 ExamResultScreen

**交卷流程**：
```
交卷 → 收集所有答案 → 批量写入 answer_record
     → 答错题自动添加错题标记
     → 计算 correctCount/score
     → 更新 exam_session
     → 跳转结果页
```

### 4.6 ExamResultScreen (考试结果)

**功能**：
- 正确率百分比（大字显示）
- 评级：优秀/良好/及格/不及格
- 统计：总题数、正确数、错误数、未答数
- 用时
- 查看详情：逐题查看答题情况
- 返回首页

### 4.7 WrongQuestionsScreen (错题本)

**功能**：
- 筛选：全部/未掌握/基本掌握
- 题目列表：题号+题型+内容+掌握度标签
- 重做：逐题重做，答对提升掌握度
- 掌握度2的错题自动从列表隐藏
- 清空错题本

---

## 五、核心工具类设计

### 5.1 QuestionUtils

```kotlin
object QuestionUtils {
    private val OPTION_LABELS = listOf("A", "B", "C", "D", "E", "F", "G", "H")

    /** 解析选项JSON为带标签的列表 */
    fun parseOptions(optionsJson: String): List<OptionItem>

    /** 按题型解析答案 */
    fun parseAnswer(raw: String, type: String): Any

    /** 校验答案 */
    fun checkAnswer(userAnswer: Any, correctAnswer: Any, type: String): Boolean

    /** 推断题型 */
    fun inferType(answer: String): String
}

data class OptionItem(val label: String, val content: String)
```

### 5.2 CountdownTimer

```kotlin
class CountdownTimer(
    private val totalSeconds: Int,
    private val onTick: (remainingSeconds: Int) -> Unit,
    private val onFinish: () -> Unit
) {
    fun start()
    fun stop()
    fun pause()
    fun resume()
    fun formatTime(seconds: Int): String  // "MM:SS"
}
```

### 5.3 FileParser (文件导入解析)

```kotlin
object FileParser {
    /** 解析txt/tsv/csv文件为题目列表 */
    suspend fun parseFile(uri: Uri, context: Context): ParseResult

    /** 预览导入数据 */
    fun previewImport(data: List<RawQuestion>): PreviewResult
}

data class RawQuestion(
    val content: String,
    val category: String,
    val answer: String,
    val options: List<String>
)

data class ParseResult(
    val questions: List<RawQuestion>,
    val errors: List<ParseError>
)
```

**解析规则**（与源项目一致）：
- 分隔符：Tab 或 逗号
- 列顺序：题目内容 | 分类 | 答案 | 选项A | 选项B | 选项C | 选项D
- 首行检测：含"题目/题干/内容/question/content/序号/编号"视为表头跳过
- 题型推断：T/F→判断，多字母→多选，单字母→单选
- 判断题：选项A=正确文本，选项B=错误文本

---

## 六、关键流程设计

### 6.1 刷题续答流程

```
PracticeScreen.onEnter
  → practiceProgressDao.load()  // 查询是否有进度
  → 有进度 → 显示"续答"按钮
  → 用户点续答 → restoreProgress()
  → currentIndex 变化 → 自动触发 saveProgress()（300ms 防抖）

PracticeScreen.onExit
  → saveProgress()（立即保存）
  → 清空状态
```

### 6.2 考试交卷流程

```
ExamViewModel.finishExam()
  → 收集所有已答题目，构建 answerList
  → answerRecordDao.batchInsert(records)  // 批量写入
  → 答错题 → questionFlagDao.insertOrUpdate(wrong flag)
  → 计算 correctCount / score
  → examSessionDao.updateFinished(sessionId, correctCount, score)
  → 导航到 ExamResultScreen
```

### 6.3 导入流程

```
HomeScreen → 点击导入 → SAF文件选择器
  → FileParser.parseFile(uri)
  → 显示预览（有效/无效/错误列表）
  → 用户确认导入
  → questionDao.batchInsert(questions)  // 分批100条
  → 更新 questionBank.questionCount
  → 显示导入报告
```

### 6.4 错题掌握度更新

```
WrongQuestionsScreen → 答题
  → checkAnswer() → 正确/错误
  → 正确：masteryLevel 0→1 或 1→2
  → 错误：masteryLevel 1→0 或 2→1
  → masteryLevel == 2 → 自动从错题列表隐藏
  → questionFlagDao.updateMastery(questionId, newLevel)
```

---

## 七、准入规范合规检查清单

### 7.1 功能合规

| 检查项 | 要求 | 状态 |
|--------|------|------|
| 安装/卸载 | 正常安装、升级安装、卸载后重装 | 待验证 |
| 启动 | 新安装/升级后正常启动 | 待验证 |
| 运行稳定 | 遍历所有界面无崩溃、无ANR | 待验证 |
| 不影响其他应用 | 运行时不影响电话/短信/相机等 | 待验证 |
| 布局自适应 | 无黑白边、无错乱 | 待验证 |

### 7.2 性能合规

| 检查项 | 要求 | 验证方法 | 状态 |
|--------|------|----------|------|
| 冷启动 | < 3000ms | adb shell am start -W | 待验证 |
| 热启动 | < 1000ms | 前后台切换 | 待验证 |
| 帧率 | > 20FPS | GPU呈现模式分析 | 待验证 |
| CPU | 不持续超70% | Android Profiler | 待验证 |
| 内存 | 不持续超70% | Android Profiler | 待验证 |

### 7.3 隐私合规

| 检查项 | 要求 |
|--------|------|
| 权限 | 仅申请必要权限（文件读取用于导入） |
| 数据收集 | 不收集无关信息，不联网 |
| 权限提示 | 使用文件时显性提示 |

### 7.4 信息展现合规

| 检查项 | 要求 |
|--------|------|
| 应用名称 | ≤12字符，无特殊字符 |
| 图标 | 144×144px，圆角矩形，非透明/黑色背景 |
| 启动页 | 底部居中维护单位+电话，≤3秒 |
| 介绍 | ≤200字，含功能/对象/维护单位/电话 |

---

## 八、开发计划

### Phase 1：基础框架搭建

1. 初始化 Android 项目（Kotlin + Compose + Hilt + Room）
2. 创建双数据库（SystemDatabase + UserDatabase）
3. 实现所有 Entity + DAO
4. 实现 Repository 层
5. 搭建导航框架
6. 实现主题（Color + Typography + Theme）

### Phase 2：核心页面开发

1. HomeScreen（首页）
2. BankScreen + QuestionListScreen（题库管理）
3. PracticeScreen（刷题 + 续答）
4. ExamScreen + ExamResultScreen（模拟考试）
5. WrongQuestionsScreen（错题本）

### Phase 3：导入与工具

1. FileParser（txt/csv 解析）
2. 导入流程（SAF + 预览 + 导入）
3. QuestionUtils（答案校验 + 题型推断）
4. CountdownTimer（倒计时）

### Phase 4：测试与优化

1. 单元测试（Repository + ViewModel + Utils）
2. 性能优化（冷启动、内存、帧率）
3. 准入规范合规检查
4. APK 构建与签名

---

## 九、风险与注意事项

| 风险 | 影响 | 应对 |
|------|------|------|
| Room 双数据库配置复杂 | 中 | 使用 @TypeConverter 处理JSON字段，两个 RoomDatabase 分别提供 |
| Compose 列表性能 | 中 | 大量题目使用 LazyColumn + key，避免重组 |
| 文件导入权限 | 低 | 使用 SAF (Storage Access Framework)，不需要 READ_EXTERNAL_STORAGE |
| 冷启动时间 | 高 | 使用 SplashScreen API，延迟初始化非关键组件 |
| 内存占用 | 中 | 题目列表分页加载，图片不缓存，及时释放资源 |
| 鸿蒙2.0兼容性 | 中 | targetSdk 33，避免使用最新API，在Mate30上实测 |
