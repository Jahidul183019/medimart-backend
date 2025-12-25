// src/main/java/com/medimart/service/InvoicePdfService.java
package com.medimart.service;

import com.medimart.model.Order;
import com.medimart.model.OrderItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoicePdfService {

    public byte[] generateInvoicePdf(Order order) {
        try {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, baos);

            doc.open();

            // ---------------------------
            // COLORS
            // ---------------------------
            Color bgTop = new Color(233, 245, 255);
            Color blue = new Color(0, 122, 255);
            Color green = new Color(0, 150, 60);
            Color lightHeader = new Color(236, 243, 255);

            // ---------------------------
            // FONTS
            // ---------------------------
            Font titleFont =
                    new Font(Font.HELVETICA, 20, Font.BOLD, blue);
            Font normalFont =
                    new Font(Font.HELVETICA, 11, Font.NORMAL, Color.DARK_GRAY);
            Font headerFont =
                    new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
            Font totalFont =
                    new Font(Font.HELVETICA, 14, Font.BOLD, green);
            Font smallGrey =
                    new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);

            // ---------------------------
            // TOP SOFT BAND
            // ---------------------------
            PdfPTable topBand = new PdfPTable(1);
            topBand.setWidthPercentage(100);
            PdfPCell bandCell = new PdfPCell(new Phrase(" "));
            bandCell.setBorder(Rectangle.NO_BORDER);
            bandCell.setBackgroundColor(bgTop);
            bandCell.setFixedHeight(40f);
            topBand.addCell(bandCell);
            doc.add(topBand);

            // ---------------------------
            // HEADER: LOGO + TITLE + META
            // ---------------------------
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{3f, 2f});

            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);

            // Try to show logo, but don't crash if missing
            try {
                InputStream logoStream =
                        getClass().getResourceAsStream("/logo_medimart.png");
                if (logoStream != null) {
                    byte[] logoBytes = logoStream.readAllBytes();
                    Image logo = Image.getInstance(logoBytes);
                    logo.scaleToFit(90, 40);
                    logo.setAlignment(Image.LEFT);
                    left.addElement(logo);
                }
            } catch (Exception ignored) {
                // skip logo silently
            }

            Paragraph title = new Paragraph("Order Bill / Invoice", titleFont);
            title.setSpacingBefore(5f);
            left.addElement(title);
            left.addElement(new Paragraph("MediMart Online Pharmacy", smallGrey));
            headerTable.addCell(left);

            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.NO_BORDER);
            right.setHorizontalAlignment(Element.ALIGN_RIGHT);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String dateStr = (order.getCreatedAt() != null)
                    ? order.getCreatedAt().format(fmt)
                    : "—";

            right.addElement(new Paragraph("Order ID: #" + order.getId(), normalFont));
            right.addElement(new Paragraph("Order Date: " + dateStr, normalFont));
            headerTable.addCell(right);

            doc.add(headerTable);
            doc.add(Chunk.NEWLINE);

            // ---------------------------
            // CUSTOMER BLOCK
            // ---------------------------
            PdfPTable customerTable = new PdfPTable(1);
            customerTable.setWidthPercentage(100);
            PdfPCell custCell = new PdfPCell();
            custCell.setPadding(8f);
            custCell.setBackgroundColor(new Color(250, 252, 255));

            String name = order.getCustomerName() != null
                    ? order.getCustomerName()
                    : "Customer";
            String addr = order.getCustomerAddress() != null
                    ? order.getCustomerAddress()
                    : "—";
            String phone = order.getCustomerPhone() != null
                    ? order.getCustomerPhone()
                    : "—";

            custCell.addElement(new Paragraph("Billed To:", headerFont));
            custCell.addElement(new Paragraph(name, normalFont));
            custCell.addElement(new Paragraph(addr, normalFont));
            custCell.addElement(new Paragraph("Phone: " + phone, normalFont));

            customerTable.addCell(custCell);
            doc.add(customerTable);
            doc.add(Chunk.NEWLINE);

            // ---------------------------
            // ITEMS TABLE
            // ---------------------------
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1.2f, 2f, 2f});

            addHeaderCell(table, "Medicine", headerFont, lightHeader);
            addHeaderCell(table, "Qty", headerFont, lightHeader);
            addHeaderCell(table, "Unit Price (BDT)", headerFont, lightHeader);
            addHeaderCell(table, "Line Total (BDT)", headerFont, lightHeader);

            double total = 0.0;
            List<OrderItem> items = order.getItems();

            if (items != null && !items.isEmpty()) {
                for (OrderItem item : items) {
                    String medName =
                            (item.getMedicine() != null && item.getMedicine().getName() != null)
                                    ? item.getMedicine().getName()
                                    : "Medicine";

                    int qty = item.getQuantity();           // assumes primitive int
                    double price = item.getUnitPrice();     // assumes primitive double
                    double lineTotal = qty * price;
                    total += lineTotal;

                    addBodyCell(table, medName, normalFont);
                    addBodyCell(table, String.valueOf(qty), normalFont);
                    addBodyCell(table, String.format("%.2f", price), normalFont);
                    addBodyCell(table, String.format("%.2f", lineTotal), normalFont);
                }
            } else {
                PdfPCell empty = new PdfPCell(
                        new Phrase("No items found for this order.", normalFont));
                empty.setColspan(4);
                empty.setPadding(8f);
                table.addCell(empty);

                if (order.getTotalAmount() != null) {
                    total = order.getTotalAmount();
                }
            }

            doc.add(table);

            // ---------------------------
            // GRAND TOTAL
            // ---------------------------
            Paragraph totalP = new Paragraph(
                    "Grand Total: " + String.format("%.2f BDT", total),
                    totalFont
            );
            totalP.setSpacingBefore(16f);
            doc.add(totalP);

            // ---------------------------
            // THANK YOU
            // ---------------------------
            Paragraph thankYou = new Paragraph(
                    "Thank you for shopping with MediMart.",
                    normalFont
            );
            thankYou.setAlignment(Element.ALIGN_CENTER);
            thankYou.setSpacingBefore(25f);
            doc.add(thankYou);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();  // so you can see real cause in console
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    // ---------------------------------------------------------
    // HELPER: Add header cell
    // ---------------------------------------------------------
    private void addHeaderCell(PdfPTable table,
                               String text,
                               Font font,
                               Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(bg);
        cell.setPadding(8);
        table.addCell(cell);
    }

    // ---------------------------------------------------------
    // HELPER: Add body cell
    // ---------------------------------------------------------
    private void addBodyCell(PdfPTable table,
                             String text,
                             Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        table.addCell(cell);
    }
}
