Aqui está o **enunciado inteiro convertido para Markdown**, organizado e formatado:

---

# **Trabalho da 3ª Unidade – Testes de Mutação e Testes com Dublês (Fakes e Mocks)**

**Disciplina:** Testes de Software
**Professor:** Eiji Adachi

---

## **1. Contexto**

Este trabalho é uma continuação direta do enunciado da 2ª unidade, no qual você testou o método `calcularCustoTotal()` de uma aplicação de e-commerce.

Nesta etapa, você deverá:

* Testar uma **versão simplificada** do cálculo do custo total.
* Criar testes para o método `finalizarCompra()`, utilizando **fakes e mocks** para simular dependências.

O trabalho pode ser realizado individualmente ou em dupla.

---

## **2. Método Simplificado `calcularCustoTotal()`**

Você deverá implementar novamente o método de cálculo de custo total, agora numa versão reduzida seguindo as regras abaixo.

---

### **2.1 Regra de Desconto**

* Se **valor total ≥ R$ 1000,00** → aplicar **20% de desconto**
* Se **valor total ≥ R$ 500,00 e < R$ 1000,00** → aplicar **10% de desconto**
* Outros valores → **sem desconto**

---

### **2.2 Regra de Frete**

O frete deve ser calculado com base apenas no **peso físico total**, seguindo a tabela:

| Faixa | Peso total      | Valor do frete      |
| ----- | --------------- | ------------------- |
| A     | 0–5 kg          | Frete isento (R$ 0) |
| B     | >5 kg e ≤10 kg  | R$ 2,00 por kg      |
| C     | >10 kg e ≤50 kg | R$ 4,00 por kg      |
| D     | >50 kg          | R$ 7,00 por kg      |

**Observações importantes:**

* Produtos **frágeis**: adicionar **R$ 5,00 por unidade frágil**.
* Não há adicional por região.
* Não há desconto por fidelidade (Ouro/Prata/Bronze).

---

### **2.3 Ordem de Cálculo**

1. **Subtotal** = soma de (preço unitário × quantidade).
2. Aplicar o **desconto**.
3. Calcular o **frete**.
4. **Total** = subtotalComDesconto + frete.
5. O valor final deve ser **arredondado para duas casas decimais**.

---

## **3. Testes Obrigatórios para `calcularCustoTotal()`**

### **3.1 Cobertura Estrutural**

Obrigatório atingir:

* **100% de cobertura de arestas (branch coverage)**
* O relatório do **JaCoCo** deve comprovar que **todas as arestas foram cobertas**.

---

### **3.2 Mutação**

Obrigatório:

* Utilizar **PITEST** para análise de mutação.
* Atingir **100% de mutantes mortos** no método.

O README deve documentar:

* Linha de comando utilizada.
* Como verificar que não restaram mutantes sobreviventes.
* Estratégias usadas para matar mutantes sobreviventes.

---

## **4. Testes do Método `finalizarCompra()`**

Além dos testes estruturais, criar testes que simulem todo o comportamento usando dublês adequados.

### **4.1 Objetivo do Teste**

O teste deve cobrir todo o fluxo:

1. Verificação de estoque via serviço externo.
2. Cálculo do custo total.
3. Autorização de pagamento.
4. Atualização do estoque e finalização da compra.

Os testes devem garantir:

* **100% de cobertura de decisão** do método.
* Verificação de chamadas esperadas (**métodos invocados**).
* Resultado retornado de acordo com a especificação.

---

### **4.2 Cenários de Teste**

Você deve criar **dois cenários**, cada um em um arquivo `.java` distinto.

---

#### **Cenário 1**

* Criar **fakes** para simular:

    * `IEstoqueExternal`
    * `IPagamentoExternal`
* As demais dependências devem ser simuladas usando **mocks com Mockito**.

---

#### **Cenário 2**

* Criar **mocks** para:

    * `IEstoqueExternal`
    * `IPagamentoExternal`
* Usar **fakes** para simular as dependências da camada **repository**.

---

## **5. Entrega**

A entrega deve conter:

* Projeto Maven em **formato ZIP**

* Nome do projeto e `artifactId` no formato:
  **nome1-nome2**

* Arquivo **README.md** contendo:

    * Nome dos autores
    * Instruções de execução
    * Como rodar os testes
    * Como visualizar relatórios de cobertura
    * Como gerar e interpretar o relatório de mutação

---
