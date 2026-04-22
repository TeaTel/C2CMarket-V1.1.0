# skill-protector

**Description:** 保护技能文件不被误删除，提供删除前的确认和备份机制。当执行可能删除技能文件的操作时自动触发保护机制。

## 核心功能

### 1. 技能文件检测
- 自动识别`.trae/skills/`目录下的所有技能文件
- 检测SKILL.md和_meta.json等关键文件
- 监控文件删除操作的目标路径

### 2. 删除保护机制
- 当检测到技能文件删除操作时，自动触发保护
- 提供删除确认提示
- 支持强制删除模式（需要明确授权）

### 3. 备份与恢复
- 在执行删除前自动创建备份
- 提供一键恢复功能
- 保留删除历史记录

### 4. 智能提醒
- 当用户尝试删除技能时显示警告
- 提供技能使用统计信息
- 显示技能依赖关系

## 使用场景

### 场景1：防止误删除技能
**触发条件**: 用户执行`rm -rf .trae/skills/`或类似命令
**保护动作**:
1. 检测到技能目录删除操作
2. 显示警告："您正在删除技能目录，这将影响所有技能功能"
3. 要求确认："确认删除？(y/n)"
4. 如果确认，创建备份后执行删除

### 场景2：单个技能删除保护
**触发条件**: 用户删除特定技能文件
**保护动作**:
1. 识别被删除的技能名称
2. 显示技能信息和使用统计
3. 询问："确定要删除技能'xxx'吗？"
4. 提供备份选项

### 场景3：批量操作保护
**触发条件**: 用户执行批量文件操作
**保护动作**:
1. 扫描所有受影响文件
2. 识别其中的技能文件
3. 显示受影响技能列表
4. 逐项确认或批量确认

## 实现机制

### 文件监控
```bash
# 监控技能目录的文件操作
monitor_skill_dir() {
    inotifywait -m -r .trae/skills/ -e delete -e moved_from |
    while read path action file; do
        if [[ "$action" == "DELETE" || "$action" == "MOVED_FROM" ]]; then
            trigger_protection "$path/$file"
        fi
    done
}
```

### 删除拦截
```bash
# 拦截删除命令
intercept_delete() {
    local target=$1
    
    # 检查是否是技能文件
    if [[ "$target" == *".trae/skills/"* ]]; then
        skill_name=$(extract_skill_name "$target")
        show_warning "$skill_name"
        
        if confirm_deletion; then
            create_backup "$target"
            execute_delete "$target"
        else
            cancel_operation
        fi
    fi
}
```

### 备份管理
```bash
# 创建技能备份
create_skill_backup() {
    local skill_path=$1
    local backup_dir=".trae/backups/skills/$(date +%Y%m%d_%H%M%S)"
    
    mkdir -p "$backup_dir"
    cp -r "$skill_path" "$backup_dir/"
    
    echo "备份已创建: $backup_dir"
    log_backup "$skill_path" "$backup_dir"
}
```

## 配置选项

### 保护级别设置
```yaml
protection_level:
  low:    # 仅警告
    - show_warning: true
    - require_confirmation: false
    - create_backup: false
    
  medium: # 默认级别
    - show_warning: true
    - require_confirmation: true
    - create_backup: true
    
  high:   # 严格保护
    - show_warning: true
    - require_confirmation: true
    - create_backup: true
    - allow_force_delete: false
```

### 白名单/黑名单
```yaml
skill_protection:
  whitelist:
    - "test-skills/"  # 测试技能可自由删除
    - "temp-skills/"  # 临时技能目录
    
  blacklist:
    - "core-skills/"  # 核心技能禁止删除
    - "multi-agent-coordinator/"  # 关键协调技能
```

## 集成指南

### 与现有技能集成
```bash
# 在技能创建时自动注册保护
register_skill_protection() {
    local skill_name=$1
    local skill_path=$2
    
    echo "$skill_name:$skill_path" >> .trae/skills/protected_skills.list
    echo "技能 '$skill_name' 已加入保护列表"
}
```

### 与文件系统集成
```bash
# 包装删除命令
protected_rm() {
    for file in "$@"; do
        if is_protected_skill "$file"; then
            skill_protector_intercept "$file"
        else
            /bin/rm "$file"
        fi
    done
}

# 包装文件移动命令
protected_mv() {
    local source=$1
    local dest=$2
    
    if is_protected_skill "$source"; then
        skill_protector_intercept "$source" "move"
    else
        /bin/mv "$source" "$dest"
    fi
}
```

## 恢复功能

### 从备份恢复
```bash
# 列出可用备份
list_skill_backups() {
    find .trae/backups/skills -name "*.backup" | sort -r
}

# 恢复特定备份
restore_skill_backup() {
    local backup_file=$1
    local skill_name=$(basename "$backup_file" .backup)
    
    echo "正在恢复技能: $skill_name"
    tar -xzf "$backup_file" -C ".trae/skills/"
    echo "恢复完成"
}
```

### 撤销删除
```bash
# 撤销最近的删除操作
undo_last_deletion() {
    local last_deletion=$(tail -1 .trae/skills/deletion_log.txt)
    
    if [[ -n "$last_deletion" ]]; then
        local backup_path=$(echo "$last_deletion" | cut -d'|' -f3)
        
        if [[ -f "$backup_path" ]]; then
            restore_from_backup "$backup_path"
            echo "删除操作已撤销"
        else
            echo "备份文件不存在，无法撤销"
        fi
    else
        echo "没有可撤销的删除记录"
    fi
}
```

## 使用示例

### 示例1：删除技能时的保护
```bash
# 用户尝试删除技能
$ rm -rf .trae/skills/github/

# 技能保护器触发
[技能保护器] 警告：您正在删除 'github' 技能
[技能保护器] 该技能最近被使用了 15 次
[技能保护器] 确认删除？(y/n): n
[技能保护器] 删除操作已取消
```

### 示例2：强制删除技能
```bash
# 用户确认删除
$ rm -rf .trae/skills/temp-skill/

[技能保护器] 警告：您正在删除 'temp-skill' 技能
[技能保护器] 确认删除？(y/n): y
[技能保护器] 正在创建备份...
[技能保护器] 备份已保存至: .trae/backups/skills/temp-skill_20250422_143022.backup
[技能保护器] 删除完成
```

### 示例3：恢复误删除的技能
```bash
# 用户恢复误删除的技能
$ skill-protector restore github

[技能保护器] 找到以下备份：
1) github_20250421_101522.backup
2) github_20250420_093015.backup
选择要恢复的备份 [1]: 1
[技能保护器] 正在恢复 github 技能...
[技能保护器] 恢复完成
```

## 最佳实践

### 1. 定期备份
```bash
# 设置定时备份
crontab -e
# 每天凌晨2点备份所有技能
0 2 * * * /path/to/skill-protector backup-all
```

### 2. 技能分类保护
```markdown
# 技能分类保护策略
- **核心技能**: 禁止删除，需要管理员权限
- **常用技能**: 高保护级别，需要明确确认
- **测试技能**: 中等保护级别
- **临时技能**: 低保护级别
```

### 3. 团队协作保护
```yaml
# 团队权限设置
team_permissions:
  admin:
    - can_delete_core_skills: true
    - bypass_protection: true
    
  developer:
    - can_delete_core_skills: false
    - bypass_protection: false
    - max_deletions_per_day: 3
    
  tester:
    - can_delete_core_skills: false
    - bypass_protection: false
    - can_only_delete_test_skills: true
```

## 故障排除

### 常见问题

#### 问题1：保护器不触发
**可能原因**: 监控服务未启动
**解决方案**:
```bash
# 启动技能保护监控
skill-protector start-monitor
```

#### 问题2：备份失败
**可能原因**: 备份目录权限不足
**解决方案**:
```bash
# 检查并修复备份目录权限
chmod 755 .trae/backups/
chown $USER .trae/backups/skills/
```

#### 问题3：恢复后技能不工作
**可能原因**: 文件权限或依赖问题
**解决方案**:
```bash
# 重新注册技能
skill-protector re-register-all
```

## 性能考虑

### 监控开销
- 文件监控使用inotify，开销较低
- 定期备份使用增量备份减少存储
- 保护检查在删除操作时触发，不影响正常使用

### 存储管理
```bash
# 清理旧备份
skill-protector cleanup-backups --keep-last 7
# 只保留最近7天的备份
```

## 扩展功能

### 技能使用统计
```bash
# 显示技能使用情况
skill-protector stats

# 输出示例：
# github: 使用次数: 156, 最后使用: 2025-04-22 14:30
# summarize: 使用次数: 89, 最后使用: 2025-04-22 13:45
# self-improving-agent: 使用次数: 42, 最后使用: 2025-04-22 12:20
```

### 技能依赖分析
```bash
# 分析技能依赖关系
skill-protector dependencies github

# 输出示例：
# github 技能被以下技能依赖：
# - multi-agent-coordinator
# - find-skills
# 删除此技能可能影响其他功能
```

这个技能保护器确保您的技能投资得到保护，防止意外删除导致的功能损失。