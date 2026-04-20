---
name: "multi-agent-coordinator"
description: "Coordinates all available agents for comprehensive task execution. Invoke when executing complex commands or when multi-disciplinary expertise is needed for optimal results."
---

# Multi-Agent Coordinator Skill

This skill coordinates all available specialized agents to work together on complex tasks, ensuring comprehensive coverage and optimal results across all aspects of software development.

## When to Use This Skill

**CRITICAL: Invoke this skill when:**
- Executing complex commands that require multiple areas of expertise
- Working on full-stack development tasks
- Troubleshooting multi-faceted issues
- Performing comprehensive code reviews
- Optimizing application performance across frontend and backend
- Deploying applications with multiple components
- Testing end-to-end user flows
- Refactoring large codebases
- Implementing new features with UI, API, and database components

## Available Agents and Their Roles

### 1. **search** - Research and Discovery Agent
**Role**: Locates code, documentation, and patterns across the codebase
**When to use**: 
- Finding existing implementations
- Understanding codebase architecture
- Researching best practices
- Discovering related files and modules

### 2. **backend-architect** - Backend Systems Agent
**Role**: Designs and implements server-side logic, APIs, and databases
**When to use**:
- API design and implementation
- Database schema design
- Business logic implementation
- Server optimization
- Authentication and authorization systems

### 3. **frontend-architect** - Frontend Development Agent
**Role**: Builds user interfaces, components, and state management
**When to use**:
- UI component development
- State management implementation
- Frontend performance optimization
- Routing and navigation
- Component architecture design

### 4. **ui-designer** - UI/UX Design Agent
**Role**: Creates user interfaces, design systems, and visual aesthetics
**When to use**:
- UI component design
- Design system implementation
- Visual hierarchy optimization
- User experience improvements
- Responsive design implementation

### 5. **performance-expert** - Performance Optimization Agent
**Role**: Conducts performance testing, profiling, and optimization
**When to use**:
- Performance bottleneck identification
- Load testing and optimization
- Database query optimization
- Application profiling
- Resource usage optimization

### 6. **api-test-pro** - API Testing Agent
**Role**: Performs comprehensive API testing including performance and contract testing
**When to use**:
- API endpoint testing
- Performance benchmarking
- Load testing scenarios
- Contract validation
- Integration testing

## Coordination Strategies

### Strategy 1: Sequential Coordination
For tasks with clear sequential dependencies:
```
search → backend-architect → frontend-architect → ui-designer → performance-expert → api-test-pro
```

**Example**: Implementing a new feature
1. **search**: Find existing patterns and related code
2. **backend-architect**: Design and implement API endpoints
3. **frontend-architect**: Create frontend components
4. **ui-designer**: Optimize UI/UX design
5. **performance-expert**: Profile and optimize performance
6. **api-test-pro**: Test API endpoints comprehensively

### Strategy 2: Parallel Coordination
For independent tasks that can be executed simultaneously:
```
[search, backend-architect] → [frontend-architect, ui-designer] → [performance-expert, api-test-pro]
```

**Example**: Full-stack optimization
- **search** and **backend-architect** work on backend improvements
- **frontend-architect** and **ui-designer** work on frontend enhancements
- **performance-expert** and **api-test-pro** validate improvements

### Strategy 3: Hierarchical Coordination
For complex projects with multiple layers:
```
search (research phase)
├── backend-architect (server layer)
├── frontend-architect (client layer)
└── ui-designer (presentation layer)
    ├── performance-expert (optimization)
    └── api-test-pro (validation)
```

## Implementation Patterns

### Pattern 1: Task Delegation Template
```markdown
# Multi-Agent Task Coordination

## Task: [Describe the task]

### Agent Assignments:
1. **search**: [Specific research tasks]
2. **backend-architect**: [Backend implementation tasks]
3. **frontend-architect**: [Frontend implementation tasks]
4. **ui-designer**: [UI/UX design tasks]
5. **performance-expert**: [Performance optimization tasks]
6. **api-test-pro**: [Testing and validation tasks]

### Coordination Flow:
[Describe how agents should coordinate]
```

### Pattern 2: Concurrent Execution Template
```javascript
// Example of concurrent agent execution
const agents = [
  { type: 'search', task: 'Find authentication patterns' },
  { type: 'backend-architect', task: 'Design auth API endpoints' },
  { type: 'frontend-architect', task: 'Create login components' },
  { type: 'ui-designer', task: 'Design login UI' },
  { type: 'performance-expert', task: 'Profile auth flow performance' },
  { type: 'api-test-pro', task: 'Test auth endpoints' }
];

// Execute all agents in parallel
agents.forEach(agent => {
  launchAgent(agent.type, agent.task);
});
```

## Common Use Cases

### Use Case 1: Full-Stack Feature Implementation
**Task**: Implement user profile management system
**Agent Coordination**:
1. **search**: Find existing user-related code and patterns
2. **backend-architect**: Design profile API endpoints (GET/PUT /api/users/profile)
3. **frontend-architect**: Create profile Vue components
4. **ui-designer**: Design profile page layout and styling
5. **performance-expert**: Optimize profile data loading
6. **api-test-pro**: Test profile API endpoints

### Use Case 2: Performance Optimization
**Task**: Optimize application loading performance
**Agent Coordination**:
1. **search**: Find performance bottlenecks in codebase
2. **performance-expert**: Profile application and identify issues
3. **backend-architect**: Optimize database queries and API responses
4. **frontend-architect**: Implement code splitting and lazy loading
5. **ui-designer**: Optimize image loading and rendering
6. **api-test-pro**: Load test optimized endpoints

### Use Case 3: Deployment Troubleshooting
**Task**: Fix deployment issues on Railway/Vercel
**Agent Coordination**:
1. **search**: Find deployment configuration files
2. **backend-architect**: Fix backend deployment configuration
3. **frontend-architect**: Fix frontend build configuration
4. **performance-expert**: Analyze deployment performance issues
5. **api-test-pro**: Test deployed API endpoints

## Best Practices for Agent Coordination

### 1. Clear Task Definition
- Define specific, measurable tasks for each agent
- Set clear expectations and deliverables
- Establish communication protocols between agents

### 2. Resource Management
- Monitor agent execution to prevent resource conflicts
- Balance workload across agents
- Handle agent failures gracefully

### 3. Result Integration
- Combine results from multiple agents into cohesive solutions
- Resolve conflicts between agent recommendations
- Create comprehensive documentation from multi-agent work

### 4. Quality Assurance
- Validate that all agent tasks are completed
- Ensure consistency across agent outputs
- Perform final integration testing

## Agent Communication Protocols

### Protocol 1: Shared Context
```yaml
# Shared context file for agent coordination
shared_context:
  project: campus-marketplace
  task_id: user-auth-implementation
  agents_involved:
    - search
    - backend-architect
    - frontend-architect
    - api-test-pro
  shared_artifacts:
    - api_spec.yaml
    - database_schema.sql
    - component_structure.md
  communication_channels:
    - shared_logs.txt
    - agent_coordination.md
```

### Protocol 2: Result Aggregation
```markdown
# Agent Results Aggregation

## Task: User Authentication Implementation

### search Results:
- Found existing auth patterns in auth/
- Located JWT implementation in SecurityConfig.java
- Discovered related frontend components in src/views/auth/

### backend-architect Results:
- Implemented /api/auth/login endpoint
- Created JWT token generation and validation
- Added password hashing with BCrypt

### frontend-architect Results:
- Created Login.vue and Register.vue components
- Implemented auth state management
- Added route guards for protected routes

### api-test-pro Results:
- Tested all auth endpoints (100% pass rate)
- Validated JWT token flow
- Load tested with 100 concurrent users
```

## Troubleshooting Multi-Agent Coordination

### Issue 1: Agent Conflict
**Symptoms**: Agents provide conflicting recommendations
**Solution**: 
1. Review each agent's perspective
2. Identify root cause of conflict
3. Create compromise solution
4. Document decision rationale

### Issue 2: Resource Contention
**Symptoms**: Agents compete for same resources
**Solution**:
1. Implement resource locking
2. Schedule agent execution sequentially
3. Allocate dedicated resources per agent

### Issue 3: Incomplete Coverage
**Symptoms**: Some aspects of task are not addressed
**Solution**:
1. Audit agent assignments
2. Identify missing expertise areas
3. Assign additional agents as needed
4. Update coordination strategy

## Integration with Existing Workflows

### Integration with TodoWrite
```yaml
# Multi-agent todo list example
todos:
  - id: research-auth-patterns
    agent: search
    status: pending
    priority: high
    
  - id: implement-auth-api
    agent: backend-architect
    dependencies: [research-auth-patterns]
    status: pending
    priority: high
    
  - id: create-auth-components
    agent: frontend-architect
    dependencies: [implement-auth-api]
    status: pending
    priority: high
    
  - id: test-auth-endpoints
    agent: api-test-pro
    dependencies: [implement-auth-api, create-auth-components]
    status: pending
    priority: medium
```

### Integration with RunCommand
```bash
# Example of coordinated command execution
# Phase 1: Research
Task search "Find authentication implementation patterns"

# Phase 2: Backend Implementation
Task backend-architect "Implement JWT authentication API"

# Phase 3: Frontend Implementation  
Task frontend-architect "Create login and registration components"

# Phase 4: Testing
Task api-test-pro "Test authentication endpoints comprehensively"
```

## Continuous Improvement

### Agent Performance Metrics
- Track completion time per agent
- Measure quality of agent outputs
- Monitor resource usage efficiency
- Collect user feedback on agent effectiveness

### Coordination Optimization
- Analyze coordination patterns for efficiency
- Identify bottlenecks in multi-agent workflows
- Optimize agent assignment strategies
- Improve communication protocols

This skill ensures that all available specialized agents work together harmoniously to deliver comprehensive, high-quality solutions for complex software development tasks.