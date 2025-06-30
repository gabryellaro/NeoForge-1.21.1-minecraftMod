
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

Após a conclusão bem-sucedida do build, clique em **"Current File"** e selecione **"Client"**.  
Em seguida, clique no botão à direita que contém **Run Client** e aguarde o jogo abrir.

> Com o jogo iniciado corretamente, você pode prosseguir para os próximos passos.

## 📦 Utilizando o Drone

Para testar o drone no jogo, siga as instruções abaixo:

### 1. Criando um mundo

Crie um novo mundo no modo **Criativo** com cheats ativados e tipo de terreno **flat** (plano). Esse modo permite que você tenha acesso livre aos itens e blocos, além de poder usar comandos para facilitar os testes com o drone.

Siga estes passos para configurar o mundo:

- Na tela inicial do Minecraft, clique em **Singleplayer**.
- Clique em **Create New World**.
- Defina o nome do mundo, por exemplo, `DroneTestWorld`.
- Escolha o **Modo Criativo**.
- Ative a opção **Allow Cheats: ON**.
- Clique em **More World Options...**.
- No campo **World Type**, selecione **Superflat**.
- (Opcional) Clique em **Customize** para ajustar o terreno plano, caso deseje.
- Clique em **Create New World** para iniciar o mundo.

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

### 🌐 Importando o Mundo de Teste

Utilize o arquivo **MapaTeste.zip**, disponível neste repositório, para carregar o ambiente com configurações de automatização já preparadas.

Após executar o ambiente pela primeira vez, siga os passos abaixo:

1. Navegue até o diretório `run/saves` no seu sistema.
2. Extraia o conteúdo de **MapaTeste.zip** dentro dessa pasta.
3. Reinicie o Minecraft (se estiver aberto).

O mundo estará disponível na lista de mundos com o nome correspondente, pronto para uso nos testes com o drone.
