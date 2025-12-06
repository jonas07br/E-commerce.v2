#To Do

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
