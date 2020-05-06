package com.qiwen.base.util;

import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

@SuppressWarnings("unchecked")
public class GsonUtil {

    public static class BigDecimalTypeEmptyToZeroAdapter implements JsonSerializer<BigDecimal>, JsonDeserializer<BigDecimal> {

        public JsonElement serialize(BigDecimal src, Type arg1, JsonSerializationContext arg2) {
            String dataString = src.toString();
            return new JsonPrimitive(dataString);
        }

        public BigDecimal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!(json instanceof JsonPrimitive)) {
                throw new JsonParseException("The date should be a string value");
            }
            try {
                String content = json.getAsString();
                BigDecimal bigDecimal = new BigDecimal(0);
                if(null != content && !content.trim().isEmpty()) {
                    bigDecimal = new BigDecimal(content);
                }
                return  bigDecimal;
            } catch (NumberFormatException e) {
                System.out.println("DateTypeAdapter input date value:" + json.getAsString());
                return null;
            }
        }
    }

    public static class DateTypeEmptyToNullAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

        public JsonElement serialize(Date src, Type arg1, JsonSerializationContext arg2) {
            //SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateFormatAsString = sdf.format(src);
            return new JsonPrimitive(dateFormatAsString);
        }

        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!(json instanceof JsonPrimitive)) {
                throw new JsonParseException("The date should be a string value");
            }
            try {
                String content = json.getAsString();
                Date date = null;
                if(null != content && !content.trim().isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    date = sdf.parse(content);
                }
                return  date;
            } catch (NumberFormatException e) {
                System.out.println("DateTypeAdapter input date value:" + json.getAsString());
                return null;
            }
            catch (ParseException e) {
                throw new JsonParseException(e);
            }
        }
    }

    public static class LocalDateTimeTypeEmptyToNullAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public JsonElement serialize(LocalDateTime src, Type arg1, JsonSerializationContext arg2) {
            //SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
            String dateFormatAsString = src.format(formatter);
            return new JsonPrimitive(dateFormatAsString);
        }

        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!(json instanceof JsonPrimitive)) {
                throw new JsonParseException("The localDateTime should be a string value");
            }
            try {
                String content = json.getAsString();
                LocalDateTime date = null;
                if(null != content && !content.trim().isEmpty()) {
                    date = LocalDateTime.parse(content, formatter);
                }
                return  date;
            } catch (DateTimeParseException e) {
                System.out.println("DateTypeAdapter input localDateTime value:" + json.getAsString());
                return null;
            }
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(Date.class, new DateTypeEmptyToNullAdapter())
            .registerTypeAdapter(BigDecimal.class, new BigDecimalTypeEmptyToZeroAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeEmptyToNullAdapter())
            .create();

    /**
     * jason字串转为对象实例
     * @param json    json格式字符串
     * @param mainClass  结果对象class
     * @param subClass  如果对象class中包含泛型字段，此为泛型class
     * @param <T>
     * @return
     */
    public static <T> T fromJsonToClass(String json, Class mainClass, Class subClass) {
        if(null == subClass) {
            return (T) GSON.fromJson(json, mainClass);
        }
        else {
            Type objectType = type(mainClass, subClass);
            return GSON.fromJson(json, objectType);
        }
    }

    public static <T> T fromJsonToClass(String json, Class<T> clz) {
        return GSON.fromJson(json, clz);
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static String toJson(Object obj, boolean prettyPrinting) {
        if(prettyPrinting) {
            return new GsonBuilder()
                    .serializeNulls()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .registerTypeAdapter(Date.class, new DateTypeEmptyToNullAdapter())
                    .setPrettyPrinting()
                    .create().toJson(obj);
        }
        else {
            return GSON.toJson(obj);
        }
    }

    /**
     * 对象实例转jason格式字符串
     * @param obj  待转实例
     * @param mainClass  对象class
     * @param subClass 如果对象class中包含泛型字段，此为泛型class
     * @return
     */
    public static String toJsonFromClass(Object obj, Class mainClass, Class subClass) {
        if(null == subClass) {
            return GSON.toJson(obj, mainClass);
        }
        else {
            Type objectType = type(mainClass, subClass);
            return GSON.toJson(obj, objectType);
        }

    }

    /**
     * @param raw
     * @param args
     * @return
     */
    public static ParameterizedType type(final Class raw, final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return raw;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }
        };
    }
}
