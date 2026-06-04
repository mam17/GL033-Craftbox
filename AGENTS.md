# AGENTS.md

## Codex Project Skill

Before changing this repo, use the local skill at:

- `.codex/skills/gl027-sms-theme-android/SKILL.md`
- `PROJECT_COMMANDMENTS.md`
- `PROJECT_COMMANDMENTS_OVERRIDE.md`

This project is no longer only an Android template. Treat it as a real SMS/MMS app with customizable colors, backgrounds, fonts, and themes. The local skill contains the compact repo map, SMS/MMS guardrails, theme rules, and UI/architecture commandments so agents do not need to rediscover the whole project every time.

Hướng dẫn về AI Agents cho dự án Android này.

## Tổng Quan Agents

Dự án này tích hợp ClaudeKit Framework với 17+ specialized AI agents để hỗ trợ development process.

## Core Agents Cho Android Development

### 1. **Planner Agent** 🎯
**Mục Đích**: Lập kế hoạch chi tiết cho features mới

**Khi Sử Dụng**:
```bash
/ck:plan "implement user authentication with JWT"
```

**Output**:
- Architecture decisions
- Data model plan (Entity/DAO structure)
- UI component plan
- Business logic plan
- Test plan

**Responsibilities**:
- Analyze requirements
- Research best practices
- Create detailed implementation plan
- Identify dependencies

---

### 2. **Tester Agent** 🧪
**Mục Đích**: Sinh test cases tự động

**Khi Sử Dụng**:
```bash
/ck:test "validate user authentication feature"
```

**Output**:
- Unit tests cho UseCase & ViewModel
- Instrumented tests cho UI
- Test coverage report

**Responsibilities**:
- Generate comprehensive test suites
- Validate functionality
- Ensure test coverage
- Report quality metrics

---

### 3. **Code Reviewer Agent** 🔍
**Mục Đích**: Review code quality & enforce rules

**Khi Sử Dụng**:
```bash
/ck:code-review
```

**Checks**:
✅ Activity/Fragment extends Base classes (BẮT BUỘC)  
✅ Adapter extends BaseAdapter (BẮT BUỘC)  
✅ Layout XML có file riêng (BẮT BUỘC)  
✅ Ripple effect trên buttons (BẮT BUỘC)  
✅ Style dùng cho text (BẮT BUỘC)  
✅ ViewBinding được sử dụng  
✅ Naming conventions  
✅ Security issues  
✅ Performance concerns  

**Responsibilities**:
- Enforce architectural standards
- Check code quality
- Identify security issues
- Suggest improvements

---

### 4. **Debugger Agent** 🐛
**Mục Đích**: Debug issues từ logs

**Khi Sử Dụng**:
```bash
/ck:debug "investigate login crash"
```

**Output**:
- Root cause analysis
- Fix recommendations
- Test suggestions

**Responsibilities**:
- Analyze crash logs
- Investigate performance issues
- Diagnose CI/CD problems
- Provide debugging guidance

---

### 5. **Docs Manager Agent** 📚
**Mục Đích**: Update & maintain documentation

**Khi Sử Dụng**:
```bash
/ck:docs
```

**Updates**:
- `README.md` - Project overview
- `CHANGELOG.md` - Version history
- API documentation
- Architecture docs

**Responsibilities**:
- Keep docs synchronized with code
- Update API documentation
- Maintain codebase summaries
- Ensure documentation accuracy

---

### 6. **Git Manager Agent** 📝
**Mục Đích**: Manage git workflow

**Responsibilities**:
- Create clean, conventional commits
- Manage branching strategy
- Ensure professional git history
- Sync version info

---

## Workflow Examples

### Example 1: Add New Feature
```bash
# Step 1: Planning
/ck:plan "implement product listing screen with filtering"
# → Planner creates detailed plan

# Step 2: Implementation (you do this or ask Claude to cook)
# Create Activity, Fragment, Layout, DAO, UseCase

# Step 3: Testing
/ck:test
# → Tester generates unit & instrumented tests

# Step 4: Code Review
/ck:code-review
# → Reviewer checks all rules

# Step 5: Documentation
/ck:docs
# → Docs manager updates README, CHANGELOG

# Step 6: Commit & Push
# Git manager creates clean commits
```

### Example 2: Bug Fixing
```bash
# Analyze
/ck:debug "database query timeout issue"

# Plan fix
/ck:plan "optimize database queries"

# Implement
# Your implementation

# Test & Review
/ck:test
/ck:code-review

# Documentation
/ck:docs
```

### Example 3: Refactoring
```bash
# Plan refactor
/ck:plan "refactor user adapter for better maintainability"

# Implement refactor
# Your implementation

# Validate
/ck:test
/ck:code-review
/ck:docs
```

---

## Android-Specific Rules Agents Will Enforce

### Critical Rules (MANDATORY)
1. **Base Classes** ✅
   - Activity → `BaseActivity<VB>`
   - Fragment → `BaseFragment`
   - Adapter → `BaseAdapter` (choose appropriate type)
   - Dialog → `BaseDialog`

2. **Layout Files** ✅
   - `activity_[name].xml`
   - `fragment_[name].xml`
   - `item_[name].xml` for RecyclerView items
   - MUST use ViewBinding

3. **UI Elements** ✅
   - Ripple effect on ALL buttons/clickable areas
   - Style for ALL text views (no hardcoded textSize/color)
   - ConstraintLayout for better performance

4. **Architecture** ✅
   - Clean Architecture pattern
   - Entity → DAO → Model → UseCase flow
   - Dependency Injection via Hilt
   - Separation of concerns

---

## How to Work With Agents

### When Planning
```bash
/ck:plan "[detailed requirement]"
# Agents will:
# - Analyze complexity
# - Research best practices
# - Create implementation plan
# - Identify edge cases
```

### When Implementing
```bash
# Follow the plan from Planner agent
# Use BaseActivity/Fragment/Adapter
# Follow Layout & Ripple & Style rules
# Write tests alongside
```

### When Reviewing
```bash
/ck:code-review
# Agents will:
# - Check Base class extensions
# - Verify Layout rules
# - Check ripple & style usage
# - Review code quality
# - Suggest improvements
```

### When Testing
```bash
/ck:test
# Agents will:
# - Generate unit tests
# - Generate instrumented tests
# - Run test suite
# - Report coverage
```

---

## Configuration

### .claude/rules/
All rules are defined here:
- `primary-workflow.md` - Main workflow
- `development-rules.md` - General standards
- `android-development-rules.md` - Android-specific rules (CRITICAL)
- `orchestration-protocol.md` - Agent coordination
- `documentation-management.md` - Doc standards

### .claude/CLAUDE.md
Instructions for Claude Code agents working with this project.

### ./CLAUDE.md
Project-specific documentation and instructions.

---

## Quick Reference

| Need | Command | Agent |
|------|---------|-------|
| Plan feature | `/ck:plan "..."` | Planner |
| Generate tests | `/ck:test` | Tester |
| Review code | `/ck:code-review` | Code Reviewer |
| Debug issue | `/ck:debug "..."` | Debugger |
| Update docs | `/ck:docs` | Docs Manager |
| Check status | `/ck:watzup` | Project Manager |

---

## Important Notes

1. **Agents enforce Android rules strictly** - No exceptions
2. **Base classes are mandatory** - Every Activity/Fragment/Adapter must extend Base
3. **Layout XML is required** - Every screen must have its own XML file
4. **Ripple & Style are mandatory** - Code review will fail without them
5. **Architecture must be Clean** - Follow Entity→DAO→Model→UseCase pattern

---

**Remember**: Agents are here to help maintain code quality and enforce best practices. Follow their recommendations and use them regularly to ensure consistent, high-quality code!
