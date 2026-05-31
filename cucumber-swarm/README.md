# cucumber-swarm

[简体中文](README.zh-CN.md)

`cucumber-swarm` runs Cucumber with a **master / worker execution model**.  
It was created for end-to-end test suites that need higher throughput, but cannot safely rely on Cucumber's built-in
multi-threaded execution against a single shared system under test.

## Overview

The motivation is practical. Cucumber's built-in parallel execution is thread-based, but many end-to-end systems cannot
reliably support multiple tests running at the same time against a single shared deployment. In those cases, increasing
throughput usually means preparing **multiple equivalent test environments**—for example several docker-compose
stacks—and distributing scenarios across them.

That changes the problem from "how to use more threads in one runtime" to "how to coordinate execution across several
isolated environments while still treating the outcome as one Cucumber run". `cucumber-swarm` addresses that problem by
moving scheduling and execution into separate roles. The **master** plans the run, manages the scenario queue, and
collects results. **Workers** execute scenarios in their own runtime and report execution information back. The master
then republishes that aggregated result stream to summaries and master-side plugins.

Compared with plain Cucumber, this keeps the mental model of one test run, but changes where work is done. That makes
`cucumber-swarm` a better fit when the goal is centralized scheduling, execution across local or remote workers, and a
single aggregated result for reporting.

### What stays familiar

`cucumber-swarm` still preserves most of the behaviors users expect from a normal Cucumber run: standard scenario and
step results, passed / failed / skipped / pending / undefined / ambiguous outcomes, undefined-step snippets in the
final output, feature-directory and single-feature targets, and one final exit status for the whole run.

### What changes

The execution model is different in ways that matter operationally:

| Aspect              | Plain Cucumber                    | cucumber-swarm                          |
|---------------------|-----------------------------------|-----------------------------------------|
| Scenario scheduling | local runtime decides what to run | master assigns work                     |
| Scenario execution  | same process that planned the run | worker process/runtime                  |
| Initialization      | one runtime initializes once      | each worker initializes its own runtime |
| Plugin observation  | local execution stream            | master-side aggregated stream           |
| Output ownership    | same runtime that executes        | summary/result owned by master          |
| Remote execution    | not the default model             | built into the workflow                 |

## Can you treat it like plain Cucumber?

**Often yes, but not always.**

For many usage patterns, it is reasonable to think of `cucumber-swarm` as "Cucumber with distributed execution behind
one master result stream". That mental model works best when your main concern is scenario and step lifecycle results,
one overall summary, and final pass/fail reporting. It is still not a perfect drop-in replacement for every
single-process assumption, so compatibility should be judged with some care.

### Plugin compatibility

The safest expectation is that `cucumber-swarm` fits best with plugins and tooling that care about scenario execution
and final results. It needs more careful evaluation when a plugin depends on process-local behavior, worker-local
stdout, or the full set of internal runtime events.

In the current implementation, the master always adds `MasterPlugin`, workers always add `WorkerForwardingPlugin`, and
user-supplied `--plugin` options remain on the master side. Worker execution is forwarded back primarily as test-case
and test-step lifecycle information, with selected Cucumber message payloads also being forwarded. Some worker-local
events are intentionally not forwarded as-is. In practice, that means plugins that only need the aggregated run outcome
are more likely to behave well than plugins that assume they are attached directly to the runtime that executed the
step.

### Output compatibility

One important practical difference is **stdout behavior**. In **local-worker mode**, step-level `System.out.println(...)`
output appears in the final run output. In **remote-worker mode**, the final stdout shows the master summary, but
worker-side `System.out.println(...)` output is not merged into that final stdout in the same way. If your workflow
depends on worker console output being part of the main terminal output, remote mode needs extra care.

### Initialization compatibility

Because workers execute the scenarios, the worker environment must contain everything the test run needs: feature
targets, glue, hooks, runtime dependencies, and any launcher-specific JVM or process options. This is especially
important in remote mode. A master can coordinate the run, but a worker still needs a complete executable Cucumber
environment.

## What distributed execution changes in practice

### 1. Execution and initialization are separated

The master does not execute steps. Workers do.

That means anything tied to step execution happens in worker runtimes, including:

- glue loading
- hook execution
- object initialization needed for step execution
- worker-local stdout/stderr behavior

### 2. The master is the aggregation point

The master is where the run is coordinated and where the final result is assembled.

That is why the master remains the best place for:

- final summary output
- centralized result interpretation
- master-side plugin consumption
- startup timeout and worker availability handling

### 3. Remote mode should be treated as a deployment concern, not only a Cucumber option

Remote mode is not simply a CLI variation. It is a deployment and orchestration concern: you are deciding how each
worker is started, which environment it runs in, and how that environment reaches the master.

The examples in this README use `java` because that is the lowest-level form and matches the module's own test setup.
In real projects, workers are often launched through higher-level commands instead of invoking the JVM directly. Common
patterns include:

- `docker-compose exec` into a dedicated test container and run a Gradle or Maven Cucumber task
- `ssh` to another machine and run the project's standard test command there
- wrap the worker start logic in a script that prepares the expected runtime and then invokes Cucumber

For that reason, `--remote-worker-launcher` should be understood as the entry command for your remote execution model,
not as something that is inherently tied to `java`. The practical concerns are the same either way: runtime bootstrap,
build-tool task selection, logging, environment-specific configuration, and host / port reachability back to the
master.

## Running cucumber-swarm

### Local worker mode

```bash
java ... org.testcharm.cucumber.swarm.Main \
  --glue steps \
  features
```

Behavior:

- the master starts
- one local worker is created by default
- the worker requests scenarios from the master
- the final exit code is produced by the master run

This form is useful for understanding the execution model. In a build, the same invocation may be wrapped by a Gradle
or Maven task rather than being called directly through `java`.

### Remote worker mode

At the lowest level, a remote worker launch can look like this:

```bash
java ... org.testcharm.cucumber.swarm.Main \
  --swarm-port 20000 \
  --disable-local-worker \
  --remote-worker-count 1 \
  --remote-options-json '["-Djava.util.logging.config.file=/tmp/cucumber/logging.properties","-cp","...","org.testcharm.cucumber.swarm.Main"]' \
  --remote-worker-launcher java \
  --glue steps \
  features
```

Meaning:

- `--remote-worker-launcher` supplies the entry command used to start a worker
- `--remote-options-json` supplies the fixed launcher/runtime prefix
- swarm appends worker identity, swarm connection options, and normalized feature targets

In practice, many teams will point `--remote-worker-launcher` to a script, a `docker-compose` command, or an `ssh`
command that eventually runs the project's normal Gradle or Maven Cucumber task in the target environment.

## CLI options

| Option                               | Default     | Meaning                                                              |
|--------------------------------------|-------------|----------------------------------------------------------------------|
| `--remote-worker-launcher <bin>`     | none        | Executable used to start each remote worker process                  |
| `--disable-local-worker`             | off         | Disable the default in-process local worker                          |
| `--swarm-host <host>`                | `localhost` | Hostname used by remote workers to reach the master                  |
| `--swarm-port <port>`                | `10083`     | Master coordination port                                             |
| `--worker-timeout <seconds>`         | `40`        | How long the master waits for a worker to register and become available |
| `--remote-worker-count <n>`          | `0`         | Number of remote workers to launch                                   |
| `--remote-options-json <json-array>` | `[]`        | Extra launcher/runtime arguments inserted after the launcher command |
| `--worker-id <id>`                   | none        | Marks a process as a worker instance; local worker is always `0`, remote workers start at `1` and increment |

## Operational notes and limitations

### Threads are forced to 1

The current preprocessing forces:

```bash
--threads 1
```

for master and worker runtimes.

### Plugin routing is deliberate

`cucumber-swarm` does not let every process behave like an ordinary plugin host.

- the **master** owns the aggregated event stream and final reporting surface
- the **worker** owns execution and forwarding
- the worker-side forwarding plugin is always present so the master can rebuild a coherent run

This is one of the main reasons lifecycle-oriented reporting tends to fit better than plugins that depend on fully local
execution internals.

### Feature targets are normalized for remote workers

Remote workers support:

- feature directories
- single feature files
- feature files with line selectors
- multiple targets
- automatic normalization from absolute paths to relative targets for generated remote worker args

### Worker startup is coordinated explicitly

Before the master starts assigning scenarios, it waits for at least one worker to register that it is available to
accept work. The `--worker-timeout` option controls how long the master waits for that initial worker availability.
If no worker becomes available within that window, the run stops with:

```text
No worker available after waiting for <n> seconds
```

This is relevant in both local and remote execution, but it matters most in remote mode, where startup may depend on
container scheduling, remote command execution, network access, or build-tool bootstrap time.

### Worker ids are stable by role

The built-in local worker always uses id `0`.

Remote workers are assigned ids starting at `1`, increasing by one for each launched remote process. This keeps the
default local worker distinct from all remote workers in logs, HTTP headers, and forwarded execution data.

### Remote workers need an explicit launch command

If you request remote workers, you must also provide the command used to start them. Without that command, swarm cannot
materialize a remote worker process and the run stops with:

```text
Missing option --remote-worker-launcher
```

## Implementation notes

You do not need these details to use the library, but they help explain the compatibility model.

### Coordination protocol

The master exposes three endpoints:

| Method | Path      | Purpose                                                     |
|--------|-----------|-------------------------------------------------------------|
| `GET`  | `/pickle` | worker requests the next scenario                           |
| `POST` | `/events` | worker reports execution information back                   |
| `POST` | `/ready`  | worker confirms that it has completed startup and can accept scenarios |

Workers repeatedly request scenarios through `/pickle` until no more work remains. The separate `/ready` step is what
allows the master to distinguish "worker process was launched" from "worker runtime is initialized and available for
execution".

### What is forwarded back from workers

At the plugin-event level, the current implementation forwards worker execution primarily as:

- `io.cucumber.plugin.event.TestCaseStarted`
- `io.cucumber.plugin.event.TestStepStarted`
- `io.cucumber.plugin.event.TestStepFinished`
- `io.cucumber.plugin.event.TestCaseFinished`

At the Cucumber message level, the forwarded envelope payloads include:

- `io.cucumber.messages.types.TestCase`
- `io.cucumber.messages.types.TestCaseStarted`
- `io.cucumber.messages.types.TestStepStarted`
- `io.cucumber.messages.types.TestStepFinished`
- `io.cucumber.messages.types.TestCaseFinished`

This is why `cucumber-swarm` can still present a meaningful aggregated run to the master side, but it is also why plugin
compatibility should be evaluated with the distributed model in mind rather than assumed automatically.

### Master forwarding does not currently canonicalize event order

Plain `io.cucumber.core.cli.Main` in multi-threaded mode does not expose raw arrival order directly to ordinary
`EventListener` plugins. It buffers events through Cucumber's canonical ordering publisher and flushes them at
`TestRunFinished`, producing the serial order those plugins would have seen in a single-threaded run.

`cucumber-swarm` does not currently perform an equivalent master-side reorder step. Worker events are forwarded to the
master and then republished in the order they arrive. As a result, tools that depend on canonical serial ordering—such
as some IDE test-tree formatters—may show split feature nodes or other ordering-sensitive UI differences.

### Delivery is queued, then flushed before worker exit

Workers do not synchronously POST every event inline with step execution.  
They enqueue events, a dedicated forwarding thread sends them to the master, and the worker waits for that queue to
flush before exiting.

That reduces coupling between step execution and HTTP delivery, but it also means the worker is not simply "running a
plugin locally and printing everything directly".

### Why remapping exists

Workers do not send back live in-process Java objects. They send serialized execution information.

The master rebuilds the corresponding aggregated objects so that:

- scenario / step identity can be recovered
- paths can be mapped back consistently
- master-side summaries and plugins can observe one coherent run

### Remote exceptions are preserved as far as possible

When a worker-side failure is serializable, swarm forwards the original throwable data.  
If it is not serializable, swarm falls back to a `RemoteException` wrapper that preserves the remote exception type,
message, and stack trace as transport-safe data.

### There are built-in swarm extension hooks

In addition to normal Cucumber plugins, swarm also scans for:

- `MasterPluginExtension`
- `WorkerForwardingPluginExtension`

under:

- `org.testcharm.cucumber.extensions`
- `org.testcharm.extensions.cucumber`

Most users will not need these APIs, but they are relevant if you are extending swarm-aware behavior rather than only
consuming the aggregated master-side run.
