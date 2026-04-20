#!/bin/bash

# 校园二手交易平台项目清理脚本
# 将不必要的文件移动到临时文件夹

set -e  # 遇到错误立即退出

echo "========================================="
echo "校园二手交易平台 - 项目清理脚本"
echo "========================================="

# 创建临时文件夹
TEMP_DIR="temp-archive-$(date +%Y%m%d-%H%M%S)"
echo "创建临时文件夹: $TEMP_DIR"
mkdir -p "$TEMP_DIR"

echo "开始清理项目..."

# 1. 移动到临时文件夹的文件列表
echo ""
echo "1. 移动重复的SQL文件到临时文件夹..."

# 检查并移动重复的SQL文件
if [ -f "env-setup/backend/init-scripts/01-schema.sql" ]; then
    echo "  移动: env-setup/backend/init-scripts/01-schema.sql"
    mv "env-setup/backend/init-scripts/01-schema.sql" "$TEMP_DIR/"
fi

if [ -f "env-setup/backend/init-scripts/02-data.sql" ]; then
    echo "  移动: env-setup/backend/init-scripts/02-data.sql"
    mv "env-setup/backend/init-scripts/02-data.sql" "$TEMP_DIR/"
fi

# 2. 移动开发过程文档
echo ""
echo "2. 移动开发过程文档到临时文件夹..."

for doc in "env-setup/校园二手交易平台_2026-"*.md; do
    if [ -f "$doc" ]; then
        echo "  移动: $doc"
        mv "$doc" "$TEMP_DIR/"
    fi
done

# 3. 移动设计文档
echo ""
echo "3. 移动设计文档到临时文件夹..."

if [ -f "env-setup/校园二手交易平台_数据库设计.md" ]; then
    echo "  移动: env-setup/校园二手交易平台_数据库设计.md"
    mv "env-setup/校园二手交易平台_数据库设计.md" "$TEMP_DIR/"
fi

if [ -f "env-setup/校园二手交易平台_用户故事地图.md" ]; then
    echo "  移动: env-setup/校园二手交易平台_用户故事地图.md"
    mv "env-setup/校园二手交易平台_用户故事地图.md" "$TEMP_DIR/"
fi

# 4. 移动图片文件
echo ""
echo "4. 移动图片文件到临时文件夹..."

if [ -f "env-setup/tongyi-mermaid-2026-03-27-104647.png" ]; then
    echo "  移动: env-setup/tongyi-mermaid-2026-03-27-104647.png"
    mv "env-setup/tongyi-mermaid-2026-03-27-104647.png" "$TEMP_DIR/"
fi

# 5. 检查并移动完整的数据库设计文档（如果与现有SQL文件重复）
echo ""
echo "5. 检查数据库设计文档..."

if [ -f "env-setup/校园二手交易平台_数据库.sql" ]; then
    echo "  移动: env-setup/校园二手交易平台_数据库.sql"
    mv "env-setup/校园二手交易平台_数据库.sql" "$TEMP_DIR/"
fi

# 6. 清理后端构建产物（如果存在）
echo ""
echo "6. 检查后端构建产物..."

if [ -d "env-setup/backend/target" ]; then
    echo "  发现: env-setup/backend/target 目录"
    read -p "  是否移动到临时文件夹? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "  移动: env-setup/backend/target"
        mv "env-setup/backend/target" "$TEMP_DIR/"
    fi
fi

# 7. 清理前端构建产物（如果存在）
echo ""
echo "7. 检查前端构建产物..."

if [ -d "env-setup/frontend/node_modules" ]; then
    echo "  发现: env-setup/frontend/node_modules 目录 (41MB)"
    read -p "  是否移动到临时文件夹? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "  移动: env-setup/frontend/node_modules"
        mv "env-setup/frontend/node_modules" "$TEMP_DIR/"
    fi
fi

if [ -d "env-setup/frontend/dist" ]; then
    echo "  发现: env-setup/frontend/dist 目录 (324KB)"
    read -p "  是否移动到临时文件夹? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "  移动: env-setup/frontend/dist"
        mv "env-setup/frontend/dist" "$TEMP_DIR/"
    fi
fi

# 8. 检查重复的配置文件
echo ""
echo "8. 检查重复的配置文件..."

# 检查railway.toml和nixpacks.toml是否已被删除
if [ -f "env-setup/railway.toml" ]; then
    echo "  发现: env-setup/railway.toml (已被用户删除)"
fi

if [ -f "env-setup/nixpacks.toml" ]; then
    echo "  发现: env-setup/nixpacks.toml (已被用户删除)"
fi

# 9. 创建.gitignore文件（如果不存在）
echo ""
echo "9. 检查.gitignore文件..."

if [ ! -f "env-setup/.gitignore" ]; then
    echo "  创建: env-setup/.gitignore"
    cat > "env-setup/.gitignore" << 'EOF'
# 构建产物
backend/target/
frontend/node_modules/
frontend/dist/
frontend/.vite/

# 日志文件
*.log
logs/

# 操作系统文件
.DS_Store
Thumbs.db

# IDE文件
.idea/
.vscode/
*.swp
*.swo

# 环境变量文件（除了模板）
.env.local
.env.*.local

# 临时文件
*.tmp
*.temp

# 测试覆盖率
coverage/
.nyc_output/

# 生产环境文件
.env.production
EOF
else
    echo "  已存在: env-setup/.gitignore"
fi

echo ""
echo "========================================="
echo "清理完成!"
echo "========================================="
echo ""
echo "已移动到临时文件夹 '$TEMP_DIR' 的内容:"
echo "-----------------------------------------"
if [ -d "$TEMP_DIR" ]; then
    find "$TEMP_DIR" -type f | while read file; do
        size=$(du -h "$file" | cut -f1)
        echo "  $size - $file"
    done
fi

echo ""
echo "项目剩余大小:"
du -sh env-setup/backend/ env-setup/frontend/ 2>/dev/null || true

echo ""
echo "建议下一步操作:"
echo "1. 检查临时文件夹内容，确认没有误移动重要文件"
echo "2. 如果需要恢复文件，可以从 '$TEMP_DIR' 复制回来"
echo "3. 运行 'git status' 查看文件变更"
echo "4. 提交清理后的项目到GitHub"
echo ""
echo "注意: 临时文件夹不会被推送到GitHub，请根据需要备份重要文件"