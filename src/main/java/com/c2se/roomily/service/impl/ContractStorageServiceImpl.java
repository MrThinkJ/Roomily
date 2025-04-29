package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.service.ContractStorageService;
import com.c2se.roomily.service.StorageService;
import com.lowagie.text.pdf.BaseFont;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class ContractStorageServiceImpl implements ContractStorageService {
    @Value("${app.resource.static-location}")
    private String staticLocation;
    private final StorageService storageService;
    private final StorageConfig storageConfig;

    @Override
    public void saveRoomContract(String roomId, String contractHtml) {
        String contractFilename = "contract_" + roomId + ".html";
        try {
            storageService.putObject(contractHtml.getBytes(), storageConfig.getBucketContract(), contractFilename,
                                     "text/html");
            uploadPdf(contractHtml, "contract_" + roomId + ".pdf");
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error saving room contract: " + e.getMessage());
        }
    }

    @Override
    public void saveRentedRoomContract(String rentedRoomId, String contractHtml) {
        String contractFilename = "contract_rented_room_" + rentedRoomId + ".html";
        try {
            storageService.putObject(contractHtml.getBytes(), storageConfig.getBucketContract(), contractFilename,
                                     "text/html");
            uploadPdf(contractHtml, "contract_rented_room_" + rentedRoomId + ".pdf");
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error saving rented room contract: " + e.getMessage());
        }
    }

    @Override
    public byte[] getLandlordContract(String landlordId) {
        String contractFilename = "landlord_contract_" + landlordId + ".html";
        try {
            byte[] contractBytes = storageService.getObject(storageConfig.getBucketContract(), contractFilename).readAllBytes();
            Document document = Jsoup.parse(new String(contractBytes, "UTF-8"));
            return document.html().getBytes("UTF-8");
        } catch (Exception e) {
            return null; // Return null if the contract is not found
        }
    }

    @Override
    public byte[] getRoomContract(String roomId) {
        String contractFilename = "contract_" + roomId + ".html";
        try {
//            byte[] contractBytes = storageService.getObject(storageConfig.getBucketContract(), contractFilename).readAllBytes();
            return storageService.getObject(storageConfig.getBucketContract(), contractFilename).readAllBytes();
//            Document document = Jsoup.parse(new String(contractBytes, "UTF-8"));
//            return document.html().getBytes("UTF-8");
        } catch (Exception e) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR,
                                   "Contract not found for room: " + roomId);
        }
    }

    @Override
    public byte[] getRentedRoomContract(String rentedRoomId) {
        String contractFilename = "contract_rented_room_" + rentedRoomId + ".html";
        try {
            byte[] contractBytes = storageService.getObject(storageConfig.getBucketContract(), contractFilename).readAllBytes();
            Document document = Jsoup.parse(new String(contractBytes, "UTF-8"));
            return document.html().getBytes("UTF-8");
        } catch (Exception e) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR,
                                   "Contract not found for rented room: " + rentedRoomId);
        }
    }

    @Override
    public byte[] getRoomContractPdf(String roomId) {
        String contractFilename = "contract_" + roomId + ".pdf";
        try {
            return storageService.getObject(storageConfig.getBucketContract(), contractFilename).readAllBytes();
        } catch (Exception e) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR,
                                   "Contract PDF not found for room: " + roomId);
        }
    }

    @Override
    public byte[] getRentedRoomContractPdf(String rentedRoomId) {
        String contractFilename = "contract_rented_room_" + rentedRoomId + ".pdf";
        try {
            return storageService.getObject(storageConfig.getBucketContract(), contractFilename).readAllBytes();
        } catch (Exception e) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR,
                                   "Contract PDF not found for rented room: " + rentedRoomId);
        }
    }

    @Override
    public boolean roomContractExists(String roomId) {
        String contractFilename = "contract_" + roomId + ".html";
        try {
            storageService.getObject(storageConfig.getBucketContract(), contractFilename);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean rentedRoomContractExists(String rentedRoomId) {
        String contractFilename = "contract_rented_room_" + rentedRoomId + ".html";
        try {
            storageService.getObject(storageConfig.getBucketContract(), contractFilename);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void uploadPdf(String contractHtml, String contractFilename) {
        try {
            Document document = Jsoup.parse(contractHtml, "UTF-8");
            document.outputSettings()
                    .syntax(Document.OutputSettings.Syntax.xml)
                    .escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml)
                    .charset("UTF-8")
                    .prettyPrint(false);
            StringBuilder cssBuilder = new StringBuilder();
            document.select("style").forEach(styleTag -> {
                cssBuilder.append(styleTag.html());
            });

            cssBuilder.append("\n")
                    .append("* { margin: 0; padding: 0; box-sizing: border-box; }\n")
                    .append("body { font-family: 'Times New Roman', serif; margin: 0; padding: 0; }\n")
                    .append("@page { margin: 0cm; }\n")
                    .append(".page { width: 100%; max-width: 21cm; margin: 0 auto; padding: 1cm; background: white; text-align: justify; line-height: 1.3; }\n")
                    .append(".content { margin: 0; padding: 0; }\n")
                    .append(".content p { margin: 0.5em 0; text-indent: 1.5em; line-height: 1.3; }\n")
                    .append(".input-field { border: none; border-bottom: 1px dotted #999; padding: 2px 5px; }\n")
                    .append(".header { text-align: center; margin-bottom: 20px; }\n")
                    .append(".title { text-align: center; font-weight: bold; font-size: 18px; margin: 20px 0; }\n");

            // Update the style in the document
            Element head = document.head();
            head.select("style").remove();
            head.append("<style type=\"text/css\">" + cssBuilder.toString() + "</style>");

            // Apply critical styles inline
            fixPageStyles(document);

            // Clean up the document structure
            document.childNodes().forEach(node -> {
                if (node.nodeName().equals("#doctype")) {
                    node.remove();
                }
            });

            // Create proper XHTML
            String xhtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
                    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                    document.html();
            try (ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {
                ITextRenderer renderer = new ITextRenderer();
                renderer.getFontResolver().addFont(staticLocation+"/fonts/times.ttf", "Times New Roman",
                                                   BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
                renderer.setDocumentFromString(xhtml);
                renderer.layout();
                renderer.createPDF(pdfOutputStream);
                ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
                storageService.putObject(pdfInputStream, storageConfig.getBucketContract(), contractFilename,
                                         "application/pdf");
            } catch (Exception e) {
                throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                       "Error uploading PDF: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error processing contract: " + e.getMessage());
        }
    }

    private void fixPageStyles(Document document) {
        document.body().attr("style", "margin: 0; padding: 0; font-family: 'Times New Roman', serif;");
        document.select(".page").forEach(element -> {
            element.attr("style", "width: 100%; max-width: 21cm; margin: 0 auto; padding: 1cm; text-align: justify; line-height: 1.3; background: white;");
        });
        document.select(".content").forEach(element -> {
            element.attr("style", "margin: 0; padding: 0;");
        });
        document.select("p").forEach(element -> {
            String currentStyle = element.attr("style");
            element.attr("style", currentStyle + "; margin: 0.5em 0; text-indent: 1.5em;");
        });
    }
}