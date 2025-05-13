package utils;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFGenerator {
    public static void generateContract(String partnerName, String email, String phone, String address) throws Exception {
        Document document = new Document();
        String fileName = "contract_" + partnerName.replaceAll("\\s+", "_") + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        // Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Partnership Contract", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Add partner information
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        document.add(new Paragraph("Partner Information:", normalFont));
        document.add(new Paragraph("Name: " + partnerName, normalFont));
        document.add(new Paragraph("Email: " + email, normalFont));
        document.add(new Paragraph("Phone: " + phone, normalFont));
        document.add(new Paragraph("Address: " + address, normalFont));
        document.add(new Paragraph("\n"));

        // Add contract details
        document.add(new Paragraph("Contract Details:", normalFont));
        document.add(new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), normalFont));
        document.add(new Paragraph("This contract is valid for one year from the date of signing.", normalFont));
        document.add(new Paragraph("\n"));

        // Add terms and conditions
        document.add(new Paragraph("Terms and Conditions:", normalFont));
        document.add(new Paragraph("1. Both parties agree to maintain confidentiality of shared information.", normalFont));
        document.add(new Paragraph("2. The partnership can be terminated with 30 days written notice.", normalFont));
        document.add(new Paragraph("3. All disputes will be resolved through mutual discussion.", normalFont));
        document.add(new Paragraph("\n"));

        // Add signature lines
        document.add(new Paragraph("Signatures:", normalFont));
        document.add(new Paragraph("_____________________", normalFont));
        document.add(new Paragraph("Partner Representative", normalFont));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("_____________________", normalFont));
        document.add(new Paragraph("Eventuras Representative", normalFont));

        document.close();
    }
} 