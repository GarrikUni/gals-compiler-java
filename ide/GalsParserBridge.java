import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class GalsParserBridge {

    private final URLClassLoader classLoader;
    private final Class<?> lexicoClass;
    private final Class<?> sintaticoClass;
    private final Class<?> semanticoClass;

    private List<Object> tabelaSimbolos;

    public GalsParserBridge() throws Exception {
        Path parserDir = Path.of("parser-bin");
        URL[] urls = { parserDir.toUri().toURL() };
        classLoader = new URLClassLoader(urls);

        lexicoClass = classLoader.loadClass("Lexico");
        sintaticoClass = classLoader.loadClass("Sintatico");
        semanticoClass = classLoader.loadClass("Semantico");
    }

    public String analisar(String codigo) {
        try {
            Object lexico = criarLexico(codigo);
            Object sintatico = sintaticoClass.getDeclaredConstructor().newInstance();
            Object semantico = semanticoClass.getDeclaredConstructor().newInstance();

            Method parseMethod = sintaticoClass.getMethod("parse", lexicoClass, semanticoClass);
            parseMethod.invoke(sintatico, lexico, semantico);

            tabelaSimbolos = carregarTabelaSimbolos(semantico);

            return "Compilação concluída com sucesso.\nNenhum erro sintático encontrado.";

        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();

            int posicao = -1;
            try {
                Method getPosition = cause.getClass().getMethod("getPosition");
                Object pos = getPosition.invoke(cause);
                if (pos instanceof Integer) {
                    posicao = (Integer) pos;
                }
            } catch (Exception ignored) {
            }

            String tipo = cause.getClass().getSimpleName();
            String mensagemOriginal = cause.getMessage() != null ? cause.getMessage() : "Erro não identificado.";

            StringBuilder sb = new StringBuilder();
            sb.append("Erro durante a análise.\n");

            if (tipo.toLowerCase().contains("lex")) {
                sb.append("Tipo: Erro léxico\n");
            } else if (tipo.toLowerCase().contains("synt")) {
                sb.append("Tipo: Erro sintático\n");
            } else if (tipo.toLowerCase().contains("sem")) {
                sb.append("Tipo: Erro semântico\n");
            } else {
                sb.append("Tipo: ").append(tipo).append("\n");
            }

            sb.append("Mensagem técnica: ").append(mensagemOriginal).append("\n");

            if (posicao >= 0) {
                int[] linhaColuna = calcularLinhaColuna(codigo, posicao);
                sb.append("Linha: ").append(linhaColuna[0]).append("\n");
                sb.append("Coluna: ").append(linhaColuna[1]).append("\n");
                sb.append("Trecho próximo: ").append(obterTrechoProximo(codigo, posicao)).append("\n");
            }

            sb.append("\nDica: verifique a estrutura esperada pela gramática.");

            return sb.toString();

        } catch (Exception e) {
            return "Falha ao executar o analisador.\n" + e.getMessage();
        }
    }

    private List<Object> carregarTabelaSimbolos(Object semantico) throws Exception {

        Method metodo = semanticoClass.getMethod("getTabelaSimbolos");
        Object retorno = metodo.invoke(semantico);

        if (retorno instanceof List<?> lista) {
            return (List<Object>) lista;
        }

        return new ArrayList<>();
    }

    private Object criarLexico(String codigo) throws Exception {
        try {
            Constructor<?> c = lexicoClass.getDeclaredConstructor(String.class);
            return c.newInstance(codigo);
        } catch (NoSuchMethodException e) {
            Object lexico = lexicoClass.getDeclaredConstructor().newInstance();
            Method setInput = lexicoClass.getMethod("setInput", String.class);
            setInput.invoke(lexico, codigo);
            return lexico;
        }
    }

    private int[] calcularLinhaColuna(String texto, int posicao) {
        int linha = 1;
        int coluna = 1;

        for (int i = 0; i < posicao && i < texto.length(); i++) {
            if (texto.charAt(i) == '\n') {
                linha++;
                coluna = 1;
            } else {
                coluna++;
            }
        }

        return new int[]{linha, coluna};
    }

    private String obterTrechoProximo(String texto, int posicao) {
        if (texto == null || texto.isEmpty()) {
            return "(vazio)";
        }

        int inicio = Math.max(0, posicao - 10);
        int fim = Math.min(texto.length(), posicao + 10);

        String trecho = texto.substring(inicio, fim);
        trecho = trecho.replace("\n", "\\n");

        return "\"" + trecho + "\"";
    }

    public List<Object> getTabelaSimbolos()
    {
        return tabelaSimbolos;
    }
}