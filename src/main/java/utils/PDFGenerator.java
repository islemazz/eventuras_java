package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFGenerator {
    private static final String CONTRACTS_DIR = "contracts";

    public static String generateContract(String partnerName, String partnerEmail, String partnerPhone, String partnerAddress,
                                      int partnershipId, String contractType, String description, int organizerId, String organizerName) throws Exception {
        // Create contracts directory if it doesn't exist
        File contractsDir = new File(CONTRACTS_DIR);
        if (!contractsDir.exists()) {
            contractsDir.mkdirs();
        }

        Document document = new Document();
        String fileName = CONTRACTS_DIR + File.separator + "contract_" + partnershipId + "_" + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        System.out.println("Generating contract PDF at: " + fileName);
        
        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Partnership Agreement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Add partnership details
            document.add(new Paragraph("Partnership ID: " + partnershipId));
            document.add(new Paragraph("Contract Type: " + contractType));
            document.add(new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            document.add(new Paragraph("\n"));

            // Add partner details
            document.add(new Paragraph("Partner Details:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
            document.add(new Paragraph("Name: " + partnerName));
            document.add(new Paragraph("Email: " + partnerEmail));
            document.add(new Paragraph("Phone: " + partnerPhone));
            document.add(new Paragraph("Address: " + partnerAddress));
            document.add(new Paragraph("\n"));

            // Add organizer details
            document.add(new Paragraph("Organizer Details:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
            document.add(new Paragraph("Name: " + organizerName));
            document.add(new Paragraph("\n"));

            // Add partnership description
            document.add(new Paragraph("Partnership Description:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
            document.add(new Paragraph(description));
            document.add(new Paragraph("\n"));

            // Add signature spaces
            document.add(new Paragraph("Signatures:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
            document.add(new Paragraph("\n\n"));
            
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            
            PdfPCell partnerCell = new PdfPCell(new Paragraph("Partner Signature:"));
            partnerCell.setBorder(Rectangle.TOP);
            partnerCell.setPaddingTop(30);
            
            PdfPCell organizerCell = new PdfPCell(new Paragraph("Organizer Signature:"));
            organizerCell.setBorder(Rectangle.TOP);
            organizerCell.setPaddingTop(30);
            
            signatureTable.addCell(partnerCell);
            signatureTable.addCell(organizerCell);
            
            document.add(signatureTable);
            
            System.out.println("Contract PDF generated successfully");
        } catch (Exception e) {
            System.err.println("Error generating contract PDF: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }
        return fileName;
    }
} 