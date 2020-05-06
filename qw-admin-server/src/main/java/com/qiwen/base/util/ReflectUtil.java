package com.qiwen.base.util;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.AbstractConverter;
import org.apache.commons.beanutils.converters.DateConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectUtil {

    /**
     * 只能指定处理 String 类型的转换
     */
    public static class CustomStringConverter extends AbstractConverter {

        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public CustomStringConverter() {
            super();
        }

        public CustomStringConverter(Object defaultValue) {
            super(defaultValue);
        }

        @Override
        public Object convert(Class type, Object value) {
            if(value instanceof Date) {
                return sdf.format(value);
            }
            return super.convert(type, value);
        }

        protected Class getDefaultType() {
            return String.class;
        }

        protected Object convertToType(Class type, Object value) throws Throwable {
            return value.toString();
        }
    }

    private static void preProcess() {
        // 日期转换格式
        DateConverter dateConverter = new DateConverter(null);
        dateConverter.setPatterns(new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss"});
        ConvertUtils.register(dateConverter, Date.class);
        // 字符串日期转换格式
        ConvertUtils.register(new CustomStringConverter(null), String.class);
    }

    /**
     * @Name: getClass
     * @Description: 获取bean中的字段和相应类型并且以键值对的方式保存在一个Map集合中保存并返回。
     * 不支持父类字段信息的查找!
     * @Author: YXC（作者）
     * @Version: V1.00 （版本号）
     * @Create Date: 2016-9-12 （创建日期）
     * @Parameters: Object bean(目标bean)
     * @Return: 返回以fieldName->fielsName为键值对的Map集合，当出现任何异常时返回null.
     */
    public static Map<String, Object> bean2Map(Object bean) {
        Map<String, Object> fieldNameAndValueMap = new HashMap<String, Object>();

        try {
            List<Field> fields = getAllField(bean.getClass());

            for (int i = 0; fields != null && i < fields.size(); i++) {
                fields.get(i).setAccessible(true); //暴力反射。
                String fieldName = fields.get(i).getName();
                Object valueValue = fields.get(i).get(bean);
                fieldNameAndValueMap.put(fieldName, valueValue);
            }
            return fieldNameAndValueMap.size() == 0 ? null : fieldNameAndValueMap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Name: getClass
     * @Description: 获取bean中的字段和相应类型并且以键值对的方式保存在一个Map集合中保存并返回。
     * 不支持父类字段信息的查找!
     * @Author: YXC（作者）
     * @Version: V1.00 （版本号）
     * @Create Date: 2016-9-12 （创建日期）
     * @Parameters bean 目标bean
     * @param isInclude 是否是包含
     * @param filedNames 包含字段
     * @Return: 指定过滤条件过滤之后的 Map
     */
    public static Map<String, Object> bean2Map(Object bean, boolean isInclude, String... filedNames) {
        Map<String, Object> fieldNameAndValueMap = new HashMap<String, Object>();

        try {
            List<Field> fields = getAllField(bean.getClass());
            List<String> nameList = Arrays.asList(filedNames);

            for (int i = 0; fields != null && i < fields.size(); i++) {
                Field field = fields.get(i);
                field.setAccessible(true); //暴力反射。
                String fieldName = field.getName();
                if(isInclude != nameList.contains(fieldName)) continue;
                Object valueValue = field.get(bean);
                fieldNameAndValueMap.put(fieldName, valueValue);
            }

            return fieldNameAndValueMap.size() == 0 ? null : fieldNameAndValueMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Name: getClass
     * @Description: 获取bean中的字段和相应类型并且以键值对的方式保存在一个Map集合中保存并返回。
     * 不支持父类字段信息的查找!
     * @Author: YXC（作者）
     * @Version: V1.00 （版本号）
     * @Create Date: 2016-9-12 （创建日期）
     * @param  srcList 目标集合
     * @param isInclude 是否是包含
     * @param filedNames 包含字段
     * @Return 指定过滤条件过滤之后的结果集合
     */
    public static <T> List<Map<String, Object>> beanList2MapList(List<T> srcList, final boolean isInclude, final String... filedNames) {
        return srcList.stream()
                .map(bean -> ReflectUtil.bean2Map(bean, isInclude, filedNames))
                .collect(Collectors.toList());
    }

    /**
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @Name: FieldValue
     * @Description: 将两个字段中相同类型并且名字相同的资源信息对拷，注意:引用类型的拷贝是引用拷贝。
     * 不支持父类字段信息的查找，例如:不能讲Student的信息对拷如Perosn中，即使它们
     * 之间是继承的关系，但是在此方法中此种关系是不可见的。
     * @Author: YXC（作者）
     * @Version: V1.00 （版本号）
     * @Create Date: 2016-9-13 （创建日期）
     * @Parameters: Object bean(目标bean)
     * @Return: 返回拷入的对象，如果srcObj为null，则返回null。
     */
    public static <T> T O2ObySameField(Object srcObj, Class<T> targetClass) {
        if (srcObj == null) {
            return null;
        }

        //对拷。
        try {
            preProcess();
            T targetObj = targetClass.newInstance();
            BeanUtils.copyProperties(targetObj, srcObj);
            return targetObj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Name: beanType2Map
     * @Description: 获取clazz中的字段和相应类型并且以键值对的方式保存在一个Map集合中保存并返回。
     * 不支持父类字段信息的查找!
     * @Author: YXC（作者）
     * @Version: V1.00 （版本号）
     * @Create Date: 2016-9-13 （创建日期）
     * @Parameters: Class clazz(目标类型)
     * @Return: 返回该对象所有字段名称(key)和其所对应的字段类型(Value)作为键值对的Map集合，没有字段信息的时候返回null。
     */
    public static Map<String, Class> beanType2Map(Class clazz) {
        Map<String, Class> fieldNameAndTypeMap = new HashMap<String, Class>();

        try {
            List<Field> fields = getAllField(clazz);

            for (int i = 0; fields != null && i < fields.size(); i++) {
                String name = fields.get(i).getName();
                Class type = fields.get(i).getType();
                fieldNameAndTypeMap.put(name, type);
            }

            return fieldNameAndTypeMap.size() == 0 ? null : fieldNameAndTypeMap;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * @Name: replaceListElementType
     * @Description: 替换List集合中的元素类型，以前元素中的数据将会被拷贝新的集合元素之中，
     * 注意:这种拷贝仅仅是"原集合中的元素"和"现集合中的元素"中具有"相同字段名"
     * 并且"类型"相同的拷贝，如果字段为引用类型的，这种拷贝是引用拷贝!
     * @Author: YXC（作者）
     * @Version: V1.00 （版本号）
     * @Create Date: 2016-9-13 （创建日期）
     * @Parameters: Object bean(目标bean)
     * @Return: 返回元素类型被替换之后的新的集合对象。srcList为null或者为大小(size)为0的时候返回null.
     */
    public static <T> List<T> replaceListElementType(List srcList, Class<T> targetListElementType) {
        if (srcList == null || srcList.size() == 0) {
            return null;
        }

        List<T> targetList = new ArrayList<T>();

        for (Object srcEle : srcList) {
            T targetEle = (T) O2ObySameField(srcEle, targetListElementType);
            targetList.add(targetEle);
        }

        return targetList;
    }


    /**
     * @Name: isEmpty
     * @Description: obj为null，返回true。
     * 为String类型，只要去除首尾的空格之后等于""(空字符串)， 返回true。
     * obj为Number类型，只要数值为0，返回true。
     * obj为Map类型，如果没有任何元素，返回true。
     * obj为Collection类型，如果没有任何元素， 放回true。
     * obj为数组时，当其中的数值全部为null，返回true。
     * @Author: YXC（作者）
     * @Version: V1.00 （版本号）
     * @Create Date: 2016-9-15 （创建日期）
     * @Parameters: Object obj
     * @Return: 判断结果(boolean)
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object obj) {

        if (obj == null) {
            return true;
        }

        if (obj instanceof String) {
            return ((String) obj).trim().equals("");
        }

        if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        }

        if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        }

        if (obj.getClass().isArray()) {
            boolean isEnd = false;

            for (int i = 0; !isEnd; i++) {
                try {
                    if (Array.get(obj, i) != null) {
                        return false;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    isEnd = true;
                }
            }

            return true;
        }

        //以上条件均不满足，返回false
        return false;
    }

    /**
     * @Name: reverse
     * @Description: 支持将传入的obj对象中存放的数据进行反转操作。
     * 该操作虽然不会影响原集合元素的顺序，但是新的集合中的应用类型数据
     * 都是原来集合中的元素，这点需要特别注意。
     * 支持的类型: String, Array, Collection, Map。
     * @Author: YXC（作者）
     * @Version: V1.00 （版本号）
     * @Create Date: 2017-05-22 （创建日期）
     * @Parameters: Object obj
     * @Return: 反转之后的对象。
     */
    public Object reverse(Object obj) {
        return null;
    }

    private static Object[] revserveArr(Object[] srcArr) {
        Object[] buff = srcArr;
        Object temp = null;
        int size = srcArr.length;

        for (int i = 0; i < size / 2; i++) {
            temp = buff[i];
            buff[i] = buff[size - 1 - i];
            buff[size - 1 - i] = temp;
        }

        return buff;
    }

    /**
     * 获取target中指定的字段信息。
     */
    public static Object getFieldValue(Object target, String fieldName) {
        try {
            Field field = getFieldByFieldName(target.getClass(), fieldName);

            if (field != null) {
                field.setAccessible(true);
                Object value = field.get(target);
                return value;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将target的名字为fieldName的属性值设置为value
     *
     * @param target
     * @param fieldName
     * @param value
     */
    public static void setFieldValue(Object target, String fieldName, Object value) {
        try {
            preProcess();
            BeanUtils.setProperty(target, fieldName, value);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * 将 Map 中的键值对赋值到相应 target 中同名且通类型的字段.
     *
     * @param target
     * @param map
     */
    public static void setFieldValue(Object target, Map<String, Object> map) {
        try {
            preProcess();
            BeanUtils.copyProperties(target, map);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * 获取一个类的所有定义的字段, 包括父类中的.
     *
     * @param clazz
     * @return
     */
    public static List<Field> getAllField(Class clazz) {
        List<Field> fieldList = new ArrayList<Field>();
        //获取所有的类, 包括父类.
        List<Class> clazzList = new ArrayList<Class>();
        clazzList.add(clazz);

        while (clazz != null) {
            Class superClazz = clazz.getSuperclass();
            clazz = superClazz;

            if (superClazz.getName().equals(Object.class.getName())) {
                break;
            }

            clazzList.add(superClazz);
        }

        //获取所有字段信息。
        for (Class tClazz : clazzList) {
            fieldList.addAll(Arrays.asList(tClazz.getDeclaredFields()));
        }

        return fieldList;
    }

    /**
     * 获取一个类的所有定义的方法, 包括父类中的.
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAllDeclaredMethod(Class clazz) {
        List<Method> methodList = new ArrayList<Method>();
        //获取所有的类, 包括父类.
        List<Class> clazzList = new ArrayList<Class>();
        clazzList.add(clazz);

        while (clazz != null) {
            Class superClazz = clazz.getSuperclass();
            clazz = superClazz;

            if (superClazz.getName().equals(Object.class.getName())) {
                break;
            }

            clazzList.add(superClazz);
        }

        //获取所有字段信息。
        for (Class tClazz : clazzList) {
            methodList.addAll(Arrays.asList(tClazz.getDeclaredMethods()));
        }

        return methodList;
    }

    /**
     * 按照 fieldName 查找相应的字段对象.
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Field getFieldByFieldName(Class clazz, String fieldName) {
        List<Field> fieldList = getAllField(clazz);

        if (fieldList == null) {
            return null;
        }

        for (Field field : fieldList) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }

        return null;
    }

    /**
     * 获取 javabean 的所有字段名称。
     *
     * @param clazz
     * @return
     */
    public static List<String> getBeanFieldNames(Class clazz, String... excludeNames) {
        List<String> fieldNameList = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();

        for (int i = 0; declaredFields != null && i < declaredFields.length; i++) {
            String fieldName = declaredFields[i].getName();
            boolean excluedFlag = false;

            for (int j = 0; excludeNames != null && j < excludeNames.length; j++) {
                if (fieldName.equals(excludeNames[j])) {
                    excluedFlag = true;
                    break;
                }
            }

            if (!excluedFlag) {
                fieldNameList.add(fieldName);
            }
        }

        return fieldNameList;
    }

}