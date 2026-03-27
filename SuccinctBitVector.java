/**
 * SuccinctBitVector - Vetor de bits sucinto com suporte a rank/select em O(1).
 *
 * Base teórica:
 * O artigo "Succinct encodings of binary trees with application to AVL trees"
 * (Theoretical Computer Science, 2025) utiliza decomposição em blocos para criar
 * estruturas de dados que suportam rank e select em tempo constante sobre sequências
 * de bits. Esta estrutura é a fundação para a codificação sucinta de árvores AVL.
 *
 * Complexidade espacial: n + o(n) bits (sucinto)
 * rank1/rank0 em O(1)
 * select em O(log n) amortizado via busca binária sobre superblocks
 */
public class SuccinctBitVector {

    // --- Estrutura interna ---
    private final long[] words;         // Bits compactados em palavras de 64 bits
    private final int    length;        // Número de bits lógicos

    // Estruturas auxiliares de rank (espaço sub-linear o(n))
    private final int[] superBlockRank; // rank acumulado a cada SUPER_BLOCK bits
    private final int[] blockRank;      // rank incremental a cada BLOCK bits dentro do superbloco

    // Parâmetros dos blocos (podem ser ajustados para n grande)
    static final int BLOCK_SIZE       = 64;   // bits por bloco (1 palavra de 64 bits)
    static final int SUPER_BLOCK_SIZE = 2048; // bits por superboco (32 blocos)

    /**
     * Constrói o SuccinctBitVector a partir de um array de booleanos.
     *
     * @param bits array onde true = 1, false = 0
     */
    public SuccinctBitVector(boolean[] bits) {
        this.length = bits.length;
        int numWords = (length + 63) / 64;
        this.words   = new long[numWords];

        // Preenche as palavras
        for (int i = 0; i < length; i++) {
            if (bits[i]) {
                words[i / 64] |= (1L << (63 - (i % 64)));
            }
        }

        // Pré-computa rank
        int numSuperBlocks = (length + SUPER_BLOCK_SIZE - 1) / SUPER_BLOCK_SIZE;
        int numBlocks      = (length + BLOCK_SIZE - 1) / BLOCK_SIZE;

        this.superBlockRank = new int[numSuperBlocks + 1];
        this.blockRank      = new int[numBlocks + 1];

        int cumulative = 0;
        for (int i = 0; i < numBlocks; i++) {
            if (i % (SUPER_BLOCK_SIZE / BLOCK_SIZE) == 0) {
                superBlockRank[i / (SUPER_BLOCK_SIZE / BLOCK_SIZE)] = cumulative;
            }
            blockRank[i] = cumulative - superBlockRank[i / (SUPER_BLOCK_SIZE / BLOCK_SIZE)];
            cumulative  += Long.bitCount(words[i]);
        }
        superBlockRank[numSuperBlocks] = cumulative;
    }

    /**
     * rank1(i) - número de 1s em bits[0..i-1] (prefixo de tamanho i).
     * Implementado em O(1) via superbloco + bloco + popcount da palavra parcial.
     *
     * @param i posição (0 ≤ i ≤ length)
     * @return quantidade de 1s nos primeiros i bits
     */
    public int rank1(int i) {
        if (i <= 0) return 0;
        if (i > length) i = length;

        int blockIdx      = (i - 1) / BLOCK_SIZE;
        int superBlockIdx = blockIdx / (SUPER_BLOCK_SIZE / BLOCK_SIZE);
        int bitInBlock    = i % BLOCK_SIZE; // bits a contar na palavra parcial

        long mask = (bitInBlock == 0)
                ? words[blockIdx]
                : (words[blockIdx] >>> (BLOCK_SIZE - bitInBlock)) << (BLOCK_SIZE - bitInBlock);

        return superBlockRank[superBlockIdx]
             + blockRank[blockIdx]
             + Long.bitCount(mask);
    }

    /**
     * rank0(i) - número de 0s em bits[0..i-1].
     */
    public int rank0(int i) {
        return i - rank1(i);
    }

    /**
     * select1(j) - posição (1-indexada) do j-ésimo 1 no vetor.
     * Implementado via busca binária sobre superBlockRank + scan fino.
     *
     * @param j índice do 1 desejado (1-indexado)
     * @return posição 1-indexada do j-ésimo bit 1; -1 se não existir
     */
    public int select1(int j) {
        if (j <= 0 || j > rank1(length)) return -1;

        // Busca binária sobre superBlocos
        int lo = 0, hi = superBlockRank.length - 1;
        while (lo < hi - 1) {
            int mid = (lo + hi) / 2;
            if (superBlockRank[mid] < j) lo = mid; else hi = mid;
        }
        int superStart = lo * (SUPER_BLOCK_SIZE / BLOCK_SIZE);
        int remaining  = j - superBlockRank[lo];

        // Scan linear nos blocos do superboco
        int blocksPerSuper = SUPER_BLOCK_SIZE / BLOCK_SIZE;
        int blockIdx = superStart;
        while (blockIdx < superStart + blocksPerSuper && blockIdx < words.length) {
            int cnt = Long.bitCount(words[blockIdx]);
            if (blockRank[blockIdx] + cnt >= remaining) break;
            blockIdx++;
        }
        remaining -= blockRank[blockIdx];

        // Scan bit a bit dentro da palavra
        long w = words[blockIdx];
        for (int bit = 0; bit < BLOCK_SIZE; bit++) {
            if ((w & (1L << (63 - bit))) != 0) {
                remaining--;
                if (remaining == 0) return blockIdx * BLOCK_SIZE + bit + 1;
            }
        }
        return -1;
    }

    /** Retorna o bit na posição i (0-indexado). */
    public boolean get(int i) {
        return (words[i / 64] & (1L << (63 - (i % 64)))) != 0;
    }

    /** Número total de bits. */
    public int length() { return length; }

    /** Total de 1s no vetor. */
    public int totalOnes() { return rank1(length); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(length, 80); i++) sb.append(get(i) ? '1' : '0');
        if (length > 80) sb.append("...");
        return sb.toString();
    }
}