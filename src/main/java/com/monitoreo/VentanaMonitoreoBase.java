package com.monitoreo;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ImageIcon;

/**
 * Clase base para las ventanas principales del sistema de monitoreo.
 * Centraliza estilos, colores y componentes gráficos reutilizables
 * para reducir código duplicado en clases de interfaz grandes.
 *
 * Demuestra el uso de HERENCIA: otras ventanas como {@link InterfazGrafica}
 * extienden de esta clase para heredar comportamiento y utilidades comunes.
 */
public abstract class VentanaMonitoreoBase extends JFrame {

    // Colores de estilo compartidos por toda la interfaz
    protected static final Color COLOR_FONDO = new Color(245, 248, 255);
    protected static final Color COLOR_PANEL = Color.WHITE;
    protected static final Color COLOR_TEXTO = new Color(34, 40, 49);
    protected static final Color COLOR_TEXTO_SUAVE = new Color(105, 117, 134);
    protected static final Color COLOR_BORDE = new Color(220, 226, 240);
    protected static final Color COLOR_ACCION_PRIMARIA = new Color(76, 132, 255);
    protected static final Color COLOR_ACCION_SECUNDARIA = new Color(78, 205, 196);
    protected static final Color COLOR_ACCION_PELIGRO = new Color(255, 99, 132);
    protected static final Color COLOR_ACCION_AVISO = new Color(255, 170, 76);
    protected static final Color COLOR_ACCION_OK = new Color(46, 213, 115);
    protected static final Color COLOR_GRADIENT_INICIO = new Color(108, 149, 255);
    protected static final Color COLOR_GRADIENT_FIN = new Color(165, 120, 255);

    /**
     * Constructor protegido para evitar instancias directas de la clase base.
     * Las subclases deben llamar a super() en su propio constructor.
     */
    protected VentanaMonitoreoBase(String titulo) {
        super(titulo);
        // Configuración básica común
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO);
    }

    /**
     * Crea un icono de botón con un color de fondo y un símbolo centrado.
     * Método utilitario compartido para mantener consistencia visual.
     */
    protected ImageIcon crearIconoBoton(Color colorFondo, String simbolo) {
        int size = 18;
        BufferedImage icono = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icono.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(colorFondo);
        g2d.fillRoundRect(0, 0, size - 1, size - 1, 6, 6);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD,
                simbolo.length() > 2 ? 9 : 12));
        java.awt.font.FontRenderContext frc = g2d.getFontRenderContext();
        java.awt.geom.Rectangle2D bounds = g2d.getFont().getStringBounds(simbolo, frc);
        int x = (int) ((size - bounds.getWidth()) / 2);
        int y = (int) ((size - bounds.getHeight()) / 2 - bounds.getY());
        g2d.drawString(simbolo, x, y);
        g2d.dispose();
        return new ImageIcon(icono);
    }

    /**
     * Crea el icono principal de la aplicación (usado en la ventana y diálogos).
     */
    protected Image crearIcono() {
        int size = 32;
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo con gradiente azul
        GradientPaint gradient = new GradientPaint(0, 0, new Color(52, 152, 219),
                                                   size, size, new Color(41, 128, 185));
        g2d.setPaint(gradient);
        g2d.fillRoundRect(2, 2, size - 4, size - 4, 6, 6);

        // Dibujar símbolo de red/monitoreo
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new java.awt.BasicStroke(2.5f));

        // Nodo central
        int centerX = size / 2;
        int centerY = size / 2;
        g2d.fillOval(centerX - 4, centerY - 4, 8, 8);

        // Ondas concéntricas
        g2d.setStroke(new java.awt.BasicStroke(1.5f));
        for (int i = 0; i < 3; i++) {
            int radius = 8 + i * 4;
            g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 0, 360);
        }

        g2d.dispose();
        return icon;
    }

    /**
     * Panel con bordes redondeados reutilizable.
     * Se usa por ejemplo como "card" en la interfaz principal.
     */
    protected static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(LayoutManager layout, int radius) {
            super(layout);
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Panel con fondo de gradiente reutilizable.
     * Útil para secciones destacadas como el encabezado "hero".
     */
    protected static class GradientPanel extends JPanel {
        private final Color inicio;
        private final Color fin;

        public GradientPanel(Color inicio, Color fin) {
            this.inicio = inicio;
            this.fin = fin;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, inicio, getWidth(), getHeight(), fin));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}


