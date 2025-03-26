package org.jrl.redis.core;

import org.jrl.redis.connect.ConnectResource;
import org.jrl.redis.core.cache.redis.commands.RedisAsyncCommands;
import org.jrl.redis.core.model.LettucePipelineCommand;
import org.jrl.redis.core.model.CachePipeline;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheExecutorFactory
 * @Description: 执行器接口
 * @date 2021/1/19 8:58 PM
 */
public interface InterfaceCacheExecutor {

    /**
     * 传入连接资源
     *
     * @param connectionResource
     * @return
     */
    void setConnectionResource(ConnectResource connectionResource);

    /**
     * 获取连接资源
     *
     * @return
     */
    Object getConnectResource();

    /**
     * 释放资源
     */
    void returnConnectResource();

    /**
     * 关闭链接
     */
    void close();

    void connectionClose();

    /**
     * 获取连接池
     *
     * @return
     */
    Object getPool();

    /**
     * 获取异步执行器
     * @return
     */
    RedisAsyncCommands async();

    /**
     * 获取异步执行器，生菜原生
     * @return
     */
    RedisClusterAsyncCommands asyncL();

    /**
     * 通过管道批量执行命令（lettuce专用）
     * @param commands
     * @return
     */
    List<Supplier<Object>> lettucePipeline(List<LettucePipelineCommand> commands);

    /**
     * 通过管道批量执行命令（lettuce专用）
     * @param commands
     * @return
     */
    CompletableFuture<List<Supplier<Object>>> lettucePipelineAsync(List<LettucePipelineCommand> commands);

    /**
     * 通过管道批量执行命令（lettuce专用）
     * @param commands
     * @return
     */
    List<Supplier<Object>> pipeline(List<CachePipeline> commands);

    /**
     * 通过管道批量执行命令（lettuce专用）
     * @param commands
     * @return
     */
    CompletableFuture<List<Supplier<Object>>> pipelineAsync(List<CachePipeline> commands);
}
