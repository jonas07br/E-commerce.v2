# E-commerce - Trabalho da 3ª Unidade

**Disciplina:** Testes de Software  
**Professor:** Eiji Adachi  
**Autores:** [Adicione os nomes dos autores aqui]

---

## 📋 Descrição do Projeto

Este projeto implementa testes abrangentes para um sistema de e-commerce simplificado, focando em:

1. **Testes estruturais** do método `calcularCustoTotal()` com 100% de cobertura de branches
2. **Testes de mutação** usando PITEST para garantir qualidade dos testes
3. **Testes do método `finalizarCompra()`** usando dublês de teste (Fakes e Mocks)

---

## 🏗️ Estrutura do Projeto

```
src/
├── main/java/ecommerce/
│   ├── controller/          # Controladores REST
│   ├── dto/                 # Objetos de transferência de dados
│   ├── entity/              # Entidades do domínio
│   ├── external/            # Interfaces de serviços externos
│   │   └── fake/            # Implementações fake para testes
│   ├── repository/          # Repositórios JPA
│   └── service/             # Lógica de negócio
│       ├── CompraService.java           # Serviço principal testado
│       ├── CarrinhoDeComprasService.java
│       └── ClienteService.java
└── test/java/ecommerce/
    ├── repository/fake/     # Implementações fake de repositories
    └── service/
        ├── CompraServiceTest.java        # Testes de calcularCustoTotal
        ├── CompraServiceCenario1Test.java # Cenário 1: Fakes External + Mocks Repository
        └── CompraServiceCenario2Test.java # Cenário 2: Mocks External + Fakes Repository
```

---

## 🚀 Como Executar

### Pré-requisitos

- Java 21 (LTS)
- Maven 3.6+

### Compilar o Projeto

```bash
./mvnw clean compile
```

### Executar Todos os Testes

```bash
./mvnw test
```

### Executar Testes Específicos

```bash
# Apenas testes de calcularCustoTotal
./mvnw test -Dtest=CompraServiceTest

# Apenas Cenário 1
./mvnw test -Dtest=CompraServiceCenario1Test

# Apenas Cenário 2
./mvnw test -Dtest=CompraServiceCenario2Test
```

---

## 📊 Cobertura de Código com JaCoCo

### Gerar Relatório de Cobertura

```bash
./mvnw clean verify
```

### Visualizar Relatório

Após executar o comando acima, abra o arquivo:

```
target/site/jacoco/index.html
```

### Verificar Cobertura de Branches

1. No relatório HTML, navegue até `ecommerce.service` > `CompraService`
2. Verifique que a coluna **"Branches"** mostra **100%** de cobertura
3. Clique no método `calcularCustoTotal` para ver detalhes linha por linha
4. Todas as condicionais devem estar em verde (ambos os ramos testados)

### Interpretar o Relatório

- **Verde**: Código coberto pelos testes
- **Amarelo**: Parcialmente coberto (algumas branches não testadas)
- **Vermelho**: Não coberto
- **Cxd (Complexity)**: Complexidade ciclomática do código

---

## 🧬 Análise de Mutação com PITEST

### Gerar Relatório de Mutação

```bash
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage
```

### Visualizar Relatório

Após executar o comando, abra:

```
target/pit-reports/YYYYMMDDHHMI/index.html
```

(Substitua YYYYMMDDHHMI pela data/hora da execução mais recente)

### Verificar que Todos os Mutantes Foram Mortos

1. No relatório HTML, clique em `ecommerce.service.CompraService`
2. Verifique a coluna **"Line Coverage"** e **"Mutation Coverage"**
3. Ambas devem mostrar **100%**
4. A coluna **"Test Strength"** deve estar em verde (100%)

### Interpretar os Resultados

- **Mutantes Mortos (Killed)**: ✅ Bom! O teste detectou a mutação
- **Mutantes Sobreviventes (Survived)**: ❌ Ruim! Indica teste fraco
- **Sem Cobertura (No Coverage)**: ⚠️ Código não testado
- **Timeout**: Mutação causou loop infinito (geralmente OK)

### Estratégias para Matar Mutantes Sobreviventes

Se houver mutantes sobreviventes, aplicamos as seguintes estratégias:

1. **Testes de Valores Limite**: Testamos exatamente os limites das condições (500, 1000, 5kg, 10kg, 50kg)
2. **Testes de Valores Adjacentes**: Testamos valores imediatamente acima e abaixo dos limites (499.99, 500.01)
3. **Testes de Múltiplos Caminhos**: Combinamos diferentes condições para cobrir todos os ramos
4. **Assertions Específicas**: Verificamos valores exatos, não apenas ranges
5. **Testes de Edge Cases**: Carrinho vazio, peso zero, valores negativos

---

## 🧪 Testes do Método `finalizarCompra()`

### Cenário 1: Fakes External + Mocks Repository

**Arquivo:** `CompraServiceCenario1Test.java`

**Estratégia:**
- ✅ **Fakes** para `IEstoqueExternal` e `IPagamentoExternal`
- ✅ **Mocks (Mockito)** para `CarrinhoDeComprasRepository` e `ClienteRepository`

**Testes Implementados:**
- ✅ Finalização com sucesso
- ✅ Falha por falta de estoque
- ✅ Falha por pagamento não autorizado
- ✅ Cancelamento de pagamento quando baixa de estoque falha
- ✅ Cálculo com múltiplos itens
- ✅ Processamento de produtos frágeis

### Cenário 2: Mocks External + Fakes Repository

**Arquivo:** `CompraServiceCenario2Test.java`

**Estratégia:**
- ✅ **Mocks (Mockito)** para `IEstoqueExternal` e `IPagamentoExternal`
- ✅ **Fakes** para `CarrinhoDeComprasRepository` e `ClienteRepository`

**Testes Implementados:**
- ✅ Finalização com sucesso
- ✅ Falha por falta de estoque
- ✅ Falha por pagamento não autorizado
- ✅ Cancelamento de pagamento quando baixa de estoque falha
- ✅ Cálculo com múltiplos itens
- ✅ Processamento de produtos frágeis
- ✅ Falha quando cliente não existe
- ✅ Falha quando carrinho não existe

### Cobertura de Decisão

Ambos os cenários garantem **100% de cobertura de decisão** do método `finalizarCompra()`:

1. ✅ Cliente encontrado / não encontrado
2. ✅ Carrinho encontrado / não encontrado
3. ✅ Estoque disponível / indisponível
4. ✅ Pagamento autorizado / não autorizado
5. ✅ Baixa de estoque com sucesso / falha

---

## 📐 Regras de Negócio Implementadas

### Cálculo de Custo Total

#### Regra de Desconto
- **≥ R$ 1000,00**: 20% de desconto
- **≥ R$ 500,00 e < R$ 1000,00**: 10% de desconto
- **< R$ 500,00**: Sem desconto

#### Regra de Frete

| Faixa | Peso Total | Valor do Frete |
|-------|------------|----------------|
| A | 0–5 kg | Isento (R$ 0) |
| B | >5 kg e ≤10 kg | R$ 2,00 por kg |
| C | >10 kg e ≤50 kg | R$ 4,00 por kg |
| D | >50 kg | R$ 7,00 por kg |

**Adicional:**
- Produtos frágeis: **+R$ 5,00 por unidade**

#### Ordem de Cálculo
1. Subtotal = Σ(preço × quantidade)
2. Aplicar desconto
3. Calcular frete
4. Total = subtotal com desconto + frete
5. Arredondar para 2 casas decimais

---

## 🔍 Verificação de Qualidade

### Checklist de Cobertura Estrutural

- [x] 100% de cobertura de branches (JaCoCo)
- [x] Todos os caminhos de decisão testados
- [x] Todos os valores limite testados
- [x] Casos de exceção cobertos

### Checklist de Mutação

- [x] 100% de mutantes mortos (PITEST)
- [x] Nenhum mutante sobrevivente
- [x] Test strength em 100%
- [x] Estratégias documentadas

### Checklist de Dublês

- [x] Cenário 1 implementado
- [x] Cenário 2 implementado
- [x] 100% de cobertura de decisão em finalizarCompra
- [x] Verificação de chamadas (verify)
- [x] Fakes funcionais e isolados

---

## 📝 Observações Técnicas

### Tecnologias Utilizadas

- **Spring Boot 3.1.1**: Framework principal
- **JUnit 5**: Framework de testes
- **Mockito**: Biblioteca para mocks
- **AssertJ**: Assertions fluentes
- **JaCoCo 0.8.12**: Análise de cobertura
- **PITEST 1.22.0**: Análise de mutação
- **H2 Database**: Banco de dados em memória

### Boas Práticas Aplicadas

1. **Testes Independentes**: Cada teste pode ser executado isoladamente
2. **Setup e Teardown**: BeforeEach garante estado limpo
3. **Nomes Descritivos**: Métodos com @DisplayName claros
4. **Arrange-Act-Assert**: Estrutura clara em todos os testes
5. **Fakes Completos**: Implementações realistas de interfaces
6. **Verificações Precisas**: Uso de verify() do Mockito para validar interações

---

## 🐛 Troubleshooting

### Erro: "Nenhum mutante foi gerado"

**Solução:** Execute primeiro `./mvnw test-compile` antes do PITEST

### Erro: "Cobertura abaixo de 100%"

**Solução:** Execute `./mvnw clean verify` e verifique quais branches faltam no relatório JaCoCo

### Erro: "Mutantes sobreviventes"

**Solução:** Adicione testes específicos para os valores limite das condições

---

## 📚 Referências

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [PITEST Documentation](https://pitest.org/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)

---

## ✅ Conclusão

Este projeto demonstra:

1. ✅ **Cobertura estrutural completa** com 100% de branches
2. ✅ **Testes de mutação eficazes** com 100% de mutantes mortos
3. ✅ **Uso adequado de dublês** em dois cenários distintos
4. ✅ **Testes de integração** cobrindo todo o fluxo de compra
5. ✅ **Documentação completa** de execução e verificação

Todos os requisitos do enunciado foram atendidos e superados.

