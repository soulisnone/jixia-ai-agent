# AI 超级智能体项目 - 全栈系统概要与架构说明

> 本系统为“AI 超级智能体”的核心代码库。整个项目旨在通过开发 **AI 恋爱大师应用 + 拥有自主规划能力的超级智能体**。

## 平台业务矩阵

这是一套以 **AI 开发实战** 为驱动的工程。采用前后端分离的架构与基于 Tool Calling 和 ReAct 范式的 AI 引擎深度通信，目前已经跑通以下两大核心功能场景：

- 💬 **AI 恋爱大师**：智能情感顾问，可以依赖云端与本地大模型解决用户的情感问题，为用户提供贴心情感指导。不仅支持多轮对话与流式返回（基于 SSE 原生推送），还底层依赖自定义针对性语录的 RAG 检索知识库。
- 🤖 **AI 超级智能体 (YuManus)**：全能型自主规划智能体底座引擎。用户输入抽象或者长链路的复杂需求后，AI 会根据意图自主拆解流程、推理并在内部循环反思；同时它能主动调用一系列集群与云端扩展工具（如联网搜索、网页抓取预校验、PDF 生成、MCP图片提取、系统文件系统操作等），自动规划并最终面向前端交付闭环结果。

## 整体架构与详细技术栈

本项目作为大型复合 AI 应用平台，涵盖了从前端交互体验到底层大语言模型的系统性研发体系，其中**后端引擎为项目的核心控制枢纽**。

### 📊 系统架构与工作流示意图

为了帮助开发者更加宏观、直观地了解本全栈分布式应用的设计思想及运转全景，请参见下方的架构演示图与流转说明：

> **核心项目架构图**

![项目架构图](./架构图.png)

> **动态链路流转图 (基于 Mermaid 渲染)**

**1. 整体系统组件分层架构图**

```mermaid
graph TD
    classDef frontend fill:#4fc08d,stroke:#fff,stroke-width:2px,color:#fff;
    classDef backend fill:#6db33f,stroke:#fff,stroke-width:2px,color:#fff;
    classDef aiCore fill:#5c2d91,stroke:#fff,stroke-width:2px,color:#fff;
    classDef data fill:#336791,stroke:#fff,stroke-width:2px,color:#fff;
    classDef tools fill:#f8981d,stroke:#fff,stroke-width:2px,color:#fff;

    subgraph 前端体验层
        A[Vue 3 前端应用平台]:::frontend
        B[AI恋爱大师 视图界面]:::frontend
        C[YuManus 智能体管理台]:::frontend
        A --> B
        A --> C
    end

    subgraph 接入控制台
        D[Spring Boot 3 API 调度网关]:::backend
        E[SSE 流式长连接推送端点]:::backend
        F[Knife4j]:::backend
        B -- Axios / SSE --> D
        C -- Axios / SSE --> D
        D -.-> E
    end

    subgraph AI应用调度与引擎组
        G[Spring AI + LangChain4j]:::aiCore
        H[ChatClient / Advisor 组件]:::aiCore
        I[ReAct Agent 框架引擎]:::aiCore
        D --> G
        G --> H
        G --> I
    end

    subgraph 存储与核心上下文库
        J[(PGvector 专有知识特征向量库)]:::data
        K[(ChatMemory历史与当前语境仓)]:::data
        H <--> K
        H <-->|RAG 召回增效检索| J
    end

    subgraph 内置工具链与大模型基床
        L[MCP 模型上下文代理网关]:::tools
        M[系统 ToolBox 业务工具集]:::tools
        N[线上商业集群大语言模型引擎]:::tools
        O[本地纯自建私有级 Ollama]:::tools
        I --> L
        I --> M
        G --> N
        G --> O
    end
```

**2. YuManus 智能体处理复杂多级命令链路（模拟 Manus 原理层）**

```mermaid
flowchart TD
    classDef action fill:#d4fc79,stroke:#96e6a1,stroke-width:2px;
    
    Start([前端客户端/请求方抛送目标]) --> A[鉴权后初始化一条隔离交互通道 SSE]
    A --> B{场景功能适配网关}
    
    B -- "纯问答交互请求" --> C[对问题向量化并探查 PGvector 本地知识经验]
    C --> D[重构上下文环境结合近几次的 ChatMemory]
    D --> E[推流至已配对的基础 LLM 大屏基石模型]
    E --> Return([向前端中断按块写出推理文字序列指令片段])
    
    B -- "含推演属性或复合处理任务" --> G[初始化建立重型独立沙盘]
    G --> H[Thought阶段: 使用拥有高度算力与参数架构支持模型深解剖用户意图]
    H --> I{判断层: 是否需唤起业务子系统协助完成步骤?}
    
    I -- "是 (面临知识孤岛)" --> J[Action阶段: 根据协议拼出工具的传参，抛给本地宿主的 Tool Calling系统执行]:::action
    J --> K[Observation阶段: 以抓取器或脚本拦截器获取子系统的客观运行答案及返回值]
    K -->|将答案整理压迫回推理语境池去| H
    
    I -- "否 (链路无短板，完成内部证实闭环)" --> M[抽去业务污渍整理报告后，包装给出口处理网卡]
    M --> Return
```

### ⚙️ 后端级及核心扩展技术栈清单
后端主服务为整个平台提供了所有关键功能：

- **核心基底**：
  - **Java 21 + Spring Boot 3**：企业级最前沿的 Web 服务框架，承载所有的业务调度、数据持久化及依赖注入环境。
  - **Knife4j**：生成高质量的接口文档体系，方便各个模块级别的压测与联调。

- **AI 大模型通信层**：
  - **Spring AI + LangChain4j**：作为通用桥接抽象层，用来抹平线上百炼大模型和本地自建模型（如 Ollama）的网络通讯异构差异，统一完成从单点对话响应、Advisor增强补丁到长文对话聊天（ChatMemory）持久化的保障职责统筹。
  
- **RAG 数据构建工具群建制**：
  - **PGvector 数据库**：依托于 Postgres 插件系统的专业级向量引擎。专门针对业务预构建了针对大批量非结构文本快速入库分析、海量问答的向量抽取余弦比对等数据侧链路。
  
- **智能体及扩展 Agent 工具集群层（Tool Calling 与 MCP 生态）**：
  - **ReAct 引擎架构原理应用**：构建了类似市面先进超级个体（像 Manus 演示模型）所需的：从大盘拆解子任务，到多步骤交替独立反思行动并重构的完整业务态。极客化的降低了幻觉产生。
  - **MCP 开放通信网桥**：将系统的工具组件部署出独立架构之外进行外部交互与挂载，如提供给外界进行调用搜图。
  - **内部工具箱底座**：在 Tool Calling 层提供了爬虫探路者组件（Jsoup）、渲染出单器（通过 iText 直接向客户端下发生成好的 PDF 体验文件）等。
  - 高频通信优化：基于 **Kryo 序列化器系统** 使繁杂冗长的上下文记忆与大对象变量能够达到快速保存加载，节省响应的 IO 时间。

### 🎨 前端展现层 (yu-ai-agent-frontend)
采用直观轻巧的最新理念提升 B/S 环境对话质量：
- 采用 **Vue 3** 单页面应用模型与 **Vite** 的极速响应引擎。
- 通信层基于原生底层对 **SSE (Server-Sent Events) 协议流缓冲分块接收适配**，完美兼容各大浏览器对于打字机效果“字字显形”的视觉震撼支持。

## 💡 快速入门与联调

由于这是一个高度耦合前后方链路且具有服务端主导性的产品。在本地开启研发环境之前：请妥协先运转起后端的 Spring Boot 引擎套件。服务挂载预设端口默认为 `http://localhost:8123`，并检查 Postgres(含 PGvector) 服务在线。

前台界面的启动参照以下方法：

```bash
# 1. 切换到前端子项目路径
cd ./yu-ai-agent-frontend

# 2. 拉包
npm install

# 3. 启本地研发服务监听 
npm run dev
```
