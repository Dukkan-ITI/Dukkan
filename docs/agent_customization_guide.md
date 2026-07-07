# Agent Customization & Feature Integration Guide

This guide explains how to create new features, skills, and rules that the AI coding agent can use within this project. By adding customizations to the workspace, you can teach the agent new workflows, project-specific guidelines, or provide it with custom scripts to assist with tasks.

## Workspace Customization Root
All project-specific agent customizations should be placed in the `.agents` directory at the root of the workspace. The agent automatically discovers and loads customizations placed here.

There are two primary ways to customize the agent:
1. **Rules**: For style guidelines, behavioral constraints, and general instructions.
2. **Skills**: For specialized tasks that require step-by-step instructions or custom tools/scripts.

---

## 1. Adding Rules
If you want to enforce a project-wide convention or instruction (e.g., "Always use `ViewModel` instead of `Presenter`", or "Use specific formatting rules"), you can define a rule.

To add a rule:
- Open or create the `.agents/AGENTS.md` file in the workspace root.
- Append your guidelines and instructions in standard markdown format.
- The agent will read this file automatically and apply its constraints to all future tasks in this project.

---

## 2. Creating Skills (New Agent Features)
A "Skill" allows the agent to handle complex, specialized tasks by following detailed instructions, running provided scripts, or referencing specific examples.

### Skill Structure
Each skill resides in its own folder under `.agents/skills/<skill_name>/`. A basic skill requires a `SKILL.md` file, but can optionally include other resources.

Example directory structure:
```text
.agents/
└── skills/
    └── my_new_feature_skill/
        ├── SKILL.md       # (Required) Main instructions and metadata
        ├── scripts/       # (Optional) Helper scripts extending agent capabilities
        ├── examples/      # (Optional) Reference implementations
        └── resources/     # (Optional) Templates, assets, or extra references
```

### Writing `SKILL.md`
The `SKILL.md` file must start with YAML frontmatter containing a `name` and `description`. These fields are critical because they help the agent "discover" and trigger the skill when a relevant task arises. The rest of the file contains the markdown instructions.

Here is a template for `SKILL.md`:

```markdown
---
name: my_new_feature_skill
description: A brief description of what this skill does and when the agent should use it (e.g., "Use this skill to deploy the app to staging").
---

# Instructions for the Agent
When using this skill, follow these exact steps:
1. Read the configuration file in `config/staging.json`.
2. Run the deployment script located in `.agents/skills/my_new_feature_skill/scripts/deploy.ps1`.
3. Verify the deployment was successful by checking the logs.

# Important Notes
- Always ensure tests pass before deployment.
- Refer to `examples/sample_log.txt` to see what a successful log looks like.
```

### Important Guidelines for Skills
- Keep the `SKILL.md` markdown body concise (under 500 lines). If you need to provide extensive documentation, place it in a `references/` subdirectory and instruct the agent to read it when needed.
- If you place skills in a non-standard location (outside of `.agents/`), you must create an `.agents/skills.json` registry file to point the agent to that directory.
- The agent executes its skills autonomously, so instructions should be highly specific and actionable!

---

By extending the `.agents` folder, you empower the entire team's AI coding assistant with shared intelligence tailored specifically for this project.
