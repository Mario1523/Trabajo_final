package com.monitoreo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Interfaz gráfica para el sistema de monitoreo de hosts y dispositivos de red.
 * Proporciona una interfaz visual para gestionar dispositivos, iniciar/detener monitoreo,
 * escanear la red local y generar reportes en PDF.
 * 
 * @author Sistema de Monitoreo
 * @version 1.0
 */
public class InterfazGrafica extends VentanaMonitoreoBase {

    // Componentes principales de la interfaz
    private Monitoreo monitoreo;                    // Instancia del sistema de monitoreo
    private DefaultTableModel tablaModelo;          // Modelo de datos para la tabla de dispositivos
    private JTable tablaDispositivos;               // Tabla que muestra los dispositivos monitoreados
    private JTextArea areaAlertas;                  // Área de texto para mostrar alertas y eventos
    private JButton btnIniciar;                     // Botón para iniciar el monitoreo
    private JButton btnDetener;                     // Botón para detener el monitoreo
    private JButton btnEscanear;                    // Botón para escanear la red
    private JButton btnGenerarPDF;                  // Botón para generar informe PDF
    private JTextField txtIdDispositivo;             // Campo de texto para ID del dispositivo
    private JTextField txtIPDispositivo;             // Campo de texto para IP/Host del dispositivo
    private JSpinner spinnerIntervalo;               // Spinner para configurar intervalo de monitoreo
    private javax.swing.Timer timerActualizacion;   // Timer para actualizar la tabla periódicamente
    private DecimalFormat formatoDecimal;          // Formateador para números decimales

    /**
     * Constructor principal de la interfaz gráfica.
     * Inicializa todos los componentes y configura la interfaz.
     * 
     * @param monitoreo Instancia del sistema de monitoreo a utilizar
     */
    public InterfazGrafica(Monitoreo monitoreo) {
        super("Sistema de Monitoreo de Hosts");
        this.monitoreo = monitoreo;
        this.formatoDecimal = new DecimalFormat("#.##");
        
        // Inicializar componentes de la interfaz
        inicializarComponentes();
        configurarInterfaz();
        configurarTimer();
        
        // Configurar listener para recibir alertas del sistema de monitoreo
        monitoreo.getManejoAlertas().agregarObservador(mensaje -> {
            SwingUtilities.invokeLater(() -> {
                agregarAlerta(mensaje);
            });
        });
        
        // Escanear red WiFi automáticamente al iniciar (opcional)
        escanearRedAlInicio();
    }

    private void estilizarBotonesSpinner(JSpinner spinner) {
        for (java.awt.Component comp : spinner.getComponents()) {
            if (comp instanceof javax.swing.JButton) {
                javax.swing.JButton btn = (javax.swing.JButton) comp;
                btn.setBackground(COLOR_ACCION_PRIMARIA);
                btn.setForeground(Color.WHITE);
                btn.setOpaque(true);
                btn.setBorder(BorderFactory.createLineBorder(COLOR_ACCION_PRIMARIA.darker()));
            }
        }
    }

    private RoundedPanel crearCard(String titulo) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(), 24);
        card.setBackground(COLOR_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(228, 234, 249)),
            BorderFactory.createEmptyBorder(12, 18, 18, 18)
        ));
        card.add(crearEncabezadoSeccion(titulo), BorderLayout.NORTH);
        return card;
    }

    private JPanel crearEncabezadoSeccion(String titulo) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel label = new JLabel(titulo);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        label.setForeground(COLOR_TEXTO);
        header.add(label, BorderLayout.WEST);
        JSeparator separator = new JSeparator();
        separator.setForeground(COLOR_BORDE);
        separator.setBackground(COLOR_BORDE);
        header.add(separator, BorderLayout.SOUTH);
        return header;
    }

    /**
     * Muestra un diálogo al iniciar la aplicación preguntando si se desea escanear la red.
     * Si el usuario acepta, inicia el escaneo automático de dispositivos en la red WiFi.
     */
    private void escanearRedAlInicio() {
        // Mostrar diálogo de bienvenida con opción de escanear
        SwingUtilities.invokeLater(() -> {
            int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Desea escanear automáticamente los dispositivos en su red WiFi?\n\n" +
                "Esto puede tardar 30-60 segundos.",
                "Escaneo Inicial de Red",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (opcion == JOptionPane.YES_OPTION) {
                escanearRed();
            } else {
                agregarAlerta("Aplicación iniciada. Use 'Escanear Red' para descubrir dispositivos en su red WiFi.");
            }
        });
    }

    /**
     * Inicializa todos los componentes visuales de la interfaz.
     * Configura tablas, campos de texto, botones y otros elementos.
     */
    private void inicializarComponentes() {
        // Configurar tabla de dispositivos con columnas predefinidas
        String[] columnas = {"Dispositivo", "IP", "Estado", "Disponibilidad %", "Tiempo Resp. (ms)", "Total Chequeos", "Fallos"};
        tablaModelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // La tabla no es editable
            }
        };
        tablaDispositivos = new JTable(tablaModelo);
        tablaDispositivos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaDispositivos.setRowHeight(26);
        tablaDispositivos.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        
        // Configurar área de alertas (no editable, solo lectura)
        areaAlertas = new JTextArea(10, 50);
        areaAlertas.setEditable(false);
        areaAlertas.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaAlertas.setMargin(new java.awt.Insets(8, 10, 8, 10));
        
        // Configurar campos de texto para agregar dispositivos
        txtIdDispositivo = new JTextField(15);
        txtIdDispositivo.setBackground(Color.WHITE);
        txtIdDispositivo.setForeground(COLOR_TEXTO);
        txtIdDispositivo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        txtIdDispositivo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtIPDispositivo = new JTextField(15);
        txtIPDispositivo.setBackground(Color.WHITE);
        txtIPDispositivo.setForeground(COLOR_TEXTO);
        txtIPDispositivo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        txtIPDispositivo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // Configurar spinner para intervalo de monitoreo (1-300 segundos, valor inicial 10)
        spinnerIntervalo = new JSpinner(new SpinnerNumberModel(10, 1, 300, 1));
        // Configurar colores del spinner: fondo negro y texto blanco
        spinnerIntervalo.setBackground(Color.WHITE);
        spinnerIntervalo.setForeground(COLOR_TEXTO);
        spinnerIntervalo.setOpaque(true);
        // Configurar el editor del spinner con fondo negro y texto blanco
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinnerIntervalo.getEditor();
        editor.getTextField().setBackground(Color.WHITE);
        editor.getTextField().setForeground(COLOR_TEXTO);
        editor.getTextField().setCaretColor(COLOR_ACCION_PRIMARIA);
        editor.getTextField().setOpaque(true);
        editor.getTextField().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12)); // Texto en negrita y más grande
        editor.getTextField().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACCION_PRIMARIA, 2),
            BorderFactory.createEmptyBorder(3, 8, 3, 8) // Más padding
        ));
        
        // Configurar botón "Iniciar Monitoreo" (verde)
        btnIniciar = new JButton("Iniciar Monitoreo");
        btnIniciar.setBackground(COLOR_ACCION_OK);
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setOpaque(true);
        btnIniciar.setBorderPainted(false);
        btnIniciar.setFocusPainted(false);
        btnIniciar.setIcon(crearIconoBoton(COLOR_ACCION_OK.darker(), "\u25B6"));
        btnIniciar.setIconTextGap(8);
        
        // Configurar botón "Detener Monitoreo" (rojo)
        btnDetener = new JButton("Detener Monitoreo");
        btnDetener.setBackground(COLOR_ACCION_PELIGRO);
        btnDetener.setForeground(Color.WHITE);
        btnDetener.setOpaque(true);
        btnDetener.setBorderPainted(false);
        btnDetener.setFocusPainted(false);
        btnDetener.setEnabled(false); // Inicialmente deshabilitado
        btnDetener.setIcon(crearIconoBoton(COLOR_ACCION_PELIGRO.darker(), "\u25A0"));
        btnDetener.setIconTextGap(8);
        
        // Configurar botón "Escanear Red" (azul claro)
        btnEscanear = new JButton("Escanear Red");
        btnEscanear.setIcon(crearIconoBoton(COLOR_ACCION_SECUNDARIA.darker(), "\uD83D\uDD0D"));
        btnEscanear.setIconTextGap(6);
        
        // Configurar botón "Generar Informe PDF" (azul)
        btnGenerarPDF = new JButton("Generar Informe PDF");
        btnGenerarPDF.setBackground(COLOR_ACCION_PRIMARIA);
        btnGenerarPDF.setForeground(Color.WHITE);
        btnGenerarPDF.setFocusPainted(false);
        btnGenerarPDF.setOpaque(true);
        btnGenerarPDF.setBorderPainted(false);
        btnGenerarPDF.setContentAreaFilled(true);
        btnGenerarPDF.setIcon(crearIconoBoton(COLOR_ACCION_PRIMARIA.darker(), "PDF"));
        btnGenerarPDF.setIconTextGap(8);
    }

    /**
     * Configura el layout y los componentes principales de la interfaz.
     * Establece colores, bordes, paneles y organiza todos los elementos visuales.
     */
    private void configurarInterfaz() {
        setTitle("Sistema de Monitoreo de Hosts");
        setLayout(new BorderLayout(16, 16));
        
        // Hero con gradiente
        GradientPanel panelHero = new GradientPanel(COLOR_GRADIENT_INICIO, COLOR_GRADIENT_FIN);
        panelHero.setLayout(new BorderLayout());
        panelHero.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));
        
        JPanel heroContenido = new JPanel();
        heroContenido.setOpaque(false);
        heroContenido.setLayout(new BoxLayout(heroContenido, BoxLayout.Y_AXIS));
        
        JLabel lblTituloHero = new JLabel("Monitoreo Inteligente de Hosts");
        lblTituloHero.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        lblTituloHero.setForeground(Color.WHITE);
        JLabel lblDescripcionHero = new JLabel("Supervisa, detecta y genera reportes en tiempo real con un vistazo.");
        lblDescripcionHero.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        lblDescripcionHero.setForeground(new Color(255, 255, 255, 210));
        
        heroContenido.add(lblTituloHero);
        heroContenido.add(Box.createVerticalStrut(4));
        heroContenido.add(lblDescripcionHero);
        heroContenido.add(Box.createVerticalStrut(10));
        
        panelHero.add(heroContenido, BorderLayout.CENTER);
        
        // Card controles
        RoundedPanel cardControles = crearCard("Acciones Rápidas");
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        panelControles.setOpaque(false);
        JLabel lblIntervalo = new JLabel("Intervalo (seg):");
        lblIntervalo.setForeground(COLOR_TEXTO);
        panelControles.add(lblIntervalo);
        panelControles.add(spinnerIntervalo);
        spinnerIntervalo.setEnabled(false);
        SwingUtilities.invokeLater(() -> estilizarBotonesSpinner(spinnerIntervalo));
        panelControles.add(Box.createHorizontalStrut(10));
        panelControles.add(btnIniciar);
        panelControles.add(btnDetener);
        panelControles.add(btnGenerarPDF);
        cardControles.add(panelControles, BorderLayout.CENTER);
        
        JPanel panelSuperior = new JPanel(new BorderLayout(0, 12));
        panelSuperior.setOpaque(false);
        panelSuperior.add(panelHero, BorderLayout.NORTH);
        panelSuperior.add(cardControles, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);
        
        // Card agregar
        RoundedPanel cardAgregar = crearCard("Agregar Dispositivo");
        JPanel panelAgregar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelAgregar.setOpaque(false);
        JLabel lblId = new JLabel("ID:");
        lblId.setForeground(COLOR_TEXTO);
        panelAgregar.add(lblId);
        panelAgregar.add(txtIdDispositivo);
        JLabel lblIP = new JLabel("IP/Host:");
        lblIP.setForeground(COLOR_TEXTO);
        panelAgregar.add(lblIP);
        panelAgregar.add(txtIPDispositivo);
        
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.setBackground(COLOR_ACCION_OK);
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setOpaque(true);
        btnAgregar.setBorderPainted(false);
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> agregarDispositivo());
        btnAgregar.setIcon(crearIconoBoton(COLOR_ACCION_OK.darker(), "+"));
        btnAgregar.setIconTextGap(6);
        panelAgregar.add(btnAgregar);
        
        btnEscanear.setBackground(COLOR_ACCION_SECUNDARIA);
        btnEscanear.setForeground(Color.WHITE);
        btnEscanear.setOpaque(true);
        btnEscanear.setBorderPainted(false);
        btnEscanear.setFocusPainted(false);
        btnEscanear.addActionListener(e -> escanearRed());
        panelAgregar.add(btnEscanear);
        
        JButton btnRemover = new JButton("Remover Seleccionado");
        btnRemover.setBackground(COLOR_ACCION_PELIGRO);
        btnRemover.setForeground(Color.WHITE);
        btnRemover.setOpaque(true);
        btnRemover.setBorderPainted(false);
        btnRemover.setFocusPainted(false);
        btnRemover.addActionListener(e -> removerDispositivo());
        btnRemover.setIcon(crearIconoBoton(COLOR_ACCION_PELIGRO.darker(), "-"));
        btnRemover.setIconTextGap(6);
        panelAgregar.add(btnRemover);
        cardAgregar.add(panelAgregar, BorderLayout.CENTER);
        
        // Card tabla
        RoundedPanel cardTabla = crearCard("Dispositivos Monitoreados");
        tablaDispositivos.setBackground(Color.WHITE);
        tablaDispositivos.setForeground(COLOR_TEXTO);
        tablaDispositivos.setGridColor(COLOR_BORDE);
        tablaDispositivos.setSelectionBackground(COLOR_ACCION_PRIMARIA);
        tablaDispositivos.setSelectionForeground(Color.WHITE);
        tablaDispositivos.setFillsViewportHeight(true);
        tablaDispositivos.setRowSelectionAllowed(true);
        tablaDispositivos.setColumnSelectionAllowed(false);
        tablaDispositivos.setFocusable(true);
        tablaDispositivos.setShowHorizontalLines(false);
        tablaDispositivos.setShowVerticalLines(false);
        tablaDispositivos.setIntercellSpacing(new Dimension(10, 8));
        tablaDispositivos.setRowMargin(6);
        estilizarEncabezadoTabla(tablaDispositivos);
        JScrollPane scrollTabla = new JScrollPane(tablaDispositivos);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        scrollTabla.setBackground(COLOR_PANEL);
        scrollTabla.getViewport().setBackground(COLOR_PANEL);
        cardTabla.add(scrollTabla, BorderLayout.CENTER);
        
        JPanel panelIzquierdo = new JPanel(new BorderLayout(0, 12));
        panelIzquierdo.setOpaque(false);
        panelIzquierdo.add(cardAgregar, BorderLayout.NORTH);
        panelIzquierdo.add(cardTabla, BorderLayout.CENTER);
        add(panelIzquierdo, BorderLayout.CENTER);
        
        // Card alertas
        RoundedPanel cardAlertas = crearCard("Alertas y Eventos");
        areaAlertas.setBackground(Color.WHITE);
        areaAlertas.setForeground(COLOR_TEXTO);
        JScrollPane scrollAlertas = new JScrollPane(areaAlertas);
        scrollAlertas.setBorder(BorderFactory.createEmptyBorder());
        scrollAlertas.setBackground(COLOR_PANEL);
        scrollAlertas.getViewport().setBackground(Color.WHITE);
        cardAlertas.add(scrollAlertas, BorderLayout.CENTER);
        
        JButton btnLimpiar = new JButton("Limpiar Alertas");
        btnLimpiar.setBackground(COLOR_ACCION_AVISO);
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setOpaque(true);
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.addActionListener(e -> areaAlertas.setText(""));
        btnLimpiar.setIcon(crearIconoBoton(COLOR_ACCION_AVISO.darker(), "\u267A"));
        btnLimpiar.setIconTextGap(6);
        
        JPanel footerAlertas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerAlertas.setOpaque(false);
        footerAlertas.add(btnLimpiar);
        cardAlertas.add(footerAlertas, BorderLayout.SOUTH);
        cardAlertas.setPreferredSize(new Dimension(340, 0));
        add(cardAlertas, BorderLayout.EAST);
        
        btnIniciar.addActionListener(e -> iniciarMonitoreo());
        btnDetener.addActionListener(e -> detenerMonitoreo());
        btnGenerarPDF.addActionListener(e -> generarInformePDF());
        
        actualizarTabla();
        
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 650));
    }

    /**
     * Configura un timer que actualiza la tabla de dispositivos cada segundo.
     * Esto permite ver los cambios en tiempo real sin necesidad de refrescar manualmente.
     */
    private void configurarTimer() {
        timerActualizacion = new javax.swing.Timer(1000, e -> actualizarTabla());
        timerActualizacion.start();
    }

    /**
     * Actualiza la tabla de dispositivos con la información más reciente.
     * Obtiene los datos de cada dispositivo monitoreado y sus estadísticas,
     * luego los muestra en la tabla.
     */
    private void actualizarTabla() {
        // Limpiar tabla antes de agregar nuevos datos
        tablaModelo.setRowCount(0);
        
        // Iterar sobre todos los dispositivos monitoreados
        for (String dispositivoId : monitoreo.getDispositivos()) {
            Dispositivos dispositivo = monitoreo.getDispositivo(dispositivoId);
            HostEstadisticas stats = monitoreo.getEstadisticas(dispositivoId);
            
            // Si el dispositivo y sus estadísticas existen, agregar a la tabla
            if (dispositivo != null && stats != null) {
                String estado = dispositivo.getEstado();
                String disponibilidad = formatoDecimal.format(stats.getDisponibilidad()) + "%";
                String tiempoRespuesta = formatoDecimal.format(stats.getTiempoRespuestaPromedio()) + " ms";
                
                // Obtener total de chequeos y fallos
                int totalChequeos = stats.getTotalChequeos();
                int fallos = stats.getFallos();
                
                // Crear fila con todos los datos del dispositivo
                Object[] fila = {
                    dispositivoId,
                    dispositivo.getDireccionIP(),
                    estado,
                    disponibilidad,
                    tiempoRespuesta,
                    totalChequeos,
                    fallos
                };
                tablaModelo.addRow(fila);
            }
        }
    }

    /**
     * Inicia el monitoreo de los dispositivos con el intervalo configurado.
     * Habilita el botón de detener y deshabilita el botón de iniciar.
     */
    private void iniciarMonitoreo() {
        int intervalo = (Integer) spinnerIntervalo.getValue();
        if (intervalo > 0) {
            monitoreo.iniciar();
            btnIniciar.setEnabled(false);
            btnDetener.setEnabled(true);
            agregarAlerta("Monitoreo iniciado con intervalo de " + intervalo + " segundos");
        }
    }

    /**
     * Detiene el monitoreo de los dispositivos.
     * Habilita el botón de iniciar y deshabilita el botón de detener.
     */
    private void detenerMonitoreo() {
        monitoreo.detenerMonitoreo();
        btnIniciar.setEnabled(true);
        btnDetener.setEnabled(false);
        agregarAlerta("Monitoreo detenido");
    }

    /**
     * Agrega un nuevo dispositivo al sistema de monitoreo.
     * Valida que se hayan ingresado tanto el ID como la IP del dispositivo.
     */
    private void agregarDispositivo() {
        String id = txtIdDispositivo.getText().trim();
        String ip = txtIPDispositivo.getText().trim();
        
        // Validar que ambos campos estén completos
        if (id.isEmpty() || ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor ingrese ID e IP/Host del dispositivo", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Agregar dispositivo al sistema de monitoreo
        monitoreo.agregarDispositivo(id, ip);
        txtIdDispositivo.setText("");
        txtIPDispositivo.setText("");
        actualizarTabla();
        agregarAlerta("Dispositivo agregado: " + id + " (" + ip + ")");
    }

    /**
     * Remueve el dispositivo seleccionado de la tabla del sistema de monitoreo.
     * Valida que se haya seleccionado una fila antes de proceder.
     */
    private void removerDispositivo() {
        int filaSeleccionada = tablaDispositivos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String dispositivoId = (String) tablaModelo.getValueAt(filaSeleccionada, 0);
            monitoreo.removerDispositivo(dispositivoId);
            actualizarTabla();
            agregarAlerta("Dispositivo removido: " + dispositivoId);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione un dispositivo para remover", 
                "Error", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Agrega un mensaje de alerta al área de alertas con timestamp.
     * 
     * @param mensaje Mensaje a mostrar en el área de alertas
     */
    private void agregarAlerta(String mensaje) {
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        areaAlertas.append("[" + timestamp + "] " + mensaje + "\n");
        areaAlertas.setCaretPosition(areaAlertas.getDocument().getLength());
    }
    
    /**
     * Escanea la red local en busca de dispositivos activos.
     * Muestra un diálogo de progreso durante el escaneo y luego muestra
     * los dispositivos encontrados en un diálogo modal.
     */
    private void escanearRed() {
        btnEscanear.setEnabled(false);
        String redBase = EscaneadorRed.obtenerRedLocal();
        agregarAlerta("Iniciando escaneo de red: " + redBase + ".x");
        
        // Crear un diálogo de progreso para mostrar durante el escaneo
        JDialog dialogProgreso = new JDialog(this, "Escaneando Red", true);
        dialogProgreso.getContentPane().setBackground(COLOR_PANEL);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JLabel lblMensaje = new JLabel("Escaneando dispositivos en la red...");
        lblMensaje.setForeground(COLOR_TEXTO);
        JPanel panelProgreso = new JPanel(new BorderLayout(10, 10));
        panelProgreso.setBackground(COLOR_PANEL);
        panelProgreso.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelProgreso.add(lblMensaje, BorderLayout.NORTH);
        panelProgreso.add(progressBar, BorderLayout.CENTER);
        dialogProgreso.add(panelProgreso);
        dialogProgreso.setSize(300, 120);
        dialogProgreso.setLocationRelativeTo(this);
        
        // Ejecutar escaneo en un thread separado para no bloquear la UI
        new Thread(() -> {
            try {
                // Escanear rango completo de IPs (1-254) con timeout de 500ms
                java.util.List<EscaneadorRed.DispositivoEncontrado> dispositivos = 
                    EscaneadorRed.escanearRangoCompleto(redBase, 1, 254, 500);
                
                SwingUtilities.invokeLater(() -> {
                    dialogProgreso.dispose();
                    btnEscanear.setEnabled(true);
                    
                    // Si no se encontraron dispositivos, mostrar mensaje informativo
                    if (dispositivos.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                            "No se encontraron dispositivos activos en la red.",
                            "Escaneo Completado",
                            JOptionPane.INFORMATION_MESSAGE);
                        agregarAlerta("Escaneo completado: No se encontraron dispositivos");
                    } else {
                        // Mostrar diálogo con dispositivos encontrados
                        mostrarDispositivosEncontrados(dispositivos);
                        agregarAlerta("Escaneo completado: " + dispositivos.size() + " dispositivos encontrados");
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    dialogProgreso.dispose();
                    btnEscanear.setEnabled(true);
                    JOptionPane.showMessageDialog(this,
                        "Error durante el escaneo: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    agregarAlerta("Error en escaneo: " + e.getMessage());
                });
            }
        }).start();
        
        dialogProgreso.setVisible(true);
    }
    
    /**
     * Muestra un diálogo con los dispositivos encontrados en el escaneo de red.
     * Permite al usuario seleccionar dispositivos para agregarlos al monitoreo.
     * 
     * @param dispositivos Lista de dispositivos encontrados en el escaneo
     */
    private void mostrarDispositivosEncontrados(java.util.List<EscaneadorRed.DispositivoEncontrado> dispositivos) {
        // Crear diálogo modal para mostrar dispositivos encontrados
        JDialog dialog = new JDialog(this, "Dispositivos Conectados al Router", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(1100, 650);
        dialog.setLocationRelativeTo(this);
        
        // Configurar icono del diálogo
        try {
            dialog.setIconImage(crearIcono());
        } catch (Exception e) {
            // Ignorar si no se puede cargar el icono
        }
        
        // Fondo del diálogo: NEGRO
        dialog.getContentPane().setBackground(COLOR_PANEL);
        
        // Panel de encabezado con información de la red
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelHeader.setBackground(COLOR_PANEL);

        JLabel lblTitulo = new JLabel("<html><h2 style='color:#222831;'>Dispositivos Conectados</h2></html>");
        JLabel lblDescripcion = new JLabel("Dispositivos detectados en tu red WiFi");
        lblDescripcion.setForeground(COLOR_TEXTO_SUAVE);
        
        String redBase = EscaneadorRed.obtenerRedLocal();
        JLabel lblInfo = new JLabel("Red: " + redBase + ".x | Total: " + dispositivos.size() + " dispositivos");
        lblInfo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblInfo.setForeground(COLOR_TEXTO_SUAVE);
        
        // Botón "Actualizar" para volver a escanear
        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.setBackground(COLOR_ACCION_PRIMARIA);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setOpaque(true);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setIcon(crearIconoBoton(COLOR_ACCION_PRIMARIA.darker(), "\u21BB"));
        btnRefresh.setIconTextGap(6);
        btnRefresh.addActionListener(e -> {
            dialog.dispose();
            escanearRed();
        });
        
        panelHeader.add(lblTitulo, BorderLayout.NORTH);
        panelHeader.add(lblDescripcion, BorderLayout.CENTER);
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInfo.setBackground(COLOR_PANEL);
        panelInfo.add(lblInfo);
        panelInfo.add(Box.createHorizontalStrut(20));
        panelInfo.add(btnRefresh);
        panelHeader.add(panelInfo, BorderLayout.SOUTH);
        
        // Crear tabla con información de dispositivos encontrados
        String[] columnas = {"Host Name", "IP Address", "MAC Address", "Type", "Interface", "Status"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }
        };
        
        // Agregar cada dispositivo encontrado a la tabla
        for (EscaneadorRed.DispositivoEncontrado dispositivo : dispositivos) {
            String hostName = dispositivo.getNombre();
            if (hostName.equals("Desconocido")) {
                hostName = "Unknown";
            }
            
            String status = dispositivo.getEstado();
            String interfaz = dispositivo.getInterfaz();
            if (interfaz.equals("Unknown")) {
                interfaz = "WiFi";
            }
            
            modelo.addRow(new Object[]{
                hostName,
                dispositivo.getIp(),
                dispositivo.getMacAddress(),
                dispositivo.getTipoConexion(),
                interfaz,
                status
            });
        }
        
        // Configurar tabla con tema oscuro
        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(35);
        tabla.setBackground(Color.WHITE);
        tabla.setForeground(COLOR_TEXTO);
        tabla.setGridColor(COLOR_BORDE);
        tabla.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        tabla.getTableHeader().setBackground(COLOR_ACCION_PRIMARIA);
        tabla.getTableHeader().setForeground(Color.WHITE);
        
        // Ajustar ancho de columnas para mejor visualización
        tabla.getColumnModel().getColumn(0).setPreferredWidth(220); // Host Name
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140); // IP Address
        tabla.getColumnModel().getColumn(2).setPreferredWidth(170); // MAC Address
        tabla.getColumnModel().getColumn(3).setPreferredWidth(110); // Type
        tabla.getColumnModel().getColumn(4).setPreferredWidth(110); // Interface
        tabla.getColumnModel().getColumn(5).setPreferredWidth(110); // Status
        
        // Renderizar columna de Status con colores (verde para activo, rojo para inactivo)
        tabla.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                javax.swing.table.DefaultTableCellRenderer renderer = (javax.swing.table.DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null && value.toString().equals("Activo")) {
                    renderer.setForeground(COLOR_ACCION_OK.darker()); // Verde para activo
                    renderer.setFont(renderer.getFont().deriveFont(Font.BOLD));
                } else {
                    renderer.setForeground(COLOR_ACCION_PELIGRO.darker());
                }
                return renderer;
            }
        });
        
        // Agregar listener para doble clic: agregar dispositivo directamente
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabla.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        agregarDispositivoDesdeLista(modelo, row);
                    }
                }
            }
        });
        
        // ScrollPane con borde y tema oscuro
        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            "Connected Devices",
            0, 0,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            COLOR_TEXTO
        ));
        scrollPane.setBackground(COLOR_PANEL);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Panel de botones inferiores
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotones.setBackground(COLOR_PANEL);
        
        // Botón "Agregar Seleccionado" (azul)
        JButton btnAgregarSeleccionado = new JButton("Agregar Seleccionado");
        btnAgregarSeleccionado.setBackground(COLOR_ACCION_PRIMARIA);
        btnAgregarSeleccionado.setForeground(Color.WHITE);
        btnAgregarSeleccionado.setFocusPainted(false);
        btnAgregarSeleccionado.setOpaque(true);
        btnAgregarSeleccionado.setBorderPainted(false);
        btnAgregarSeleccionado.setContentAreaFilled(true);
        btnAgregarSeleccionado.setIcon(crearIconoBoton(COLOR_ACCION_PRIMARIA.darker(), "\u2713"));
        btnAgregarSeleccionado.setIconTextGap(6);
        btnAgregarSeleccionado.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                agregarDispositivoDesdeLista(modelo, fila);
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Por favor seleccione un dispositivo de la lista",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // Botón "Agregar Todos" (verde)
        JButton btnAgregarTodos = new JButton("Agregar Todos");
        btnAgregarTodos.setBackground(COLOR_ACCION_OK);
        btnAgregarTodos.setForeground(Color.WHITE);
        btnAgregarTodos.setFocusPainted(false);
        btnAgregarTodos.setOpaque(true);
        btnAgregarTodos.setBorderPainted(false);
        btnAgregarTodos.setContentAreaFilled(true);
        btnAgregarTodos.setIcon(crearIconoBoton(COLOR_ACCION_OK.darker(), "\u271A"));
        btnAgregarTodos.setIconTextGap(6);
        
        // Botón "Cerrar" (gris)
        JButton btnCancelar = new JButton("Cerrar");
        btnCancelar.setBackground(COLOR_ACCION_AVISO);
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setOpaque(true);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(true);
        btnCancelar.setIcon(crearIconoBoton(COLOR_ACCION_AVISO.darker(), "\u2715"));
        btnCancelar.setIconTextGap(6);
        
        // Acción del botón "Agregar Todos"
        btnAgregarTodos.addActionListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(
                dialog,
                "¿Desea agregar todos los " + dispositivos.size() + " dispositivos encontrados al monitoreo?",
                "Agregar Todos los Dispositivos",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                int agregados = 0;
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    agregarDispositivoDesdeLista(modelo, i);
                    agregados++;
                }
                agregarAlerta("Se agregaron " + agregados + " dispositivos de la red WiFi al monitoreo");
                actualizarTabla();
                dialog.dispose();
            }
        });
        
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        panelBotones.add(btnAgregarSeleccionado);
        panelBotones.add(btnAgregarTodos);
        panelBotones.add(btnCancelar);
        
        // Agregar componentes al diálogo
        dialog.add(panelHeader, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void estilizarEncabezadoTabla(JTable tabla) {
        javax.swing.table.DefaultTableCellRenderer headerRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel header = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                header.setOpaque(true);
                header.setBackground(COLOR_ACCION_PRIMARIA.darker());
                header.setForeground(Color.WHITE);
                header.setHorizontalAlignment(JLabel.CENTER);
                header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE));
                return header;
            }
        };
        Enumeration<TableColumn> columnas = tabla.getColumnModel().getColumns();
        while (columnas.hasMoreElements()) {
            columnas.nextElement().setHeaderRenderer(headerRenderer);
        }
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 34));
    }

    // Las clases RoundedPanel y GradientPanel ahora se heredan desde VentanaMonitoreoBase
    // mediante VentanaMonitoreoBase.RoundedPanel y VentanaMonitoreoBase.GradientPanel.

    /**
     * Agrega un dispositivo desde la lista de dispositivos encontrados al sistema de monitoreo.
     * 
     * @param modelo Modelo de la tabla con los dispositivos encontrados
     * @param fila Índice de la fila seleccionada en la tabla
     */
    private void agregarDispositivoDesdeLista(DefaultTableModel modelo, int fila) {
        String nombre = (String) modelo.getValueAt(fila, 0); // Host Name
        String ip = (String) modelo.getValueAt(fila, 1);     // IP Address
        
        // Usar el nombre del dispositivo como ID, o generar uno si es "Unknown"
        String id;
        if (nombre != null && !nombre.equals("Unknown") && !nombre.isEmpty()) {
            id = nombre;
        } else {
            // Si no hay nombre, usar la IP como base para el ID
            id = "Dispositivo_" + ip.replace(".", "_");
        }
        
        // Agregar dispositivo al sistema de monitoreo
        monitoreo.agregarDispositivo(id, ip);
        agregarAlerta("Dispositivo agregado: " + id + " (" + ip + ")");
    }

    /**
     * Genera un informe PDF con la información de todos los dispositivos monitoreados.
     * Ejecuta la generación en un thread separado para no bloquear la interfaz.
     */
    private void generarInformePDF() {
        // Verificar que haya dispositivos monitoreados
        if (monitoreo.getDispositivos().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No hay dispositivos monitoreados para generar el informe.",
                "Sin Dispositivos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Deshabilitar el botón mientras se genera el PDF
        btnGenerarPDF.setEnabled(false);
        btnGenerarPDF.setText("Generando...");
        
        // Ejecutar generación en un thread separado para no bloquear la UI
        new Thread(() -> {
            try {
                // Obtener la lista de dispositivos
                ArrayList<Dispositivos> dispositivos = monitoreo.getListaDispositivos();
                
                // Generar el PDF usando el generador de reportes
                GeneradorReportes generador = monitoreo.getGeneradorReportes();
                String rutaArchivo = generador.generarPDF(dispositivos);
                
                // Actualizar UI en el thread de eventos
                SwingUtilities.invokeLater(() -> {
                    btnGenerarPDF.setEnabled(true);
                    btnGenerarPDF.setText("Generar Informe PDF");
                    
                    if (rutaArchivo != null) {
                        agregarAlerta("Reporte generado y guardado con éxito: " + rutaArchivo);
                        JOptionPane.showMessageDialog(this,
                            "✓ Reporte generado y guardado con éxito\n\n" +
                            "Ubicación: " + rutaArchivo,
                            "Reporte Guardado",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        agregarAlerta("Error al generar el informe PDF");
                        JOptionPane.showMessageDialog(this,
                            "Error al generar el informe PDF.\n" +
                            "Verifique los logs para más detalles.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    btnGenerarPDF.setEnabled(true);
                    btnGenerarPDF.setText("Generar Informe PDF");
                    agregarAlerta("Error al generar PDF: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                        "Error al generar el informe PDF:\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    /**
     * Método llamado cuando se cierra la ventana.
     * Detiene el timer y el monitoreo antes de cerrar.
     */
    @Override
    public void dispose() {
        // Detener timer de actualización
        if (timerActualizacion != null) {
            timerActualizacion.stop();
        }
        // Detener monitoreo si está activo
        if (monitoreo != null && monitoreo.estaActivo()) {
            monitoreo.detenerMonitoreo();
        }
        super.dispose();
    }

    /**
     * Método principal para iniciar la aplicación.
     * Configura el classpath para las dependencias de iText y crea la interfaz gráfica.
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Cargar dependencias de iText desde lib si están disponibles
        try {
            java.io.File libDir = new java.io.File("lib");
            if (!libDir.exists()) {
                libDir = new java.io.File("target/classes/lib");
            }
            if (libDir.exists() && libDir.isDirectory()) {
                // Crear classloader dinámico con los JARs de iText
                java.net.URLClassLoader classLoader = new java.net.URLClassLoader(
                    java.util.Arrays.stream(libDir.listFiles((dir, name) -> name.endsWith(".jar")))
                        .map(file -> {
                            try {
                                return file.toURI().toURL();
                            } catch (java.net.MalformedURLException e) {
                                return null;
                            }
                        })
                        .filter(url -> url != null)
                        .toArray(java.net.URL[]::new),
                    InterfazGrafica.class.getClassLoader()
                );
                Thread.currentThread().setContextClassLoader(classLoader);
            }
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudieron cargar las dependencias de iText: " + e.getMessage());
        }
        
        // Crear directorio para reportes si no existe
        new java.io.File("reportes").mkdirs();
        
        // Crear sistema de monitoreo para red local (sin dispositivos por defecto)
        // El usuario agregará sus dispositivos locales desde la interfaz
        ArrayList<String> hosts = new ArrayList<>();
        // Ejemplos para red local (descomentar si se desea):
        // hosts.add("192.168.1.1");  // Router/Gateway
        // hosts.add("192.168.1.100"); // Servidor local
        // hosts.add("localhost");     // Localhost
        
        // Crear instancia del sistema de monitoreo con intervalo de 10 segundos
        Monitoreo monitoreo = new Monitoreo(hosts, 10);
        
        // Iniciar interfaz gráfica en el thread de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                // Usar el look and feel del sistema operativo
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Crear y mostrar la interfaz gráfica
            InterfazGrafica interfaz = new InterfazGrafica(monitoreo);
            interfaz.setVisible(true);
        });
    }
}