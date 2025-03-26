package org.jrl.redis.core.model;

import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 管道
 *
 * @author JerryLong
 */
public class CachePipeline {
    /**
     * 命令
     */
    private final String command;
    /**
     * 命令类型，1普通命令，2lua脚本
     */
    private final Integer commandType;
    private final String[] keys;
    private List<Object> args;
    private final CommandOutput outputType;

    protected CachePipeline(String command, Integer commandType, CommandOutput outputType, String... keys) {
        this.command = command;
        this.commandType = commandType;
        this.outputType = outputType;
        this.keys = keys;
        args = new ArrayList<>();
    }

    public String getCommand() {
        return command;
    }

    public String[] getKeys() {
        return keys;
    }

    public List<Object> getArgs() {
        return args;
    }

    public CachePipeline addArgs(List<Object> args) {
        if (CollectionUtils.isNotEmpty(args)) {
            this.args.addAll(args);
        }
        return this;
    }

    public CachePipeline addArg(Object arg) {
        this.args.add(arg);
        return this;
    }

    public CommandOutput getOutputType() {
        return outputType;
    }

    public Integer getCommandType() {
        return commandType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String command;
        private String[] keys;
        private List<Object> args;
        private CommandOutput outputType = new ValueOutput(StringCodec.UTF8);
        private Integer commandType = 1;

        public Builder() {
            args = new ArrayList<>();
        }

        public Builder command(String command) {
            this.command = command;
            return this;
        }

        /**
         * 脚本sha1，必须是RedisLuaCommands内定义的lua脚本方法
         *
         * @param cacheLuaCommand，必须是 RedisLuaCommands中定义的
         * @return
         */
        public Builder evalsha(String cacheLuaCommand) {
            this.command = cacheLuaCommand;
            this.commandType = 2;
            return this;
        }

        public Builder keys(String... keys) {
            this.keys = keys;
            return this;
        }

        public Builder arg(Object arg) {
            this.args.add(arg);
            return this;
        }

        public Builder args(Object... args) {
            this.args.addAll(Arrays.asList(args));
            return this;
        }

        public Builder args(List<Object> args) {
            this.args.addAll(args);
            return this;
        }

        public Builder outputType(RedisLuaOutPut outPut) {
            switch (outPut) {
                case INTEGER:
                    this.outputType = new IntegerOutput(StringCodec.UTF8);
                    break;
                case MAP:
                    this.outputType = new MapOutput(StringCodec.UTF8);
                    break;
                case VOID:
                    this.outputType = new VoidOutput(StringCodec.UTF8);
                    break;
                case BOOLEAN:
                    this.outputType = new BooleanOutput(StringCodec.UTF8);
                    break;
                case BOOLEAN_LIST:
                    this.outputType = new BooleanListOutput(StringCodec.UTF8);
                    break;
                case MULTI:
                    this.outputType = new MultiOutput(StringCodec.UTF8);
                    break;
                case ARRAY:
                    this.outputType = new ArrayOutput(StringCodec.UTF8);
                    break;
                case KEY_VALUE:
                    this.outputType = new KeyValueOutput(StringCodec.UTF8);
                    break;
                case KEY_VALUE_LIST:
                    this.outputType = new KeyValueListOutput(StringCodec.UTF8);
                    break;
                case DOUBLE:
                    this.outputType = new DoubleOutput(StringCodec.UTF8);
                    break;
                case DOUBLE_LIST:
                    this.outputType = new DoubleListOutput(StringCodec.UTF8);
                    break;
                case STRING_LIST:
                    this.outputType = new StringListOutput(StringCodec.UTF8);
                    break;
                case GET_COORDINATES_LIST:
                    this.outputType = new GeoCoordinatesListOutput(StringCodec.UTF8);
                    break;
                case GET_COORDINATES_VALUE_LIST:
                    this.outputType = new GeoCoordinatesValueListOutput(StringCodec.UTF8);
                    break;
                case VALUE:
                default:
                    this.outputType = new ValueOutput(StringCodec.UTF8);
                    break;
            }
            return this;
        }

        public CachePipeline build() {
            final CachePipeline cachePipeline = new CachePipeline(command, this.commandType, this.outputType, keys);
            cachePipeline.addArgs(args);
            return cachePipeline;
        }
    }
}
