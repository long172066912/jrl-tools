package org.jrl.redis.core.model;

import io.lettuce.core.output.CommandOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;

public class LettucePipelineCommand<K, V, T> {
    /**
     * 命令
     */
    private CommandType command;
    /**
     * 参数
     */
    private CommandArgs<K, V> args;
    /**
     * 输出
     */
    private CommandOutput<K, V, T> output;

    public LettucePipelineCommand(CommandType command, CommandArgs<K, V> args, CommandOutput<K, V, T> output) {
        this.command = command;
        this.args = args;
        this.output = output;
    }

    public CommandType getCommand() {
        return command;
    }

    public void setCommand(CommandType command) {
        this.command = command;
    }

    public CommandArgs<K, V> getArgs() {
        return args;
    }

    public void setArgs(CommandArgs<K, V> args) {
        this.args = args;
    }

    public CommandOutput<K, V, T> getOutput() {
        return output;
    }

    public void setOutput(CommandOutput<K, V, T> output) {
        this.output = output;
    }
}
