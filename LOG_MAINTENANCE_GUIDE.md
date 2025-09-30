# Project Log Maintenance Guide

## Overview
This guide explains how to maintain the PROJECT_LOG.md and PROJECT_LOG.txt files across Cursor sessions and ensure they stay updated when changes are made to the project.

## Files Created
- `PROJECT_LOG.md` - Markdown version with rich formatting
- `PROJECT_LOG.txt` - Plain text version for compatibility
- `update_logs.sh` - Script to update timestamps in both files

## How to Keep Logs Updated Across Cursor Sessions

### Method 1: Manual Update Script (Recommended)
Run the update script whenever you make significant changes:

```bash
# From the project root directory
./update_logs.sh
```

This will update the timestamp in both log files to reflect when changes were made.

### Method 2: Git Hooks (Automatic)
Set up git hooks to automatically update logs when commits are made:

1. Create a pre-commit hook:
```bash
# Create the hook file
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
# Update project logs before commit
if [ -f "update_logs.sh" ]; then
    ./update_logs.sh
    git add PROJECT_LOG.md PROJECT_LOG.txt
fi
EOF

# Make it executable
chmod +x .git/hooks/pre-commit
```

### Method 3: IDE Integration
Configure your IDE to run the update script:

1. **VS Code/Cursor**: Add to tasks.json:
```json
{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "Update Project Logs",
            "type": "shell",
            "command": "./update_logs.sh",
            "group": "build",
            "presentation": {
                "echo": true,
                "reveal": "always",
                "focus": false,
                "panel": "shared"
            }
        }
    ]
}
```

2. **IntelliJ IDEA**: Create a run configuration:
   - Run → Edit Configurations
   - Add new Shell Script configuration
   - Command: `./update_logs.sh`
   - Working directory: Project root

### Method 4: Watch Script (Advanced)
Create a file watcher to automatically update logs when source files change:

```bash
# Install fswatch (macOS) or inotify-tools (Linux)
# macOS:
brew install fswatch

# Linux:
sudo apt-get install inotify-tools

# Create watch script
cat > watch_changes.sh << 'EOF'
#!/bin/bash
echo "Watching for changes in src/ directory..."
fswatch -o src/ | while read; do
    echo "Changes detected, updating logs..."
    ./update_logs.sh
done
EOF

chmod +x watch_changes.sh
```

## When to Update the Logs

Update the logs whenever you make changes to:

### Code Changes
- New classes or significant modifications to existing classes
- Changes to protocol buffer definitions
- Modifications to data models
- Updates to configuration files
- Changes to build scripts or deployment scripts

### Documentation Changes
- Updates to README.md
- Changes to USAGE.md
- Modifications to project_notes.txt
- New example queries or usage patterns

### Infrastructure Changes
- New dependencies in pom.xml
- Changes to startup/shutdown scripts
- Modifications to logging configuration
- Updates to data files or schemas

## Maintaining Context Across Sessions

### For Humans
1. **Always check the timestamp** at the bottom of both log files
2. **Read the PROJECT_LOG.md** for detailed technical information
3. **Use PROJECT_LOG.txt** for quick reference or when markdown isn't available

### For LLMs/AI Assistants
1. **Reference both files** when working on the project
2. **Check the timestamp** to understand how recent the information is
3. **Use the structured format** to quickly understand system architecture
4. **Follow the update patterns** described in this guide

## Best Practices

### Regular Updates
- Update logs after each significant feature addition
- Update logs when fixing bugs that affect system behavior
- Update logs when changing configuration or deployment procedures

### Version Control
- Always commit both PROJECT_LOG.md and PROJECT_LOG.txt together
- Include log updates in the same commit as the code changes
- Use descriptive commit messages that reference log updates

### Documentation Quality
- Keep both versions synchronized
- Ensure technical accuracy in descriptions
- Update example queries when adding new features
- Maintain the educational value and learning outcomes

## Troubleshooting

### If Logs Get Out of Sync
1. Check the timestamps in both files
2. Compare content between PROJECT_LOG.md and PROJECT_LOG.txt
3. Re-run the update script: `./update_logs.sh`
4. If needed, manually update the newer file and copy changes to the other

### If Update Script Fails
1. Check file permissions: `ls -la update_logs.sh`
2. Ensure you're in the project root directory
3. Verify both log files exist
4. Run manually: `bash update_logs.sh`

### Cross-Platform Issues
- The update script handles both macOS and Linux
- For Windows, use Git Bash or WSL
- Ensure line endings are consistent (LF for Unix, CRLF for Windows)

## Integration with Development Workflow

### Daily Development
1. Start work by checking log timestamps
2. Make code changes
3. Run `./update_logs.sh` before committing
4. Commit both code and log changes together

### Feature Development
1. Update logs when starting a new feature
2. Update logs when completing the feature
3. Update logs when fixing bugs related to the feature
4. Update logs when adding documentation

### Code Reviews
1. Include log updates in pull requests
2. Review log changes for accuracy
3. Ensure both versions are updated
4. Verify timestamps are current

This systematic approach ensures that your project documentation remains accurate and useful across all development sessions and team members.
