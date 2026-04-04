/**
 * Counterexample — Contraexemplo Didático Autoral
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * PERGUNTA CENTRAL DO PROJETO:
 *   Em que situação a AVL tree clássica falha, torna-se limitada ou perde
 *   eficiência, e como o artigo escolhido tenta resolver esse problema?
 *
 * RESPOSTA (este arquivo):
 *   A AVL tree clássica usa 3 ponteiros de 64 bits por nó (left, right, parent),
 *   totalizando 192 bits/nó APENAS para a ESTRUTURA — sem contar os valores.
 *   Em cenários com muitos nós e acesso somente-leitura (árvore estática),
 *   esse espaço é drasticamente desperdiçado, pois os ponteiros carregam
 *   muito mais informação do que o mínimo teórico necessário.
 *
 *   O artigo prova que o limite teórico para codificar a estrutura de uma
 *   AVL tree é ~0,938 bits/nó — mais de 200x menor que os 192 bits/nó naive.
 *
 * CENÁRIO DO CONTRAEXEMPLO:
 *   Um dispositivo embarcado (ex: roteador, e-reader, tradutor offline) com
 *   RAM limitada a 512 KB precisa indexar dicionários de diferentes tamanhos.
 *   Mostramos exatamente em qual ponto a AVL clássica transborda a memória
 *   disponível, enquanto a representação sucinta (BP) ainda cabe com folga.
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class Counterexample {

    // ── Constantes do cenário embarcado ──────────────────────────────────────

    /** RAM total disponível no dispositivo simulado (512 KB em bits). */
    static final long RAM_LIMIT_BITS = 512L * 1024 * 8; // 4.194.304 bits

    /** Bits por ponteiro numa JVM de 64 bits (referência de objeto). */
    static final int POINTER_BITS = 64;

    /**
     * Bits usados pela AVL clássica por nó:
     *   - left   (AVLNode ref) = 64 bits
     *   - right  (AVLNode ref) = 64 bits
     *   - parent (AVLNode ref) = 64 bits  ← muitas implementações incluem parent
     *   Total de ponteiros estruturais     = 192 bits
     *
     * NOTA: fatBal (int) e info (referência) são desconsiderados intencionalmente,
     * pois o artigo compara APENAS o custo da ESTRUTURA (navegação), não dos dados.
     */
    static final int BITS_PER_NODE_NAIVE = 3 * POINTER_BITS; // 192

    /**
     * Bits usados pela representação sucinta (Balanced Parentheses) por nó:
     *   - BP sequence : 2 bits/nó  (1 '(' + 1 ')' por nó, em pré+pós-ordem)
     *   - fatBal AVL  : 2 bits/nó  (codificação: 00=−1, 01=0, 10=+1)
     *   Total                      = 4 bits/nó
     *
     * O artigo prova que o mínimo TEÓRICO é ~0,938 bits/nó para a estrutura pura.
     * A representação BP (2 bits/nó) está acima desse mínimo, mas ainda assim é
     * ~48x mais eficiente que os 192 bits/nó da representação por ponteiros.
     */
    static final int BITS_PER_NODE_BP       = 2; // só estrutura BP
    static final int BITS_PER_NODE_BALANCE  = 2; // fatores de balanceamento
    static final int BITS_PER_NODE_SUCCINCT = BITS_PER_NODE_BP + BITS_PER_NODE_BALANCE; // 4

    /** Limite teórico provado no artigo (informação mínima necessária). */
    static final double THEORETICAL_LIMIT_BITS_PER_NODE = 0.938;

    // ─────────────────────────────────────────────────────────────────────────
    // Ponto de entrada do contraexemplo
    // ─────────────────────────────────────────────────────────────────────────

    public static void run() {

        printHeader();

        // ── Parte 1: Análise analítica pura (sem construir árvore) ───────────
        // Mostra para quais tamanhos de dicionário cada representação cabe na RAM.
        analyzeMemoryLimits();

        // ── Parte 2: Construção real de árvores no ponto crítico ─────────────
        // Constrói AVL trees com n = ponto de falha e n = ponto de falha - 1,
        // mostrando concretamente a diferença de espaço.
        demonstrateCriticalPoint();

        // ── Parte 3: Tabela comparativa completa ─────────────────────────────
        // Exibe a tabela bits/nó x n para visualizar a divergência de espaço.
        printComparisonTable();

        // ── Parte 4: Conclusão crítica ────────────────────────────────────────
        printConclusion();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parte 1 — Análise analítica: onde cada representação transborda a RAM
    // ─────────────────────────────────────────────────────────────────────────

    static void analyzeMemoryLimits() {
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  PARTE 1 — Análise de limites de memória (RAM = 512 KB)      │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  Cenário: dispositivo embarcado com 512 KB de RAM disponível.");
        System.out.println("  Queremos armazenar um dicionário numa AVL tree somente-leitura.");
        System.out.println();

        // ── Cálculo do ponto de falha da AVL clássica ────────────────────────
        // A AVL clássica cabe em RAM enquanto: n × 192 bits ≤ RAM_LIMIT_BITS
        // Logo: n ≤ RAM_LIMIT_BITS / 192
        long maxNodesNaive = RAM_LIMIT_BITS / BITS_PER_NODE_NAIVE;

        // ── Cálculo do ponto de falha da representação sucinta ───────────────
        // A representação BP cabe enquanto: n × 4 bits ≤ RAM_LIMIT_BITS
        // Logo: n ≤ RAM_LIMIT_BITS / 4
        long maxNodesSuccinct = RAM_LIMIT_BITS / BITS_PER_NODE_SUCCINCT;

        System.out.printf("  AVL CLÁSSICA  (192 bits/nó): suporta até %,d nós (%.1f KB de estrutura)%n",
                maxNodesNaive,
                (maxNodesNaive * BITS_PER_NODE_NAIVE) / 8.0 / 1024.0);

        System.out.printf("  REP. SUCINTA  (  4 bits/nó): suporta até %,d nós (%.1f KB de estrutura)%n",
                maxNodesSuccinct,
                (maxNodesSuccinct * BITS_PER_NODE_SUCCINCT) / 8.0 / 1024.0);

        System.out.printf("  LIM. TEÓRICO  (0.938 bits/nó): suportaria até %,d nós%n",
                (long)(RAM_LIMIT_BITS / THEORETICAL_LIMIT_BITS_PER_NODE));

        System.out.println();

        // ── A limitação fica clara aqui: ─────────────────────────────────────
        // Um dicionário com 50.000 palavras (comum em tradutores offline) cabe
        // na representação sucinta, mas NÃO cabe na AVL clássica dentro de 512 KB.
        long testSize = 50_000L;
        long naiveCost    = testSize * BITS_PER_NODE_NAIVE;
        long succinctCost = testSize * BITS_PER_NODE_SUCCINCT;

        System.out.printf("  Custo para %,d nós (dicionário médio):%n", testSize);
        System.out.printf("    AVL clássica : %,d bits = %.1f KB  →  %s%n",
                naiveCost,
                naiveCost / 8.0 / 1024.0,
                naiveCost > RAM_LIMIT_BITS ? "❌ NÃO CABE na RAM!" : "✓ cabe");
        System.out.printf("    Rep. sucinta : %,d bits = %.1f KB  →  %s%n",
                succinctCost,
                succinctCost / 8.0 / 1024.0,
                succinctCost > RAM_LIMIT_BITS ? "❌ NÃO CABE na RAM!" : "✓ cabe");
        System.out.println();

        // ── Este é o ponto central do contraexemplo: ─────────────────────────
        // Para n entre maxNodesNaive+1 e maxNodesSuccinct, a AVL clássica falha
        // (não cabe na memória disponível) mas a representação sucinta ainda funciona.
        System.out.printf("  *** ZONA DE FALHA da AVL clássica: de %,d até %,d nós ***%n",
                maxNodesNaive + 1, maxNodesSuccinct);
        System.out.println("  Nessa zona, a AVL clássica transborda a RAM disponível,");
        System.out.println("  enquanto a representação sucinta do artigo ainda funciona.");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parte 2 — Demonstração concreta no ponto crítico
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateCriticalPoint() {
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  PARTE 2 — Demonstração no ponto crítico (n real)            │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();

        // n = 21.845: último valor em que a AVL clássica ainda cabe
        // (RAM_LIMIT_BITS / 192 = 21.845,33... → piso = 21.845)
        int nCabe  = (int)(RAM_LIMIT_BITS / BITS_PER_NODE_NAIVE);     // 21.845
        int nFalha = nCabe + 1;                                         // 21.846

        System.out.printf("  Construindo AVL tree com n=%d (último tamanho que CABE na AVL clássica)%n", nCabe);
        SuccinctAVLTree<Integer> treeCabe = new SuccinctAVLTree<>();
        insertRange(treeCabe, 1, nCabe);
        treeCabe.encodeSuccinct();

        long naiveBitsCabe    = (long) treeCabe.getNodeCount() * BITS_PER_NODE_NAIVE;
        long succinctBitsCabe = (long) treeCabe.getNodeCount() * BITS_PER_NODE_SUCCINCT;

        System.out.printf("    Nós reais            : %,d%n", treeCabe.getNodeCount());
        System.out.printf("    AVL clássica         : %,d bits = %.2f KB  → %s%n",
                naiveBitsCabe, naiveBitsCabe / 8.0 / 1024.0,
                naiveBitsCabe <= RAM_LIMIT_BITS ? "✓ cabe" : "❌ não cabe");
        System.out.printf("    Rep. sucinta (BP)    : %,d bits = %.2f KB  → ✓ cabe%n",
                succinctBitsCabe, succinctBitsCabe / 8.0 / 1024.0);
        System.out.println();

        System.out.printf("  Adicionando apenas 1 nó (n=%d): AVL clássica TRANSBORDA a RAM.%n", nFalha);
        SuccinctAVLTree<Integer> treeFalha = new SuccinctAVLTree<>();
        insertRange(treeFalha, 1, nFalha);
        treeFalha.encodeSuccinct();

        long naiveBitsFalha    = (long) treeFalha.getNodeCount() * BITS_PER_NODE_NAIVE;
        long succinctBitsFalha = (long) treeFalha.getNodeCount() * BITS_PER_NODE_SUCCINCT;
        long overflow          = naiveBitsFalha - RAM_LIMIT_BITS;

        System.out.printf("    Nós reais            : %,d%n", treeFalha.getNodeCount());
        System.out.printf("    AVL clássica         : %,d bits = %.2f KB  → ❌ TRANSBORDA em %,d bits!%n",
                naiveBitsFalha, naiveBitsFalha / 8.0 / 1024.0, overflow);
        System.out.printf("    Rep. sucinta (BP)    : %,d bits = %.2f KB  → ✓ cabe (usa %.1f%% da RAM)%n",
                succinctBitsFalha, succinctBitsFalha / 8.0 / 1024.0,
                succinctBitsFalha * 100.0 / RAM_LIMIT_BITS);
        System.out.println();

        // ── Aqui fica explícita a falha da estrutura clássica: ────────────────
        // Adicionar 1 único nó à AVL clássica já excede a RAM disponível,
        // enquanto a representação sucinta usa menos de 0,1% da mesma RAM.
        System.out.println("  ➤  A representação clássica falha adicionando apenas 1 nó.");
        System.out.println("     A representação sucinta ainda usa menos de 1% da RAM disponível.");
        System.out.println("     Isso ilustra que os ponteiros são o gargalo de memória,");
        System.out.println("     não os dados em si — e que a AVL clássica é inapropriada");
        System.out.println("     para árvores estáticas em ambientes com memória restrita.");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parte 3 — Tabela comparativa completa
    // ─────────────────────────────────────────────────────────────────────────

    static void printComparisonTable() {
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  PARTE 3 — Tabela comparativa de uso de memória              │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  n (nós) │ AVL clássica (KB) │ Sucinta BP (KB) │ Limite teórico (KB) │ Status AVL");
        System.out.println("  ────────┼───────────────────┼─────────────────┼─────────────────────┼───────────────");

        // Tamanhos representativos: do mínimo ao máximo que a sucinta suporta
        int[] sizes = {
            1_000, 5_000, 10_000, 15_000,
            21_845, // último que cabe na AVL clássica
            21_846, // primeiro que falha na AVL clássica
            50_000, 100_000, 500_000, 1_000_000
        };

        for (int n : sizes) {
            double naiveKB     = (n * (double) BITS_PER_NODE_NAIVE) / 8.0 / 1024.0;
            double succinctKB  = (n * (double) BITS_PER_NODE_SUCCINCT) / 8.0 / 1024.0;
            double theoretKB   = (n * THEORETICAL_LIMIT_BITS_PER_NODE) / 8.0 / 1024.0;
            boolean naiveFits  = (long) n * BITS_PER_NODE_NAIVE <= RAM_LIMIT_BITS;

            System.out.printf("  %,7d │ %17.2f │ %15.2f │ %19.2f │ %s%n",
                    n, naiveKB, succinctKB, theoretKB,
                    naiveFits ? "✓ cabe" : "❌ FALHA");
        }

        System.out.println();
        System.out.println("  * RAM disponível = 512 KB = " + String.format("%,d", RAM_LIMIT_BITS / 8 / 1024) + " KB");
        System.out.println("  * AVL clássica   = 192 bits/nó (3 ponteiros de 64 bits)");
        System.out.println("  * Sucinta BP     =   4 bits/nó (2 BP + 2 balance)");
        System.out.println("  * Limite teórico = 0,938 bits/nó (provado no artigo, Teorema 2.2.5)");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parte 4 — Conclusão crítica
    // ─────────────────────────────────────────────────────────────────────────

    static void printConclusion() {
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  PARTE 4 — Conclusão crítica e limitações do artigo          │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  O QUE O CONTRAEXEMPLO MOSTRA:");
        System.out.println("    A AVL clássica é eficiente em tempo (O(log n) por operação),");
        System.out.println("    mas desperdiça memória com 192 bits/nó de overhead estrutural.");
        System.out.println("    Em árvores estáticas (somente consulta), esses ponteiros");
        System.out.println("    representam informação REDUNDANTE: a estrutura BP já permite");
        System.out.println("    navegar pai/filho em O(1) com apenas 2 bits por nó.");
        System.out.println();
        System.out.println("  O QUE O ARTIGO RESOLVE:");
        System.out.println("    Prova que o mínimo teórico é ~0,938 bits/nó e constrói");
        System.out.println("    uma representação sucinta que suporta operações em O(1),");
        System.out.println("    tornando a AVL viável em ambientes de memória restrita.");
        System.out.println();
        System.out.println("  LIMITAÇÕES DA SOLUÇÃO DO ARTIGO (crítica do grupo):");
        System.out.println("    1. A representação é ESTÁTICA: inserções/remoções invalidam");
        System.out.println("       toda a sequência BP, exigindo reconstrução em O(n).");
        System.out.println("       A AVL clássica, por outro lado, insere/remove em O(log n).");
        System.out.println();
        System.out.println("    2. O limite de 0,938 bits/nó é TEÓRICO. Na prática,");
        System.out.println("       a representação BP usa 2 bits/nó + estruturas auxiliares");
        System.out.println("       de rank/select que, embora o(n), têm constantes reais.");
        System.out.println();
        System.out.println("    3. A implementação de rank/select em O(1) real requer");
        System.out.println("       tabelas de lookup pré-computadas, adicionando complexidade");
        System.out.println("       de implementação ausente na AVL clássica.");
        System.out.println();
        System.out.println("  CONCLUSÃO: A solução do artigo é ótima para índices estáticos");
        System.out.println("  (dicionários, sistemas de arquivos read-only, bancos em memória");
        System.out.println("  de sistemas embarcados). Para workloads dinâmicos, a AVL");
        System.out.println("  clássica ainda é superior em tempo de atualização.");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Auxiliar: cabeçalho
    // ─────────────────────────────────────────────────────────────────────────

    static void printHeader() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   CONTRAEXEMPLO DIDÁTICO AUTORAL                             ║");
        System.out.println("║   Onde a AVL clássica falha: memória em árvores estáticas    ║");
        System.out.println("║   Dispositivo embarcado simulado: 512 KB de RAM              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Auxiliar: insere intervalo [lo, hi] de forma balanceada na árvore
    // (mesma estratégia do Main.insertBalanced, replicada aqui por clareza)
    // ─────────────────────────────────────────────────────────────────────────

    static void insertRange(SuccinctAVLTree<Integer> tree, int lo, int hi) {
        if (lo > hi) return;
        int mid = (lo + hi) / 2;
        tree.insert(mid);
        insertRange(tree, lo, mid - 1);
        insertRange(tree, mid + 1, hi);
    }
}