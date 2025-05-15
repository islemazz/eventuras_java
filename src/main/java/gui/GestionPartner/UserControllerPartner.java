package gui.GestionPartner;

import java.awt.Desktop;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;

import entities.ContractType;
import entities.Partner;
import entities.PartnerType;
import entities.Partnership;
import gui.GestionUser.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.PartnerService;
import services.PartnershipService;
import services.userService;
import utils.PDFGenerator; // AWT Rectangle2D for defining regions

public class UserControllerPartner implements Initializable {

    private final PartnerService ps = new PartnerService();
    private PartnershipService partnershipService;
    private userService uService;
    @FXML
    public Button save;
    @FXML
    public Button returnBtn;
    @FXML
    public AnchorPane SelecPartner;
    
    @FXML
    private Button uploadSignedContractButton;

    @FXML
    private ListView<Partner> partnersList;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<PartnerType> typeFilter;

    @FXML
    private ComboBox<ContractType> partnershipTypeComboBox;
    @FXML
    private TextArea descriptionField;

    @FXML
    private ImageView myImage;

    @FXML
    private VBox partnershipForm;

    @FXML
    private Button createAndGenerateContractButton;

    private Partner selectedPartner;
    private Partnership currentWorkingPartnership;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        uploadSignedContractButton.setVisible(false);
        partnershipService = new PartnershipService();
        uService = new userService();

        typeFilter.setItems(FXCollections.observableArrayList(PartnerType.values()));
        typeFilter.valueProperty().addListener((obs, oldVal, newValPartnerType) -> {
             filterPartnersByType(newValPartnerType); 
        });
        
        partnershipTypeComboBox.setItems(FXCollections.observableArrayList(ContractType.values()));
        
        try {
            refreshPartnersList();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load partners: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        
        partnersList.setCellFactory(listView -> new PartnerCell());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                handleSearch();
            } catch (SQLException e) {
                showAlert("Error", "Failed to search partners: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });

        partnershipForm.setVisible(false);

        partnersList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValuePartner) -> {
            if (newValuePartner != null) {
                selectedPartner = newValuePartner;
                System.out.println("Selected partner: " + selectedPartner.getName());
                partnershipForm.setVisible(true);
                DisplayImage();
                
                currentWorkingPartnership = null;
                if (uploadSignedContractButton != null) {
                    uploadSignedContractButton.setVisible(false);
                }
                clearPartnershipForm();

            } else {
                selectedPartner = null;
                partnershipForm.setVisible(false);
                if (myImage != null) {
                    myImage.setImage(null);
                }
                currentWorkingPartnership = null;
                if (uploadSignedContractButton != null) {
                    uploadSignedContractButton.setVisible(false);
                }
            }
        });
    }

    private void filterPartnersByType(PartnerType type) {
        System.out.println("Filtering by type: " + type);
        try {
            List<Partner> allPartners = ps.readAll();
            if (type == null) {
                partnersList.getItems().setAll(allPartners);
            } else {
                List<Partner> filtered = allPartners.stream()
                                                    .filter(p -> p.getType() == type)
                                                    .collect(Collectors.toList());
                partnersList.getItems().setAll(filtered);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error refreshing list during type filter: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() throws SQLException {
        String searchText = searchField.getText().trim().toLowerCase();
        PartnerType selectedType = typeFilter.getValue();

        List<Partner> partners = ps.readAll();
        List<Partner> filteredPartners = partners.stream()
            .filter(partner -> {
                boolean matchesSearch = searchText.isEmpty() ||
                    partner.getName().toLowerCase().contains(searchText) ||
                    (partner.getDescription() != null && partner.getDescription().toLowerCase().contains(searchText)) ||
                    partner.getEmail().toLowerCase().contains(searchText);
                boolean matchesType = selectedType == null || partner.getType() == selectedType;
                return matchesSearch && matchesType;
            })
            .collect(Collectors.toList());
        partnersList.getItems().setAll(filteredPartners);
    }

    @FXML
    private void handleRefresh() {
        try {
            refreshPartnersList();
            searchField.clear();
            typeFilter.setValue(null);
        } catch (SQLException e) {
            showAlert("Error", "Failed to refresh partners: " + e.getMessage());
        }
    }

    private void refreshPartnersList() throws SQLException {
        List<Partner> partners = ps.readAll();
        partnersList.getItems().setAll(partners);
    }

    @FXML
    void handleCreateAndGenerateContract(ActionEvent event) {
        if (selectedPartner == null) {
            showAlert("Error", "Please select a partner first", Alert.AlertType.ERROR);
            return;
        }

        if (partnershipTypeComboBox.getValue() == null) {
            showAlert("Error", "Please select a contract type", Alert.AlertType.ERROR);
            return;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter a partnership description", Alert.AlertType.ERROR);
            return;
        }

        UserSession currentSession;
        try {
            currentSession = UserSession.getInstance();
        } catch (IllegalStateException e) {
            showAlert("Error", "User session not found. Please log in again.", Alert.AlertType.ERROR);
            System.err.println("Error: UserSession.getInstance() failed: " + e.getMessage());
            return;
        }

        if (createAndGenerateContractButton != null) {
            createAndGenerateContractButton.setDisable(true);
        }
        if (uploadSignedContractButton != null) {
            uploadSignedContractButton.setDisable(true);
        }

        Partnership tempPartnership = null;

        try {
            int organizerIdToSet = currentSession.getId();
            String organizerSessionName = currentSession.getFirstname() + " " + currentSession.getLastname();
            System.out.println("Using current logged-in organizer ID: " + organizerIdToSet + " (Name from session: " + organizerSessionName + ")");

            Partnership partnership = new Partnership();
            partnership.setPartnerId(selectedPartner.getId());
            partnership.setOrganizerId(organizerIdToSet);
            partnership.setContractType(partnershipTypeComboBox.getValue().toString());
            partnership.setDescription(descriptionField.getText().trim());
            partnership.setCreatedAt(LocalDateTime.now());
            partnership.setStatus("Pending - Unsigned");
            partnership.setSigned(false);

            String generatedContractPath = PDFGenerator.generateContract(
                selectedPartner.getName(),
                selectedPartner.getEmail(),
                selectedPartner.getPhone(),
                selectedPartner.getAddress(),
                partnership.getId(),
                partnership.getContractType(),
                partnership.getDescription(),
                partnership.getOrganizerId(),
                organizerSessionName
            );

            if (generatedContractPath == null || generatedContractPath.trim().isEmpty()) {
                 showAlert("Error", "Contract PDF generation failed (no path returned).");
                 return;
            }
            partnership.setGeneratedContractPath(generatedContractPath);

            tempPartnership = partnershipService.create(partnership);
            if (tempPartnership == null || tempPartnership.getId() == 0) {
                showAlert("Error", "Failed to create initial partnership record in database.");
                try { Files.deleteIfExists(Paths.get(generatedContractPath)); } catch (IOException ignored) {}
                return;
            }
            System.out.println("Initial partnership record created (ID: " + tempPartnership.getId() + ") with generated contract: " + generatedContractPath);
            currentWorkingPartnership = tempPartnership;
            
            showAlert("Success", "Partnership created (ID: " + currentWorkingPartnership.getId() + ") and contract generated: " + generatedContractPath, Alert.AlertType.INFORMATION);

            if (uploadSignedContractButton != null && currentWorkingPartnership != null && !currentWorkingPartnership.isSigned()) {
                uploadSignedContractButton.setVisible(true);
                uploadSignedContractButton.setDisable(false);
            }

            File pdfFile = new File(generatedContractPath);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                new Thread(() -> {
                    try {
                        Desktop.getDesktop().open(pdfFile);
                    } catch (IOException ex) {
                        System.err.println("Error opening contract (IOException): " + ex.getMessage());
                        Platform.runLater(() -> showAlert("File Open Error", "Could not open contract: " + ex.getMessage(), Alert.AlertType.ERROR));
                    } catch (Throwable t) {
                         System.err.println("Unexpected error opening file: " + t.getMessage());
                         Platform.runLater(() -> showAlert("File Open Error", "Unexpected error opening file: " + t.getMessage(), Alert.AlertType.ERROR));
                    }
                }).start();
            } else if (!pdfFile.exists()){
                 showAlert("Warning", "Generated PDF file not found at: " + generatedContractPath, Alert.AlertType.WARNING);
            }

        } catch (IOException pdfIoException) { 
            showAlert("File Error", "Failed to generate contract PDF (IOException): " + pdfIoException.getMessage(), Alert.AlertType.ERROR);
            pdfIoException.printStackTrace();
            if (currentWorkingPartnership != null && currentWorkingPartnership.getId() != 0) {
                if (partnershipService.delete(currentWorkingPartnership.getId())) {
                    System.out.println("Rolled back: Deleted partnership record (ID: " + currentWorkingPartnership.getId() + ") due to PDF generation failure (IOException).");
                } else {
                    System.err.println("Failed to rollback (delete) partnership record (ID: " + currentWorkingPartnership.getId() + ") after IOException.");
                }
            }
            currentWorkingPartnership = null;
        } catch (Exception e) { 
            showAlert("PDF Generation Error", "Failed to generate contract PDF (General Exception): " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            if (currentWorkingPartnership != null && currentWorkingPartnership.getId() != 0) {
                if (partnershipService.delete(currentWorkingPartnership.getId())) {
                    System.out.println("Rolled back: Deleted partnership record (ID: " + currentWorkingPartnership.getId() + ") due to PDF generation failure (General Exception).");
                } else {
                    System.err.println("Failed to rollback (delete) partnership record (ID: " + currentWorkingPartnership.getId() + ") after General Exception.");
                }
            }
            currentWorkingPartnership = null;
        } finally {
            if (createAndGenerateContractButton != null) {
                createAndGenerateContractButton.setDisable(false);
            }
            if (uploadSignedContractButton != null && currentWorkingPartnership != null && !currentWorkingPartnership.isSigned() && uploadSignedContractButton.isVisible()) {
                 uploadSignedContractButton.setDisable(false);
            } else if (uploadSignedContractButton != null) {
                uploadSignedContractButton.setDisable(true);
            }
        }
    }

    @FXML
    void handleUploadSignedContract(ActionEvent event) {
        if (currentWorkingPartnership == null) {
            showAlert("Error", "No partnership context available. Please select or create a partnership first.", Alert.AlertType.ERROR);
            return;
        }

        if (currentWorkingPartnership.isSigned()) {
            showAlert("Info", "This partnership's contract has already been marked as signed.", Alert.AlertType.INFORMATION);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Signed Contract PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File signedPdfFile = fileChooser.showOpenDialog(uploadSignedContractButton.getScene().getWindow());

        if (signedPdfFile != null) {
            try {
                PDDocument signedDocument = Loader.loadPDF(signedPdfFile);
                String originalPdfPath = currentWorkingPartnership.getGeneratedContractPath();
                File originalPdfFile = null;
                PDDocument originalDocument = null;
                File originalPdfFileForDeletion = null; 

                if (originalPdfPath != null && new File(originalPdfPath).exists()) {
                    originalPdfFile = new File(originalPdfPath);
                    originalDocument = Loader.loadPDF(originalPdfFile);
                } else {
                    showAlert("Info", "Original unsigned contract not found locally. Regenerating for comparison...", Alert.AlertType.INFORMATION);
                    File regeneratedFile = regenerateUnsignedPdfAndGetFile(currentWorkingPartnership);
                    if (regeneratedFile != null && regeneratedFile.exists()) {
                        originalDocument = Loader.loadPDF(regeneratedFile);
                        originalPdfFileForDeletion = regeneratedFile; 
                    } else {
                        originalDocument = null; 
                    }

                    if (originalDocument == null) { 
                        showAlert("Error", "Failed to obtain the original unsigned contract for comparison.", Alert.AlertType.ERROR);
                        if(signedDocument != null) signedDocument.close();
                        return;
                    }
                }

                boolean signatureVerified = verifySignature(originalDocument, signedDocument);

                if (signatureVerified) {
                    String contractsDir = "signed_contracts";
                    Path contractsPath = Paths.get(contractsDir);
                    if (!Files.exists(contractsPath)) {
                        Files.createDirectories(contractsPath);
                    }
                    String fileName = "signed_contract_pid_" + currentWorkingPartnership.getId() + "_" + signedPdfFile.getName();
                    Path destinationPath = contractsPath.resolve(fileName);
                    Files.copy(signedPdfFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    String storedSignedPath = destinationPath.toString();

                    partnershipService.recordSignatureVerified(currentWorkingPartnership.getId(), storedSignedPath);
                    currentWorkingPartnership.setSigned(true);
                    currentWorkingPartnership.setStatus("Signed"); 
                    currentWorkingPartnership.setSignedContractFile(storedSignedPath); 

                    showAlert("Success", "Contract signature verified and partnership updated!", Alert.AlertType.INFORMATION);
                    uploadSignedContractButton.setVisible(false); 
                     if (createAndGenerateContractButton != null) {
                        createAndGenerateContractButton.setDisable(true); 
                    }
                } else {
                    showAlert("Verification Failed", "Could not verify a signature in the uploaded document.", Alert.AlertType.WARNING);
                }

                if (originalDocument != null) originalDocument.close();
                signedDocument.close();

                if (originalPdfFileForDeletion != null) {
                    if (originalPdfFileForDeletion.exists()) {
                        System.out.println("Attempting to delete temporary regenerated file: " + originalPdfFileForDeletion.getAbsolutePath());
                        if (originalPdfFileForDeletion.delete()) {
                            System.out.println("Successfully deleted temporary file.");
                        } else {
                            System.err.println("Failed to delete temporary file: " + originalPdfFileForDeletion.getAbsolutePath());
                        }
                    }
                }

            } catch (IOException e) {
                showAlert("Error", "Failed to process PDF files: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to update partnership: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private static class KeywordFinder extends PDFTextStripper {
        private String keyword;
        private List<Rectangle2D> foundKeywordLocations = new ArrayList<>();
        private PDPage pageOfKeyword = null;

        public KeywordFinder(String keyword) throws IOException {
            super();
            this.keyword = keyword.toLowerCase();
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            if (text.toLowerCase().contains(keyword)) {
                if (!textPositions.isEmpty()) {
                    TextPosition firstPos = textPositions.get(0);
                    TextPosition lastPos = textPositions.get(textPositions.size() - 1);
                    
                    Rectangle2D.Float rect = new Rectangle2D.Float(
                        (float) firstPos.getXDirAdj(), 
                        (float) (firstPos.getYDirAdj() - firstPos.getHeightDir()),
                        (float) (lastPos.getXDirAdj() + lastPos.getWidthDirAdj() - firstPos.getXDirAdj()),
                        (float) firstPos.getHeightDir()
                    );
                    foundKeywordLocations.add(rect);
                    if (pageOfKeyword == null) { 
                         pageOfKeyword = getCurrentPage();
                    }
                }
            }
            super.writeString(text, textPositions);
        }

        public List<Rectangle2D> getFoundKeywordLocations() {
            return foundKeywordLocations;
        }
        public PDPage getPageOfKeyword() {
            return pageOfKeyword;
        }
    }

    private boolean verifySignature(PDDocument originalDoc, PDDocument signedDoc) throws IOException {
        String[] keywordsToTry = {"Partner Signature:", "Organizer Signature:", "Signature:", "________________________"};
        Rectangle2D signatureRegion = null;
        PDPage pageContainingKeyword = null;
        int pageIndexOfKeyword = 0;

        for (String keyword : keywordsToTry) {
            KeywordFinder finder = new KeywordFinder(keyword);
            finder.setSortByPosition(true);
            finder.getText(originalDoc); 
            List<Rectangle2D> locations = finder.getFoundKeywordLocations();

            if (!locations.isEmpty()) {
                signatureRegion = locations.get(0); 
                pageContainingKeyword = finder.getPageOfKeyword();
                if (pageContainingKeyword != null) {
                    pageIndexOfKeyword = originalDoc.getPages().indexOf(pageContainingKeyword);
                    if (pageIndexOfKeyword < 0) pageIndexOfKeyword = 0; 
                }
                System.out.println("Found keyword '" + keyword + "' at: " + signatureRegion + " on page index: " + pageIndexOfKeyword);
                break; 
            }
        }

        if (signatureRegion == null) {
            System.out.println("None of the target keywords found. Using fallback ROI.");
            PDPage firstPage = originalDoc.getPage(0);
            float pageHeight = firstPage.getMediaBox().getHeight();
            float pageWidth = firstPage.getMediaBox().getWidth();
            signatureRegion = new Rectangle2D.Float(pageWidth * 0.1f, pageHeight * 0.70f, pageWidth * 0.5f, pageHeight * 0.15f);
            pageIndexOfKeyword = 0;
             showAlert("Debug", "Keywords not found. Using default ROI. This might be inaccurate.", Alert.AlertType.WARNING);
        } else {
            float yOffsetBelowKeyword = 5f; 
            float roiHeight = 80f; 
            float roiWidth = Math.max((float)signatureRegion.getWidth() + 150f, 250f); 

            signatureRegion = new Rectangle2D.Float(
                (float)signatureRegion.getX(),
                (float)(signatureRegion.getY() + signatureRegion.getHeight() + yOffsetBelowKeyword),
                roiWidth,
                roiHeight
            );
            System.out.println("Defined ROI below keyword: " + signatureRegion + " on page index: " + pageIndexOfKeyword);
        }

        System.out.println("Final Identified ROI: " + signatureRegion.toString() + " on page index: " + pageIndexOfKeyword);

        try {
            PDFTextStripperByArea originalStripper = new PDFTextStripperByArea();
            originalStripper.addRegion("signatureROI", signatureRegion);
            PDPage originalPageToAnalyze = originalDoc.getPage(pageIndexOfKeyword);
            originalStripper.extractRegions(originalPageToAnalyze);
            String originalTextInROI = originalStripper.getTextForRegion("signatureROI").trim();
            System.out.println("Original Text in ROI: '" + originalTextInROI + "'");

            PDFTextStripperByArea signedStripper = new PDFTextStripperByArea();
            signedStripper.addRegion("signatureROI", signatureRegion);
            if (signedDoc.getNumberOfPages() <= pageIndexOfKeyword) {
                System.err.println("Signed document has fewer pages than where keyword was found in original. Cannot compare.");
                return false;
            }
            PDPage signedPageToAnalyze = signedDoc.getPage(pageIndexOfKeyword);
            signedStripper.extractRegions(signedPageToAnalyze);
            String signedTextInROI = signedStripper.getTextForRegion("signatureROI").trim();
            System.out.println("Signed Text in ROI: '" + signedTextInROI + "'");

            if (!signedTextInROI.equals(originalTextInROI) && !signedTextInROI.isEmpty()) {
                System.out.println("New text found in ROI. Assuming signed.");
                return true;
            } else if (signedTextInROI.isEmpty() && originalTextInROI.isEmpty()) {
                 System.out.println("Both ROIs are textually empty. Proceeding to image comparison.");
                 return compareRoiAsImages(originalDoc, signedDoc, pageIndexOfKeyword, signatureRegion);
            } else if (signedTextInROI.equals(originalTextInROI)){
                 System.out.println("Texts in ROI are identical. Assuming not signed by text logic. Proceeding to image comparison.");
                 return compareRoiAsImages(originalDoc, signedDoc, pageIndexOfKeyword, signatureRegion);
            } else {
                 System.out.println("Text comparison inconclusive (Original: '" + originalTextInROI + "', Signed: '" + signedTextInROI + "'). Proceeding to image comparison.");
                 return compareRoiAsImages(originalDoc, signedDoc, pageIndexOfKeyword, signatureRegion);
            }

        } catch (IOException e) {
            System.err.println("Error during PDF text stripping for signature verification: " + e.getMessage());
            e.printStackTrace();
            throw e; 
        }
    }
    
    private boolean compareRoiAsImages(PDDocument doc1, PDDocument doc2, int pageIndex, Rectangle2D roi) throws IOException {
        PDFRenderer renderer1 = new PDFRenderer(doc1);
        BufferedImage img1 = renderer1.renderImageWithDPI(pageIndex, 96); 
        BufferedImage roiImg1 = getSubImage(img1, roi);

        PDFRenderer renderer2 = new PDFRenderer(doc2);
        if (doc2.getNumberOfPages() <= pageIndex) {
            System.err.println("Signed document has fewer pages than where keyword was found. Cannot render image for comparison.");
            return false;
        }
        BufferedImage img2 = renderer2.renderImageWithDPI(pageIndex, 96);
        BufferedImage roiImg2 = getSubImage(img2, roi);

        if (roiImg1 == null || roiImg2 == null) {
            System.err.println("Could not extract image ROIs for comparison.");
            return false;
        }
        
        long diffPixels = 0;
        if (roiImg1.getWidth() != roiImg2.getWidth() || roiImg1.getHeight() != roiImg2.getHeight()) {
            System.out.println("ROI image dimensions differ significantly (W1:"+roiImg1.getWidth()+", H1:"+roiImg1.getHeight()+"; W2:"+roiImg2.getWidth()+", H2:"+roiImg2.getHeight()+"). Assuming change (signature).");
            return true; 
        }

        for (int y = 0; y < roiImg1.getHeight(); y++) {
            for (int x = 0; x < roiImg1.getWidth(); x++) {
                if (roiImg1.getRGB(x, y) != roiImg2.getRGB(x, y)) {
                    diffPixels++;
                }
            }
        }
        double diffPercentage = (roiImg1.getWidth() * roiImg1.getHeight() == 0) ? 0 : (double) diffPixels / (roiImg1.getWidth() * roiImg1.getHeight());
        System.out.println("Image ROI pixel difference percentage: " + diffPercentage * 100 + "%");

        return diffPercentage > 0.02; 
    }

    private BufferedImage getSubImage(BufferedImage parentImage, Rectangle2D roi) {
        int x = Math.max(0, (int) roi.getX());
        int y = Math.max(0, (int) roi.getY());
        int w = Math.min((int) roi.getWidth(), parentImage.getWidth() - x);
        int h = Math.min((int) roi.getHeight(), parentImage.getHeight() - y);

        if (w <= 0 || h <= 0) { 
            System.err.println("getSubImage: Invalid ROI dimensions after adjusting for parent bounds (x:"+x+", y:"+y+", w:"+w+", h:"+h+", parentW:"+parentImage.getWidth()+", parentH:"+parentImage.getHeight()+") from original ROI: "+roi);
            return null;
        }
        return parentImage.getSubimage(x, y, w, h);
    }

    private File regenerateUnsignedPdfAndGetFile(Partnership partnership) throws IOException {
        if (selectedPartner == null && partnership != null) {
             showAlert("Error", "Selected partner details are missing for regeneration.", Alert.AlertType.ERROR);
             System.err.println("SelectedPartner is null during regeneration attempt. This might be an issue.");
        }

        Partner partnerForRegen = null;
        if (selectedPartner != null && selectedPartner.getId() == partnership.getPartnerId()) {
            partnerForRegen = selectedPartner;
        } else {
            try {
                partnerForRegen = ps.findById(partnership.getPartnerId()); 
            } catch (SQLException e) {
                throw new IOException("Failed to fetch partner details for PDF regeneration: " + e.getMessage(), e);
            }
        }
        if (partnerForRegen == null) {
            throw new IOException("Could not retrieve partner details for PDF regeneration (ID: " + partnership.getPartnerId() + ").");
        }

        UserSession session = UserSession.getInstance(); 
        String organizerName = session.getFirstname() + " " + session.getLastname(); 

        String generatedFilePath;
        try {
            generatedFilePath = PDFGenerator.generateContract(
                partnerForRegen.getName(),
                partnerForRegen.getEmail(),
                partnerForRegen.getPhone(), 
                partnerForRegen.getAddress(),
                partnership.getId(),
                partnership.getContractType(),
                partnership.getDescription(),
                partnership.getOrganizerId(),
                organizerName
            );
        } catch (Exception e) {
            throw new IOException("Failed to generate contract PDF using PDFGenerator: " + e.getMessage(), e);
        }

        File tempFile = new File(generatedFilePath); 
        if (!tempFile.exists()) {
            throw new IOException("Failed to regenerate the unsigned PDF to temporary file: " + generatedFilePath);
        }
        PDDocument doc = Loader.loadPDF(tempFile);
        System.out.println("Successfully regenerated unsigned PDF to: " + tempFile.getAbsolutePath() + " (caller will delete after use)");
        doc.close(); 
        return tempFile;
    }

    private void clearPartnershipForm() {
        partnershipTypeComboBox.setValue(null);
        descriptionField.clear();
    }

    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    static class PartnerCell extends ListCell<Partner> {
        private final HBox content;
        private final ImageView imageView;
        private final Label nameLabel;
        private final Label typeLabel;
        private final Label emailLabel;

        public PartnerCell() {
            super();
            imageView = new ImageView();
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            imageView.setPreserveRatio(true);

            nameLabel = new Label();
            nameLabel.setStyle("-fx-font-weight: bold;");
            typeLabel = new Label();
            emailLabel = new Label();

            VBox textDetails = new VBox(nameLabel, typeLabel, emailLabel);
            textDetails.setSpacing(2);
            content = new HBox(imageView, textDetails);
            content.setSpacing(10);
        }

        @Override
        protected void updateItem(Partner partner, boolean empty) {
            super.updateItem(partner, empty);
            if (empty || partner == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(partner.getName());
                typeLabel.setText("Type: " + (partner.getType() != null ? partner.getType().toString() : "N/A"));
                emailLabel.setText("Email: " + (partner.getEmail() != null ? partner.getEmail() : "N/A"));
                if (partner.getImagePath() != null && !partner.getImagePath().isEmpty()) {
                    try {
                        File imageFile = new File(partner.getImagePath());
                        if(imageFile.exists()){
                             Image image = new Image(imageFile.toURI().toString(), 50, 50, true, true);
                             imageView.setImage(image);
                        } else {
                             imageView.setImage(null);
                        }
                    } catch (Exception e) {
                        imageView.setImage(null);
                        System.err.println("Error loading image for partner cell " + partner.getName() + ": " + e.getMessage());
                    }
                } else {
                    imageView.setImage(null);
                }
                setGraphic(content);
            }
        }
    }

    @FXML
    private void DisplayImage() {
        if (selectedPartner != null && selectedPartner.getImagePath() != null && !selectedPartner.getImagePath().isEmpty()) {
            try {
                File imageFile = new File(selectedPartner.getImagePath());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    myImage.setImage(image);
                    myImage.setVisible(true);
                } else {
                    System.err.println("DisplayImage: Image file not found: " + selectedPartner.getImagePath());
                    myImage.setImage(null);
                    myImage.setVisible(false);
                }
            } catch (Exception e) {
                System.err.println("DisplayImage: Error loading image: " + e.getMessage());
                e.printStackTrace();
                myImage.setImage(null);
                myImage.setVisible(false);
            }
        } else {
            myImage.setImage(null);
            myImage.setVisible(false);
        }
    }

    @FXML
    private void goToAdminDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load admin dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void returnToOrganizer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/organisateurDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) returnBtn.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
}
