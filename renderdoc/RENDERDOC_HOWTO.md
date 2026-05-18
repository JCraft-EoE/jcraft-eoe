## *NOTE:* This was only tested on and made for Unix based systems (i.e. Linux).
* Step 2 will stay the same, but Step 1 will change on Windows.

# Steps:
## 1. Get launch arguments:
1. Run the client (either Forge or Fabric).
2. In a terminal, run `jps` (the [Java Virtual Machine Process Status Tool](https://docs.oracle.com/javase/7/docs/technotes/tools/share/jps.html)), and search for "TransformerRuntime". Example output:
```
98305 Main
121633 TransformerRuntime
99267 GradleDaemon
```
3. Note down the Process ID of "TransformerRuntime" (in this case, `121633`)
4. In a terminal, run `ps <the process id>` (ex: `ps 121633`). Example output:
```
    PID TTY      STAT   TIME COMMAND
 121633 ?        Sl     1:24 /path/to/java (a lot of arguments...)
```
5. Note down the path to the Java executable and the full list of arguments *(NOTE: you may have to send the output to a file, ex: `ps 121633 > output.txt`)*.
## 2. Launch with RenderDoc
1. In RenderDoc, set the Executable Path to the path to Java from Step 1.
2. Set the Command-line arguments to the arguments from Step 1. *(NOTE: do not include the /path/to/java part.)*
3. Launch
4. Profit