package com.flashtix.ticketing.service;

import com.flashtix.common.config.RabbitMQConfig;
import com.flashtix.common.service.FileStorageService;
import com.flashtix.ticketing.entity.Order;
import com.flashtix.ticketing.repository.OrderRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketGeneratorService {

    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;

    /**
     * RabbitMQ consumer — triggered by OrderConsumer after a ticket is confirmed.
     * Runs in a separate thread pool from Kafka, so PDF generation never blocks order processing.
     *
     * Flow: OrderConsumer → RabbitMQ queue → TicketGeneratorService → SeaweedFS (S3)
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    @Transactional
    public void generateTicket(Long orderId) {
        log.info("[PDF-GENERATOR] ▶ RabbitMQ task started — generating PDF for orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("[PDF-GENERATOR] ❌ Order not found — orderId={}", orderId);
                    return new RuntimeException("Order not found for PDF generation: " + orderId);
                });

        log.info("[PDF-GENERATOR] Generating ticket for user='{}', event='{}', qty={}",
                order.getUser().getUsername(), order.getEvent().getName(), order.getTicketQuantity());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // ── Build PDF document ────────────────────────────────────────────────
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD);
            document.add(new Paragraph("FLASHTIX OFFICIAL TICKET", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Event: " + order.getEvent().getName()));
            document.add(new Paragraph("Ticket Holder: " + order.getUser().getUsername()));
            document.add(new Paragraph("Quantity: " + order.getTicketQuantity()));
            document.add(new Paragraph("Total Paid: $" + order.getTotalAmount()));
            document.add(new Paragraph("Order ID: " + order.getId()));
            document.close();

            int pdfSizeKb = baos.size() / 1024;
            log.debug("[PDF-GENERATOR] PDF built — size={}KB", pdfSizeKb);

            // ── Upload to SeaweedFS (S3-compatible) ───────────────────────────────
            String fileName = "ticket_order_" + orderId + ".pdf";
            String fileUrl = fileStorageService.uploadTicketPdf(fileName, baos.toByteArray());

            log.info("[PDF-GENERATOR] ✅ Ticket PDF uploaded — orderId={}, file='{}', url='{}'",
                    orderId, fileName, fileUrl);

        } catch (Exception e) {
            log.error("[PDF-GENERATOR] ❌ Failed to generate/upload PDF for orderId={} — {}",
                    orderId, e.getMessage(), e);
        }
    }
}