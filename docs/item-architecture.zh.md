# SlashBlade 物品架构

## 目的

本文定义 SlashBlade-DHJ-Edition 的目标物品架构。

它的目标是在保留原始 SlashBlade 内容生产灵活性的前提下，解决大量刀共享同一注册物品所带来的身份识别与兼容性问题。

这是一份目标架构文档，不是对当前实现的逐行描述。

## 当前问题

当前实现依赖少量已注册的 `Item` 实例，加上大量基于 NBT 的 `ItemStack` 原型。

这会带来几个实际问题：

- JEI 往往会把多个名刀看成同一种物品，并关联到错误的配方。
- 任务类模组可能会把所有 `slashbladeNamed` 都视为同一种物品，或者在可变 NBT 变化后无法正确匹配。
- 附属依赖 `CurrentItemName` 这类 NBT 身份字段，但这种字段不足以作为跨模组的稳定身份契约。
- 配方、铁砧、进化逻辑需要手工复制大量 NBT 字段，一旦新增状态字段就很容易漏改。
- 兼容逻辑会分散在核心代码、名刀加载器、配方和附属补丁里，维护成本很高。

## 设计目标

目标物品架构应满足以下要求：

- 对重要刀种提供稳定的外部身份。
- 清晰分离“物品身份”和“可变刀状态”。
- 改善 JEI、任务模组和脚本模组兼容性。
- 同时支持内置刀和附属/自定义刀。
- 为旧附属保留一条兼容路径。
- 尽量复用战斗与渲染逻辑，避免无意义重复。

## 核心原则

### 1. 身份与状态必须分离

“这把刀是什么”不能依赖可变的成长型 NBT。

- 身份：
  注册名、稳定刀 ID、定义 ID
- 状态：
  ProudSoul、KillCount、RepairCount、Owner、充能状态、能量、临时标记

### 2. 逻辑复用是好事，共享身份不是

很多刀可以复用同一个实现类，但不应该都共享同一个注册物品身份。

### 3. 动态内容与固定内容不应走同一条路径

内置名刀和重要 compat 刀应该使用稳定注册物品。
真正的动态刀应走专门的动态路径。

### 4. 旧兼容必须显式存在

旧 API 可以保留，但应明确标记为 legacy，并在内部桥接到新架构。

## 目标物品层次

物品系统应拆分为四层。

### 第一层：注册身份层

这一层定义哪些内容真正注册进 Forge 的物品注册表。

推荐分类：

- `ItemProudSoul`
  刀魂、锭、球、晶体、四面体等材料物品
- `ItemSlashBladeBase`
  木刀、竹光、白鞘等基础成长刀
- `ItemSlashBladeNamed`
  具有独立注册项的固定名刀
- `ItemSlashBladeWrapper`
  包裹刀、鞘类桥接物品
- `ItemSlashBladeDynamic`
  给配置生成刀或 legacy 原型刀使用的动态回退物品

规则：

- 每把内置名刀都应该拥有自己的注册名。
- 每把重要 compat 刀也应尽量拥有自己的注册名。
- 只有动态生成或 legacy 生成的刀，才应该共享动态物品。

可接受的身份示例：

- `slashblade:yamato`
- `slashblade:tagayasan`
- `slashblade:agito_rust`
- `slashblade:wrapper_bamboomod_katana`
- `slashblade:dynamic_named_blade`

## 刀定义层

这一层负责定义不可变的刀数据。

推荐的 `BladeDefinition` 内容：

- `bladeId`
- 翻译 key
- 模型路径
- 贴图路径
- 基础攻击力
- 最大耐久
- 待机渲染类型
- 默认特殊攻击
- 默认刀属性集合
- 修理材料定义
- 稀有度
- compat 标签

规则：

- 刀定义一旦加载完成，应视为不可变。
- 应能在不读取玩家成长状态的前提下直接获取刀定义。
- 配方和集成逻辑应能直接解析刀定义。

定义来源建议：

- 内置 Java 注册
- 未来可扩展的 JSON / 数据驱动定义
- compat 模块注册
- legacy 原型刀桥接转换

## 刀状态层

这一层负责保存可变成长与运行时状态。

推荐状态分组：

- `progress`
  ProudSoul、KillCount、RepairCount、精炼相关数据
- `ownership`
  Owner UUID、权限、封印解锁状态
- `combat`
  连段、充能状态、目标实体 ID、攻击倍率
- `special`
  特效等级、召唤剑颜色、激活状态
- `runtime`
  可安全重新生成的临时标记

推荐的 NBT 布局：

```text
tag
|- BladeId
|- DefinitionVersion
|- Progress
|  |- ProudSoul
|  |- KillCount
|  |- RepairCount
|- Ownership
|  |- Owner
|- Combat
|  |- Combo
|  |- Charge
|- Special
|  |- Effects
|  |- Energy
|- Legacy
```

规则：

- `BladeId` 是这把刀的稳定逻辑身份。
- 可变字段绝不能作为唯一身份键。
- 运行时临时字段应尽量减少，并清晰隔离。
- 读取辅助方法应避免在“读取时顺手写回 NBT”，除非确实在做初始化。

## 类职责划分

推荐的高层职责拆分：

- `ItemSlashBladeBase`
  共享战斗逻辑、耐久逻辑、通用渲染钩子
- `ItemSlashBladeNamed`
  固定名刀身份、名刀定义查询
- `ItemSlashBladeWrapper`
  包裹刀专属逻辑
- `ItemSlashBladeDynamic`
  动态、配置或 legacy 刀的回退物品
- `BladeDefinition`
  不可变刀元数据
- `BladeState`
  状态访问器与迁移辅助
- `BladeIdentity`
  注册名、blade id、legacy alias 的统一解析辅助
- `BladeDefinitionRegistry`
  集中式定义注册表
- `BladeStateCodec`
  状态 NBT 读写与迁移辅助

## 推荐物品类别

模组应明确区分以下几类物品。

### 1. 材料物品

例如：

- proud soul
- ingot blade soul
- sphere blade soul
- crystal blade soul
- tiny blade soul
- trapezohedron blade soul

特点：

- 稳定注册物品
- 很少或没有刀成长状态
- 如果愿意，仍可保留 metadata / subtype 方案

### 2. 核心成长刀

例如：

- 木刀
- 竹光
- 银竹光
- 白鞘
- 无铭拔刀剑基础款

特点：

- 稳定注册物品
- 允许拥有成长状态
- 配方应直接面向真实注册项

### 3. 固定名刀

例如：

- Yamato
- Tagayasan
- Agito
- Muramasa 系列
- Fox、Sange、Yasha 等路线

特点：

- 每个重要刀变体一个注册物品
- 可以共用同一个实现类
- 应对 JEI 和任务模组暴露稳定注册身份

### 4. 包裹与桥接刀

例如：

- wrapper scabbard
- compat wrapper variants

特点：

- 行为可能委托给内部物品
- 但如果结果属于内置或 curated compat 刀，仍应拥有稳定身份

### 5. 动态与自定义刀

例如：

- 配置生成刀
- 未来数据驱动刀
- 仍使用原型注册的 legacy 附属刀

特点：

- 可以共享一个回退注册物品
- 但必须具有稳定 `BladeId`
- 应视为兼容路径，而不是核心内容默认路径

## 身份规则

物品系统应按以下顺序解析身份：

1. 固定注册刀的真实注册名。
2. 动态刀的稳定 `BladeId` 字段。
3. 旧字段如 `CurrentItemName` 的 legacy alias 映射。

规则：

- `CurrentItemName` 应降级为 legacy alias 字段，而不是主身份字段。
- `TrueItemName` 应视为迁移元数据，而不是长期公开契约。
- 外部集成应统一调用辅助方法，而不是自己直接读原始 NBT key。

## 配方架构

配方应按“它关心什么”来拆分。

### 身份型配方

这类配方关心“输入到底是哪把刀”。

例如：

- 破损刀觉醒为特定名刀
- 某个 compat 联动转化配方

匹配方式应为：

- 固定刀优先按注册物品匹配
- 动态 legacy 内容再按稳定 `BladeId` 匹配

### 状态继承型配方

这类配方关心成长状态是否被保留。

例如：

- 升级配方
- 觉醒配方
- 修理进化路线

它们应通过统一的状态迁移 helper 复制状态，而不是在每个 recipe 里手抄一批 NBT 字段。

### 材料型配方

这类配方不关心刀身份。

例如：

- 刀魂升级
- 修理材料处理

它们应只操作材料物品定义。

## 兼容架构

兼容应分层设计。

### 核心兼容层

核心模组应暴露：

- 刀身份解析器
- 刀定义注册表
- 刀状态迁移 helper
- legacy 原型注册桥接层

### Compat 模块

单独的 compat 模块可以负责：

- 附属刀注册
- JEI subtype 与 ingredient 辅助
- 任务模组匹配桥接
- 脚本集成辅助
- 必要时的附属定向 Mixin 补丁

### Legacy 附属桥接

仍使用 `registerCustomItemStack` 的旧附属应继续可用，但核心应把它们内部转换成：

- 一个可解析的 `BladeDefinition`
- 一个稳定的 `BladeId`
- 一条归类后的动态 / legacy 刀记录

## 迁移策略

该架构应分阶段落地。

### 第一阶段

- 引入 `BladeId` 和集中式身份 helper。
- 保留旧原型注册路径可用。
- 让 JEI 和匹配逻辑改为走稳定身份 helper。

### 第二阶段

- 将内置名刀迁移为独立注册物品。
- 保留 legacy alias，确保旧 key 仍能解析。
- 集中化状态迁移逻辑。

### 第三阶段

- 为附属和 compat 模块提供真正的早期注册 API。
- 将共享动态物品路径保留给配置刀与 legacy 内容。

### 第四阶段

- 废弃直接依赖 `CurrentItemName` 的方案。
- 将配方和任务集成逐步迁移为基于定义或稳定身份的匹配。

## 哪些内容应该继续共享

以下内容可以继续共享：

- 战斗逻辑
- 连段逻辑
- 特殊攻击分发
- 渲染钩子
- Tooltip 辅助
- 状态访问辅助

共享逻辑没有问题，真正有问题的是共享注册身份。

## 哪些内容不应继续隐藏在共享注册项后面

以下内容不应继续被塞在同一个共享注册物品后面：

- 重要名刀身份
- JEI 可见的配方输出
- 任务模组可见的奖励目标
- 重要 compat 联动刀输出

## 最终建议

这个模组的目标物品架构应当是：

- 固定刀使用真实注册身份
- 动态刀使用专门的回退物品加稳定 `BladeId`
- 刀定义集中化且不可变
- 刀状态结构化且可变
- 配方通过统一状态迁移 helper 继承成长
- 兼容通过显式桥接层完成，而不是依赖展示导向的 NBT 身份字段

这样既能保留旧原型模型的灵活性，也能摆脱旧身份模型在外部兼容上的弱点。
