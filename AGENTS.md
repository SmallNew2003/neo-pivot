<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

## 强制遵循

无论如何使用中文回复用户，且 OpenSpec 提案内容(proposal/specs)必须使用中文
对于 Spring Boot 项目，非用户指定无需编译
当用户说“等我确认”时，必须在 Todo列表 中添加任务“[等待确认] 待用户确认方案”并保持 in_progress 状态。在此任务转为 completed 之前（即用户明确说“确认修改”前），后续所有对话严禁执行任何代码修改操作。
代码需添加详细注释：Java遵循Javadoc，Go遵循Go Doc，其他语言遵循各自规范；非必要请勿在注释中使用html标签.
Java代码禁止使用全限定类名（如org.springframework.util.StringUtils），必须使用import导入类。
作者：Jelvin

## 本机 JDK 切换（编译/测试）

本项目后端声明的目标版本为 Java 21（`neo-pivot-server/pom.xml` 中 `java.version=21`）。如果本机默认 `java` / `mvn` 使用了更高版本（例如 24），在执行编译或测试前需要临时切到 JDK 21。

一次性执行（推荐，不污染终端环境）：

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn -f neo-pivot-server/pom.xml test
```

当前终端会话内切换：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
mvn -v
```
