#!/usr/bin/env python3
"""
HTML to PDF Converter for Project Portfolio
This script converts the HTML file to PDF using weasyprint or alternative methods
"""

import os
import sys
import subprocess
from pathlib import Path

def check_weasyprint():
    """Check if weasyprint is available"""
    try:
        import weasyprint
        return True
    except ImportError:
        return False

def install_weasyprint():
    """Install weasyprint using pip"""
    try:
        subprocess.check_call([sys.executable, "-m", "pip", "install", "weasyprint"])
        return True
    except subprocess.CalledProcessError:
        return False

def convert_html_to_pdf():
    """Convert HTML to PDF using weasyprint"""
    try:
        from weasyprint import HTML, CSS
        
        html_file = "PROJECT_PORTFOLIO.html"
        css_file = "pdf-style.css"
        pdf_file = "PROJECT_PORTFOLIO.pdf"
        
        if not os.path.exists(html_file):
            print(f"❌ Error: {html_file} not found!")
            return False
            
        print("🔄 Converting HTML to PDF using WeasyPrint...")
        
        # Read CSS content
        css_content = ""
        if os.path.exists(css_file):
            with open(css_file, 'r', encoding='utf-8') as f:
                css_content = f.read()
        
        # Convert HTML to PDF
        html_doc = HTML(filename=html_file)
        css_doc = CSS(string=css_content) if css_content else None
        
        if css_doc:
            html_doc.write_pdf(pdf_file, stylesheets=[css_doc])
        else:
            html_doc.write_pdf(pdf_file)
            
        print(f"✅ PDF created successfully: {pdf_file}")
        return True
        
    except Exception as e:
        print(f"❌ Error converting to PDF: {e}")
        return False

def main():
    """Main function"""
    print("🚀 Project Portfolio PDF Converter")
    print("=" * 40)
    
    # Check if HTML file exists
    if not os.path.exists("PROJECT_PORTFOLIO.html"):
        print("❌ PROJECT_PORTFOLIO.html not found!")
        print("Please run: pandoc PROJECT_PORTFOLIO.md -o PROJECT_PORTFOLIO.html --css=pdf-style.css --standalone --toc --toc-depth=3")
        return
    
    # Check for weasyprint
    if not check_weasyprint():
        print("📦 WeasyPrint not found. Installing...")
        if install_weasyprint():
            print("✅ WeasyPrint installed successfully!")
        else:
            print("❌ Failed to install WeasyPrint")
            print("\n🔄 Alternative method: Manual conversion")
            print("1. Open PROJECT_PORTFOLIO.html in your browser")
            print("2. Press Cmd+P (Print)")
            print("3. Select 'Save as PDF'")
            print("4. Choose appropriate settings and save")
            return
    
    # Convert to PDF
    if convert_html_to_pdf():
        print("\n🎉 Conversion completed successfully!")
        print("📁 Files created:")
        print("   - PROJECT_PORTFOLIO.pdf (your portfolio)")
        print("   - PROJECT_PORTFOLIO.html (source)")
        print("   - pdf-style.css (styling)")
    else:
        print("\n🔄 Manual conversion required:")
        print("1. Open PROJECT_PORTFOLIO.html in your browser")
        print("2. Press Cmd+P (Print)")
        print("3. Select 'Save as PDF'")
        print("4. Choose appropriate settings and save")

if __name__ == "__main__":
    main()
