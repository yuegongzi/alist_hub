package org.alist.hub.service;

import org.alist.hub.repository.Condition;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface GenericService<T, ID> {
    /**
     * 根据给定的例子查询一个实体。
     *
     * @param example 用于查询的实体例子，基于这个例子的属性进行匹配查询。
     * @param <S>     查询实体的类型，该类型必须是T的子类型。
     * @return 返回一个Optional，如果找到匹配的实体，则包含该实体，否则返回空。
     */
    <S extends T> Optional<S> findOne(Example<S> example);

    /**
     * 根据给定的例子查询所有实体，并分页返回结果。
     *
     * @param example  用于查询的实体例子，基于这个例子的属性进行匹配查询。
     * @param pageable 分页参数，包括页码、页大小等。
     * @param <S>      查询实体的类型，该类型必须是T的子类型。
     * @return 返回一个Page对象，包含查询结果的集合及分页信息。
     */
    <S extends T> Page<S> findAll(Example<S> example, Pageable pageable);

    /**
     * 根据给定的例子查询匹配的实体总数。
     *
     * @param example 用于查询的实体例子，基于这个例子的属性进行匹配查询。
     * @param <S>     查询实体的类型，该类型必须是T的子类型。
     * @return 返回匹配的实体总数。
     */
    <S extends T> long count(Example<S> example);

    /**
     * 根据给定的例子判断是否存在匹配的实体。
     *
     * @param example 用于查询的实体例子，基于这个例子的属性进行匹配查询。
     * @param <S>     查询实体的类型，该类型必须是T的子类型。
     * @return 如果存在匹配的实体返回true，否则返回false。
     */
    <S extends T> boolean exists(Example<S> example);

    /**
     * 根据给定的例子进行查询，并通过函数式接口定制查询行为。
     *
     * @param example       用于查询的实体例子，基于这个例子的属性进行匹配查询。
     * @param queryFunction 一个函数式接口，用于定制查询行为，例如选择特定的字段等。
     * @param <S>           查询实体的类型，该类型必须是T的子类型。
     * @param <R>           查询结果的类型。
     * @return 返回查询定制函数R类型的结果。
     */
    <S extends T, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction);

    /**
     * 刷新当前Session，将所有更改写入数据库。
     */
    void flush();

    /**
     * 保存实体并立即刷新Session。
     *
     * @param entity 要保存的实体，其类型是T的子类型。
     * @param <S>    实体类型，继承自T。
     * @return 保存后的实体。
     */
    <S extends T> S saveAndFlush(S entity);

    /**
     * 保存多个实体并立即刷新Session。
     *
     * @param entities 要保存的实体集合，其类型是S的集合，S继承自T。
     * @param <S>      实体类型，继承自T。
     * @return 保存后的实体列表。
     */
    <S extends T> List<S> saveAllAndFlush(Iterable<S> entities);

    /**
     * 批量删除指定实体。
     *
     * @param entities 要删除的实体集合。
     */
    void deleteAllInBatch(Iterable<T> entities);

    /**
     * 批量删除指定ID的实体。
     *
     * @param ids 要删除的实体的ID集合。
     */
    void deleteAllByIdInBatch(Iterable<ID> ids);

    /**
     * 批量删除所有实体。
     */
    void deleteAllInBatch();

    /**
     * 通过ID获取实体的引用，不会从数据库加载实体属性。
     *
     * @param id 实体的ID。
     * @return 实体的引用。
     */
    T getReferenceById(ID id);

    /**
     * 根据给定的示例查询实体列表。
     *
     * @param example 查询示例，包含要匹配的属性及其条件。
     * @param <S>     查询实体类型，继承自T。
     * @return 匹配的实体列表。
     */
    <S extends T> List<S> findAll(Example<S> example);

    /**
     * 根据给定的示例和排序条件查询实体列表。
     *
     * @param example 查询示例，包含要匹配的属性及其条件。
     * @param sort    排序条件。
     * @param <S>     查询实体类型，继承自T。
     * @return 匹配的实体列表。
     */
    <S extends T> List<S> findAll(Example<S> example, Sort sort);

    <S extends T> List<S> findAll(Class<S> entityClass, List<Condition> conditions);

    /**
     * 保存单个实体。
     *
     * @param entity 要保存的实体，类型为S，S是T的子类型。
     * @param <S>    实体类型，继承自T。
     * @return 返回保存后的实体。
     */
    <S extends T> S save(S entity);

    /**
     * 批量保存实体。
     *
     * @param entities 要保存的实体集合，类型为Iterable<S>，S是T的子类型。
     * @param <S>      实体类型，继承自T。
     * @return 返回保存后的实体集合。
     */
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);

    /**
     * 根据ID查找实体。
     *
     * @param id 要查找的实体的ID。
     * @return 如果找到，返回Optional包含的实体；如果未找到，返回空的Optional。
     */
    Optional<T> findById(ID id);

    /**
     * 根据ID判断实体是否存在。
     *
     * @param id 要检查的实体的ID。
     * @return 实体存在返回true，否则返回false。
     */
    boolean existsById(ID id);

    /**
     * 查找所有实体。
     *
     * @return 返回所有实体的集合。
     */
    Iterable<T> findAll();

    /**
     * 根据ID集合查找所有实体。
     *
     * @param ids 要查找实体的ID集合。
     * @return 返回对应ID集合的所有实体集合。
     */
    Iterable<T> findAllById(Iterable<ID> ids);

    /**
     * 计算实体总数。
     *
     * @return 返回实体总数。
     */
    long count();

    /**
     * 根据ID删除实体。
     *
     * @param id 要删除实体的ID。
     */
    void deleteById(ID id);

    /**
     * 删除单个实体。
     *
     * @param entity 要删除的实体。
     */
    void delete(T entity);

    /**
     * 根据ID集合批量删除实体。
     *
     * @param ids 要删除实体的ID集合。
     */
    void deleteAllById(Iterable<? extends ID> ids);

    /**
     * 批量删除实体。
     *
     * @param entities 要删除的实体集合。
     */
    void deleteAll(Iterable<? extends T> entities);

    /**
     * 清空所有实体。
     */
    void deleteAll();

    /**
     * 根据排序条件查找所有实体。
     *
     * @param sort 排序条件。
     * @return 返回按照排序条件查找的所有实体集合。
     */
    Iterable<T> findAll(Sort sort);

    /**
     * 根据分页条件查找所有实体。
     *
     * @param pageable 分页条件。
     * @return 返回分页查询结果。
     */
    Page<T> findAll(Pageable pageable);

    /**
     * 查询指定实体类的所有数据。
     *
     * @param entityClass 实体类的Class对象，用于指定查询的数据类型。
     * @param conditions  查询条件列表，可以为空，表示无条件查询。
     * @param pageable    分页参数，包括页码和每页大小等信息。
     * @return 返回查询结果的Page对象，包含当前页的数据列表和分页信息。
     */
    Page<T> findAll(Class<T> entityClass, List<Condition> conditions, Pageable pageable);

}
