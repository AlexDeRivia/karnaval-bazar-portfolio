package com.karnaval.servicio;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import com.karnaval.entidad.OnlineOrder;
import com.karnaval.entidad.OnlineOrderLine;

@Service
public class InvoicePdfService {
    private static final PDType1Font REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Lima"));

    public byte[] generate(OnlineOrder order) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream canvas = new PDPageContentStream(document, page)) {
                canvas.setNonStrokingColor(new Color(31, 46, 44));
                canvas.addRect(0, 772, 595, 70);
                canvas.fill();
                canvas.setNonStrokingColor(Color.WHITE);
                write(canvas, BOLD, 18, 48, 801, "BAZAR CENTRAL");
                write(canvas, REGULAR, 9, 48, 785, "FACTURA INFORMATIVA - SIN VALIDEZ TRIBUTARIA");

                canvas.setNonStrokingColor(new Color(31, 46, 44));
                write(canvas, BOLD, 10, 48, 741, "Pedido");
                write(canvas, REGULAR, 10, 48, 724, order.getId());
                write(canvas, BOLD, 10, 360, 741, "Fecha de confirmacion");
                write(canvas, REGULAR, 10, 360, 724, DATE.format(order.getPaidAt()));
                if (order.getCustomerEmail() != null) {
                    write(canvas, REGULAR, 10, 48, 698, "Comprador: " + trim(order.getCustomerEmail(), 70));
                }

                canvas.setStrokingColor(new Color(216, 223, 218));
                canvas.moveTo(48, 675);
                canvas.lineTo(547, 675);
                canvas.stroke();
                write(canvas, BOLD, 10, 48, 655, "PRODUCTO");
                write(canvas, BOLD, 10, 385, 655, "CANT.");
                write(canvas, BOLD, 10, 445, 655, "IMPORTE");
                float y = 630;
                for (OnlineOrderLine line : order.getLines()) {
                    write(canvas, REGULAR, 10, 48, y, fit(line.getName(), 310));
                    write(canvas, REGULAR, 10, 390, y, Integer.toString(line.getQuantity()));
                    write(canvas, REGULAR, 10, 445, y, money(line.getSubtotal()));
                    y -= 23;
                }
                canvas.moveTo(48, y - 3);
                canvas.lineTo(547, y - 3);
                canvas.stroke();
                write(canvas, BOLD, 13, 370, y - 29, "TOTAL  " + money(order.getTotalAmount()));
                write(canvas, REGULAR, 9, 48, 88, "Pago confirmado mediante Stripe Checkout.");
                write(canvas, REGULAR, 9, 48, 73, "Este documento no tiene validez tributaria.");
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el recibo PDF", ex);
        }
    }

    private static String money(long cents) {
        return String.format(java.util.Locale.US, "S/ %,.2f", cents / 100.0);
    }

    private static String trim(String text, int max) {
        return text.length() > max ? text.substring(0, max - 3) + "..." : text;
    }

    private static String fit(String value, float width) throws IOException {
        String result = ascii(value);
        while (REGULAR.getStringWidth(result) * 10 / 1000 > width && result.length() > 3) {
            result = result.substring(0, result.length() - (result.endsWith("...") ? 4 : 1)) + "...";
        }
        return result;
    }

    private static void write(PDPageContentStream canvas, PDType1Font font, int size,
            float x, float y, String value) throws IOException {
        canvas.beginText();
        canvas.setFont(font, size);
        canvas.newLineAtOffset(x, y);
        canvas.showText(ascii(value));
        canvas.endText();
    }

    private static String ascii(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^\\x20-\\x7E]", "?");
    }
}
