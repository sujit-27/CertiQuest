package com.web.CertiQuest.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.web.CertiQuest.dao.CertificateDao;
import com.web.CertiQuest.model.Certificate;
import com.web.CertiQuest.model.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class CertificateService {

    @Autowired
    private CertificateDao certificateRepository;

    @Autowired
    private ProfileService profileService;

    public Certificate generateCertificate(String userId, String userName, String quizTitle,
                                           int score, int totalQuestions, String difficulty, int quizId) throws Exception {

        String folder = "certificates";
        File dir = new File(folder);
        if (!dir.exists()) dir.mkdirs();

        String safeUserName = userName.replaceAll("\\s+", "_"); // sanitize username for filename
        String fileName = "Certificate_" + safeUserName + "_" + LocalDate.now() + System.currentTimeMillis() + ".pdf";  // no spaces in filename
        String filePath = folder + "/" + fileName;
        String fileUrl = "http://localhost:8080/api/certificates/download/" + fileName;

        try {
            generateCertificatePdf(userName, quizTitle, score, totalQuestions, difficulty, filePath);
        } catch (Exception e) {
            e.printStackTrace(); // Log full stack trace for debugging
            throw new RuntimeException("Failed to generate certificate: " + e.getMessage());
        }

        double percentage = totalQuestions > 0 ? ((double) score / totalQuestions) * 100 : 0;

        Certificate certificate = new Certificate();
        certificate.setUserId(userId);
        certificate.setUserName(userName);
        certificate.setQuizTitle(quizTitle);
        certificate.setScore(score);
        certificate.setTotalQuestions(totalQuestions);
        certificate.setDifficulty(difficulty);
        certificate.setPercentage(percentage);
        certificate.setCertificateUrl(fileUrl);
        certificate.setIssuedAt(LocalDate.now());
        certificate.setQuizId(quizId);

        return certificateRepository.save(certificate);
    }

    public List<Certificate> getAllCertificates() {
        Profile profile = profileService.getCurrentProfile();

        String userId = profile.getClerkId();
        return certificateRepository.findALlByUserId(userId);
    }

    private void generateCertificatePdf(String userName, String quizTitle, int score,
                                        int totalQuestions, String difficulty, String filePath) throws Exception {
        Document document = new Document(PageSize.A4.rotate()); // Landscape orientation
        FileOutputStream fos = new FileOutputStream(filePath);
        PdfWriter writer = PdfWriter.getInstance(document, fos);
        document.open();

        // Enhanced Colors & Fonts
        Color goldColor = new Color(184, 134, 11);      // Darker gold
        Color lightGoldColor = new Color(218, 165, 32); // Light gold accent
        Color darkColor = new Color(41, 49, 51);        // Professional dark
        Color cardBackground = new Color(255, 255, 255); // Pure white
        Color textColor = new Color(60, 60, 60);        // Softer black

        // Font definitions with better hierarchy
        // Note: For cursive name font, you'll need to register a custom font
        // Using italic as fallback, but ideally use a script font like "Great Vibes" or "Pacifico"
        BaseFont cursiveBase = BaseFont.createFont("Helvetica", BaseFont.WINANSI, BaseFont.EMBEDDED);
        Font titleFont = new Font(Font.HELVETICA, 48, Font.BOLD, goldColor);
        Font subtitleFont = new Font(Font.HELVETICA, 18, Font.NORMAL, textColor);
        Font labelFont = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(120, 120, 120));
        Font nameFont = new Font(Font.TIMES_ROMAN, 38, Font.BOLDITALIC, darkColor); // Cursive-like
        Font contentFont = new Font(Font.HELVETICA, 13, Font.NORMAL, textColor);
        Font footerFont = new Font(Font.HELVETICA, 10, Font.ITALIC, new Color(140, 140, 140));
        Font signatureFont = new Font(Font.TIMES_ROMAN, 14, Font.BOLDITALIC, darkColor);

        PdfContentByte canvas = writer.getDirectContent();

        float pageWidth = PageSize.A4.rotate().getWidth();
        float pageHeight = PageSize.A4.rotate().getHeight();

        // Larger card size for landscape (80% of page)
        float cardWidth = pageWidth * 0.85f;
        float cardHeight = pageHeight * 0.85f;

        // Center the card perfectly
        float cardX = (pageWidth - cardWidth) / 2;
        float cardY = (pageHeight - cardHeight) / 2;

        // Draw subtle shadow effect
        canvas.setColorFill(new Color(200, 200, 200));
        canvas.roundRectangle(cardX + 5, cardY - 5, cardWidth, cardHeight, 15);
        canvas.fill();

        // Main card background
        canvas.setColorFill(cardBackground);
        canvas.roundRectangle(cardX, cardY, cardWidth, cardHeight, 15);
        canvas.fill();

        // Decorative double border
        // Outer gold border
        canvas.setLineWidth(8f);
        canvas.setColorStroke(goldColor);
        canvas.roundRectangle(cardX + 20, cardY + 20, cardWidth - 40, cardHeight - 40, 10);
        canvas.stroke();

        // Inner dark border
        canvas.setLineWidth(2f);
        canvas.setColorStroke(darkColor);
        canvas.roundRectangle(cardX + 30, cardY + 30, cardWidth - 60, cardHeight - 60, 8);
        canvas.stroke();

        // Decorative corner elements
        float cornerSize = 30f;
        drawCornerDecoration(canvas, cardX + 40, cardY + cardHeight - 40, cornerSize, goldColor, true, true);
        drawCornerDecoration(canvas, cardX + cardWidth - 40, cardY + cardHeight - 40, cornerSize, goldColor, false, true);
        drawCornerDecoration(canvas, cardX + 40, cardY + 40, cornerSize, goldColor, true, false);
        drawCornerDecoration(canvas, cardX + cardWidth - 40, cardY + 40, cornerSize, goldColor, false, false);

        // Content positioning - perfectly centered
        float centerX = cardX + cardWidth / 2;
        float currentY = cardY + cardHeight - 100;

        // Title: "CERTIFICATE"
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("CERTIFICATE", titleFont),
                centerX, currentY, 0);
        currentY -= 55;

        // Subtitle with decorative line
        float lineLength = 150f;
        canvas.setLineWidth(1.5f);
        canvas.setColorStroke(lightGoldColor);
        canvas.moveTo(centerX - lineLength, currentY + 5);
        canvas.lineTo(centerX - 80, currentY + 5);
        canvas.stroke();
        canvas.moveTo(centerX + 80, currentY + 5);
        canvas.lineTo(centerX + lineLength, currentY + 5);
        canvas.stroke();

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("OF ACHIEVEMENT", subtitleFont),
                centerX, currentY, 0);
        currentY -= 60;

        // "This is to certify that" label
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("This certificate is proudly presented to", labelFont),
                centerX, currentY, 0);
        currentY -= 35;

        // Recipient name with underline
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase(userName, nameFont),
                centerX, currentY, 0);

        // Name underline
        float nameUnderlineLength = 250f;
        canvas.setLineWidth(1.5f);
        canvas.setColorStroke(goldColor);
        canvas.moveTo(centerX - nameUnderlineLength / 2, currentY - 10);
        canvas.lineTo(centerX + nameUnderlineLength / 2, currentY - 10);
        canvas.stroke();
        currentY -= 55;

        // Achievement description with better text wrapping
        String achievement = "For successfully completing the \"" + quizTitle + "\" quiz with an outstanding score of "
                + score + " out of " + totalQuestions + " questions (" +
                String.format("%.1f", (score * 100.0 / totalQuestions)) + "%) at " +
                difficulty.toUpperCase() + " difficulty level.";

        // Use Paragraph for better text wrapping
        Paragraph achievementPara = new Paragraph(achievement, contentFont);
        achievementPara.setAlignment(Element.ALIGN_CENTER);
        achievementPara.setLeading(18f);

        ColumnText ct = new ColumnText(canvas);
        ct.setSimpleColumn(cardX + 100, currentY - 80, cardX + cardWidth - 100, currentY);
        ct.addElement(achievementPara);
        ct.go();
        currentY -= 90;

        // Decorative divider
        canvas.setLineWidth(2f);
        canvas.setColorStroke(lightGoldColor);
        canvas.moveTo(centerX - 200, currentY);
        canvas.lineTo(centerX - 20, currentY);
        canvas.stroke();

        // Small circle in center
        canvas.circle(centerX, currentY, 6);
        canvas.fill();

        canvas.moveTo(centerX + 20, currentY);
        canvas.lineTo(centerX + 200, currentY);
        canvas.stroke();
        currentY -= 50;

        // Date and badge score display
        String dateText = "Date: " + java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase(dateText, footerFont),
                centerX, currentY, 0);
        currentY -= 35;

        // Signature section at bottom
        float sigY = cardY + 70;
        float sigSpacing = 200f;

        // Left signature - Authorized Signature with name
        canvas.setLineWidth(1f);
        canvas.setColorStroke(textColor);
        canvas.moveTo(cardX + 100, sigY);
        canvas.lineTo(cardX + 100 + 180, sigY);
        canvas.stroke();

        // Signature name in cursive style
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("Sujit Kumar Shaw", signatureFont),
                cardX + 190, sigY + 5, 0);

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("Authorized Signature", footerFont),
                cardX + 190, sigY - 15, 0);

        // Right signature - CertiQuest
        canvas.moveTo(cardX + cardWidth - 280, sigY);
        canvas.lineTo(cardX + cardWidth - 100, sigY);
        canvas.stroke();

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("CertiQuest", signatureFont),
                cardX + cardWidth - 190, sigY + 5, 0);

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("Platform", footerFont),
                cardX + cardWidth - 190, sigY - 15, 0);

        // Add stamp next to CertiQuest signature
        drawStamp(canvas, cardX + cardWidth - 120, sigY + 40, goldColor, darkColor);

        // Score badge in top right corner
        drawScoreBadge(canvas, cardX + cardWidth - 100, cardY + cardHeight - 100,
                score, totalQuestions, goldColor, darkColor);

        document.close();
        fos.close();
    }

    // Helper method to draw corner decorations
    private void drawCornerDecoration(PdfContentByte canvas, float x, float y,
                                      float size, Color color, boolean left, boolean top) {
        canvas.setLineWidth(3f);
        canvas.setColorStroke(color);

        if (left && top) {
            canvas.moveTo(x, y);
            canvas.lineTo(x, y - size);
            canvas.moveTo(x, y);
            canvas.lineTo(x + size, y);
        } else if (!left && top) {
            canvas.moveTo(x, y);
            canvas.lineTo(x, y - size);
            canvas.moveTo(x, y);
            canvas.lineTo(x - size, y);
        } else if (left && !top) {
            canvas.moveTo(x, y);
            canvas.lineTo(x, y + size);
            canvas.moveTo(x, y);
            canvas.lineTo(x + size, y);
        } else {
            canvas.moveTo(x, y);
            canvas.lineTo(x, y + size);
            canvas.moveTo(x, y);
            canvas.lineTo(x - size, y);
        }
        canvas.stroke();
    }

    // Helper method to draw score badge
    private void drawScoreBadge(PdfContentByte canvas, float x, float y,
                                int score, int totalQuestions, Color goldColor, Color darkColor) {
        float badgeSize = 60f;

        // Outer circle
        canvas.setLineWidth(4f);
        canvas.setColorStroke(goldColor);
        canvas.circle(x, y, badgeSize / 2);
        canvas.stroke();

        // Inner circle
        canvas.setColorFill(new Color(255, 255, 255));
        canvas.circle(x, y, badgeSize / 2 - 5);
        canvas.fill();

        // Score text
        Font badgeFont = new Font(Font.HELVETICA, 18, Font.BOLD, darkColor);
        Font badgeLabelFont = new Font(Font.HELVETICA, 8, Font.NORMAL, darkColor);

        String scoreText = score + "/" + totalQuestions;
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase(scoreText, badgeFont),
                x, y - 5, 0);

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("SCORE", badgeLabelFont),
                x, y - 20, 0);
    }

    // Helper method to draw stamp
    private void drawStamp(PdfContentByte canvas, float x, float y, Color goldColor, Color darkColor) {
        float stampSize = 70f;

        // Outer circle with rotation effect
        canvas.saveState();
        canvas.setLineWidth(3f);
        canvas.setColorStroke(new Color(200, 50, 50)); // Red stamp color
        canvas.circle(x, y, stampSize / 2);
        canvas.stroke();

        // Inner circle
        canvas.setLineWidth(2f);
        canvas.circle(x, y, stampSize / 2 - 8);
        canvas.stroke();

        // Stamp text - rotated for authentic look
        Font stampFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(200, 50, 50));
        Font stampCenterFont = new Font(Font.HELVETICA, 8, Font.BOLD, new Color(200, 50, 50));

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("CERTIFIED", stampFont),
                x, y + 10, 0);

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("CertiQuest", stampCenterFont),
                x, y - 2, 0);

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("AUTHENTIC", stampFont),
                x, y - 15, 0);

        // Add star in center
        drawStar(canvas, x, y + 2, 6f, new Color(200, 50, 50));

        canvas.restoreState();
    }

    // Helper method to draw a star
    private void drawStar(PdfContentByte canvas, float x, float y, float size, Color color) {
        canvas.saveState();
        canvas.setColorFill(color);

        float[] xPoints = new float[10];
        float[] yPoints = new float[10];

        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + (2 * Math.PI * i / 10);
            float radius = (i % 2 == 0) ? size : size / 2;
            xPoints[i] = x + (float)(radius * Math.cos(angle));
            yPoints[i] = y + (float)(radius * Math.sin(angle));
        }

        canvas.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < 10; i++) {
            canvas.lineTo(xPoints[i], yPoints[i]);
        }
        canvas.closePath();
        canvas.fill();

        canvas.restoreState();
    }
}