// src/main/java/com/medimart/service/InvoiceService.java
package com.medimart.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.medimart.model.Order;
import com.medimart.model.OrderItem;
import com.medimart.repository.OrderItemRepository;
import com.medimart.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class InvoiceService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;

    public InvoiceService(OrderRepository orderRepo,
                          OrderItemRepository itemRepo) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
    }

    public byte[] generateInvoicePdf(Long orderId) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // uses relation: OrderItem.order.id
        List<OrderItem> items = itemRepo.findByOrder_Id(orderId);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(doc, baos);

            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font bold = new Font(Font.HELVETICA, 11, Font.BOLD);

            // ---------- HEADER ----------
            Paragraph title = new Paragraph("MediMart Invoice", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Order ID: " + order.getId(), normal));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph(" "));

            // ---------- TABLE ----------
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1.5f, 2f, 2f});

            table.addCell(new Phrase("Item", bold));
            table.addCell(new Phrase("Qty", bold));
            table.addCell(new Phrase("Unit Price", bold));
            table.addCell(new Phrase("Subtotal", bold));

            double total = 0.0;

            for (OrderItem it : items) {
                double sub = it.getQuantity() * it.getUnitPrice();
                total += sub;

                // ✅ item name from Medicine
                String itemName = "Medicine";
                if (it.getMedicine() != null && it.getMedicine().getName() != null) {
                    itemName = it.getMedicine().getName();
                }

                table.addCell(new Phrase(itemName, normal));
                table.addCell(new Phrase(String.valueOf(it.getQuantity()), normal));
                table.addCell(new Phrase(String.format("%.2f BDT", it.getUnitPrice()), normal));
                table.addCell(new Phrase(String.format("%.2f BDT", sub), normal));
            }

            doc.add(table);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(
                    "Total Amount: " + String.format("%.2f BDT", total), bold));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
}
