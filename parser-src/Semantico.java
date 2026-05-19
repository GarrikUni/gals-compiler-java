import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class Semantico implements Constants
{
    public static class Simbolo {
        String id;
        String tipo;
        boolean ini;
        boolean usada;
        String escopo;
        boolean param;
        int pos;
        boolean vet;
        boolean matriz;
        boolean ref;
        boolean func;

        public Simbolo(
                String id,
                String tipo,
                boolean ini,
                boolean usada,
                String escopo,
                boolean param,
                int pos,
                boolean vet,
                boolean matriz,
                boolean ref,
                boolean func
        ) {
            this.id = id;
            this.tipo = tipo;
            this.ini = ini;
            this.usada = usada;
            this.escopo = escopo;
            this.param = param;
            this.pos = pos;
            this.vet = vet;
            this.matriz = matriz;
            this.ref = ref;
            this.func = func;
        }
    }

    private static final String ERRO = "erro";

    private String[][] soma = {
            // int      float     char      string    bool
            {"int",    "float",  ERRO,     ERRO,     ERRO},
            {"float",  "float",  ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     "string", "string", ERRO},
            {ERRO,     ERRO,     "string", "string", ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO}
    };

    private String[][] sub = {
            // int      float     char      string    bool
            {"int",    "float",  ERRO,     ERRO,     ERRO},
            {"float",  "float",  ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     "char",   "char",   ERRO},
            {ERRO,     ERRO,     "string", "string", ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO}
    };

    private String[][] mult = {
            // int      float     char      string    bool
            {"int",    "float",  ERRO,     ERRO,     ERRO},
            {"float",  "float",  ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO}
    };

    private String[][] div = {
            // int      float     char      string    bool
            {"int",    "float",  ERRO,     ERRO,     ERRO},
            {"float",  "float",  ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO}
    };

    private String[][] mod = {
            // int      float     char      string    bool
            {"int",    ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO}
    };

    private String[][] rel = {
            // int      float     char      string    bool
            {"bool",   "bool",   ERRO,     ERRO,     ERRO},
            {"bool",   "bool",   ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     "bool",   "bool",   ERRO},
            {ERRO,     ERRO,     "bool",   "bool",   ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     "bool"}
    };

    private String[][] logic = {
            // int      float     char      string    bool
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     ERRO},
            {ERRO,     ERRO,     ERRO,     ERRO,     "bool"}
    };

    private List<Simbolo> tabelaSimbolos = new ArrayList<>();

    private Stack<String> pilhaEscopos = new Stack<>();
    private int nivelEscopo = 0;

    private String tipoAtual;

    private Stack<String> pilhaTipos = new Stack<>();
    private Stack<String> pilhaOps = new Stack<>();

    private Simbolo simboloAtual;
    private Simbolo lhsAtual;

    private Simbolo acessoAtual;
    private int quantidadeIndices;

    private List<String> avisos = new ArrayList<>();
    private Set<String> avisosEmitidos = new LinkedHashSet<>();

    public Semantico() {
        pilhaEscopos.push("global");
    }

    public void executeAction(int action, Token token) throws SemanticError
    {
        switch(action)
        {
            case 1:
                tipoAtual = token.getLexeme();
                break;

            case 2:
                simboloAtual = inserirIdentificador(token);
                break;

            case 3:
                simboloAtual = verificarIdentificador(token);
                break;

            case 4:
                entrarEscopo();
                break;

            case 5:
                sairEscopo();
                break;

            case 6:
                validarAtribuicao(token);
                break;

            case 7:
                pilhaTipos.push("int");
                break;

            case 8:
                pilhaTipos.push("float");
                break;

            case 9:
                pilhaTipos.push("string");
                break;

            case 10:
                pilhaTipos.push("char");
                break;

            case 11:
                pilhaTipos.push("bool");
                break;

            case 12:
                Simbolo s = verificarIdentificador(token);

                if(s.vet || s.matriz) {
                    throw new SemanticError(
                            "Uso inválido de vetor/matriz sem índice: " + s.id,
                            token.getPosition()
                    );
                }

                verificarInicializacao(s, token);
                pilhaTipos.push(s.tipo);
                break;

            case 14:
                pilhaOps.push("==");
                break;

            case 15:
                pilhaOps.push("!=");
                break;

            case 16:
                pilhaOps.push("<");
                break;

            case 17:
                pilhaOps.push(">");
                break;

            case 18:
                pilhaOps.push("<=");
                break;

            case 19:
                pilhaOps.push(">=");
                break;

            case 20:
                pilhaOps.push("+");
                break;

            case 21:
                pilhaOps.push("-");
                break;

            case 22:
                pilhaOps.push("*");
                break;

            case 23:
                pilhaOps.push("/");
                break;

            case 24:
                pilhaOps.push("%");
                break;

            case 25:
                pilhaOps.push("||");
                break;

            case 26:
                pilhaOps.push("&&");
                break;

            case 27:
                pilhaOps.push("!");
                break;

            case 30:
                finalizarExpr(token);
                break;

            case 31:
                pilhaOps.clear();
                pilhaTipos.clear();
                break;

            case 32:
                lhsAtual = buscarIdentificador(token);
                break;

            case 33:
                lhsAtual = simboloAtual;
                break;

            case 40:
                inserirParametro(token);
                break;

            case 50:
                entrarEscopo();
                break;

            case 51:
                inserirFuncao(token);
                break;

            case 52:
                verificarFuncao(token);
                break;

            case 60:
                marcarVetor();
                break;

            case 61:
                marcarMatriz();
                break;

            case 70:
                acessoAtual = buscarIdentificador(token);
                quantidadeIndices = 0;
                break;

            case 71:
                validarIndice(token);
                quantidadeIndices++;
                break;

            case 72:
                validarAcessoVetor(token);
                break;

            case 74:
                validarAcessoMatriz(token);
                break;
        }
    }

    private Simbolo inserirIdentificador(Token token) throws SemanticError
    {
        String nome = token.getLexeme();

        if(buscarNoEscopoAtual(nome) != null)
        {
            throw new SemanticError(
                    "Identificador já declarado no escopo: " + nome,
                    token.getPosition()
            );
        }

        Simbolo s = new Simbolo(
                nome,
                tipoAtual,
                false,
                false,
                pilhaEscopos.peek(),
                false,
                token.getPosition(),
                false,
                false,
                false,
                false
        );

        tabelaSimbolos.add(s);

        System.out.println(
                "Inserido: " + nome +
                " tipo: " + tipoAtual +
                " escopo: " + s.escopo
        );

        return s;
    }

    private Simbolo inserirParametro(Token token) throws SemanticError
    {
        String nome = token.getLexeme();

        if(buscarNoEscopoAtual(nome) != null)
        {
            throw new SemanticError(
                    "Parâmetro já declarado: " + nome,
                    token.getPosition()
            );
        }

        Simbolo s = new Simbolo(
                nome,
                tipoAtual,
                true,
                false,
                pilhaEscopos.peek(),
                true,
                token.getPosition(),
                false,
                false,
                false,
                false
        );

        tabelaSimbolos.add(s);

        System.out.println(
                "Parâmetro inserido: " + nome +
                " tipo: " + tipoAtual +
                " escopo: " + s.escopo
        );

        return s;
    }

    private Simbolo verificarIdentificador(Token token) throws SemanticError
    {
        Simbolo s = buscarIdentificador(token);

        s.usada = true;

        System.out.println(
                "Uso de identificador: " + s.id +
                " tipo: " + s.tipo
        );

        return s;
    }

    private Simbolo buscarIdentificador(Token token) throws SemanticError
    {
        String nome = token.getLexeme();

        Simbolo s = buscarSimbolo(nome);

        if(s == null)
        {
            throw new SemanticError(
                    "Identificador não declarado: " + nome,
                    token.getPosition()
            );
        }

        return s;
    }

    private Simbolo inserirFuncao(Token token) throws SemanticError
    {
        String nome = token.getLexeme();

        for(Simbolo s : tabelaSimbolos)
        {
            if(s.id.equals(nome) && s.func)
            {
                throw new SemanticError(
                        "Função já declarada: " + nome,
                        token.getPosition()
                );
            }
        }

        Simbolo s = new Simbolo(
                nome,
                tipoAtual,
                true,
                false,
                "global",
                false,
                token.getPosition(),
                false,
                false,
                false,
                true
        );

        tabelaSimbolos.add(s);

        System.out.println(
                "Função declarada: " + nome +
                " retorno: " + tipoAtual
        );

        return s;
    }

    private Simbolo verificarFuncao(Token token) throws SemanticError
    {
        String nome = token.getLexeme();

        Simbolo s = buscarSimbolo(nome);

        if(s == null || !s.func)
        {
            throw new SemanticError(
                    "Função não declarada: " + nome,
                    token.getPosition()
            );
        }

        s.usada = true;

        System.out.println(
                "Chamada de função válida: " + nome
        );

        return s;
    }

    private void marcarVetor()
    {
        if(simboloAtual != null)
        {
            simboloAtual.vet = true;
            simboloAtual.matriz = false;

            System.out.println(
                    "Identificador '" +
                            simboloAtual.id +
                            "' marcado como vetor"
            );
        }
    }

    private void marcarMatriz()
    {
        if(simboloAtual != null)
        {
            simboloAtual.matriz = true;
            simboloAtual.vet = false;

            System.out.println(
                    "Identificador '" +
                            simboloAtual.id +
                            "' marcado como matriz"
            );
        }
    }

    private void validarIndice(Token token) throws SemanticError
    {
        if(pilhaTipos.isEmpty())
        {
            throw new SemanticError(
                    "Índice inválido",
                    token.getPosition()
            );
        }

        String tipoIndice = pilhaTipos.pop();

        if(!tipoIndice.equals("int"))
        {
            throw new SemanticError(
                    "Índice deve ser int",
                    token.getPosition()
            );
        }
    }

    private void validarAcessoVetor(Token token) throws SemanticError
    {
        if(acessoAtual == null)
        {
            throw new SemanticError(
                    "Acesso inválido de vetor",
                    token.getPosition()
            );
        }

        if(!acessoAtual.vet)
        {
            throw new SemanticError(
                    "'" + acessoAtual.id + "' não é vetor",
                    token.getPosition()
            );
        }

        acessoAtual.usada = true;
        verificarInicializacao(acessoAtual, token);
        pilhaTipos.push(acessoAtual.tipo);
    }

    private void validarAcessoMatriz(Token token) throws SemanticError
    {
        if(acessoAtual == null)
        {
            throw new SemanticError(
                    "Acesso inválido de matriz",
                    token.getPosition()
            );
        }

        if(!acessoAtual.matriz)
        {
            throw new SemanticError(
                    "'" + acessoAtual.id + "' não é matriz",
                    token.getPosition()
            );
        }

        if(quantidadeIndices != 2)
        {
            throw new SemanticError(
                    "Matriz requer dois índices",
                    token.getPosition()
            );
        }

        acessoAtual.usada = true;
        verificarInicializacao(acessoAtual, token);
        pilhaTipos.push(acessoAtual.tipo);
    }

    private Simbolo buscarNoEscopoAtual(String nome)
    {
        String escopoAtual = pilhaEscopos.peek();

        for(Simbolo s : tabelaSimbolos)
        {
            if(s.id.equals(nome) && s.escopo.equals(escopoAtual))
            {
                return s;
            }
        }

        return null;
    }

    private Simbolo buscarSimbolo(String nome)
    {
        for(int i = pilhaEscopos.size() - 1; i >= 0; i--)
        {
            String escopo = pilhaEscopos.get(i);

            for(Simbolo s : tabelaSimbolos)
            {
                if(s.id.equals(nome) && s.escopo.equals(escopo))
                {
                    return s;
                }
            }
        }

        return null;
    }

    private void entrarEscopo()
    {
        nivelEscopo++;

        pilhaEscopos.push(
                "escopo_" + nivelEscopo
        );

        System.out.println(
                "Entrou no escopo: " +
                        pilhaEscopos.peek()
        );
    }

    private void sairEscopo()
    {
        if(pilhaEscopos.size() > 1)
        {
            String escopo = pilhaEscopos.pop();

            System.out.println(
                    "Saiu do escopo: " + escopo
            );
        }
    }

    private boolean tiposCompativeis(String destino, String origem)
    {
        if(destino.equals(origem))
            return true;

        if(destino.equals("float") && origem.equals("int"))
            return true;

        return false;
    }

    private void validarAtribuicao(Token token) throws SemanticError
    {
        if(lhsAtual == null)
        {
            throw new SemanticError(
                    "Atribuição sem variável de destino",
                    token.getPosition()
            );
        }

        if(pilhaTipos.isEmpty())
        {
            throw new SemanticError(
                    "Expressão sem tipo",
                    token.getPosition()
            );
        }

        finalizarExpr(token);

        if(pilhaTipos.isEmpty())
        {
            throw new SemanticError(
                    "Expressão sem tipo",
                    token.getPosition()
            );
        }

        String tipoExpr = pilhaTipos.pop();
        String tipoVar = lhsAtual.tipo;

        if(!tiposCompativeis(tipoVar, tipoExpr))
        {
            throw new SemanticError(
                    "Tipos incompatíveis: não é possível atribuir '" +
                            tipoExpr +
                            "' em '" +
                            tipoVar + "'",
                    token.getPosition()
            );
        }

        lhsAtual.ini = true;
        lhsAtual.usada = true;

        System.out.println(
            "Atribuição válida: " +
                    tipoVar + " <- " + tipoExpr
        );
    }

    private int indice(String tipo)
    {
        switch(tipo)
        {
            case "int":
                return 0;

            case "float":
                return 1;

            case "char":
                return 2;

            case "string":
                return 3;

            case "bool":
                return 4;

            default:
                return -1;
        }
    }

    private String resolverOperador(String op, String t1, String t2)
    {
        int i = indice(t1);
        int j = indice(t2);

        if(i < 0 || j < 0)
        {
            return ERRO;
        }

        switch(op)
        {
            case "+":
                return soma[i][j];

            case "-":
                return sub[i][j];

            case "*":
                return mult[i][j];

            case "/":
                return div[i][j];

            case "%":
                return mod[i][j];

            case "==":
            case "!=":
            case "<":
            case ">":
            case "<=":
            case ">=":
                return rel[i][j];

            case "&&":
            case "||":
                return logic[i][j];

            default:
                return ERRO;
        }
    }

    private void finalizarExpr(Token token) throws SemanticError
    {
        while(!pilhaOps.isEmpty())
        {
            String op = pilhaOps.pop();
            reduzir(op, token);
        }
    }

    private void reduzir(String op, Token token) throws SemanticError
    {
        if(op.equals("!"))
        {
            if(pilhaTipos.isEmpty())
            {
                throw new SemanticError(
                        "Expressão inválida",
                        token.getPosition()
                );
            }

            String tipo = pilhaTipos.pop();

            if(!tipo.equals("bool"))
            {
                throw new SemanticError(
                        "Operador ! requer bool",
                        token.getPosition()
                );
            }

            pilhaTipos.push("bool");
            return;
        }

        if(pilhaTipos.size() < 2)
        {
            throw new SemanticError(
                    "Expressão inválida",
                    token.getPosition()
            );
        }

        String direita = pilhaTipos.pop();
        String esquerda = pilhaTipos.pop();

        String resultado = resolverOperador(op, esquerda, direita);

        if(resultado.equals(ERRO))
        {
            throw new SemanticError(
                    "Operação inválida: " +
                            esquerda + " " + op + " " + direita,
                    token.getPosition()
            );
        }

        pilhaTipos.push(resultado);
    }

    private void verificarInicializacao(Simbolo s, Token token)
    {
        if(
                s != null &&
                !s.ini &&
                !s.func &&
                !s.param
        )
        {
            adicionarAviso(
                    "Aviso: identificador '" +
                            s.id +
                            "' usado sem inicialização."
            );
        }
    }

    private void adicionarAviso(String aviso)
    {
        if(!avisosEmitidos.contains(aviso))
        {
            avisosEmitidos.add(aviso);
            avisos.add(aviso);
        }
    }

    public void finalizarAnalise()
    {
        for(Simbolo s : tabelaSimbolos)
        {
            if(!s.usada && !s.func)
            {
                adicionarAviso(
                        "Aviso: identificador '" +
                                s.id +
                                "' declarado e não usado."
                );
            }
        }
    }

    private String modalidade(Simbolo s)
    {
        if(s.func)
            return "Função";

        if(s.param)
            return "Parâmetro";

        if(s.matriz)
            return "Matriz";

        if(s.vet)
            return "Vetor";

        return "Variável";
    }

    public List<String[]> getTabelaParaIDE()
    {
        List<String[]> linhas = new ArrayList<>();

        for(Simbolo s : tabelaSimbolos)
        {
            linhas.add(
                    new String[]{
                            s.id,
                            s.tipo,
                            s.escopo,
                            modalidade(s),
                            s.ini ? "Sim" : "Não",
                            s.usada ? "Sim" : "Não"
                    }
            );
        }

        return linhas;
    }

    public List<String> getAvisos()
    {
        return avisos;
    }
}