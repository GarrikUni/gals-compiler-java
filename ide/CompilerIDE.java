import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.List;

public class CompilerIDE extends JFrame {

    private JTextArea editorArea;
    private JTextArea outputArea;

    // Tabela de símbolos
    private JTable tabelaSimbolos;
    private DefaultTableModel modeloTabela;

    public CompilerIDE() {
        setTitle("IDE - Compilador");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        criarComponentes();
    }

    private void criarComponentes() {

        // =========================
        // CORES
        // =========================
        Color fundoJanela = new Color(245, 247, 250);
        Color fundoEditor = Color.WHITE;
        Color fundoSaida = new Color(250, 250, 250);

        Color corBorda = new Color(180, 190, 200);

        Color corBotao = new Color(52, 120, 246);
        Color corTextoBotao = Color.WHITE;

        Color corTitulo = new Color(60, 63, 65);

        // =========================
        // FONTES
        // =========================
        Font fonteCodigo = new Font("Monospaced", Font.PLAIN, 14);
        Font fonteTitulo = new Font("SansSerif", Font.BOLD, 14);
        Font fonteBotao = new Font("SansSerif", Font.BOLD, 14);

        // =========================
        // PAINEL PRINCIPAL
        // =========================
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));
        painelPrincipal.setBackground(fundoJanela);

        // =========================
        // EDITOR DE CÓDIGO
        // =========================
        editorArea = new JTextArea();

        editorArea.setFont(fonteCodigo);
        editorArea.setBackground(fundoEditor);
        editorArea.setForeground(Color.BLACK);
        editorArea.setCaretColor(Color.BLACK);

        editorArea.setTabSize(4);
        editorArea.setLineWrap(false);

        JScrollPane editorScroll = new JScrollPane(editorArea);

        editorScroll.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(corBorda, 1),
                        "Código-fonte",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        fonteTitulo,
                        corTitulo
                )
        );

        // =========================
        // ÁREA DE SAÍDA
        // =========================
        outputArea = new JTextArea();

        outputArea.setFont(fonteCodigo);
        outputArea.setEditable(false);

        outputArea.setBackground(fundoSaida);
        outputArea.setForeground(new Color(40, 40, 40));
        outputArea.setCaretColor(Color.BLACK);

        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane outputScroll = new JScrollPane(outputArea);

        outputScroll.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(corBorda, 1),
                        "Saída / Mensagens",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        fonteTitulo,
                        corTitulo
                )
        );

        // =========================
        // TABELA DE SÍMBOLOS
        // =========================
        String[] colunas = {
        "Id",
        "Tipo",
        "Escopo",
        "Ini",
        "Usada",
        "Param",
        "Vet",
        "Matriz",
        "Func"
        };

        modeloTabela = new DefaultTableModel(colunas, 0);

        tabelaSimbolos = new JTable(modeloTabela);

        tabelaSimbolos.setFont(fonteCodigo);
        tabelaSimbolos.setRowHeight(24);

        JScrollPane tabelaScroll = new JScrollPane(tabelaSimbolos);

        tabelaScroll.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(corBorda, 1),
                        "Tabela de Símbolos",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        fonteTitulo,
                        corTitulo
                )
        );

        // =========================
        // SPLIT VERTICAL
        // Código + saída
        // =========================
        JSplitPane splitVertical = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                editorScroll,
                outputScroll
        );

        splitVertical.setDividerLocation(420);
        splitVertical.setResizeWeight(0.7);

        // =========================
        // SPLIT HORIZONTAL
        // IDE + tabela
        // =========================
        JSplitPane splitHorizontal = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                splitVertical,
                tabelaScroll
        );

        splitHorizontal.setDividerLocation(850);
        splitHorizontal.setResizeWeight(0.8);
        splitHorizontal.setBorder(null);

        // =========================
        // BOTÃO COMPILAR
        // =========================
        JButton botaoCompilar = new JButton("Compilar");

        botaoCompilar.setFont(fonteBotao);

        botaoCompilar.setFocusPainted(false);

        botaoCompilar.setBackground(corBotao);
        botaoCompilar.setForeground(corTextoBotao);

        botaoCompilar.setPreferredSize(new Dimension(140, 40));

        botaoCompilar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        botaoCompilar.addActionListener(e -> compilar());

        // =========================
        // PAINEL INFERIOR
        // =========================
        JPanel painelInferior = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        painelInferior.setBackground(fundoJanela);

        painelInferior.add(botaoCompilar);

        // =========================
        // CABEÇALHO
        // =========================
        JLabel titulo = new JLabel(
                "Ambiente de Desenvolvimento Integrado - Compilador"
        );

        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));

        titulo.setForeground(new Color(35, 35, 35));

        JLabel subtitulo = new JLabel(
                "Digite o código-fonte, execute a análise sintática e visualize as mensagens."
        );

        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 13));

        subtitulo.setForeground(new Color(90, 90, 90));

        JPanel painelTopo = new JPanel();

        painelTopo.setLayout(new BoxLayout(painelTopo, BoxLayout.Y_AXIS));

        painelTopo.setBackground(fundoJanela);

        painelTopo.add(titulo);
        painelTopo.add(Box.createVerticalStrut(5));
        painelTopo.add(subtitulo);

        // =========================
        // ADICIONA COMPONENTES
        // =========================
        painelPrincipal.add(painelTopo, BorderLayout.NORTH);

        painelPrincipal.add(splitHorizontal, BorderLayout.CENTER);

        painelPrincipal.add(painelInferior, BorderLayout.SOUTH);

        add(painelPrincipal);
    }

    private void compilar() {

        String codigo = editorArea.getText();

        try {

            GalsParserBridge parser =
                    new GalsParserBridge();

            String resultado =
                    parser.analisar(codigo);

            outputArea.setText(resultado);

            // =========================
            // LIMPA TABELA VISUAL
            // =========================
            modeloTabela.setRowCount(0);

            // =========================
            // PREENCHE TABELA
            // =========================
                List<Object> tabela = parser.getTabelaSimbolos();

                if (tabela != null) {
                        for (Object obj : tabela) {

                                Class<?> c = obj.getClass();

                                String id = (String) c.getField("id").get(obj);
                                String tipo = (String) c.getField("tipo").get(obj);
                                String escopo = (String) c.getField("escopo").get(obj);

                                boolean ini = c.getField("ini").getBoolean(obj);
                                boolean usada = c.getField("usada").getBoolean(obj);
                                boolean param = c.getField("param").getBoolean(obj);
                                boolean vet = c.getField("vet").getBoolean(obj);
                                boolean matriz = c.getField("matriz").getBoolean(obj);
                                boolean func = c.getField("func").getBoolean(obj);

                                modeloTabela.addRow(new Object[]{
                                        id, tipo, escopo,
                                        ini, usada, param,
                                        vet, matriz, func
                                });
                        }
                }

        } catch (Exception ex) {

            outputArea.setText(
                    "Erro:\n" + ex.getMessage()
            );
        }
    }
}