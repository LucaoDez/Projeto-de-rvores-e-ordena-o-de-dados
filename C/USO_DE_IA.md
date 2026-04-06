## Ferramentas utilizadas
- Claude (Anthropic) — via Claude.ai e Claude Code

## Etapas em que a IA foi utilizada

### Código (C)
A IA foi utilizada para traduzir a implementação de referência
do artigo de Java para C, gerando os arquivos:
succinct_bit_vector, succinct_avl_tree, avl_tree, avl_node,
ram_monitor e main.c.

O grupo forneceu como entrada: a descrição das estruturas do
artigo, a arquitetura modular desejada (.h + .c por módulo)
e os requisitos das 4 demonstrações (árvore pequena, escala,
contraexemplo e buscas).

### O que o grupo revisou e validou
- Conferência dos cálculos do Demo 3 (contraexemplo): os
  valores de 192 bits/nó (clássica) e 4 bits/nó (sucinta)
  foram verificados manualmente pelo grupo com base na
  struct AVLNode do código gerado.
- Conferência dos resultados numéricos: 21.845 nós (clássica)
  e 1.048.576 nós (sucinta) para 512 KB de RAM.
- Leitura e validação da lógica de rank/select no arquivo
  succinct_bit_vector.c.
- Identificação do erro no README.md (que dizia 10.922 nós
  em vez de 21.845).

### O que foi decidido exclusivamente pelo grupo
- Escolha do artigo científico.
- Definição do cenário do contraexemplo (dispositivo com
  512 KB de RAM).
- Escolha dos valores comparados (192 bits vs 4 bits/nó)
  e da métrica de aumento percentual (~4.700%).
- Redação da síntese técnica e da crítica final.
- Estrutura e argumentação da apresentação oral.

## O que a IA não substitui
O grupo é responsável por explicar oralmente qualquer parte
do código ou do artigo sem apoio dos slides, incluindo:
rotações AVL, funcionamento de rank/select, e por que a
representação por parênteses balanceados funciona.