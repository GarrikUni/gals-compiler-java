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
        int tamanho;
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
                int tamanho,
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
            this.tamanho = tamanho;
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
    private Stack<String> pilhaOps = new Stack<>(); // pilha operações

    private Stack<String> pilhaOperandos = new Stack<>();

    private Simbolo simboloAtual;
    private Simbolo lhsAtual;
    private Simbolo ultimoIdentificador;

    private Simbolo acessoAtual;
    private int quantidadeIndices;

    private int nextTempAddr = 1000;
    private String novoTemp()
    {
        String temp = String.valueOf(nextTempAddr++);
        temporarios.add(temp);
        return temp;
    }
    private Set<String> temporarios = new LinkedHashSet<>();

    private List<String> avisos = new ArrayList<>();
    private Set<String> avisosEmitidos = new LinkedHashSet<>();

    private StringBuilder dataSection = new StringBuilder();
    private StringBuilder textSection = new StringBuilder();

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
                ultimoIdentificador = simboloAtual;
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
                pilhaOperandos.push(token.getLexeme());
                break;

            case 8:
                pilhaTipos.push("float");
                pilhaOperandos.push(token.getLexeme());
                break;

            case 9:
                pilhaTipos.push("string");
                pilhaOperandos.push(token.getLexeme());
                break;

            case 10:
                pilhaTipos.push("char");
                pilhaOperandos.push(token.getLexeme());
                break;

            case 11:
                pilhaTipos.push("bool");
                pilhaOperandos.push(
                    token.getLexeme().equals("true") ? "1" : "0"
                );
                break;

            case 12:
                Simbolo s = verificarIdentificador(token);

                if (s.vet) {
                    throw new SemanticError(
                        "Vetor requer índice: " + s.id,
                        token.getPosition()
                    );
                }

                verificarInicializacao(s, token);

                pilhaTipos.push(s.tipo);
                pilhaOperandos.push(s.id);
                break;

            // case 14:
            //     pilhaOps.push("==");
            //     break;

            // case 15:
            //     pilhaOps.push("!=");
            //     break;

            // case 16:
            //     pilhaOps.push("<");
            //     break;

            // case 17:
            //     pilhaOps.push(">");
            //     break;

            // case 18:
            //     pilhaOps.push("<=");
            //     break;

            // case 19:
            //     pilhaOps.push(">=");
            //     break;

            // case 20:
            //     pilhaOps.push("+");
            //     break;

            // case 21:
            //     pilhaOps.push("-");
            //     break;

            // case 22:
            //     pilhaOps.push("*");
            //     break;

            // case 23:
            //     pilhaOps.push("/");
            //     break;

            // case 24:
            //     pilhaOps.push("%");
            //     break;

            // case 25:
            //     pilhaOps.push("||");
            //     break;

            // case 26:
            //     pilhaOps.push("&&");
            //     break;

            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
                String op = token.getLexeme();

                while(!pilhaOps.isEmpty() &&
                    precedencia(pilhaOps.peek()) >= precedencia(op))
                {
                    reduzir(pilhaOps.pop(), token);
                }

                pilhaOps.push(op);
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
                ultimoIdentificador = lhsAtual;
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
                Simbolo simboloVetor = verificarIdentificador(token);

                if (!simboloVetor.vet) {
                    throw new SemanticError(
                        "Identificador não é vetor: " + simboloVetor.id,
                        token.getPosition()
                    );
                }
                acessoAtual = simboloVetor;
                quantidadeIndices = 0;
                break;

            case 71:
                validarIndice(token);
                quantidadeIndices++;
                break;

            case 72:
                validarAcessoVetor(token);
                pilhaTipos.push(acessoAtual.tipo);
                pilhaOperandos.push(acessoAtual.id);

                quantidadeIndices = 0;
                break;
            
            case 80:
                gerarInstr("LD $in_port");
                gerarInstr("STO " + ultimoIdentificador.id);
                ultimoIdentificador.ini = true;
                break;

            case 81:
                if (pilhaOperandos.isEmpty())
                    break;

                String src = pilhaOperandos.peek();

                if (src.matches("-?\\d+"))
                    gerarInstr("LDI " + src);
                else
                    gerarInstr("LD " + src);

                gerarInstr("STO $out_port");
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
                0,
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
                0,
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
                0,
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
        if (simboloAtual != null)
        {
            simboloAtual.vet = true;
            System.out.println("Vetor " + simboloAtual.id +
                            " tamanho " + simboloAtual.tamanho);
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
        if (acessoAtual == null)
            throw new SemanticError("Acesso inválido de vetor", token.getPosition());

        if (!acessoAtual.vet)
            throw new SemanticError(acessoAtual.id + " não é vetor", token.getPosition());

        if (pilhaOperandos.isEmpty())
            throw new SemanticError("Índice ausente", token.getPosition());

        String indiceStr = pilhaOperandos.pop();

        if (!indiceStr.matches("-?\\d+"))
        {
            throw new SemanticError("Índice de vetor deve ser constante neste estágio", token.getPosition());
        }

        int indice = Integer.parseInt(indiceStr);

        if (indice < 0 || indice >= acessoAtual.tamanho)
        {
            throw new SemanticError(
                "Índice fora do limite do vetor " + acessoAtual.id +
                " (0.." + (acessoAtual.tamanho - 1) + ")",
                token.getPosition()
            );
        }

        gerarInstr(buildLoad(indiceStr));
        gerarInstr("STO $indr");

        pilhaOperandos.push(acessoAtual.id);
        pilhaTipos.push(acessoAtual.tipo);

        pilhaOps.push("@VETOR_READ");
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
        if (lhsAtual == null)
            throw new SemanticError(
                "Atribuição sem variável de destino",
                token.getPosition()
            );

        finalizarExpr(token);

        if (pilhaTipos.isEmpty() || pilhaOperandos.isEmpty())
            throw new SemanticError(
                "Expressão inválida ou sem resultado",
                token.getPosition()
            );

        String tipoExpr = pilhaTipos.pop();
        String src = pilhaOperandos.pop();

        if (!tiposCompativeis(lhsAtual.tipo, tipoExpr))
        {
            throw new SemanticError(
                "Tipos incompatíveis: " + tipoExpr + " -> " + lhsAtual.tipo,
                token.getPosition()
            );
        }

        if (lhsAtual.vet)
        {
            gerarInstr(buildLoad(src));
            gerarInstr("STOV " + lhsAtual.id);
        }
        else
        {
            gerarInstr(buildLoad(src));
            gerarInstr("STO " + lhsAtual.id);
        }

        lhsAtual.ini = true;

        pilhaTipos.clear();
        pilhaOperandos.clear();
        pilhaOps.clear();

        System.out.println("Atribuição válida: " + lhsAtual.id);
    }

    private int precedencia(String op)
    {
        switch(op)
        {
            case "||": return 1;
            case "&&": return 2;

            case "==":
            case "!=":
                return 3;

            case "<":
            case ">":
            case "<=":
            case ">=":
                return 4;

            case "+":
            case "-":
                return 5;

            case "*":
            case "/":
            case "%":
                return 6;

            case "!":
                return 7;

            default:
                return 0;
        }
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
            reduzir(pilhaOps.pop(), token);
    }

    private void reduzir(String op, Token token) throws SemanticError
    {
        if(op.equals("!"))
        {
            String t = pilhaTipos.pop();
            String v = pilhaOperandos.pop();

            if(!t.equals("bool"))
                throw new SemanticError("Operador ! requer bool", token.getPosition());

            String temp = novoTemp();

            buildOp("!", v, null, temp);

            pilhaTipos.push("bool");
            pilhaOperandos.push(temp);
            return;
        }

        if(pilhaTipos.size() < 2 || pilhaOperandos.size() < 2)
            throw new SemanticError("Expressão inválida", token.getPosition());

        String t2 = pilhaTipos.pop();
        String t1 = pilhaTipos.pop();

        String v2 = pilhaOperandos.pop();
        String v1 = pilhaOperandos.pop();

        String tipoResult = resolverOperador(op, t1, t2);

        if(tipoResult.equals(ERRO))
            throw new SemanticError(
                "Operação inválida: " + t1 + " " + op + " " + t2,
                token.getPosition()
            );

        String temp = novoTemp();

        buildOp(op, v1, v2, temp);

        pilhaTipos.push(tipoResult);
        pilhaOperandos.push(temp);
        System.out.println("REDUZINDO: " + t1 + " " + op + " " + t2 + " => " + tipoResult);
    }

    private int nextLabel = 0;

    private String novoRotulo()
    {
        return "L" + (nextLabel++);
    }

    private boolean isLiteral(String s)
    {
        return s.matches("-?\\d+") &&
            !temporarios.contains(s);
    }

    private String buildLoad(String v)
    {
        if (isLiteral(v))
            return "LDI " + v;

        if (temporarios.contains(v))
            return "LD " + v;

        return "LD " + v;
    }

    private String buildAdd(String v)
    {
        return isLiteral(v) ? "ADDI " + v : "ADD " + v;
    }

    private String buildSub(String v)
    {
        return isLiteral(v) ? "SUBI " + v : "SUB " + v;
    }

    private String buildAnd(String v)
    {
        return isLiteral(v) ? "ANDI " + v : "AND " + v;
    }

    private String buildOr(String v)
    {
        return isLiteral(v) ? "ORI " + v : "OR " + v;
    }

    private void buildLeituraVetor(String base)
    {
        gerarInstr("LDV " + base);
    }

    private void buildEscritaVetor(String base)
    {
        gerarInstr("STOV " + base);
    }

    private void buildOp(String op, String a, String b, String result)
    {
        boolean isVectorRead = !pilhaOps.isEmpty() && pilhaOps.peek().equals("@VETOR_READ");

        if (isVectorRead)
        {
            pilhaOps.pop();

            gerarInstr("LDV " + a);

            switch (op)
            {
                case "+":
                    gerarInstr(buildAdd(b));
                    break;
                case "-":
                    gerarInstr(buildSub(b));
                    break;
                case "*":
                    gerarInstr(buildLoad(b));
                    break;
            }

            gerarInstr("STO " + result);
            return;
        }
        switch (op)
        {
            case "+":
                gerarInstr(buildLoad(a));
                gerarInstr(buildAdd(b));
                gerarInstr("STO " + result);
                break;

            case "-":
                gerarInstr(buildLoad(a));
                gerarInstr(buildSub(b));
                gerarInstr("STO " + result);
                break;

            case "&&":
                gerarInstr(buildLoad(a));
                gerarInstr(buildAnd(b));
                gerarInstr("STO " + result);
                break;

            case "||":
                gerarInstr(buildLoad(a));
                gerarInstr(buildOr(b));
                gerarInstr("STO " + result);
                break;

            case "!":
                gerarInstr(buildLoad(a));
                gerarInstr("NOT");
                gerarInstr("STO " + result);
                break;

            case ">":
                gerarComparacao(a, b, result, "BGT");
                break;

            case ">=":
                gerarComparacao(a, b, result, "BGE");
                break;

            case "<":
                gerarComparacao(a, b, result, "BLT");
                break;

            case "<=":
                gerarComparacao(a, b, result, "BLE");
                break;

            case "==":
                gerarComparacao(a, b, result, "BEQ");
                break;

            case "!=":
                gerarComparacao(a, b, result, "BNE");
                break;
        }
    }

    private void gerarMultiplicacao(String a, String b, String result)
    {
        String contador = novoTemp();
        String loop = novoRotulo();
        String fim = novoRotulo();

        gerarInstr("LDI 0");
        gerarInstr("STO " + result);

        gerarInstr(buildLoad(b));
        gerarInstr("STO " + contador);

        gerarInstr(loop + ":");

        gerarInstr("LD " + contador);
        gerarInstr("BEQ " + fim);

        gerarInstr("LD " + result);
        gerarInstr(buildAdd(a));
        gerarInstr("STO " + result);

        gerarInstr("LD " + contador);
        gerarInstr("SUBI 1");
        gerarInstr("STO " + contador);

        gerarInstr("JMP " + loop);

        gerarInstr(fim + ":");
    }

    private void gerarDivisao(String a, String b, String result)
    {
        String tempA = novoTemp();
        String loop = novoRotulo();
        String fim = novoRotulo();
        String check = novoRotulo();

        gerarInstr("LDI 0");
        gerarInstr("STO " + result);

        gerarInstr(buildLoad(a));
        gerarInstr("STO " + tempA);

        gerarInstr(loop + ":");

        gerarInstr("LD " + tempA);
        gerarInstr(buildSub(b));
        gerarInstr("STO " + tempA);

        gerarInstr("LD " + tempA);
        gerarInstr("BGT " + check);
        gerarInstr("JMP " + fim);

        gerarInstr(check + ":");

        gerarInstr("LD " + result);
        gerarInstr("ADDI 1");
        gerarInstr("STO " + result);

        gerarInstr("JMP " + loop);

        gerarInstr(fim + ":");
    }

    private void gerarModulo(String a, String b, String result)
    {
        String tempA = novoTemp();
        String loop = novoRotulo();
        String fim = novoRotulo();
        String check = novoRotulo();

        gerarInstr(buildLoad(a));
        gerarInstr("STO " + tempA);

        gerarInstr(loop + ":");

        gerarInstr("LD " + tempA);
        gerarInstr(buildSub(b));
        gerarInstr("STO " + tempA);

        gerarInstr("LD " + tempA);
        gerarInstr("BGT " + check);
        gerarInstr("JMP " + fim);

        gerarInstr(check + ":");

        gerarInstr("JMP " + loop);

        gerarInstr(fim + ":");

        gerarInstr("LD " + tempA);
        gerarInstr("STO " + result);
    } 

    private void gerarComparacao( String a, String b, String result, String branch )
    {
        String verdadeiro = novoRotulo();
        String fim = novoRotulo();

        gerarInstr(buildLoad(a));
        gerarInstr(buildSub(b));

        gerarInstr(branch + " " + verdadeiro);

        gerarInstr("LDI 0");
        gerarInstr("STO " + result);
        gerarInstr("JMP " + fim);

        gerarInstr(verdadeiro + ":");
        gerarInstr("LDI 1");
        gerarInstr("STO " + result);

        gerarInstr(fim + ":");
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

    private void gerarData()
    {
        //System.out.println("DEBUG tabelaSimbolos = " + tabelaSimbolos.size());
        Set<String> jaDeclarados = new LinkedHashSet<>();

        for (Simbolo s : tabelaSimbolos)
        {
            if (!s.func && !jaDeclarados.contains(s.id))
            {
                dataSection.append(s.id).append(" : ");

                if (s.vet)
                {
                    for (int i = 0; i < s.tamanho; i++)
                    {
                        dataSection.append("0");
                        if (i < s.tamanho - 1)
                            dataSection.append(",");
                    }
                    dataSection.append("\n");
                }
                else
                {
                    dataSection.append("0\n");
                }

                jaDeclarados.add(s.id);
            }
        }
    }

    private void gerarInstr(String instr)
    {
        //System.out.println("DEBUG GERARINSTR = " + instr);
        textSection.append(instr).append("\n");
    }

    public String gerarProgramaBip()
    {
        gerarData();

        StringBuilder sb = new StringBuilder();

        sb.append(".data\n");
        sb.append(dataSection);

        sb.append("\n.text\n");
        sb.append(textSection);

        sb.append("\nHLT\n");

        return sb.toString();
    }
}