/**
 * Main - Demonstração da codificação sucinta de AVL Trees.
 *
 * Implementa os conceitos do artigo:
 *   "Succinct encodings of binary trees with application to AVL trees"
 *   Theoretical Computer Science, 2025 — pii: S030439752500475X
 *
 * ESTRUTURA DA DEMONSTRAÇÃO:
 *   1. Inserção de elementos e balanceamento AVL
 *   2. Codificação sucinta via balanced parentheses (BP)
 *   3. Exibição das estatísticas de compressão vs. limite teórico (0.938 bits/nó)
 *   4. Navegação e busca na representação sucinta
 *   5. Experimento com diferentes tamanhos de árvore (tabela de eficiência)
 *   6. *** CONTRAEXEMPLO DIDÁTICO AUTORAL *** ← item obrigatório do projeto
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("  CODIFICAÇÃO SUCINTA DE ÁRVORES AVL");
        System.out.println("  Baseado em: 'Succinct encodings of binary trees with");
        System.out.println("  application to AVL trees' (TCS, 2025)");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        // ── Demonstração 1: Árvore pequena ────────────────────────────────────
        demo1_SmallTree();

        // ── Demonstração 2: Busca sucinta ─────────────────────────────────────
        demo2_SuccinctSearch();

        // ── Demonstração 3: Experimento com escalas ───────────────────────────
        demo3_ScalingExperiment();

        // ── Demonstração 4: SuccinctBitVector - rank/select ───────────────────
        demo4_RankSelect();

        // ══════════════════════════════════════════════════════════════════════
        // ── Demonstração 5: CONTRAEXEMPLO DIDÁTICO AUTORAL ────────────────────
        //
        // Pergunta do projeto: em que situação a AVL clássica falha ou perde
        // eficiência, e como o artigo resolve isso?
        //
        // O Counterexample.run() demonstra exatamente isso:
        //   - Calcula o ponto exato em que a AVL clássica (192 bits/nó) excede
        //     a RAM de um dispositivo embarcado (512 KB), enquanto a representação
        //     sucinta do artigo (4 bits/nó) ainda cabe com grande margem.
        //   - Constrói árvores reais nos tamanhos críticos (n=21.845 e n=21.846)
        //     para evidenciar concretamente a transição de "cabe" para "falha".
        //   - Exibe tabela comparativa para n de 1.000 a 1.000.000 nós.
        //   - Conclui com a crítica do grupo: a solução do artigo resolve o
        //     problema de espaço para árvores estáticas, mas não é indicada
        //     para workloads dinâmicos com inserções/remoções frequentes.
        // ══════════════════════════════════════════════════════════════════════
        separador("CONTRAEXEMPLO DIDÁTICO AUTORAL");
        Counterexample.run();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Demo 1 — Árvore pequena: inserção, balanceamento e codificação
    // ──────────────────────────────────────────────────────────────────────────
    static void demo1_SmallTree() {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  DEMO 1 — Árvore AVL pequena: inserção e codificação    │");
        System.out.println("└─────────────────────────────────────────────────────────┘\n");

        SuccinctAVLTree<Integer> tree = new SuccinctAVLTree<>();

        int[] values = {30, 20, 40, 10, 25, 35, 50, 5, 15, 28};
        System.out.print("Inserindo: ");
        for (int v : values) {
            System.out.print(v + " ");
            tree.insert(v);
        }
        System.out.println("\n");

        // In-ordem: confirma BST ordenada
        tree.printInOrder();

        // Codifica sucintamente
        tree.encodeSuccinct();

        // Sequência BP e fatores
        tree.printBPSequence();
        tree.printBalanceFactors();
        System.out.println();

        // Estatísticas baseadas no artigo
        tree.printSuccinctStats();
        System.out.println();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Demo 2 — Busca na representação sucinta
    // ──────────────────────────────────────────────────────────────────────────
    static void demo2_SuccinctSearch() {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  DEMO 2 — Busca na representação sucinta                │");
        System.out.println("└─────────────────────────────────────────────────────────┘\n");

        SuccinctAVLTree<String> strTree = new SuccinctAVLTree<>();
        String[] words = {"mango", "apple", "orange", "banana", "grape",
                          "kiwi", "lemon", "melon", "peach", "plum"};

        for (String w : words) strTree.insert(w);
        strTree.encodeSuccinct();

        System.out.println("Árvore AVL com strings (n=" + strTree.getNodeCount() + "):");
        strTree.printInOrder();
        System.out.println();

        String[] queries = {"apple", "grape", "mango", "cherry", "plum"};
        System.out.println("Buscas na representação sucinta:");
        for (String q : queries) {
            int idx = strTree.searchSuccinct(q);
            if (idx >= 0) {
                System.out.printf("  ✓ '%s' encontrado (pré-ordem idx=%d, fb=%+d)%n",
                                  q, idx, strTree.getBalanceFactor(idx));
            } else {
                System.out.printf("  ✗ '%s' não encontrado%n", q);
            }
        }
        System.out.println();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Demo 3 — Experimento de escala: bits/nó para diferentes tamanhos de n
    // ──────────────────────────────────────────────────────────────────────────
    static void demo3_ScalingExperiment() {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  DEMO 3 — Experimento de escala (bits/nó vs. n)         │");
        System.out.println("│  Limite teórico do artigo: ~0.938 bits/nó (estrutura)   │");
        System.out.println("└─────────────────────────────────────────────────────────┘\n");

        System.out.println("  n     │ bits(BP) │ bits(bal)│ total bits │ bits/nó │ naive  │ redução");
        System.out.println("  ──────┼──────────┼──────────┼────────────┼─────────┼────────┼────────");

        int[] sizes = {7, 15, 31, 63, 127, 255, 511, 1023};

        for (int n : sizes) {
            SuccinctAVLTree<Integer> tree = new SuccinctAVLTree<>();
            insertBalanced(tree, 1, n); // insere elementos de forma a exercitar rotações
            tree.encodeSuccinct();

            int actualN      = tree.getNodeCount();
            int bpBits       = 2 * actualN;
            int balBits      = 2 * actualN;
            int total        = bpBits + balBits;
            double perNode   = (double) total / actualN;
            double naivePer  = 192.0; // 3 × 64 bits (ponteiros left/right/parent)
            double reduction = (1.0 - (double) bpBits / (actualN * naivePer)) * 100;

            System.out.printf("  %-5d │ %-8d │ %-8d │ %-10d │ %7.3f │ %6.0f │ %5.1f%%%n",
                              actualN, bpBits, balBits, total, perNode, naivePer, reduction);
        }

        System.out.println();
        System.out.println("  Nota: 'bits/nó' inclui estrutura BP + fatores de balanceamento.");
        System.out.println("  O artigo prova que apenas a estrutura AVL (sem valores) requer");
        System.out.println("  ≈ 0.938 bits/nó, enquanto a rep. BP usa 2 bits/nó (overhead de");
        System.out.println("  rank/select é o(n) e desprezível para n grande).");
        System.out.println();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Demo 4 — SuccinctBitVector: rank e select
    // ──────────────────────────────────────────────────────────────────────────
    static void demo4_RankSelect() {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  DEMO 4 — SuccinctBitVector: operações rank e select    │");
        System.out.println("│  Fundação das estruturas sucintas do artigo             │");
        System.out.println("└─────────────────────────────────────────────────────────┘\n");

        // Vetor de exemplo: balanced parentheses de (()(()))
        boolean[] bits = {
            true, true, false, true, true, false, false, false  // (()(()))
        };
        SuccinctBitVector bv = new SuccinctBitVector(bits);

        System.out.println("  Vetor BP: " + bv);
        System.out.println("  (representa a árvore com raiz, filho esq. folha, filho dir. com filho)\n");

        System.out.println("  Operação rank1(i) — número de '(' nos primeiros i bits:");
        for (int i = 1; i <= bits.length; i++) {
            System.out.printf("    rank1(%d) = %d%n", i, bv.rank1(i));
        }

        System.out.println();
        System.out.println("  Operação rank0(i) — número de ')' nos primeiros i bits:");
        for (int i = 1; i <= bits.length; i++) {
            System.out.printf("    rank0(%d) = %d%n", i, bv.rank0(i));
        }

        System.out.println();
        System.out.println("  Operação select1(j) — posição do j-ésimo '(':");
        for (int j = 1; j <= bv.totalOnes(); j++) {
            System.out.printf("    select1(%d) = %d%n", j, bv.select1(j));
        }

        System.out.println();
        System.out.println("  Profundidade via rank (depth(p) = 2·rank1(p) − p):");
        for (int p = 1; p <= bits.length; p++) {
            System.out.printf("    depth(%d) = %d  [bit='%c']%n",
                              p, 2 * bv.rank1(p) - p, bv.get(p-1) ? '(' : ')');
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Auxiliar: insere n elementos de forma a exercitar rotações AVL
    // ──────────────────────────────────────────────────────────────────────────
    static void insertBalanced(SuccinctAVLTree<Integer> tree, int lo, int hi) {
        if (lo > hi) return;
        int mid = (lo + hi) / 2;
        tree.insert(mid);
        insertBalanced(tree, lo, mid - 1);
        insertBalanced(tree, mid + 1, hi);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Auxiliar: separador visual entre seções
    // ──────────────────────────────────────────────────────────────────────────
    static void separador(String titulo) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("  " + titulo);
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println();
    }
}