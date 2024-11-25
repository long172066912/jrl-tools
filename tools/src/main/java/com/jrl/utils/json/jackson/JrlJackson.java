package com.jrl.utils.json.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.jrl.utils.json.JrlJson;

import java.io.InputStream;
import java.text.SimpleDateFormat;

/**
 * jackson实现
 *
 * @author JerryLong
 */
public class JrlJackson implements JrlJson<JsonNode> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final JrlJackson JRL_JACKSON = new JrlJackson();

    private JrlJackson() {
        // 初始化,这是Jackson所谓的key缓存：对JSON的字段名是否调用String#intern方法，放进字符串常量池里，以提高效率,设置为false。
        objectMapper.getFactory().disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
        //反序列化忽略未知属性，不报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //默认情况下（false）parser解析器是不能解析包含控制字符的json字符串，设置为true不报错。
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //属性为NULL不被序列化，只对bean起作用，Map List不起作用
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 指定类型的序列化, 不同jackson版本对不同的类型的默认规则可能不一样，这里做强制指定
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(java.sql.Date.class, new DateSerializer(null, new SimpleDateFormat("yyyy-MM-dd")));
        objectMapper.registerModule(simpleModule);
        //org.json.JSONArray、org.json.JSONObject 序列化反序列化
        objectMapper.registerModule(new JsonOrgModule());
    }

    public static JrlJackson getInstance() {
        return JRL_JACKSON;
    }

    @Override
    public <T> T fromJson(String json, Class<T> valueType) throws Exception {
        return objectMapper.readValue(json, valueType);
    }

    @Override
    public <T> T fromJson(String json, TypeReference<T> valueTypeRef) throws Exception {
        return objectMapper.readValue(json, valueTypeRef);
    }

    @Override
    public <T> T fromJson(InputStream stream, Class<T> valueType) throws Exception {
        return objectMapper.readValue(stream, valueType);
    }

    @Override
    public String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Override
    public JsonNode readTree(String json) throws Exception {
        return objectMapper.readTree(json);
    }
}
