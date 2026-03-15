# cyan-datagateway

数据网关微服务，用于执行大数据SQL读写数据仓库表，记录用户执行SQL历史，并支持数据权限管理。

## 功能特性

- SQL查询执行 (SELECT)
- SQL更新执行 (INSERT/UPDATE/DELETE)
- SQL执行记录审计
- 数据权限控制（预留扩展）
- 支持 StarRocks 查询引擎

## 模块结构

```
cyan-datagateway/
├── cyan-datagateway-client/          # 客户端模块 - Feign接口 + 枚举定义
└── cyan-datagateway-application/     # 应用模块 - 主业务实现
    ├── adapter/                      # 适配器层 - HTTP接口
    ├── application/                  # 应用服务层
    ├── domain/                       # 领域层
    └── infra/                        # 基础设施层
```

## API 接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/sql/query | 执行查询SQL |
| POST | /api/v1/sql/update | 执行更新SQL |
| GET | /api/v1/sql/records | 分页查询执行记录 |
| GET | /api/v1/sql/records/{executeId} | 根据执行ID查询结果 |
| GET | /api/v1/sql/records/recent | 获取最近执行记录 |

## 配置说明

### StarRocks 配置

```yaml
starrocks:
  jdbc-url: jdbc:mysql://host:port/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
  username: root
  password: ""
```

## 数据库初始化

执行 `schema.sql` 创建所需表结构。
