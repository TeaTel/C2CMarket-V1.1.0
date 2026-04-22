# 🛡️ Skill Protector 技能保护器

## 概述

Skill Protector 是一个专门设计用来保护 Trae IDE 技能文件不被误删除的工具。它提供删除前的确认提示、自动备份和恢复功能，确保您的技能投资得到充分保护。

## 快速开始

### 1. 启用保护
```bash
# 给保护脚本执行权限
chmod +x .trae/skills/skill-protector/protect.sh

# 测试保护功能
.trae/skills/skill-protector/protect.sh help
```

### 2. 使用保护删除
```bash
# 使用保护删除功能（而不是直接使用 rm）
.trae/skills/skill-protector/protect.sh protect .trae/skills/技能名称
```

### 3. 创建别名（推荐）
```bash
# 在 ~/.bashrc 或 ~/.zshrc 中添加
alias rm-protected='.trae/skills/skill-protector/protect.sh protect'
alias skill-backup='.trae/skills/skill-protector/protect.sh backup'
alias skill-restore='.trae/skills/skill-protector/protect.sh restore'
alias skill-stats='.trae/skills/skill-protector/protect.sh stats'
```

## 核心功能

### 🔒 删除保护
- **智能检测**: 自动识别技能文件删除操作
- **确认提示**: 删除前显示详细警告信息
- **备份创建**: 自动创建技能备份
- **操作记录**: 记录所有删除操作

### 💾 备份管理
- **自动备份**: 删除前自动创建 tar.gz 备份
- **备份列表**: 查看所有可用备份
- **一键恢复**: 从备份快速恢复技能
- **备份清理**: 管理备份存储空间

### 📊 统计监控
- **技能统计**: 显示技能总数和使用情况
- **备份统计**: 查看备份数量和大小
- **删除历史**: 查看最近的删除记录
- **使用分析**: 了解技能使用频率

## 使用示例

### 示例1：删除技能时的保护
```bash
# 尝试删除 github 技能
$ .trae/skills/skill-protector/protect.sh protect .trae/skills/github

╔══════════════════════════════════════════════════════╗
║                 🛡️  技能保护器警告                  ║
╠══════════════════════════════════════════════════════╣
║ 您正在尝试删除技能: github
║
║ 这个操作可能会影响相关功能的使用。
║
║ 是否确认删除？(y/n): 
╚══════════════════════════════════════════════════════╝
> n
操作已取消
```

### 示例2：确认删除并创建备份
```bash
# 确认删除 temp-skill 技能
$ .trae/skills/skill-protector/protect.sh protect .trae/skills/temp-skill

... [显示警告信息] ...
> y
正在删除技能: temp-skill
正在创建备份: .trae/backups/skills/temp-skill_20250422_143022.tar.gz
✅ 备份创建成功
✅ 技能删除完成
📝 删除操作已记录
```

### 示例3：查看备份和统计
```bash
# 列出所有备份
$ .trae/skills/skill-protector/protect.sh list

可用备份列表:
══════════════════════════════════════════════════════
🔹 github - 20250422_143022
   路径: .trae/backups/skills/github_20250422_143022.tar.gz

🔹 temp-skill - 20250422_142015
   路径: .trae/backups/skills/temp-skill_20250422_142015.tar.gz

# 查看统计信息
$ .trae/skills/skill-protector/protect.sh stats

技能保护器统计信息:
══════════════════════════════════════════════════════
📊 技能总数: 6
💾 备份数量: 2
🗑️  删除记录: 1

最近删除记录:
──────────────────────────────────────────────────────
⏰ 2025-04-22 14:30:22 - temp-skill
   备份: temp-skill_20250422_143022.tar.gz
```

### 示例4：从备份恢复技能
```bash
# 恢复误删除的 github 技能
$ .trae/skills/skill-protector/protect.sh restore .trae/backups/skills/github_20250422_143022.tar.gz

正在从备份恢复: github_20250422_143022.tar.gz
✅ 恢复成功
```

## 集成到工作流

### 方法1：替换 rm 命令（谨慎使用）
```bash
# 在 shell 配置文件中添加
alias rm='.trae/skills/skill-protector/protect.sh protect'
```

### 方法2：使用专用命令
```bash
# 创建专用命令别名
alias sp='.trae/skills/skill-protector/protect.sh'
alias sp-protect='sp protect'
alias sp-backup='sp backup'
alias sp-restore='sp restore'
alias sp-list='sp list'
alias sp-stats='sp stats'
```

### 方法3：定期备份计划
```bash
# 添加到 crontab，每天凌晨备份所有技能
0 2 * * * /Users/teatel/Documents/学校文档/软件工程/platform0.0.1/.trae/skills/skill-protector/protect.sh backup-all
```

## 配置选项

### 保护级别设置
编辑 `protect.sh` 文件中的以下变量：
```bash
# 保护级别 (low, medium, high)
PROTECTION_LEVEL="medium"

# 是否自动创建备份
AUTO_BACKUP="true"

# 备份保留天数
BACKUP_RETENTION_DAYS=7
```

### 技能分类保护
在技能目录中创建 `.protection` 文件：
```yaml
# .trae/skills/github/.protection
protection_level: high
allow_delete: false
require_admin: true
description: "核心GitHub集成技能，禁止删除"
```

## 最佳实践

### 1. 定期备份重要技能
```bash
# 每周备份核心技能
0 3 * * 1 .trae/skills/skill-protector/protect.sh backup github
0 3 * * 1 .trae/skills/skill-protector/protect.sh backup multi-agent-coordinator
```

### 2. 团队协作规范
```markdown
# 团队技能管理规范
1. 删除技能前必须使用保护命令
2. 重要技能删除需要团队负责人批准
3. 定期检查备份完整性
4. 新成员培训时介绍技能保护器
```

### 3. 监控和告警
```bash
# 监控技能目录变化
inotifywait -m -r .trae/skills/ -e delete |
while read path action file; do
    # 发送通知
    echo "警告: 技能文件被删除 - $file" | mail -s "技能保护告警" admin@example.com
done
```

## 故障排除

### 常见问题

#### Q1: 保护脚本没有执行权限
```bash
# 解决方案：添加执行权限
chmod +x .trae/skills/skill-protector/protect.sh
```

#### Q2: 备份创建失败
```bash
# 检查备份目录权限
chmod 755 .trae/backups/
mkdir -p .trae/backups/skills
```

#### Q3: 恢复后技能不工作
```bash
# 重新加载技能
# 重启 Trae IDE 或重新扫描技能目录
```

#### Q4: 保护器不识别技能文件
```bash
# 检查技能目录路径
echo "当前技能目录: $SKILLS_DIR"
# 确保路径正确指向 .trae/skills/
```

## 高级功能

### 技能依赖分析
```bash
# 分析技能间的依赖关系（未来版本）
skill-protector analyze-dependencies
```

### 使用频率统计
```bash
# 统计技能使用频率（未来版本）
skill-protector usage-stats --period 30d
```

### 自动清理旧备份
```bash
# 自动清理7天前的备份（未来版本）
skill-protector cleanup --older-than 7d
```

## 贡献和反馈

如果您发现任何问题或有改进建议，请：
1. 检查现有备份是否完整
2. 使用 `skill-stats` 查看当前状态
3. 提交问题报告或功能请求

## 许可证

本技能保护器遵循 MIT 许可证。您可以自由使用、修改和分发，但请保留原始版权声明。

---

**记住**: 一次备份，十分安心。使用技能保护器，让您的开发环境更加安全可靠！