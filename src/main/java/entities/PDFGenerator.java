package entities;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PDFGenerator {
    public static void generateContract(String partnerName, PartnerType partnerType, String contactInfo,
                                        String description, Stage stage) {
        // Prompt user to select logo image path
        FileChooser logoFileChooser = new FileChooser();
        logoFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        logoFileChooser.setTitle("Select Logo Image");
        File logoFile = logoFileChooser.showOpenDialog(stage);

        String logoPath = logoFile != null ? logoFile.getAbsolutePath() : null; // Get the logo path if a file is selected

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin; // Start at the top of the page
            float lineSpacing = 20;

            // Add logo image if available
            if (logoPath != null && !logoPath.isEmpty()) {
                PDImageXObject logo = PDImageXObject.createFromFile(logoPath, document);
                contentStream.drawImage(logo, margin, yPosition - 50, 100, 50); // Adjust position and size
                yPosition -= 70; // Move down for the next section
            }

            // Title
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("CONTRACT AGREEMENT");
            contentStream.endText();
            yPosition -= lineSpacing * 2;

            // Add a separator line
            contentStream.setLineWidth(1);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= lineSpacing;

            // Add partner information header
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            addText(contentStream, margin, yPosition, "Partner Information:");
            yPosition -= lineSpacing;

            // Add partner attributes
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            addText(contentStream, margin, yPosition, "• Name: " + partnerName);
            yPosition -= lineSpacing;
            addText(contentStream, margin, yPosition, "• Type: " + partnerType);
            yPosition -= lineSpacing;
            addText(contentStream, margin, yPosition, "• Contact: " + contactInfo);
            yPosition -= lineSpacing;
            addText(contentStream, margin, yPosition, "• Description: " + description);
            yPosition -= lineSpacing;

            // Add another separator line
            yPosition -= lineSpacing;
            contentStream.setLineWidth(1);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= lineSpacing;

            // Add agreement terms header
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            addText(contentStream, margin, yPosition, "Agreement Terms:");
            yPosition -= lineSpacing;

            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            addText(contentStream, margin, yPosition, "1. The partner agrees to collaborate under the terms outlined.");
            yPosition -= lineSpacing;
            addText(contentStream, margin, yPosition, "2. The organizer provides necessary resources and support.");
            yPosition -= lineSpacing;
            addText(contentStream, margin, yPosition, "3. Both parties must adhere to ethical and legal guidelines.");
            yPosition -= lineSpacing;

            // Add another separator line
            yPosition -= lineSpacing;
            contentStream.setLineWidth(1);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= lineSpacing;

            // Additional Terms Section
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            addText(contentStream, margin, yPosition, "Additional Terms and Notes:");
            yPosition -= lineSpacing;
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            addText(contentStream, margin, yPosition, "(To be filled by the organizer or partner)");
            yPosition -= lineSpacing;

            // Placeholder for additional text area
            for (int i = 0; i < 4; i++) {
                addText(contentStream, margin, yPosition, "_____________________________________________________");
                yPosition -= lineSpacing;
            }

            // Date and signature placeholders
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            yPosition -= lineSpacing * 2; // Extra spacing before date
            addText(contentStream, margin, yPosition, "Date: " + java.time.LocalDate.now());
            yPosition -= lineSpacing * 2;

            addText(contentStream, margin, yPosition, "Organizer Signature:");
            addText(contentStream, margin + 250, yPosition, "Partner Signature:");
            yPosition -= lineSpacing;
            addText(contentStream, margin, yPosition, "________________________");
            addText(contentStream, margin + 250, yPosition, "________________________");

            contentStream.close();

            // Save contract with file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("Contract_" + partnerName + ".pdf");
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                document.save(file);
                System.out.println("PDF saved: " + file.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Utility function to add text at a specified position
    private static void addText(PDPageContentStream contentStream, float x, float y, String text) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }
}
