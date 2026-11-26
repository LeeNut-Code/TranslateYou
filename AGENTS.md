# 工作区协作说明

## 项目定位

- 本项目目录现在同时包含 Translate You 应用源码，以及 LLM 翻译引擎的本地需求/设计文档。
- 源码此前位于 `/tmp/TranslateYou`，已迁入当前项目根目录；旧目录暂时保留为迁移来源，不作为后续开发目标。
- 源码最初来自 `LeeNut-Code/TranslateYou` fork，旧检出中的 `origin` 指向个人 fork、`upstream` 指向 `you-apps/TranslateYou`。当前项目目录的 Git 仓库需单独核对 remote、分支和变更后再执行 push。
- 若项目布局或 remote 发生变化，先核实实际状态并同步更新文档。
- `doc/` 用于本地需求文档、技术设计和开发笔记，已在 `.gitignore` 中排除；不要为了让文档进入版本控制而擅自修改此约定。

## 项目结构

- `./app/`：Android 主应用，Kotlin、Jetpack Compose、Material Design 3。
- `./translation-engines/`：纯 JVM/Kotlin 翻译引擎库。
- `./fastlane/`：发布元数据与应用截图。
- `./doc/`：本地需求、技术设计和开发笔记（Git 忽略）。

## LLM 引擎实现约定

- 目标是通用 OpenAI Chat Completions 兼容接口，不是 DeepSeek 专用实现。DeepSeek 只是预期提供商之一。
- Base URL、可选 API Key、自由输入 model ID 由用户配置；不硬编码特定服务商模型清单或私有参数。
- 引擎实现放在 `translation-engines/src/main/java/net/youapps/translation_engines/` 下，并通过 `TranslationEngines.getAllEngines()` 注册。
- 现有 `EnginePref.kt` 的模型 UI 只支持固定 `supportedModels` 下拉。要支持任意模型 ID，应增加通用能力标记和文本输入，不要用引擎名称字符串做 UI 分支。
- 复用 `TranslationEngine` / `Translation` / `EngineSettingsProvider` 和现有错误处理流程，避免在 LLM 引擎之外重写翻译架构。
- `RetrofitHelper` 当前默认 BODY 日志会暴露请求/响应正文。LLM 请求包含用户原文和译文，必须使用无 BODY 日志的客户端，并确保 Authorization Key 不进入日志。
- 对响应内容使用容错解析；无有效 choices/content 或空译文应明确失败，不要伪装成成功结果。
- 引擎语言列表必须和应用的语言 code 兼容；实现前检查 `app/src/main/java/com/bnyro/translate/util/LocaleHelper.kt`，重点验证 `zh-rCN` 与 `zh-rTW`。

## 修改与验证

- 先阅读当前项目中的相关代码及其 `AGENTS.md`（如存在），再实施；设计文档不是现有源码行为的替代品。
- 保持改动聚焦，不调整无关引擎或 UI。
- 为 Prompt、请求/响应映射、解析兜底、Key header 和 URL 拼接增加 JVM 测试；测试不应访问真实服务或依赖真实 API Key。
- 在项目根目录运行引擎模块验证：

  ```bash
  ./gradlew :translation-engines:test :translation-engines:build
  ```

- 当前设备只用于编写代码，不运行测试或构建；测试需在其他设备执行。
- 若测试因 Android SDK、网络或环境配置无法运行，在交付时说明具体阻塞，不得声称验证通过。
- 推送前检查当前项目的 `git status`、`git diff` 和 `git remote -v`；不要提交本地 Key、`local.properties`、构建产物或 IDE 私有配置。

## 文档约定

- 需求和技术文档使用中文 Markdown，放在本工作区 `doc/` 中。
- `doc/` 被 Git 排除；根目录 `AGENTS.md` 是协作说明文件，正常纳入版本控制。
- 文档描述与实际代码不一致时，以经过核对的代码为准，并同步更新文档。
