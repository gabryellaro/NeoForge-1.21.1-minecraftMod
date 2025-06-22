
# 🛸 Drone Mod (Minecraft)

Um mod para Minecraft que adiciona drones inteligentes com movimentação autônoma baseado no simulador para drones DragonFly.

## ✅ Pré-requisitos

Antes de começar, você precisará ter o seguinte instalado na sua máquina:

- [Java 21 JDK](https://jdk.java.net/21/) — necessário para compilar e rodar o projeto
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) — recomendado para desenvolvimento (com suporte a Gradle e plugins Minecraft)

> Certifique-se de configurar o IntelliJ para usar o JDK 21 como SDK do projeto.

## 📦 Instalação

### 1. Clone o repositório

```bash
git clone 
cd seu-repositorio
``` 

## 2. Build e Possíveis Soluções

Para compilar o projeto, utilize a opção de build no IntelliJ e aguarde a conclusão do processo.  
Além disso, certifique-se de abrir a aba do Gradle (geralmente à direita), clicar com o botão direito no projeto e selecionar **"Download Sources"** para garantir que todas as dependências estejam corretamente resolvidas. 

> Após a conclusão bem-sucedida do build, você pode seguir para os próximos passos.

## 📦 Utilizando o Drone

Para testar o drone no jogo, siga as instruções abaixo:

### 1. Criando um mundo

Abra o Minecraft e crie um novo mundo no modo **Criativo**. Esse modo permite que você tenha acesso livre aos itens e blocos, o que facilita para testar o drone.

### 2. Pegando o drone

No jogo, abra o inventário (tecla padrão: `E`) e clique no ícone da **bússola** (guia de busca). Digite **"drone"** e selecione o item encontrado.  
Em seguida, clique com o **botão direito do mouse** em qualquer bloco no chão para **spawnar** (invocar) o drone no mundo.

### 3. Comandos disponíveis

Com o drone já no mundo, você pode controlar seu comportamento usando comandos no **chat do jogo** (tecla padrão: `T`). Veja os comandos disponíveis:

#### 🔧 Controle do Drone

- `/drone move <x> <y> <z>`  
  Move o drone para as coordenadas especificadas (substitua `<x>`, `<y>`, `<z>` por números).  
  > Para descobrir as coordenadas do local desejado, pressione **F3** no teclado. As coordenadas atuais do jogador aparecerão no canto superior esquerdo da tela.

- `/drone set battery <valor>`  
  Define o nível atual da bateria do drone (0–100).

- `/drone set batteryCapacity <valor>`  
  Define a capacidade máxima de bateria (padrão é 100).

#### 🌬️ Sistema de vento

- `/wind set <direction> <strength>`  
  Define manualmente a direção e força do vento.  
  Exemplos:
  - `/wind set north 0.8`
  - `/wind set south 0.3`

- `/wind random`  
  Gera uma direção e força de vento aleatórias.







Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/
