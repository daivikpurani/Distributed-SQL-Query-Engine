#!/bin/bash

# Convert HTML to PDF using system tools
# This script will open the HTML file in the default browser and provide instructions for PDF conversion

echo "🚀 Converting PROJECT_PORTFOLIO.html to PDF..."
echo ""
echo "📋 Instructions for PDF conversion:"
echo "1. The HTML file will open in your default browser"
echo "2. Press Cmd+P (Print) or go to File > Print"
echo "3. Select 'Save as PDF' as the destination"
echo "4. Choose 'More settings' and set:"
echo "   - Margins: Minimum"
echo "   - Scale: 100%"
echo "   - Options: Background graphics (checked)"
echo "5. Click 'Save' and name it 'PROJECT_PORTFOLIO.pdf'"
echo ""
echo "🌐 Opening HTML file in browser..."

# Open the HTML file in the default browser
open PROJECT_PORTFOLIO.html

echo "✅ HTML file opened! Follow the instructions above to create your PDF."
echo ""
echo "📁 Files created:"
echo "   - PROJECT_PORTFOLIO.html (ready for PDF conversion)"
echo "   - pdf-style.css (styling file)"
echo ""
echo "💡 Alternative: You can also use online HTML-to-PDF converters:"
echo "   - Upload PROJECT_PORTFOLIO.html to any HTML-to-PDF service"
echo "   - The CSS styling will be preserved"
