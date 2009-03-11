/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.webbeans.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.context.CreationalContext;

import org.apache.webbeans.annotation.CurrentLiteral;
import org.apache.webbeans.component.AbstractComponent;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.ClassUtil;

/**
 * Field type injection.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @since 1.0
 */
public class InjectableField extends AbstractInjectable
{
    protected Field field;
    protected Object instance;

    public InjectableField(Field field, Object instance, AbstractComponent<?> owner,CreationalContext<?> creationalContext)
    {
        super(owner,creationalContext);
        this.field = field;
        this.instance = instance;
        this.injectionMember = field;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.webbeans.inject.Injectable#doInjection()
     */
    public Object doInjection()
    {
        Type type = field.getGenericType();

        Annotation[] annots = field.getAnnotations();
        
        this.injectionAnnotations = annots;

        Annotation[] bindingAnnos = AnnotationUtil.getBindingAnnotations(annots);
        
        //GE : Mark this is not used! I am commenting here!
        //Annotation[] resourceAnnos = AnnotationUtil.getResourceAnnotations(annots);

        try
        {
            if (bindingAnnos.length == 0)
            {
                bindingAnnos = new Annotation[1];
                bindingAnnos[0] = new CurrentLiteral();
            }

            if (!ClassUtil.isPublic(field.getModifiers()))
            {
                field.setAccessible(true);
            }

            Type[] args = new Type[0];
            Class<?> clazz = null;
            if (type instanceof ParameterizedType)
            {
                ParameterizedType pt = (ParameterizedType) type;

                checkParametrizedTypeForInjectionPoint(pt);
                args = pt.getActualTypeArguments();

                clazz = (Class<?>) pt.getRawType();
            }
            else
            {
                clazz = (Class<?>) type;
            }

            if (!field.isAccessible())
            {
                field.setAccessible(true);
            }

            field.set(instance, inject(clazz, args, bindingAnnos));

        }
        catch (IllegalAccessException e)
        {
            throw new WebBeansException(e);
        }

        return null;
    }
}