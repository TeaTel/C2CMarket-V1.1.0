#!/bin/bash

# skill-protector 保护脚本
# 这个脚本提供基本的技能文件保护功能

SKILLS_DIR=".trae/skills"
BACKUP_DIR=".trae/backups/skills"
LOG_FILE=".trae/skills/deletion_log.txt"

# 创建必要的目录
mkdir -p "$BACKUP_DIR"
touch "$LOG_FILE"

# 检查文件是否是技能文件
is_skill_file() {
    local file="$1"
    [[ "$file" == *"$SKILLS_DIR/"* ]] && return 0 || return 1
}

# 提取技能名称
extract_skill_name() {
    local path="$1"
    local skill_name=$(echo "$path" | sed "s|.*$SKILLS_DIR/||" | cut -d'/' -f1)
    echo "$skill_name"
}

# 显示警告信息
show_warning() {
    local skill_name="$1"
    echo ""
    echo "╔══════════════════════════════════════════════════════╗"
    echo "║                 🛡️  技能保护器警告                  ║"
    echo "╠══════════════════════════════════════════════════════╣"
    echo "║ 您正在尝试删除技能: $skill_name"
    echo "║"
    echo "║ 这个操作可能会影响相关功能的使用。"
    echo "║"
    echo "║ 是否确认删除？(y/n): "
    echo "╚══════════════════════════════════════════════════════╝"
    echo -n "> "
}

# 创建备份
create_backup() {
    local skill_path="$1"
    local skill_name=$(extract_skill_name "$skill_path")
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="$BACKUP_DIR/${skill_name}_${timestamp}.tar.gz"
    
    echo "正在创建备份: $backup_file"
    tar -czf "$backup_file" -C "$SKILLS_DIR" "$skill_name" 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo "✅ 备份创建成功"
        echo "$backup_file"
    else
        echo "❌ 备份创建失败"
        echo ""
    fi
}

# 记录删除操作
log_deletion() {
    local skill_name="$1"
    local backup_file="$2"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    echo "$timestamp|$skill_name|$backup_file|$USER" >> "$LOG_FILE"
}

# 保护删除函数
protected_delete() {
    local target="$1"
    
    if is_skill_file "$target"; then
        local skill_name=$(extract_skill_name "$target")
        
        show_warning "$skill_name"
        read -r confirm
        
        if [[ "$confirm" == "y" || "$confirm" == "Y" ]]; then
            echo "正在删除技能: $skill_name"
            
            # 创建备份
            local backup_file=$(create_backup "$target")
            
            if [ -n "$backup_file" ]; then
                # 执行删除
                rm -rf "$target"
                echo "✅ 技能删除完成"
                
                # 记录删除
                log_deletion "$skill_name" "$backup_file"
                echo "📝 删除操作已记录"
            else
                echo "❌ 由于备份失败，删除操作已取消"
            fi
        else
            echo "操作已取消"
        fi
    else
        # 非技能文件，直接删除
        rm -rf "$target"
    fi
}

# 列出备份
list_backups() {
    echo "可用备份列表:"
    echo "══════════════════════════════════════════════════════"
    
    if [ -z "$(ls -A "$BACKUP_DIR" 2>/dev/null)" ]; then
        echo "暂无备份"
    else
        find "$BACKUP_DIR" -name "*.tar.gz" | sort -r | while read backup; do
            local filename=$(basename "$backup")
            local skill_name=$(echo "$filename" | cut -d'_' -f1)
            local timestamp=$(echo "$filename" | cut -d'_' -f2- | sed 's/\.tar\.gz//')
            
            echo "🔹 $skill_name - $timestamp"
            echo "   路径: $backup"
            echo ""
        done
    fi
}

# 恢复备份
restore_backup() {
    local backup_file="$1"
    
    if [ ! -f "$backup_file" ]; then
        echo "错误: 备份文件不存在: $backup_file"
        return 1
    fi
    
    echo "正在从备份恢复: $(basename "$backup_file")"
    
    # 解压备份
    tar -xzf "$backup_file" -C "$SKILLS_DIR"
    
    if [ $? -eq 0 ]; then
        echo "✅ 恢复成功"
    else
        echo "❌ 恢复失败"
    fi
}

# 显示使用统计
show_stats() {
    echo "技能保护器统计信息:"
    echo "══════════════════════════════════════════════════════"
    
    # 统计技能数量
    local skill_count=$(find "$SKILLS_DIR" -maxdepth 1 -type d | wc -l)
    echo "📊 技能总数: $((skill_count - 1))"
    
    # 统计备份数量
    local backup_count=$(find "$BACKUP_DIR" -name "*.tar.gz" 2>/dev/null | wc -l)
    echo "💾 备份数量: $backup_count"
    
    # 统计删除记录
    local deletion_count=$(wc -l < "$LOG_FILE" 2>/dev/null || echo "0")
    echo "🗑️  删除记录: $deletion_count"
    
    echo ""
    echo "最近删除记录:"
    echo "──────────────────────────────────────────────────────"
    
    if [ -f "$LOG_FILE" ] && [ -s "$LOG_FILE" ]; then
        tail -5 "$LOG_FILE" | while read line; do
            local timestamp=$(echo "$line" | cut -d'|' -f1)
            local skill_name=$(echo "$line" | cut -d'|' -f2)
            local backup_file=$(echo "$line" | cut -d'|' -f3)
            
            echo "⏰ $timestamp - $skill_name"
            echo "   备份: $(basename "$backup_file")"
            echo ""
        done
    else
        echo "暂无删除记录"
    fi
}

# 主函数
main() {
    case "$1" in
        "protect")
            if [ -z "$2" ]; then
                echo "用法: $0 protect <文件或目录>"
                exit 1
            fi
            protected_delete "$2"
            ;;
        "backup")
            if [ -z "$2" ]; then
                echo "用法: $0 backup <技能名称>"
                exit 1
            fi
            create_backup "$SKILLS_DIR/$2"
            ;;
        "restore")
            if [ -z "$2" ]; then
                echo "用法: $0 restore <备份文件>"
                exit 1
            fi
            restore_backup "$2"
            ;;
        "list")
            list_backups
            ;;
        "stats")
            show_stats
            ;;
        "help")
            echo "技能保护器命令:"
            echo "  protect <目标>    - 保护删除文件或目录"
            echo "  backup <技能名>   - 备份指定技能"
            echo "  restore <备份文件> - 从备份恢复技能"
            echo "  list             - 列出所有备份"
            echo "  stats            - 显示统计信息"
            echo "  help             - 显示帮助信息"
            ;;
        *)
            echo "技能保护器已加载"
            echo "使用 '$0 help' 查看可用命令"
            ;;
    esac
}

# 执行主函数
main "$@"