# AVL Tree Sucinta em C

Tradução completa do projeto Java "Codificação Sucinta de Árvores AVL" para **C** com medição real de RAM e estrutura modular (.h + .c por módulo).

## 📁 Estrutura de Arquivos

```
C/
├── avl_node.h / avl_node.c              # Nó básico da árvore AVL
├── avl_tree.h / avl_tree.c              # Árvore AVL clássica com ponteiros
├── succinct_bit_vector.h / .c           # Vetor de bits sucinto (rank/select)
├── succinct_avl_tree.h / .c             # Árvore AVL com codificação sucinta
├── ram_monitor.h / ram_monitor.c        # Monitoramento de RAM
├── main.c                               # Programa principal com demonstrações
├── Makefile                             # Script de compilação
└── README.md                            # Este arquivo
```

## 🔧 Compilação

### Pré-requisitos

- GCC ou Clang
- Make
- Linux (para leitura de RSS via `/proc/self/status`)

### Compilar

```bash
cd C
make clean
make
```

### Executar

```bash
make run
# ou
./bin/avl_succinct
```

## 📊 Demonstrações Incluídas

### DEMO 1: Árvore Pequena

- Construção de AVL clássica e sucinta com 10 elementos
- Comparação de espaço em memória
- Exibição em in-ordem (ordenada)

### DEMO 2: Experimento de Escala

- Tabela com diferentes tamanhos de árvore (n = 7, 15, 31, ..., 1023)
- Medição de bytes para clássica vs. sucinta
- Cálculo de bits/nó e redução percentual

### DEMO 3: Contraexemplo Didático

- Cenário: Dispositivo embarcado com 512 KB de RAM
- Cálculo de máximo de nós que cabem em cada representação
- Demonstração de quando AVL clássica falha

### DEMO 4: Operações

- Buscas em ambas as representações
- Exibição de fatores de balanceamento

## 📈 Resultados Esperados

### Comparação de Espaço (10 nós)

```
AVL Clássica:  ~240 bytes  (3 ponteiros × 64 bits/nó)
AVL Sucinta:   ~80 bytes   (2 bits BP + estruturas auxiliares)
Redução:       ~67%
```

### Limite Teórico (do artigo)

- **Estrutura pura (sem valores)**: ~0.938 bits/nó
- **Com BP + balance**: ~2 bits/nó (implementação didática)
- **Vs. ponteiros naivos**: redução de ~99%

### Para Dispositivo com 512 KB

```
AVL Clássica:   ~10,922 nós antes de falhar
AVL Sucinta:   ~1,048,576 nós (cabe toda a memória!)
```

## 🔍 Modularidade

Cada componente é independente e reutilizável:

### avl_node.h/c

- Estrutura simples do nó
- Getters/setters para valor, balance factor e ponteiros

### avl_tree.h/c

- Implementação clássica com ponteiros left/right
- Rotações (simples e dupla)
- Inserção com balanceamento automático

### succinct_bit_vector.h/c

- Vetor de bits com compressão
- rank1(i), rank0(i) em O(1) com pré-computação
- select1(j) via busca binária
- Memory-efficient: usa popcount 64-bit

### succinct_avl_tree.h/c

- Integra AVLTree + SuccinctBitVector
- Codificação sucinta via balanced parentheses (BP)
- Navigação O(log n) em representação comprimida
- Estatísticas de compressão vs. limite teórico

### ram_monitor.h/c

- Leitura de RSS via `/proc/self/status` (Linux)
- Rastreamento de pico de memória
- Relatório formatado em UTF-8

## 🧪 Características

✅ **Tradução fiel** do código Java
✅ **Medição real** de memória (RSS do SO)
✅ **Comparação direta** AVL clássica vs. sucinta
✅ **Estrutura modular** profissional (.h + .c)
✅ **Compilação padronizada** com Makefile
✅ **Documentação** comentada no código
✅ **Implementação de rank/select** O(1)
✅ **Superblocos** para estrutura auxiliar eficiente

## 📖 Referência Teórica

Baseado no artigo:

> **"Succinct encodings of binary trees with application to AVL trees"**
> Theoretical Computer Science, 2025
> pii: S030439752500475X

**Resultado principal**: Uma árvore AVL de n nós requer apenas ~0.938 bits/nó para codificar a estrutura (limite de informação).

---

**Autor da tradução**: Claude Code
**Linguagem original**: Java
**Tradução em**: C (ANSI/ISO C99)
