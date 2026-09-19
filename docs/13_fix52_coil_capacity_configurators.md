# GTOHJS fix52 Coil Capacity Configurators

## Runtime limits

The hyperdimensional smelter and hyperdimensional chemical factory continue to reuse `gtocore:chemical_complex`'s exponential coil capacity, but parallelism and threads must be calculated separately according to the real GTOLib 26.7.4 ABI:

```text
raw = 2^min(60, floor(coil temperature / 900))
parallel limit = min(9,007,199,254,740,991, raw)
thread limit = min(2,147,483,647, raw)
```

The parallel interface uses `long`, with the final cap `IParallelMachine.MAX_PARALLEL = 2^53 - 1`. The thread interface `ICrossRecipeMachine.getThread()` uses `int`. Do not implement both with one `int` capacity function again, or eternal-coil parallelism will be incorrectly truncated to `Integer.MAX_VALUE`.

## Left configurator panel

Both machines add two independent pages to the native Fancy UI's left configurator panel through `attachConfigurators(ConfiguratorPanel)`:

- Parallelism: `LongInputWidget`, using the `gtocore:infinite_parallel_hatch` icon.
- Threads: `IntInputWidget`, using the `gtocore:max_thread_hatch` icon.

The configured values are persisted through `@SaveToDisk`. For both initial data and incremental updates, the server must synchronize the current coil limit and effective selected value. Never trust a range supplied by the client.

## Clamping rules

Every input path performs server-side clamping, including buttons, text input, old saved values, and structure reformation:

1. Values below 1 become 1.
2. Values above the current coil-tier limit become the maximum supported by that coil.
3. `CrossRecipeTrait` calculates the available workload as `parallel * thread`, so their product must not exceed `Long.MAX_VALUE`. The most recently edited value takes priority; the other is reduced automatically when necessary.
4. Runtime parallelism and threads return 0 while the machine is unformed. The UI uses 1 as a safe editable lower bound.

The default configuration is the maximum parallelism supported by the current coil and one thread, matching native `chemical_complex` behavior with high parallelism and low thread count. When switching to a lower-tier coil, runtime values are automatically reduced to the new coil's limit even if they came from an old save using a higher tier.
