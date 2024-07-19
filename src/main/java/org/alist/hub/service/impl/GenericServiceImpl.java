package org.alist.hub.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.alist.hub.repository.AListHubRepository;
import org.alist.hub.repository.Condition;
import org.alist.hub.service.GenericService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Transactional
public abstract class GenericServiceImpl<T, ID extends Serializable> implements GenericService<T, ID> {

    private final AListHubRepository<T, ID> repository;
    @PersistenceContext
    protected EntityManager entityManager;

    public GenericServiceImpl(AListHubRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        return repository.findOne(example);
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return repository.findAll(example, pageable);
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        return repository.count(example);
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        return repository.exists(example);
    }

    @Override
    public <S extends T, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return repository.findBy(example, queryFunction);
    }

    @Override
    public void flush() {
        repository.flush();
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        return repository.saveAndFlush(entity);
    }

    @Override
    public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
        return repository.saveAllAndFlush(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<T> entities) {
        repository.deleteAllInBatch(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<ID> ids) {
        repository.deleteAllByIdInBatch(ids);
    }

    @Override
    public void deleteAllInBatch() {
        repository.deleteAllInBatch();
    }

    @Override
    public T getReferenceById(ID id) {
        return repository.getReferenceById(id);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        return repository.findAll(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return repository.findAll(example, sort);
    }

    @Override
    public <S extends T> List<S> findAll(Class<S> entityClass, List<Condition> conditions) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<S> cq = cb.createQuery(entityClass);
        Root<S> root = cq.from(entityClass);
        List<Predicate> predicates = new ArrayList<>();
        for (Condition condition : conditions) {
            if (condition != null) {
                Predicate predicate = condition.toPredicate(root, cb);
                predicates.add(predicate);
            }
        }
        cq.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public <S extends T> S save(S entity) {
        return repository.save(entity);
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        return repository.saveAll(entities);
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    @Override
    public Iterable<T> findAll() {
        return repository.findAll();
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        return repository.findAllById(ids);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    public void delete(T entity) {
        repository.delete(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        repository.deleteAllById(ids);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        repository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public Iterable<T> findAll(Sort sort) {
        return repository.findAll(sort);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    private List<Predicate> build(List<Condition> conditions, Root<T> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        for (Condition condition : conditions) {
            if (condition != null) {
                Predicate predicate = condition.toPredicate(root, cb);
                predicates.add(predicate);
            }
        }
        return predicates;
    }

    @Override
    public Page<T> findAll(Class<T> entityClass, List<Condition> conditions, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();


        // 构建用于查询结果集的 CriteriaQuery
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);

        // 构建查询条件
        List<Predicate> predicates = build(conditions, root, cb);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<T> typedQuery = entityManager.createQuery(query);

        // 应用分页参数
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // 查询分页结果
        List<T> resultList = typedQuery.getResultList();

        // 构建用于查询总数的 CriteriaQuery
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        List<Predicate> countPredicates = build(conditions, countRoot, criteriaBuilder);
        countQuery.select(criteriaBuilder.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));

        // 查询总记录数
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }


}
