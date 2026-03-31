# AI 超级智能体项目 - 系统架构与业务逻辑设计

本文档包含了当前系统的**整体架构图**与核心的**系统业务逻辑图**，基于 Mermaid 语法生成。

## 1. 整体系统架构图

该架构图展示了从前端用户界面、网关接入、后端 AI 调度核心，到外围工具调用与数据库底座的分层设计。

```mermaid
graph TD
    classDef frontend fill:#4fc08d,stroke:#fff,stroke-width:2px,color:#fff;
    classDef backend fill:#6db33f,stroke:#fff,stroke-width:2px,color:#fff;
    classDef aiCore fill:#5c2d91,stroke:#fff,stroke-width:2px,color:#fff;
    classDef data fill:#336791,stroke:#fff,stroke-width:2px,color:#fff;
    classDef tools fill:#f8981d,stroke:#fff,stroke-width:2px,color:#fff;

    subgraph 前端体验层
        A[Vue 3 前端应用平台]:::frontend
        B[AI恋爱大师 UI]:::frontend
        C[YuManus 智能体 UI]:::frontend
        A --> B
        A --> C
    end

    subgraph 接入层
        D[Spring Boot 3 API 调度网关]:::backend
        E[SSE 流式推送端点]:::backend
        F[Knife4j 接口文档]:::backend
        B -- Axios / SSE 请求 --> D
        C -- Axios / SSE 请求 --> D
        D -.-> E
    end

    subgraph AI 应用调度与智能体层
        G[Spring AI + LangChain4j 调度核心]:::aiCore
        H[ChatClient / Advisor 会话组件]:::aiCore
        I[ReAct Agent 规划与执行引擎]:::aiCore
        D --> G
        G --> H
        G --> I
    end

    subgraph 存储与知识库检索池
        J[(PGvector 向量数据库)]:::data
        K[(ChatMemory 记忆存储)]:::data
        H <--> K
        H <-->|RAG 查询增强| J
    end

    subgraph Tool Calling 拓展与大模型集群
        L[MCP 模型上下文协议网关]:::tools
        M[内置 ToolBox: 爬虫/PDF/终端等]:::tools
        N[云端混合超大模型]:::tools
        O[本地私有化部署 Ollama]:::tools
        I --> L
        I --> M
        G --> N
        G --> O
    end
```

## 2. 核心系统业务逻辑流转图 (以智能体响应链路为例)

该流程图详尽剖析了用户发送一条复杂请求后，AI 后台是如何经过 RAG 补充、大模型深度规划（ReAct 循环），并最终返回结果的流转过程。

```mermaid
flowchart TD
    classDef default fill:#f9f9f9,stroke:#333,stroke-width:2px;
    classDef decision fill:#ffecd2,stroke:#fcb69f,stroke-width:2px;
    classDef action fill:#d4fc79,stroke:#96e6a1,stroke-width:2px;
    
    Start([前端客户端下发 Query 请求]) --> A[API 网关路由鉴权并创建 SSE 隧道]
    A --> B{业务线分发逻辑}:::decision
    
    B -- "恋爱大师 (问答类)" --> C[启动 RAG 文档召回分析引擎]
    C --> D[向量化用户问题 & 查询 PGvector 核心语录]
    D --> E[装载并组装最近 N 条历史 ChatMemory]
    E --> F[Prompt 包装发送至大语言模型]
    F --> Return([触发 SSE 流式打字机返回到前端])
    
    B -- "超级智能体 (任务类)" --> G[启动 ReAct Agent 任务规划主线]
    G --> H[Thought: 大模型深层思考意图与拆解步骤]
    H --> I{是否需要借助外部 Tool?}:::decision
    
    I -- "Yes (需工具协助)" --> J[Action: 生成 Tool Calling 参数]:::action
    J --> K[执行工具: 调用 Jsoup网络/MCP系统/生成等]:::action
    K --> L[Observation: 工具返回运行结果环境参数]
    L -->|将观察结果回推至提示词| H
    
    I -- "No (已满足闭环)" --> M[提取最终梳理完备后的事实与回答]
    M --> Return
```
