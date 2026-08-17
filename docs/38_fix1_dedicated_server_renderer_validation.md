# GTOHJS fix1 Dedicated Server Renderer Validation

## 中文

### 问题

2.0-alpha-for-gtocore-0.5.6-beta 在专用服务端注册以下机器时，会把正常的服务端 renderer 状态误判为注册失败：

- gtocore:advanced_generator_array
- gtocore:steam_array
- gtocore:advanced_steam_array

GTCEu 的 MachineBuilder.register() 只在客户端求值 renderer supplier；专用服务端会按设计写入 IRenderer.EMPTY。旧代码却在双端都断言 renderer 必须是客户端类 ArrayMachineRenderer，因此三个 definition 虽已进入注册表，仍被 GTOHJS 标记为失败，最终在 FMLLoadCompleteEvent 阻止服务端启动。

### 修复

三个注册器现在只在 GTCEu.isClientSide() 为真时检查 ArrayMachineRenderer 类型。专用服务端仍会验证：

- definition 是否进入 GTRegistries.MACHINES；
- 配方类型是否正确；
- pattern supplier 是否存在并能构建；
- 注册状态是否在 load-complete 阶段保持有效。

这不会放宽客户端材质检查，也不会在服务端主动加载或实例化客户端 renderer。

## English

### Problem

On a dedicated server, GTCEu deliberately stores IRenderer.EMPTY because MachineBuilder.register() evaluates renderer suppliers only on the client. GTOHJS previously required all three array definitions to expose an ArrayMachineRenderer on both distributions. Their definitions were registered, but this client-only assertion marked them as failed and aborted FMLLoadCompleteEvent.

Affected machines:

- gtocore:advanced_generator_array
- gtocore:steam_array
- gtocore:advanced_steam_array

### Fix

The concrete renderer assertion now runs only when GTCEu.isClientSide() is true. Dedicated servers continue to validate the machine registry entry, recipe type, pattern supplier, built pattern, and final registration state. Client renderer validation remains strict.
