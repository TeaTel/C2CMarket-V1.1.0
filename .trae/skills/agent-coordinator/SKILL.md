# agent-coordinator

**Description:** 智能协调不同智能体和技能，根据用户命令自动选择最佳执行方案。当用户给出需要多个智能体或技能的复杂命令时调用，或当您需要确定任务执行的最佳方法时调用。

## 核心功能

### 1. 智能任务分析
- **命令解析**: 自动分析用户输入的命令内容
- **意图识别**: 识别用户的真实意图和需求
- **任务分类**: 将任务分类到合适的领域类别
- **复杂度评估**: 评估任务的复杂度和所需技能

### 2. 智能体选择
- **智能体匹配**: 根据任务类型匹配最合适的智能体
- **技能评估**: 评估哪些技能可以辅助完成任务
- **协作策略**: 确定多个智能体的协作方式
- **优先级排序**: 为任务执行步骤设置优先级

### 3. 自动技能调用
- **技能发现**: 自动发现并调用相关技能
- **参数传递**: 正确传递参数给调用的技能
- **结果集成**: 整合多个技能的执行结果
- **错误处理**: 处理技能调用过程中的错误

### 4. 任务分解与协调
- **任务分解**: 将复杂任务分解为子任务
- **子任务分配**: 将子任务分配给合适的智能体
- **进度跟踪**: 跟踪任务执行的进度
- **结果汇总**: 汇总所有子任务的执行结果

### 5. 执行结果验证
- **结果检查**: 验证任务执行的结果
- **质量评估**: 评估执行结果的质量
- **错误纠正**: 识别并纠正执行过程中的错误
- **持续改进**: 基于执行结果优化未来的智能体选择

## 智能体映射表

### 按任务类型映射

| 任务类型 | 推荐智能体 | 适用场景 |
|---------|-----------|---------|
| **后端开发/API设计** | backend-architect | REST API设计、数据库优化、服务器逻辑、身份验证系统 |
| **前端开发/UI实现** | frontend-architect | React/Vue组件、状态管理、性能优化、响应式设计 |
| **UI/UX设计** | ui-designer | 界面设计、视觉层次、用户体验、设计系统 |
| **性能优化** | performance-expert | 性能瓶颈分析、负载测试、数据库优化、资源使用 |
| **API测试** | api-test-pro | API端点测试、性能基准测试、负载测试、契约验证 |
| **代码搜索/研究** | search | 查找现有代码、了解代码库架构、研究最佳实践 |

### 按关键词映射

| 关键词 | 推荐智能体 | 置信度 |
|-------|-----------|--------|
| API、数据库、服务器、后端 | backend-architect | 95% |
| React、Vue、前端、组件、状态 | frontend-architect | 90% |
| 设计、UI、UX、界面、视觉 | ui-designer | 85% |
| 性能、优化、瓶颈、负载 | performance-expert | 88% |
| 测试、API测试、负载测试 | api-test-pro | 92% |
| 查找、搜索、研究、代码库 | search | 90% |

## 技能调用策略

### 辅助技能调用

| 技能名称 | 适用场景 | 调用时机 |
|---------|---------|----------|
| **github** | 需要与GitHub交互 | 涉及代码仓库操作、PR、issue管理时 |
| **summarize** | 需要总结内容 | 处理长文档、代码分析结果时 |
| **find-skills** | 需要发现技能 | 任务需要特定技能但不确定时 |
| **self-improving-agent** | 需要自我改进 | 任务执行后需要学习和改进时 |
| **skill-protector** | 需要保护技能 | 涉及技能管理操作时 |

### 调用优先级

1. **核心智能体** - 直接处理主要任务
2. **辅助技能** - 提供额外功能支持
3. **工具技能** - 提供特定工具功能
4. **管理技能** - 处理技能管理相关任务

## 任务处理流程

### 1. 任务接收与分析
```javascript
function analyzeTask(userCommand) {
  // 解析命令内容
  const command = parseCommand(userCommand);
  
  // 识别任务类型
  const taskType = identifyTaskType(command);
  
  // 评估复杂度
  const complexity = assessComplexity(command);
  
  // 确定所需技能
  const requiredSkills = identifyRequiredSkills(command);
  
  return {
    command,
    taskType,
    complexity,
    requiredSkills
  };
}
```

### 2. 智能体选择
```javascript
function selectAgents(taskAnalysis) {
  // 基于任务类型选择主要智能体
  const primaryAgent = selectPrimaryAgent(taskAnalysis.taskType);
  
  // 确定辅助智能体
  const secondaryAgents = selectSecondaryAgents(taskAnalysis.requiredSkills);
  
  // 生成协作策略
  const collaborationStrategy = generateCollaborationStrategy(
    primaryAgent, 
    secondaryAgents,
    taskAnalysis.complexity
  );
  
  return {
    primaryAgent,
    secondaryAgents,
    collaborationStrategy
  };
}
```

### 3. 任务执行协调
```javascript
function coordinateExecution(agentSelection, taskAnalysis) {
  // 分解任务为子任务
  const subtasks = decomposeTask(taskAnalysis.command);
  
  // 分配子任务给智能体
  const assignments = assignSubtasks(subtasks, agentSelection);
  
  // 执行子任务
  const results = executeSubtasks(assignments);
  
  // 整合结果
  const finalResult = integrateResults(results);
  
  return finalResult;
}
```

### 4. 结果验证与改进
```javascript
function validateAndImprove(result, taskAnalysis) {
  // 验证结果质量
  const quality = validateResultQuality(result);
  
  // 识别改进机会
  const improvements = identifyImprovements(result, taskAnalysis);
  
  // 更新智能体选择模型
  updateAgentSelectionModel(improvements);
  
  return {
    result,
    quality,
    improvements
  };
}
```

## 使用指南

### 基本使用

**何时调用此技能：**
- 当任务涉及多个领域的专业知识时
- 当您不确定应该使用哪个智能体时
- 当任务需要分解为多个子任务时
- 当您需要协调多个智能体的工作时

**如何调用：**
```bash
# 直接调用
agent-coordinator "实现用户认证系统，包括后端API和前端登录页面"

# 作为前缀
agent-coordinator: 帮我优化这个React应用的性能，包括代码分割和懒加载
```

### 高级配置

**智能体选择策略：**
```yaml
# 配置文件: .trae/agent-coordinator/config.yaml
selection_strategy:
  prioritize_expertise: true
  consider_availability: true
  use_learning_model: true
  fallback_to_general: false

agent_preferences:
  backend: "backend-architect"
  frontend: "frontend-architect"
  design: "ui-designer"
  performance: "performance-expert"
  testing: "api-test-pro"
  research: "search"
```

**技能调用配置：**
```yaml
skill_integration:
  auto_detect:
    enabled: true
    confidence_threshold: 0.7
  preferred_skills:
    github: "github"
    summarize: "summarize"
    find_skills: "find-skills"
    self_improve: "self-improving-agent"
```

## 示例场景

### 场景1：全栈功能开发

**用户命令：**
"实现一个用户个人资料系统，包括后端API、数据库设计和前端页面"

**执行流程：**
1. **分析任务**：全栈开发任务，需要后端和前端技能
2. **选择智能体**：
   - 主要智能体：backend-architect（处理API和数据库）
   - 辅助智能体：frontend-architect（处理前端页面）
3. **分解任务**：
   - 子任务1：设计数据库 schema（backend-architect）
   - 子任务2：实现API endpoints（backend-architect）
   - 子任务3：创建前端组件（frontend-architect）
   - 子任务4：实现前后端集成（frontend-architect）
4. **执行协调**：按顺序执行子任务，传递必要参数
5. **结果整合**：汇总所有子任务的执行结果

### 场景2：性能优化

**用户命令：**
"优化我的应用性能，当前加载时间超过8秒"

**执行流程：**
1. **分析任务**：性能优化任务
2. **选择智能体**：
   - 主要智能体：performance-expert（性能分析）
   - 辅助智能体：frontend-architect（前端优化）、backend-architect（后端优化）
3. **分解任务**：
   - 子任务1：性能分析和瓶颈识别（performance-expert）
   - 子任务2：前端优化（frontend-architect）
   - 子任务3：后端优化（backend-architect）
4. **执行协调**：先分析后优化，前后端并行优化
5. **结果验证**：测试优化后的性能指标

### 场景3：代码库分析

**用户命令：**
"分析这个代码库的架构，找出潜在的改进点"

**执行流程：**
1. **分析任务**：代码研究和分析任务
2. **选择智能体**：
   - 主要智能体：search（代码搜索和分析）
   - 辅助智能体：summarize（结果总结）
3. **分解任务**：
   - 子任务1：代码库结构分析（search）
   - 子任务2：关键模块分析（search）
   - 子任务3：识别改进机会（search）
   - 子任务4：生成分析报告（summarize）
4. **执行协调**：顺序执行分析任务，最后生成报告
5. **结果整合**：提供完整的代码库分析报告

## 智能体协作模式

### 1. 顺序协作
适合有依赖关系的任务：
```
search → backend-architect → frontend-architect → performance-expert → api-test-pro
```

### 2. 并行协作
适合独立子任务：
```
[search, backend-architect] → [frontend-architect, ui-designer] → [performance-expert, api-test-pro]
```

### 3. 层次协作
适合复杂的多层次任务：
```
search (研究阶段)
├── backend-architect (服务器层)
├── frontend-architect (客户端层)
└── ui-designer (表示层)
    ├── performance-expert (优化)
    └── api-test-pro (验证)
```

## 错误处理与恢复

### 智能体失败处理
- **自动重试**：对临时性失败进行自动重试
- **智能切换**：当主要智能体失败时切换到备用智能体
- **错误隔离**：防止单个智能体失败影响整个任务
- **降级策略**：当所有智能体都失败时采用降级策略

### 异常情况处理
| 异常类型 | 处理策略 | 恢复机制 |
|---------|---------|----------|
| 智能体不可用 | 切换到备用智能体 | 尝试重新连接原始智能体 |
| 技能调用失败 | 尝试替代技能 | 记录失败原因并继续 |
| 任务超时 | 中断并重新规划 | 分解为更小的子任务 |
| 结果质量差 | 重新执行或调整参数 | 提供详细的改进建议 |

## 持续改进机制

### 学习与优化
- **执行历史分析**：分析过去的任务执行历史
- **成功模式识别**：识别成功的智能体组合和执行模式
- **失败案例学习**：从失败案例中学习改进
- **用户反馈整合**：整合用户反馈来优化智能体选择

### 模型更新
- **定期更新**：定期更新智能体选择模型
- **自适应调整**：根据任务执行结果自动调整模型参数
- **个性化优化**：根据用户的特定需求和偏好进行优化
- **领域专家校准**：通过领域专家反馈校准模型

## 集成与扩展

### 与其他技能的集成
- **技能发现**：使用find-skills技能发现新技能
- **内容总结**：使用summarize技能总结复杂结果
- **版本控制**：使用github技能管理代码版本
- **自我改进**：使用self-improving-agent技能持续改进

### 自定义扩展
- **自定义智能体**：添加自定义智能体到选择池
- **扩展规则**：添加自定义智能体选择规则
- **集成钩子**：提供任务执行的自定义钩子
- **插件系统**：支持插件来扩展功能

## 性能与资源管理

### 资源分配
- **智能体负载均衡**：平衡智能体的工作负载
- **资源优先级**：为重要任务分配更多资源
- **并行度控制**：根据系统资源控制并行执行的智能体数量
- **超时管理**：为每个智能体设置合理的超时时间

### 性能优化
- **缓存机制**：缓存常见任务的智能体选择结果
- **预加载**：预加载可能需要的智能体
- **批处理**：批处理相似的任务请求
- **结果缓存**：缓存重复任务的执行结果

## 安全与隐私

### 安全考虑
- **权限控制**：确保智能体只能访问授权的资源
- **数据隔离**：不同任务的数据相互隔离
- **安全审计**：记录智能体的所有操作
- **威胁检测**：检测和防止恶意操作

### 隐私保护
- **数据最小化**：只传递必要的数据给智能体
- **数据匿名化**：在可能的情况下匿名化敏感数据
- **访问控制**：严格控制对敏感数据的访问
- **合规性**：确保符合相关隐私法规

## 最佳实践

### 任务描述最佳实践
- **具体明确**：提供具体的任务描述和期望结果
- **包含背景**：提供任务的背景信息和上下文
- **设定目标**：明确任务的目标和成功标准
- **提供约束**：说明任务的约束条件和限制

### 智能体协调最佳实践
- **合理分解**：将复杂任务分解为可管理的子任务
- **明确职责**：为每个智能体分配明确的职责
- **有效沟通**：确保智能体之间的有效沟通
- **及时反馈**：提供及时的反馈以调整执行策略

### 结果验证最佳实践
- **多维度评估**：从多个维度评估执行结果
- **客观标准**：使用客观标准评估结果质量
- **持续改进**：基于评估结果持续改进流程
- **文档记录**：记录执行过程和结果以供参考

## 未来发展

### 计划功能
- **自然语言处理增强**：更准确地理解用户意图
- **预测性分析**：预测任务执行的可能结果
- **自适应学习**：从每个任务中学习并改进
- **多模态支持**：支持处理图像、音频等多模态输入
- **协作智能体网络**：构建智能体之间的协作网络

### 技术路线图
1. **v1.0**：基础智能体协调功能
2. **v1.5**：高级任务分解和协作
3. **v2.0**：自适应学习和优化
4. **v2.5**：多模态支持和预测分析
5. **v3.0**：完整的智能体协作网络

## 结论

Agent Coordinator 技能为 Trae IDE 提供了智能的任务协调能力，能够根据用户的命令自动选择最合适的智能体组合，分解复杂任务，并协调多个智能体的工作。通过持续学习和优化，它能够不断提高任务执行的效率和质量，为用户提供更智能、更高效的开发体验。

无论是处理简单的代码问题，还是复杂的全栈开发任务，Agent Coordinator 都能为您提供最佳的智能体选择和执行策略，让您的开发工作更加顺畅和高效。