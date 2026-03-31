package com.yupi.yuaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "季夏.pdf";
        String content = "季夏";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}