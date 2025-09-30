#!/bin/bash

# Auto-update script for PROJECT_LOG.md and PROJECT_LOG.txt
# This script should be run whenever significant changes are made to the project

echo "Updating project documentation..."

# Get current timestamp
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

# Update the timestamp in both files
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/Last Updated: .*/Last Updated: $TIMESTAMP/" PROJECT_LOG.md
    sed -i '' "s/Last Updated: .*/Last Updated: $TIMESTAMP/" PROJECT_LOG.txt
else
    # Linux
    sed -i "s/Last Updated: .*/Last Updated: $TIMESTAMP/" PROJECT_LOG.md
    sed -i "s/Last Updated: .*/Last Updated: $TIMESTAMP/" PROJECT_LOG.txt
fi

echo "Project documentation updated with timestamp: $TIMESTAMP"
echo "Files updated:"
echo "  - PROJECT_LOG.md"
echo "  - PROJECT_LOG.txt"
