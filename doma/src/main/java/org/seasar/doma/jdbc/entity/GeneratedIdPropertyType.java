/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.jdbc.entity;

import java.sql.Statement;

import org.seasar.doma.DomaNullPointerException;
import org.seasar.doma.GenerationType;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.id.IdGenerationConfig;
import org.seasar.doma.jdbc.id.IdGenerator;
import org.seasar.doma.message.Message;
import org.seasar.doma.wrapper.NumberWrapper;

/**
 * 生成される識別子のプロパティ型です。
 * 
 * @author taedium
 * 
 */
public abstract class GeneratedIdPropertyType<E, V extends Number> extends
        BasicPropertyType<E, V> {

    /** 識別子のジェネレータ */
    protected final IdGenerator idGenerator;

    /**
     * インスタンスを構築します。
     * 
     * @param entityPropertyClass
     *            プロパティのクラス
     * @param name
     *            プロパティの名前
     * @param columnName
     *            カラム名
     * @param idGenerator
     *            識別子のジェネレータ
     */
    protected GeneratedIdPropertyType(Class<V> entityPropertyClass,
            String name, String columnName, IdGenerator idGenerator) {
        super(entityPropertyClass, name, columnName, true, true);
        if (idGenerator == null) {
            throw new DomaNullPointerException("idGenerator");
        }
        this.idGenerator = idGenerator;
    }

    @Override
    public boolean isId() {
        return true;
    }

    /**
     * 識別子の生成方法を検証します。
     * 
     * @param config
     *            識別子の生成に関する設定
     */
    public void validateGenerationStrategy(IdGenerationConfig config) {
        Dialect dialect = config.getDialect();
        GenerationType generationType = idGenerator.getGenerationType();
        if (!isGenerationTypeSupported(generationType, dialect)) {
            EntityType<?> entityType = config.getEntityType();
            throw new JdbcException(Message.DOMA2021, entityType.getName(),
                    name, generationType.name(), dialect.getName());
        }
    }

    /**
     * 識別子を生成する方法がサポートされているかどうかを返します。
     * 
     * @param generationType
     *            識別子の生成方法
     * @param dialect
     *            方言
     * @return サポートされている場合 {@code true}
     */
    protected boolean isGenerationTypeSupported(GenerationType generationType,
            Dialect dialect) {
        switch (generationType) {
        case IDENTITY: {
            return dialect.supportsIdentity();
        }
        case SEQUENCE: {
            return dialect.supportsSequence();
        }
        }
        return true;
    }

    /**
     * 識別子がINSERT文に含まれるかどうかを返します。
     * 
     * @param config
     *            識別子の生成に関する設定
     * @return 含まれる場合 {@code true}
     */
    public boolean isIncluded(IdGenerationConfig config) {
        return idGenerator.includesIdentityColumn(config);
    }

    /**
     * バッチ挿入での識別子生成がサポートされているかどうかを返します。
     * 
     * @param config
     *            識別子の生成に関する設定
     * @return サポートされている場合 {@code true}
     */
    public boolean isBatchSupported(IdGenerationConfig config) {
        return idGenerator.supportsBatch(config);
    }

    /**
     * バ{@link Statement#getGeneratedKeys()} をサポートしているかどうかを返します。
     * 
     * @param config
     *            識別子の生成に関する設定
     * @return サポートされている場合 {@code true}
     */
    public boolean isAutoGeneratedKeysSupported(IdGenerationConfig config) {
        return idGenerator.supportsAutoGeneratedKeys(config);
    }

    /**
     * INSERTの実行前に識別子を生成します。
     * 
     * @param entity
     *            エンティティ
     * @param config
     *            識別子の生成に関する設定
     */
    public void preInsert(E entity, IdGenerationConfig config) {
        Long value = idGenerator.generatePreInsert(config);
        if (value != null) {
            NumberWrapper<?> wrapper = getWrapper(entity);
            wrapper.set(value);
        }
    }

    /**
     * INSERTの実行後に識別子の生成を行います。
     * 
     * @param entity
     *            エンティティ
     * @param config
     *            識別子の生成に関する設定
     * @param statement
     *            INSERT文を実行した文
     */
    public void postInsert(E entity, IdGenerationConfig config,
            Statement statement) {
        Long value = idGenerator.generatePostInsert(config, statement);
        if (value != null) {
            NumberWrapper<?> wrapper = getWrapper(entity);
            wrapper.set(value);
        }
    }

    @Override
    public abstract NumberWrapper<V> getWrapper(E entity);
}
