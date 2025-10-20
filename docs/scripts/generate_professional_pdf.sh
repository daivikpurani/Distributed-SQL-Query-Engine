#!/bin/bash

# Professional PDF Generator for Job Applications
# Creates a concise, professional PDF perfect for job applications

echo "🚀 Generating Professional PDF for Job Applications..."
echo ""

# Check if HTML file exists
if [ ! -f "PROJECT_PORTFOLIO_CONCISE.html" ]; then
    echo "❌ HTML file not found. Generating..."
    pandoc PROJECT_PORTFOLIO_CONCISE.md -o PROJECT_PORTFOLIO_CONCISE.html --css=pdf-concise-style.css --standalone --toc --toc-depth=2
fi

echo "📋 Instructions for creating your professional PDF:"
echo ""
echo "1. 📖 The HTML file will open in your browser"
echo "2. 🖨️  Press Cmd+P (Print) or go to File > Print"
echo "3. 💾 Select 'Save as PDF' as the destination"
echo "4. ⚙️  Click 'More settings' and configure:"
echo "   • Margins: Minimum (0.5 inches)"
echo "   • Scale: 100%"
echo "   • Options: Background graphics (checked)"
echo "   • Paper size: Letter (8.5 x 11 inches)"
echo "5. 💾 Click 'Save' and name it 'PROJECT_PORTFOLIO.pdf'"
echo ""
echo "🎯 This will create a professional 2-3 page PDF perfect for job applications!"
echo ""

# Open the HTML file
open PROJECT_PORTFOLIO_CONCISE.html

echo "✅ HTML file opened! Follow the instructions above to create your PDF."
echo ""
echo "📁 Files created:"
echo "   • PROJECT_PORTFOLIO_CONCISE.html (ready for PDF conversion)"
echo "   • pdf-concise-style.css (professional styling)"
echo "   • PROJECT_PORTFOLIO_CONCISE.md (source markdown)"
echo ""
echo "💡 Pro Tips for Professional PDF:"
echo "   • Use 'Minimum' margins for maximum content"
echo "   • Enable 'Background graphics' for colors"
echo "   • Save as 'PROJECT_PORTFOLIO.pdf' for consistency"
echo "   • The PDF should be 2-3 pages when printed"
