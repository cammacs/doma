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
package org.seasar.doma.internal.apt.meta.query;

import java.sql.Blob;

import javax.lang.model.element.ExecutableElement;

import org.seasar.doma.internal.apt.Context;
import org.seasar.doma.internal.apt.reflection.BlobFactoryReflection;

/**
 * @author taedium
 * 
 */
public class BlobCreateQueryMetaFactory
        extends AbstractCreateQueryMetaFactory<BlobCreateQueryMeta> {

    public BlobCreateQueryMetaFactory(Context ctx, ExecutableElement methodElement) {
        super(ctx, methodElement, Blob.class);
    }

    @Override
    public QueryMeta createQueryMeta() {
        BlobFactoryReflection blobFactoryReflection = ctx.getReflections()
                .newBlobFactoryReflection(methodElement);
        if (blobFactoryReflection == null) {
            return null;
        }
        BlobCreateQueryMeta queryMeta = new BlobCreateQueryMeta(methodElement);
        queryMeta.setBlobFactoryReflection(blobFactoryReflection);
        queryMeta.setQueryKind(QueryKind.BLOB_FACTORY);
        doTypeParameters(queryMeta);
        doReturnType(queryMeta);
        doParameters(queryMeta);
        doThrowTypes(queryMeta);
        return queryMeta;
    }

}