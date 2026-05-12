import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Semantico implements Constants
{
    class Simbolo {
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

    private List<Simbolo> tabelaSimbolos = new ArrayList<>();
    //private Map<String, String> tabelaSimbolos = new LinkedHashMap<>();

    private Stack<String> pilhaEscopos = new Stack<>();
    private int nivelEscopo = 0;

    private String tipoAtual;

    public void executeAction(int action, Token token) throws SemanticError
    {
        switch(action)
        {
            case 1:
                // Guarda o tipo atual: int, float, char, string, bool, void
                tipoAtual = token.getLexeme();
                break;

            case 2:
                // Insere identificador na tabela
                inserirIdentificador(token);
                break;

            case 3:
                // Verifica uso de identificador
                verificarIdentificador(token);
                break;
            
            case 4:
                entrarEscopo();
                break;

            case 5:
                sairEscopo();
                break;
        }
    }

    private void inserirIdentificador(Token token) throws SemanticError
    {
        String nome = token.getLexeme();

        // verifica se já existe
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
            0,
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
    }

    private void verificarIdentificador(Token token) throws SemanticError
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

        s.usada = true;

        System.out.println(
            "Uso de identificador: " + nome +
            " tipo: " + s.tipo
        );
    }

    // busca símbolo pelo id
    private Simbolo buscarNoEscopoAtual(String nome)
    {
        String escopoAtual = pilhaEscopos.peek();

        for(Simbolo s : tabelaSimbolos)
        {
            if(
                s.id.equals(nome) &&
                s.escopo.equals(escopoAtual)
            )
            {
                return s;
            }
        }

        return null;
    }

    private Simbolo buscarSimbolo(String nome)
    {
        for(int i = pilhaEscopos.size()-1; i >= 0; i--) {
            String escopo = pilhaEscopos.get(i);

            for(Simbolo s : tabelaSimbolos) {
                if( s.id.equals(nome) && s.escopo.equals(escopo) ) {
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
        String escopo = pilhaEscopos.pop();

        tabelaSimbolos.removeIf(
            s -> s.escopo.equals(escopo)
        );

        System.out.println(
            "Saiu do escopo: " + escopo
        );
    }

    // =========================
    // RETORNA A TABELA
    // =========================
    public List<Simbolo> getTabelaSimbolos()
    {
        return tabelaSimbolos;
    }

    public Semantico() {
        pilhaEscopos.push("global");
    }
}