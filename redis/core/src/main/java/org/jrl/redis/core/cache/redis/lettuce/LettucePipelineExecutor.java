package org.jrl.redis.core.cache.redis.lettuce;

import org.jrl.redis.core.model.LettucePipelineCommand;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: LettucePipelineExecutor
 * @Description: Lettuce管道实现
 * @date 2021/2/3 11:43 AM
 */
public class LettucePipelineExecutor {

    private RedisClusterAsyncCommands pipeline;

    public LettucePipelineExecutor(RedisClusterAsyncCommands redisClusterCommands) {
        this.pipeline = redisClusterCommands;
    }

    public List<RedisFuture<Object>> pipeline(List<LettucePipelineCommand> lettucePipelineCommands) {
        return lettucePipelineCommands.stream().map(lettucePipelineCommand ->
                (RedisFuture<Object>) pipeline.dispatch(lettucePipelineCommand.getCommand(), lettucePipelineCommand.getOutput(), lettucePipelineCommand.getArgs())
        ).collect(Collectors.toList());
    }
}
