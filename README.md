# Hidrate-se+ 💧📱

Aplicativo Android desenvolvido em Kotlin para auxiliar o usuário no controle da ingestão diária de água.
O app permite registrar consumos, acompanhar o progresso da meta diária, visualizar histórico, configurar lembretes e ajustar a meta de hidratação de forma manual ou calculada.

## 🎯 Objetivo do Projeto

O Hidrate-se+ tem como objetivo incentivar hábitos saudáveis de hidratação por meio de uma aplicação simples, intuitiva e funcional.

O aplicativo permite:

* Registrar rapidamente a quantidade de água ingerida em ml;
* Acompanhar o total consumido no dia;
* Visualizar o progresso em relação à meta diária;
* Consultar o histórico de consumo;
* Configurar lembretes de hidratação;
* Definir meta manual ou calcular a meta com base no peso do usuário;
* Salvar preferências e configurações do usuário.

## 🧰 Tecnologias, Linguagens e Ferramentas

### Linguagens

* Kotlin
* XML

### Plataforma / IDE

* Android Studio
* Gradle

### Principais componentes e bibliotecas

* Activities
* View Binding
* ViewModel
* Intent
* Room Database
* Firebase
* WorkManager
* SharedPreferences
* Permissão de notificações do Android

## 🧱 Arquitetura do Projeto

O projeto segue uma estrutura organizada por camadas e responsabilidades, separando telas, lógica de negócio, persistência de dados e configurações.

A organização atual do app utiliza:

* `ui`: telas e Activities do aplicativo;
* `viewmodel`: controle da lógica de apresentação;
* `data`: entidades, banco local e acesso aos dados;
* `worker`: gerenciamento dos lembretes em segundo plano;
* `util`: classes auxiliares e validações.

Essa separação facilita a manutenção do projeto, a evolução das funcionalidades e a correção de erros sem impactar diretamente outras partes do sistema.

## 💾 Persistência de Dados

O aplicativo utiliza o Room Database para armazenar localmente os registros de consumo de água, permitindo que o usuário consulte seu histórico mesmo sem conexão com a internet.

Além disso, algumas preferências do usuário são salvas com SharedPreferences, como configurações de meta, lembretes e frequência das notificações.

## 🔔 Notificações e Lembretes

O Hidrate-se+ utiliza o WorkManager para agendar lembretes periódicos de hidratação.

Em versões recentes do Android, o app também solicita a permissão de notificações ao usuário antes de ativar os lembretes, garantindo compatibilidade com as regras atuais da plataforma.

## ⚙️ Configurações de Meta

O usuário pode escolher entre dois tipos de meta:

* **Meta manual:** o usuário informa diretamente a quantidade desejada de água por dia;
* **Meta calculada:** o sistema calcula a meta com base no peso informado pelo usuário.

A lógica do app permite manter apenas um tipo de meta ativo por vez, evitando sobreposição entre a meta manual e a meta calculada.

## 📱 Principais Telas

* **Home:** exibe o consumo diário e o progresso da meta;
* **Histórico:** apresenta os registros de consumo;
* **Configurações:** permite ajustar metas, lembretes e preferências do usuário.

## 🚀 Status do Projeto

O projeto está em desenvolvimento acadêmico, com funcionalidades principais já implementadas, incluindo persistência local, histórico de consumo, configurações de meta e lembretes de hidratação.
