package com.qgutech.fs.service.base;


import com.qgutech.fs.domain.base.BaseEntity;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import java.util.List;
import java.util.Map;

/**
 * 这个借口抽象出一些常用服务的通用方法。可以被其它服务接口集成。
 */
public interface BaseService<T extends BaseEntity> {
    /**
     * 根据传入的实体是否主键被设置来判断是执行插入（主键未设置）
     * 或者更新操作（主键被调用方设置了）
     *
     * @param model 要保存的实体。
     * @return 保存实体的主键。
     */
    String save(T model);

    /**
     * 该方法用于根据实体主键获取实体类
     * <p>load Hibernate load</p>
     *
     * @param modelId 实体主键
     * @return 对应实体T
     */
    T load(String modelId);

    /**
     * 根据实体主键和实体属性名称获得对应的实体
     *
     * @param modelId    实体主键
     * @param fieldNames 实体属性名称，属性传值如下：<ul>
     *                   <li>如果为当前实体内的属性，及使用属性名称即可，
     *                   如要获得ScTransfer的流水号，使用属性名称为serNum即可</li>
     *                   <li>如果为当前实体的外键属性，使用关联属性.外键。
     *                   如要获得ScTransfer的转账人id，使用fromAccount.accountId</li>
     *                   <li>如果为当前实体的关联实体属性，使用关联属性.属性。
     *                   如要获得ScTransfer的转账人姓名，使用fromAccount.accountName</li>
     *                   <li>不支持三级关联，即A.B.C类型</li>
     *                   </ul>
     * @return 对应实体T
     */
    T get(String modelId, String... fieldNames);

    /**
     * 根据实体属性名称和属性值获取实体对象。
     *
     * @param fieldName  实体属性名称,<b>必须是唯一键<b/>
     * @param fieldValue 属性值
     * @param fieldNames 要获取的属性，可以为空，为空时获取全部持久化属性。
     * @return 实体对象
     */
    T getByFieldNameAndValue(String fieldName, Object fieldValue, String... fieldNames);

    /**
     * 根据查询条件和属性值获取实体对象
     *
     * @param criterion  查询条件，条件唯一确定一条记录,不可为null。
     * @param fieldNames 要获取的属性，可以为空，为空时获取全部持久化属性。
     * @return 实体对象
     */
    T getByCriterion(Criterion criterion, String... fieldNames);

    /**
     * 根据指定查询条件属性名称，指定查询条件属性值以及要获取的属性按顺序获取第一个实体对象
     *
     * @param fieldName  指定查询条件属性名称，不可为空。
     * @param fieldValue 指定查询条件属性值
     * @param orders     排序列表，排序顺序按照数组顺序，不可为空。
     * @param fieldNames 要获取的属性，可以为空，为空时获取全部持久化属性。
     * @return 实体对象
     */
    T getByFieldNameAndValue(String fieldName, Object fieldValue, Order[] orders, String... fieldNames);

    /**
     * 根据指定查询条件以及要获取的属性按顺序获取第一个实体对象
     *
     * @param criterion  查询条件，不可为null。
     * @param orders     排序列表，排序顺序按照数组顺序，不可为空。
     * @param fieldNames 要获取的属性，可以为空，为空时获取全部持久化属性。
     * @return 实体对象
     */
    T getByCriterion(Criterion criterion, Order[] orders, String... fieldNames);

    /**
     * 根据实体主键和属性名获取属性值。
     *
     * @param modelId   实体主键，不可为空
     * @param fieldName 属性名,不可以为空
     * @param <V>       指定查询结果属性值对应的类型
     * @return 要获取的属性值
     */
    <V> V getFieldValueById(String modelId, String fieldName);

    /**
     * 根据查询条件属性名，查询条件属性值，属性名获取属性值。
     *
     * @param name      查询条件属性名，不可为空
     * @param value     查询条件属性值
     * @param fieldName 属性名,不可以为空
     * @param <V>       指定查询结果属性值对应的类型
     * @return 要获取的属性值
     */
    <V> V getFieldValueByFieldNameAndValue(String name, Object value, String fieldName);

    /**
     * 根据查询条件和属性获取属性值
     *
     * @param criterion 查询条件，不可为null
     * @param fieldName 属性名,不可以为空
     * @param <V>       指定查询结果属性值对应的类型
     * @return 要获取的属性值
     */
    <V> V getFieldValueByCriterion(Criterion criterion, String fieldName);

    /**
     * 根据指定查询条件属性名称，指定查询条件属性值以及指定查询结果属性名称按顺序获取第一个值
     *
     * @param name      指定查询条件属性名称，不可为空。
     * @param value     指定查询条件属性值
     * @param orders    排序列表，排序顺序按照数组顺序，不可为空。
     * @param fieldName 指定查询结果属性名称，不可为空。
     * @param <V>       指定查询结果属性值对应的类型
     * @return 指定查询结果属性值
     */
    <V> V getFieldValueByFieldNameAndValue(String name, Object value, Order[] orders, String fieldName);

    /**
     * 根据指定查询条件以及指定查询结果属性名称按顺序获取第一个值
     *
     * @param criterion 指定查询条件，不可为null。
     * @param orders    排序列表，排序顺序按照数组顺序，不可为空。
     * @param fieldName 指定查询结果属性名称，不可为空。
     * @param <V>       指定查询结果属性值对应的类型
     * @return 指定查询结果属性值
     */
    <V> V getFieldValueByCriterion(Criterion criterion, Order[] orders, String fieldName);

    /**
     * 判断实体是否存在
     *
     * @param modelId 实体主键，不可为空。
     * @return 存在返回true，不存在返回false
     */
    boolean exist(String modelId);

    /**
     * 指定查询条件属性名称以及指定查询条件属性值判断是否又满足的记录
     *
     * @param fieldName  指定查询条件属性名称，不可为空。
     * @param fieldValue 指定查询条件属性值
     * @return 存在返回true，不存在返回false
     */
    boolean exist(String fieldName, Object fieldValue);

    /**
     * 判断满足查询条件的记录是否存在
     *
     * @param criterion 查询条件，不可为null。
     * @return 存在返回true，不存在返回false
     */
    boolean exist(Criterion criterion);

    /**
     * 根据属性名称和属性值获得存在的记录数
     *
     * @param fieldName  属性名称，不可为空。
     * @param fieldValue 属性值
     * @return 存在的记录数
     */
    Long getRowCountByFieldNameAndValue(String fieldName, Object fieldValue);

    /**
     * 根据查询条件获取满足条件的记录数
     *
     * @param criterion 查询条件，不可为null。
     * @return 存在的记录数
     */
    Long getRowCountByCriterion(Criterion criterion);

    /**
     * 根据指定查询条件属性名称以及指定查询条件属性值对指定属性做sum操作
     *
     * @param fieldName  指定查询条件属性名称，不可为空。
     * @param fieldValue 指定查询条件属性值。
     * @param sumField   做sum的属性名称
     * @param <V>        sum后的返回值类型
     * @return sum后的返回值
     */
    <V> V sumByFieldNameAndValue(String fieldName, Object fieldValue, String sumField);

    /**
     * 根据sum操作条件对指定属性做sum操作
     *
     * @param criterion sum操作条件
     * @param sumField  做sum的属性名称
     * @param <V>       sum后的返回值类型
     * @return sum后的返回值
     */
    <V> V sumByCriterion(Criterion criterion, String sumField);

    /**
     * 根据查询条件，对查询结果按groupField字段做分组操作
     * ，然后分组之内的记录在sumField上做求和运算，最后获得分组字段和求和字段映射。
     *
     * @param criterion  查询条件，不可为null。
     * @param groupField 分组字段，不可为空。
     * @param sumField   求和字段，为空为空。
     * @param <K>        分组字段类型
     * @param <V>        求和字段类型
     * @return 分组字段和求和字段映射。
     */
    <K, V> Map<K, V> groupSumByCriterion(Criterion criterion, String groupField, String sumField);

    /**
     * 根据查询条件，对查询结果按groupField字段做分组操作，然后求各个分组在field上的最大值，最后获得分组字段和最大值字段映射。
     *
     * @param criterion  查询条件，不可为null。
     * @param groupField 分组字段，不可为空。
     * @param maxField   最大值字段，不可为空。
     * @param <K>        分组字段类型。
     * @param <V>        最大值字段类型。
     * @return 分组字段和最大值字段映射。
     */
    <K, V> Map<K, V> groupByCriterion(Criterion criterion, String groupField, String maxField);

    /**
     * 根据查询条件，对查询结果按groupField字段做分组操作，然后分组之内的记录按照排序列表排序，
     * ，最后求各个分组在field上的最大值，最后获得分组字段和最大值字段映射。
     *
     * @param criterion  查询条件，不可为null。
     * @param orders     排序列表，排序顺序按照数组顺序，不可为空。
     * @param groupField 分组字段，不可为空。
     * @param field      最大值字段，不可为空。
     * @param <K>        分组字段类型。
     * @param <V>        最大值字段类型。
     * @return 分组字段和最大值字段映射。
     */
    <K, V> Map<K, V> groupByCriterion(Criterion criterion, Order[] orders, String groupField, String field);

    /**
     * 根据实体主键列表以及要获得的属性列表获取实体对象列表，包含实体主键。
     *
     * @param modelIds   实体主键列表，不可为空。
     * @param fieldNames 要获取的属性，可以为空，为空时获取全部持久化属性。
     * @return 实体对象列表
     */
    List<T> listByIds(List<String> modelIds, String... fieldNames);

    /**
     * 根据实体主键列表以及要获得的属性列表获按指定顺序取实体对象列表，包含实体主键。
     *
     * @param modelIds   实体主键列表，不可为空。
     * @param orders     排序列表，排序顺序按照数组顺序，不可为空。
     * @param fieldNames 要获取的属性，可以为空，为空时获取全部持久化属性。
     * @return 实体对象列表
     */
    List<T> listByIds(List<String> modelIds, Order[] orders, String... fieldNames);

    /**
     * 根据实体属性名称和属性值以及要获得的属性列表获取实体对象列表
     * 实体对象包含fieldName的值
     *
     * @param fieldName  实体属性名称,不可为空。
     * @param fieldValue 实体属性值
     * @param fieldNames 要获取的属性，可以为空，为空时获取全部持久化属性。
     * @return 实体对象列表
     */
    List<T> listByFieldNameAndValue(String fieldName, Object fieldValue, String... fieldNames);

    /**
     * 根据实体属性名称和属性值以及要获得的属性列表按指定顺序获取实体对象列表
     * 实体对象包含fieldName的值
     *
     * @param fieldName  实体属性名称,不可为空。
     * @param fieldValue 实体属性值
     * @param orders     排序列表，排序顺序按照数组顺序，不可为空。
     * @param fieldNames 要获取的属性，可以为空，为空时获取全部持久化属性。
     * @return 实体对象列表
     */
    List<T> listByFieldNameAndValue(String fieldName, Object fieldValue, Order[] orders, String... fieldNames);

    /**
     * 根据查询条件以及要获得的属性列表获取实体对象列表
     *
     * @param criterion  查询条件，不可为null
     * @param fieldNames 要获取的属性，可以为空，为空时获取全部持久化属性。
     * @return 实体对象列表
     */
    List<T> listByCriterion(Criterion criterion, String... fieldNames);

    /**
     * 根据查询条件以及要获得的属性列表按指定顺序获取实体对象列表
     *
     * @param criterion  查询条件，不可为null
     * @param orders     排序列表，排序顺序按照数组顺序，不可为空。
     * @param fieldNames 要获取的属性，可以为空，为空时获取全部持久化属性。
     * @return 实体对象列表
     */
    List<T> listByCriterion(Criterion criterion, Order[] orders, String... fieldNames);

    /**
     * 根据指定查询条件属性名称，指定查询条件属性值以及指定查询结果属性名称按顺序获取属性值的列表
     *
     * @param name      指定查询条件属性名称，不可为空。
     * @param value     指定查询条件属性值
     * @param orders    排序列表，排序顺序按照数组顺序，不可为空。
     * @param fieldName 指定查询结果属性名称，不可为空。
     * @param <V>       指定查询结果属性值对应的类型
     * @return 指定查询结果属性值的列表
     */
    <V> List<V> listFieldValueByFieldNameAndValue(String name, Object value, Order[] orders, String fieldName);

    /**
     * 根据指定查询条件属性名称，指定查询条件属性值以及指定查询结果属性名称获取属性值的列表
     *
     * @param name      指定查询条件属性名称，不可为空。
     * @param value     指定查询条件属性值
     * @param fieldName 指定查询结果属性名称，不可为空。
     * @param <V>       指定查询结果属性值对应的类型
     * @return 指定查询结果属性值的列表
     */
    <V> List<V> listFieldValueByFieldNameAndValue(String name, Object value, String fieldName);

    /**
     * 根据指定查询条件以及指定查询结果属性名称按顺序获取属性值的列表
     *
     * @param criterion 指定查询条件，不可为null。
     * @param orders    排序列表，排序顺序按照数组顺序，不可为空。
     * @param fieldName 指定查询结果属性名称，不可为空。
     * @param <V>       指定查询结果属性值对应的类型
     * @return 指定查询结果属性值的列表
     */
    <V> List<V> listFieldValueByCriterion(Criterion criterion, Order[] orders, String fieldName);

    /**
     * 根据指定查询条件以及指定查询结果属性名称获取属性值的列表
     *
     * @param criterion 指定查询条件，不可为null。
     * @param fieldName 指定查询结果属性名称，不可为空。
     * @param <V>       指定查询结果属性值对应的类型
     * @return 指定查询结果属性值的列表
     */
    <V> List<V> listFieldValueByCriterion(Criterion criterion, String fieldName);

    /**
     * 该方法用于根据实体主键删除对应实体类
     *
     * @param modelId 实体主键
     */
    int delete(String modelId);

    /**
     * 根据主键id批量删除对应的实体类
     *
     * @param modelIds 实体主键列表
     * @return 删除数据的条数
     */
    int delete(List<String> modelIds);

    /**
     * 根据条件删除记录
     *
     * @param condition 删除条件，不可为null
     * @return 删除条数
     */
    int delete(Criterion condition);

    /**
     * 根据要更新的属性更新实体对应的属性值到数据库。
     * 1、当fields为空时，根据主键更新当前实体，如果实体主键未设置，则会抛出错误。
     * 未设置的值，会保留数据库原有值，设置的值为被更新成新值。
     * 2、当fields不为空时，只更新对应的属性值到数据库。
     *
     * @param model  要更的新实体，不可为null。
     * @param fields 要更新的属性，可以为空。
     */
    void update(T model, String... fields);

    /**
     * 根据要更新的属性批量更新实体对应的属性值到数据库。
     *
     * @param models 要更的新实体列表，不可为空。
     * @param fields 要更新的属性，可以为空。
     * @see BaseService#update(T, String...)
     */
    void update(List<T> models, String... fields);

    /**
     * 根据实体主键，属性名称和属性值更新对应的属性值。
     *
     * @param modelId   要更的新实体主键，不可为空。
     * @param fieldName 要更新的属性名称，不可为空。
     * @param value     要更新的属性值，注意类型。
     *                  比如，BigDecimal类型需要如下new BigDecimal（100）
     */
    int update(String modelId, String fieldName, Object value);

    /**
     * 根据实体主键列表，属性名称和属性值批量更新对应的属性值。
     *
     * @param modelIds  要更的新实体主键列表，不可为空。
     * @param fieldName 要更新的属性名称，不可为空。
     * @param value     要更新的属性值，注意类型。
     *                  比如，BigDecimal类型需要如下new BigDecimal（100）
     */
    int update(List<String> modelIds, String fieldName, Object value);

    /**
     * 根据实体主键列表，更新对象以及要更新的字段，更新数据。
     *
     * @param modelIds 要更的新实体主键列表，不可为空。
     * @param model    更新值保存对象，保存更新的值，不可为null
     * @param fields   要更新的属性新值，可以为空，当为空时更新所有的持久化字段
     * @return 更新条数
     */
    int update(List<String> modelIds, T model, String... fields);

    /**
     * 根据属性名称和属性新值以及更新条件更新
     *
     * @param condition  更新条件，可以为null，表示更新整张表，慎用。
     * @param fieldName  要更新的属性名称，不可为空。
     * @param fieldValue 要更新的属性新值
     * @return 更新条数
     */
    int updateByCriterion(Criterion condition, String fieldName, Object fieldValue);

    /**
     * 根据更新条件，更新对象以及要更新的字段，更新数据。
     *
     * @param condition 更新条件，不可为null
     * @param model     更新值保存对象，保存更新的值，不可为null
     * @param fields    要更新的属性新值，可以为空，当为空时更新所有的持久化字段
     * @return 更新条数
     */
    int updateByCriterion(Criterion condition, T model, String... fields);

    /**
     * 将满足条件的记录在指定属性上自增或自减
     *
     * @param condition 更新条件，可以为null，表示更新整张表，慎用。
     * @param fieldName 要更新的属性名称，不可为空。
     * @param increment 自增或自减大小
     * @return 更新条数
     */
    int incrByCriterion(Criterion condition, String fieldName, Object increment);

    /**
     * 将满足条件的记录在指定incrFields属性上自增或自减，在fields上做更新。
     *
     * @param condition  更新条件，不可为null
     * @param model      更新值保存对象，保存更新的值，不可为null
     * @param incrFields 自增或自减属性，不可为null
     * @param fields     更新属性，可以为空，为空时不更新
     * @return 更新条数
     */
    int incrByCriterion(Criterion condition, T model, String[] incrFields, String... fields);

    /**
     * 根据实体主键在指定属相上自增或自减
     *
     * @param modelId   实体主键，不可为空
     * @param fieldName 自增或自减属性，不可为空
     * @param increment 自增或自减大小，不可为null
     * @return 更新条数
     */
    int incr(String modelId, String fieldName, Object increment);

    /**
     * 根据实体主键列表在指定属相上批量自增或自减
     *
     * @param modelIds  实体主键列表，不可为空
     * @param fieldName 自增或自减属性，不可为空
     * @param increment 自增或自减大小，不可为null
     * @return 更新条数
     */
    int incr(List<String> modelIds, String fieldName, Object increment);

    /**
     * 根据实体主键在指定incrFields属性上自增或自减，在fields上做更新。
     *
     * @param modelId    实体主键，不可为空
     * @param model      更新值保存对象，保存更新的值，不可为null
     * @param incrFields 自增或自减属性，不可为null
     * @param fields     更新属性，可以为空，为空时不更新
     * @return 更新条数
     */
    int incr(String modelId, T model, String[] incrFields, String... fields);

    /**
     * 根据实体主键列表在指定incrFields属性上自增或自减，在fields上做更新。
     *
     * @param modelIds   实体主键列表，不可为空
     * @param model      更新值保存对象，保存更新的值，不可为null
     * @param incrFields 自增或自减属性，不可为null
     * @param fields     更新属性，可以为空，为空时不更新
     * @return 更新条数
     */
    int incr(List<String> modelIds, T model, String[] incrFields, String... fields);

    /**
     * 批量保存实体
     *
     * @param models 实体集合
     * @return 保存成功的主键
     */
    List<String> batchSave(List<T> models);

    /**
     * 批量保存或更新实体
     *
     * @param models 实体集合
     */
    void batchSaveOrUpdate(List<T> models);
}
