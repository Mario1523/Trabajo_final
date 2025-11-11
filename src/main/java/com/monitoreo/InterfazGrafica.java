package com.monitoreo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/**
 * Interfaz gráfica para el sistema de monitoreo de hosts y dispositivos de red.
 * Proporciona una interfaz visual para gestionar dispositivos, iniciar/detener monitoreo,
 * escanear la red local y generar reportes en PDF.
 * 
 * @author Sistema de Monitoreo
 * @version 1.0
 */
public class InterfazGrafica extends JFrame {
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
        tablaDispositivos.setRowHeight(25);
        
        // Configurar área de alertas (no editable, solo lectura)
        areaAlertas = new JTextArea(10, 50);
        areaAlertas.setEditable(false);
        areaAlertas.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // Configurar campos de texto para agregar dispositivos
        txtIdDispositivo = new JTextField(15);
        txtIdDispositivo.setBackground(Color.BLACK); // Fondo negro
        txtIdDispositivo.setForeground(Color.WHITE); // Texto blanco
        txtIdDispositivo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100)), // Borde gris
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtIPDispositivo = new JTextField(15);
        txtIPDispositivo.setBackground(Color.BLACK); // Fondo negro
        txtIPDispositivo.setForeground(Color.WHITE); // Texto blanco
        txtIPDispositivo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100)), // Borde gris
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // Configurar spinner para intervalo de monitoreo (1-300 segundos, valor inicial 10)
        spinnerIntervalo = new JSpinner(new SpinnerNumberModel(10, 1, 300, 1));
        // Configurar colores del spinner: fondo negro y texto blanco
        spinnerIntervalo.setBackground(Color.BLACK); // Fondo negro
        spinnerIntervalo.setForeground(Color.WHITE); // Texto blanco
        spinnerIntervalo.setOpaque(true);
        // Configurar el editor del spinner con fondo negro y texto blanco
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinnerIntervalo.getEditor();
        editor.getTextField().setBackground(Color.BLACK); // Fondo negro
        editor.getTextField().setForeground(Color.WHITE); // Texto blanco
        editor.getTextField().setCaretColor(Color.WHITE); // Cursor blanco
        editor.getTextField().setOpaque(true);
        editor.getTextField().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12)); // Texto en negrita y más grande
        editor.getTextField().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 2), // Borde más grueso y claro
            BorderFactory.createEmptyBorder(3, 8, 3, 8) // Más padding
        ));
        
        // Configurar botón "Iniciar Monitoreo" (verde)
        btnIniciar = new JButton("Iniciar Monitoreo");
        btnIniciar.setBackground(new Color(40, 167, 69));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setOpaque(true);
        btnIniciar.setBorderPainted(false);
        btnIniciar.setFocusPainted(false);
        
        // Configurar botón "Detener Monitoreo" (rojo)
        btnDetener = new JButton("Detener Monitoreo");
        btnDetener.setBackground(new Color(220, 53, 69));
        btnDetener.setForeground(Color.WHITE);
        btnDetener.setOpaque(true);
        btnDetener.setBorderPainted(false);
        btnDetener.setFocusPainted(false);
        btnDetener.setEnabled(false); // Inicialmente deshabilitado
        
        // Configurar botón "Escanear Red" (azul claro)
        btnEscanear = new JButton("Escanear Red");
        
        // Configurar botón "Generar Informe PDF" (azul)
        btnGenerarPDF = new JButton("Generar Informe PDF");
        btnGenerarPDF.setBackground(new Color(0, 123, 255));
        btnGenerarPDF.setForeground(Color.WHITE);
        btnGenerarPDF.setFocusPainted(false);
        btnGenerarPDF.setOpaque(true);
        btnGenerarPDF.setBorderPainted(false);
        btnGenerarPDF.setContentAreaFilled(true);
    }

    /**
     * Configura el layout y los componentes principales de la interfaz.
     * Establece colores, bordes, paneles y organiza todos los elementos visuales.
     */
    private void configurarInterfaz() {
        setTitle("Sistema de Monitoreo de Hosts");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Configurar icono de la ventana
        try {
            setIconImage(crearIcono());
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }
        
        // Color de fondo principal: NEGRO
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior - Controles de monitoreo
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelControles.setBackground(Color.BLACK); // Fondo negro
        panelControles.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1), // Borde gris
            "Controles de Monitoreo",
            0, 0,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            Color.WHITE // Texto blanco para el título del borde
        ));
        
        // Etiqueta y spinner para intervalo
        JLabel lblIntervalo = new JLabel("Intervalo (seg):");
        lblIntervalo.setForeground(Color.WHITE); // Texto blanco
        panelControles.add(lblIntervalo);
        panelControles.add(spinnerIntervalo);
        spinnerIntervalo.setEnabled(false); // El intervalo se establece al crear el monitoreo
        
        // Configurar los botones del spinner (flechas) después de agregarlo al panel
        SwingUtilities.invokeLater(() -> {
            // Buscar y configurar los botones del spinner
            for (java.awt.Component comp : spinnerIntervalo.getComponents()) {
                if (comp instanceof javax.swing.JButton) {
                    javax.swing.JButton btn = (javax.swing.JButton) comp;
                    btn.setBackground(new Color(50, 50, 50)); // Fondo gris oscuro
                    btn.setForeground(Color.WHITE); // Texto blanco
                    btn.setOpaque(true);
                    btn.setBorderPainted(true);
                    btn.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
                }
            }
        });
        
        panelControles.add(Box.createHorizontalStrut(20));
        
        // Agregar botones al panel de controles
        panelControles.add(btnIniciar);
        panelControles.add(btnDetener);
        panelControles.add(Box.createHorizontalStrut(20));
        panelControles.add(btnGenerarPDF);
        
        // Panel central izquierdo - Tabla de dispositivos monitoreados
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBackground(Color.BLACK); // Fondo negro
        panelTabla.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1), // Borde gris
            "Dispositivos Monitoreados",
            0, 0,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            Color.WHITE // Texto blanco
        ));
        JScrollPane scrollTabla = new JScrollPane(tablaDispositivos);
        scrollTabla.setBackground(Color.BLACK);
        scrollTabla.getViewport().setBackground(Color.BLACK);
        panelTabla.add(scrollTabla, BorderLayout.CENTER);
        
        // Estilo de la tabla con tema oscuro
        tablaDispositivos.setBackground(Color.BLACK); // Fondo negro para las filas
        tablaDispositivos.setForeground(Color.WHITE); // Texto blanco
        tablaDispositivos.setGridColor(new Color(60, 60, 60)); // Líneas grises oscuras
        tablaDispositivos.setSelectionBackground(new Color(52, 152, 219)); // Fondo azul para selección
        tablaDispositivos.setSelectionForeground(Color.WHITE); // Texto blanco en selección
        tablaDispositivos.getTableHeader().setBackground(new Color(52, 152, 219)); // Encabezado azul
        tablaDispositivos.getTableHeader().setForeground(Color.WHITE);
        tablaDispositivos.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        
        // Panel para agregar dispositivos
        JPanel panelAgregar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAgregar.setBackground(Color.BLACK); // Fondo negro
        panelAgregar.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1), // Borde gris
            "Agregar Dispositivo",
            0, 0,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            Color.WHITE // Texto blanco
        ));
        
        // Etiquetas y campos para agregar dispositivo
        JLabel lblId = new JLabel("ID:");
        lblId.setForeground(Color.WHITE);
        panelAgregar.add(lblId);
        panelAgregar.add(txtIdDispositivo);
        JLabel lblIP = new JLabel("IP/Host:");
        lblIP.setForeground(Color.WHITE);
        panelAgregar.add(lblIP);
        panelAgregar.add(txtIPDispositivo);
        
        // Botón "Agregar" (verde)
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.setBackground(new Color(40, 167, 69));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setOpaque(true);
        btnAgregar.setBorderPainted(false);
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> agregarDispositivo());
        panelAgregar.add(btnAgregar);
        
        // Botón "Escanear Red" (azul claro)
        btnEscanear.setBackground(new Color(23, 162, 184));
        btnEscanear.setForeground(Color.WHITE);
        btnEscanear.setOpaque(true);
        btnEscanear.setBorderPainted(false);
        btnEscanear.setFocusPainted(false);
        btnEscanear.addActionListener(e -> escanearRed());
        panelAgregar.add(btnEscanear);
        
        // Botón "Remover Seleccionado" (rojo)
        JButton btnRemover = new JButton("Remover Seleccionado");
        btnRemover.setBackground(new Color(220, 53, 69));
        btnRemover.setForeground(Color.WHITE);
        btnRemover.setOpaque(true);
        btnRemover.setBorderPainted(false);
        btnRemover.setFocusPainted(false);
        btnRemover.addActionListener(e -> removerDispositivo());
        panelAgregar.add(btnRemover);
        
        // Panel izquierdo que contiene el panel de agregar y la tabla
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setBackground(Color.BLACK); // Fondo negro
        panelIzquierdo.add(panelAgregar, BorderLayout.NORTH);
        panelIzquierdo.add(panelTabla, BorderLayout.CENTER);
        
        // Panel derecho - Alertas y eventos
        JPanel panelAlertas = new JPanel(new BorderLayout());
        panelAlertas.setBackground(Color.BLACK); // Fondo negro
        panelAlertas.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1), // Borde gris
            "Alertas y Eventos",
            0, 0,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            Color.WHITE // Texto blanco
        ));
        
        // Configurar área de alertas con tema oscuro
        areaAlertas.setBackground(Color.BLACK); // Fondo negro
        areaAlertas.setForeground(new Color(200, 200, 200)); // Texto gris claro
        JScrollPane scrollAlertas = new JScrollPane(areaAlertas);
        scrollAlertas.setBackground(Color.BLACK);
        scrollAlertas.getViewport().setBackground(Color.BLACK);
        panelAlertas.add(scrollAlertas, BorderLayout.CENTER);
        
        // Botón "Limpiar Alertas" (gris)
        JButton btnLimpiar = new JButton("Limpiar Alertas");
        btnLimpiar.setBackground(new Color(108, 117, 125));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setOpaque(true);
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.addActionListener(e -> areaAlertas.setText(""));
        panelAlertas.add(btnLimpiar, BorderLayout.SOUTH);
        
        // Layout principal: agregar todos los paneles
        add(panelControles, BorderLayout.NORTH);
        add(panelIzquierdo, BorderLayout.CENTER);
        add(panelAlertas, BorderLayout.EAST);
        
        // Configurar eventos de botones principales
        btnIniciar.addActionListener(e -> iniciarMonitoreo());
        btnDetener.addActionListener(e -> detenerMonitoreo());
        btnGenerarPDF.addActionListener(e -> generarInformePDF());
        
        // Actualizar tabla con datos iniciales
        actualizarTabla();
        
        // Configurar tamaño y posición de la ventana
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
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
        dialogProgreso.getContentPane().setBackground(Color.BLACK); // Fondo negro
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JLabel lblMensaje = new JLabel("Escaneando dispositivos en la red...");
        lblMensaje.setForeground(Color.WHITE); // Texto blanco
        JPanel panelProgreso = new JPanel(new BorderLayout(10, 10));
        panelProgreso.setBackground(Color.BLACK); // Fondo negro
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
        dialog.getContentPane().setBackground(Color.BLACK);
        
        // Panel de encabezado con información de la red
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelHeader.setBackground(Color.BLACK); // Fondo negro
        
        JLabel lblTitulo = new JLabel("<html><h2 style='color:white;'>Dispositivos Conectados</h2></html>");
        JLabel lblDescripcion = new JLabel("Dispositivos detectados en tu red WiFi");
        lblDescripcion.setForeground(Color.LIGHT_GRAY); // Texto gris claro
        
        String redBase = EscaneadorRed.obtenerRedLocal();
        JLabel lblInfo = new JLabel("Red: " + redBase + ".x | Total: " + dispositivos.size() + " dispositivos");
        lblInfo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblInfo.setForeground(Color.LIGHT_GRAY); // Texto gris claro
        
        // Botón "Actualizar" para volver a escanear
        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.setBackground(new Color(0, 123, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setOpaque(true);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            dialog.dispose();
            escanearRed();
        });
        
        panelHeader.add(lblTitulo, BorderLayout.NORTH);
        panelHeader.add(lblDescripcion, BorderLayout.CENTER);
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInfo.setBackground(Color.BLACK); // Fondo negro
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
        tabla.setBackground(Color.BLACK); // Fondo negro
        tabla.setForeground(Color.WHITE); // Texto blanco
        tabla.setGridColor(new Color(60, 60, 60)); // Líneas grises oscuras
        tabla.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(52, 152, 219)); // Encabezado azul
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
                    renderer.setForeground(new Color(0, 255, 0)); // Verde para activo
                    renderer.setFont(renderer.getFont().deriveFont(Font.BOLD));
                } else {
                    renderer.setForeground(Color.RED); // Rojo para inactivo
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
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1), // Borde gris
            "Connected Devices",
            0, 0,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            Color.WHITE // Texto blanco
        ));
        scrollPane.setBackground(Color.BLACK); // Fondo negro
        scrollPane.getViewport().setBackground(Color.BLACK); // Fondo negro del viewport
        
        // Panel de botones inferiores
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotones.setBackground(Color.BLACK); // Fondo negro
        
        // Botón "Agregar Seleccionado" (azul)
        JButton btnAgregarSeleccionado = new JButton("Agregar Seleccionado");
        btnAgregarSeleccionado.setBackground(new Color(0, 123, 255));
        btnAgregarSeleccionado.setForeground(Color.WHITE);
        btnAgregarSeleccionado.setFocusPainted(false);
        btnAgregarSeleccionado.setOpaque(true);
        btnAgregarSeleccionado.setBorderPainted(false);
        btnAgregarSeleccionado.setContentAreaFilled(true);
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
        btnAgregarTodos.setBackground(new Color(40, 167, 69));
        btnAgregarTodos.setForeground(Color.WHITE);
        btnAgregarTodos.setFocusPainted(false);
        btnAgregarTodos.setOpaque(true);
        btnAgregarTodos.setBorderPainted(false);
        btnAgregarTodos.setContentAreaFilled(true);
        
        // Botón "Cerrar" (gris)
        JButton btnCancelar = new JButton("Cerrar");
        btnCancelar.setBackground(new Color(108, 117, 125));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setOpaque(true);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(true);
        
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
     * Crea un icono personalizado para la aplicación.
     * El icono representa un símbolo de red/monitoreo con un nodo central y ondas concéntricas.
     * 
     * @return Imagen del icono generado
     */
    private Image crearIcono() {
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
        
        // Dibujar círculo central (nodo de red)
        int centerX = size / 2;
        int centerY = size / 2;
        g2d.fillOval(centerX - 4, centerY - 4, 8, 8);
        
        // Dibujar líneas de conexión (ondas concéntricas)
        g2d.setStroke(new java.awt.BasicStroke(1.5f));
        for (int i = 0; i < 3; i++) {
            int radius = 8 + i * 4;
            g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 0, 360);
        }
        
        g2d.dispose();
        return icon;
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