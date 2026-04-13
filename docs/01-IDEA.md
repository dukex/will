# **Arquitetura e Engenharia de Sistemas para Lançadores Android Centrados em Tarefas: O Projeto Will**

A evolução das interfaces de usuário em sistemas operacionais móveis tem sido marcada por uma transição gradual de ambientes puramente operacionais para ecossistemas de assistência proativa. No centro dessa evolução encontra-se o "Launcher" ou "Home App", o componente de software que define a experiência primária de interação entre o usuário e o hardware.1 O projeto **Will** propõe uma reengenharia desses sistemas para um modelo centrado em tarefas, onde a interface atua como um orquestrador inteligente de intenções, unificando fluxos passivos e ativos em uma arquitetura totalmente rastreável.

## **Arquitetura de Sistema e Fundamentos de Desenvolvimento**

O desenvolvimento de um lançador customizado no Android não é tratado pelo sistema como uma aplicação comum, mas como a substituição de um componente fundamental da interface de usuário do sistema (System UI).1 Para que o Android reconheça o **Will** como um lançador, o manifesto (AndroidManifest.xml) deve declarar filtros de intenção específicos para a atividade principal. A inclusão de android.intent.category.HOME e android.intent.category.DEFAULT dentro do elemento \<intent-filter\> é o mecanismo técnico essencial .

### **Configuração de Manifesto e Gerenciamento de Tasks**

A estabilidade do **Will** depende criticamente de como ele gerencia sua pilha de atividades. O uso do atributo android:launchMode="singleTask" é imperativo para garantir que o sistema mantenha apenas uma instância da atividade principal, limpando qualquer outra atividade da sua tarefa ao retornar à Home .

| Atributo de Manifesto | Valor Recomendado | Justificativa Técnica |
| :---- | :---- | :---- |
| android:launchMode | singleTask | Garante uma única instância e comportamento previsível do botão Home . |
| android:taskAffinity | "" (vazio) | Isola o lançador de outras aplicações, evitando confusão na pilha de recentes . |
| android:clearTaskOnLaunch | true | Garante que o lançador retorne ao estado inicial ao ser invocado.3 |
| android:configChanges | orientation, screenSize | Evita a destruição da atividade durante mudanças de layout, melhorando a performance.5 |

## **O Paradigma do Orquestrador de Intenções: Tudo é Task**

O conceito central do **Will** inverte a hierarquia tradicional. Em vez de ser um ponto de partida para abrir aplicativos, ele se torna um terminal de execução de intenções . Neste modelo, todas as entradas do smartphone são processadas e convertidas em objetos de dado do tipo **Task**.

### **1\. Conversão de Entradas Passivas (Notificações)**

Através do NotificationListenerService, as notificações são interceptadas e desserializadas . O lançador extrai metadados como título, texto e, fundamentalmente, as PendingIntents associadas. Isso permite que um alerta de mensagem se transforme na tarefa "Responder Contato", mantendo o gatilho de execução pronto no feed principal sem que o usuário precise navegar pela gaveta de notificações .

### **2\. Mapeamento de Intenções Ativas via NLP**

Para intenções criadas ativamente (ex: "pagar boleto", "mandar mensagem para minha mãe"), o sistema utiliza processamento de linguagem natural (NLP) local . O processo envolve:

* **Tokenização e NER (Named Entity Recognition):** Identificação de entidades como nomes de contatos, valores monetários ou horários .  
* **Mapeamento Semântico:** Classificação probabilística para decidir se o comando exige uma ação interna (lembrete) ou externa (despacho para outro app via deep link) .

## **Rastreabilidade Total: O Laboratório de Performance Pessoal**

Ao transformar toda interação em uma **Task**, o **Will** habilita uma camada de monitoramento que permite quantificar o comportamento digital do usuário . Esta arquitetura de "Quantified Self" utiliza o banco de dados **Room** para persistência local e auditável .

### **Schema de Rastreamento e Métricas**

Cada objeto Task possui campos granulares para medir a eficiência real do fluxo de trabalho:

* createdAt: Timestamp da recepção da notificação ou criação manual.  
* startedAt: Início da interação efetiva com a tarefa.  
* completedAt: Momento da conclusão detectada pelo sistema.  
* contextSwitchCount: Contador de alternâncias entre apps antes da finalização.

### **Monitoramento de Ciclo de Vida via Accessibility Service**

A conclusão real de tarefas em aplicativos de terceiros é monitorada através do AccessibilityService . Este serviço permite que o lançador "leia" a hierarquia de visualização (UI Tree) de outros apps e identifique sinais de sucesso (ex: o surgimento de um botão "Enviado" ou texto "Sucesso") . Ao detectar o evento, o **Will** marca automaticamente a tarefa como concluída, fechando o ciclo sem intervenção manual.

| Métrica de Produtividade | Implementação Técnica | Impacto na Saúde Digital |
| :---- | :---- | :---- |
| **Time-on-Task** | Diferença entre startedAt e completedAt no Room . | Identifica fluxos que geram procrastinação . |
| **Carga Estranha** | Medição de etapas salvas via modelo GOMS . | Quantifica o ganho de foco ao pular menus . |
| **Score de Foco** | Dashboard visual via ComposeCharts . | Visualização reativa do progresso diário . |

## **Engenharia Visual e Feed Reativo**

Para maximizar a produtividade, o **Will** implementa uma "interação sem ícones".6 A interface é composta por uma stream vertical de texto e widgets informativos, reduzindo os gatilhos de dopamina baseados em cores e badges .

### **Streams de Dados Reativos**

A arquitetura utiliza StateFlow ou SharedFlow para propagar as mudanças de estado do banco de dados Room diretamente para a UI de forma eficiente . Isso garante que o feed de tarefas reflita instantaneamente novas notificações ou mudanças de prioridade de calendário capturadas via CalendarContract .

## **Integrações Profundas e Deep Linking**

O despacho do usuário para a funcionalidade correta utiliza Deep Links e App Actions, pulando telas iniciais irrelevantes .

* **Identificação do Alvo:** Busca por padrões (como códigos de barras no clipboard para boletos ou nomes de contatos em APIs de sistema) .  
* **Execução Direta:** Envio de intenções ACTION\_VIEW com esquemas específicos (ex: whatsapp://send ou esquemas bancários verificados como itauapp://) .

## **Segurança e Privacidade**

O acesso a serviços de acessibilidade e notificações confere permissões sensíveis . O **Will** opera sob o princípio de **privilégio mínimo**: os dados de rastreamento são processados exclusivamente *on-device* e nunca deixam o dispositivo . O Android exige que o propósito dessas automações seja explicitamente declarado ao usuário e revisado pela Play Store .

## **Conclusões sobre a Engenharia do Will**

O desenvolvimento de um lançador como o **Will** transforma o smartphone de um repositório de distrações em um assistente executivo digital de alta performance. Ao unificar notificações e comandos ativos em tarefas rastreáveis, o sistema elimina a carga cognitiva estranha e fornece métricas reais para a melhoria de hábitos . A viabilidade técnica reside na integração de APIs de acessibilidade para fechamento de ciclo e bancos de dados reativos, pavimentando o caminho para uma interface invisível, onde a tecnologia serve estritamente à vontade (**Will**) e à intenção do usuário.

#### **Works cited**

1. Custom Android Launcher — Why and How do I build one? | by Viktor Mukha | Paradox Cat Tech Hub | Medium, accessed April 13, 2026, [https://medium.com/paradox-cat-tech-hub/custom-android-launcher-why-and-how-do-i-build-one-6a1b3af89d43](https://medium.com/paradox-cat-tech-hub/custom-android-launcher-why-and-how-do-i-build-one-6a1b3af89d43)  
2. Beyond the Grid: Crafting a Custom Android Launcher from Scratch | PDF \- Slideshare, accessed April 13, 2026, [https://www.slideshare.net/slideshow/beyond-the-grid-crafting-a-custom-android-launcher-from-scratch/284719966](https://www.slideshare.net/slideshow/beyond-the-grid-crafting-a-custom-android-launcher-from-scratch/284719966)  
3. android \- How to make a launcher \- Stack Overflow, accessed April 13, 2026, [https://stackoverflow.com/questions/4841686/how-to-make-a-launcher](https://stackoverflow.com/questions/4841686/how-to-make-a-launcher)  
4. android \- UPI App Deep linking using Intent \- inconsistent and buggy behavior, accessed April 13, 2026, [https://stackoverflow.com/questions/44985944/upi-app-deep-linking-using-intent-inconsistent-and-buggy-behavior](https://stackoverflow.com/questions/44985944/upi-app-deep-linking-using-intent-inconsistent-and-buggy-behavior)  
5. Create your own accessibility service \- Android Developers, accessed April 13, 2026, [https://developer.android.com/guide/topics/ui/accessibility/service](https://developer.android.com/guide/topics/ui/accessibility/service)  
6. My biggest productivity secret is my minimalist Android launcher \- MakeUseOf, accessed April 13, 2026, [https://www.makeuseof.com/my-biggest-productivity-secret-is-my-minimalist-android-launcher/](https://www.makeuseof.com/my-biggest-productivity-secret-is-my-minimalist-android-launcher/)