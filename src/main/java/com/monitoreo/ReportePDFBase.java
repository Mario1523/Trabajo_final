package com.monitoreo;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

/**
 * Clase base abstracta para la generación de reportes en PDF.
 * Centraliza comportamiento común (directorio, fecha, encabezado y pie),
 * y permite que subclases concretas definan el contenido específico.
 */
public abstract class ReportePDFBase {

    protected final String directorio;
    protected final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected ReportePDFBase(String directorio) {
        this.directorio = directorio;
        asegurarDirectorio();
    }

    /**
     * Método plantilla principal: crea documento, delega en la subclase para
     * escribir el contenido y cierra el PDF.
     *
     * @param nombreArchivo nombre completo del archivo PDF a generar
     * @return ruta del archivo generado o null si ocurre un error
     */
    protected String generarDocumento(String nombreArchivo) {
        try {
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            escribirContenido(document);

            document.close();
            return nombreArchivo;
        } catch (Exception e) {
            System.err.println("Error al generar PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Implementado por cada reporte concreto para añadir su contenido al PDF.
     */
    protected abstract void escribirContenido(Document document) throws Exception;

    /**
     * Crea un párrafo de título centrado.
     */
    protected Paragraph crearTitulo(String texto) {
        return new Paragraph(texto)
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
    }

    /**
     * Crea un párrafo con la fecha de generación del reporte.
     */
    protected Paragraph crearParrafoFecha() {
        return new Paragraph("Fecha: " + LocalDateTime.now().format(formatter))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
    }

    private void asegurarDirectorio() {
        File dir = new File(directorio);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Directorio de reportes creado: " + dir.getAbsolutePath());
        }
    }
}


