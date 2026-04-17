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
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class TicketGeneratorService {

    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    @Transactional
    public void generateTicket(Long orderId) {
        System.out.println("RabbitMQ Task Started: Generating PDF for Order ID " + orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found for PDF generation"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 1. Create the PDF Document
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // 2. Add Content to the PDF
            Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD);
            document.add(new Paragraph("FLASHTIX OFFICIAL TICKET", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Event: " + order.getEvent().getName()));
            document.add(new Paragraph("Ticket Holder: " + order.getUser().getUsername()));
            document.add(new Paragraph("Quantity: " + order.getTicketQuantity()));
            document.add(new Paragraph("Total Paid: $" + order.getTotalAmount()));
            document.add(new Paragraph("Order ID: " + order.getId()));

            document.close();

            // 3. Upload the generated PDF bytes to MinIO
            String fileName = "ticket_order_" + orderId + ".pdf";
            String fileUrl = fileStorageService.uploadTicketPdf(fileName, baos.toByteArray());

            System.out.println("RabbitMQ Task Complete: Ticket uploaded to " + fileUrl);

        } catch (Exception e) {
            System.err.println("Failed to generate PDF ticket: " + e.getMessage());
        }
    }
}