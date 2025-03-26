package org.jrl.redis.core.cache.redis.lettuce.codec;

import io.lettuce.core.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 扩展Lettuce StringCodec，设置key统一前缀
 *
 * @author JerryLong
 * @version V1.0
 * @date 2024/7/23 13:38
 */
public class JrlLettuceKeyCodec extends StringCodec {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlLettuceKeyCodec.class);

    public static final StringCodec UTF8 = new JrlLettuceKeyCodec(StandardCharsets.UTF_8);

    public JrlLettuceKeyCodec(Charset charset) {
        super(charset);
        //可扩展泳道固定前缀
    }

    @Override
    public void encodeKey(String key, ByteBuf target) {
        super.encodeKey(key, target);
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return super.encodeKey(key);
    }
}
