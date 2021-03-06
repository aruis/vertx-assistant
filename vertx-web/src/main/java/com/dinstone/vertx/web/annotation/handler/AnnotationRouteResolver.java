/*
 * Copyright (C) 2016~2017 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dinstone.vertx.web.annotation.handler;

import java.lang.annotation.Annotation;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.dinstone.vertx.web.RouteResolver;
import com.dinstone.vertx.web.annotation.Connect;
import com.dinstone.vertx.web.annotation.Consumes;
import com.dinstone.vertx.web.annotation.Delete;
import com.dinstone.vertx.web.annotation.Get;
import com.dinstone.vertx.web.annotation.Head;
import com.dinstone.vertx.web.annotation.Options;
import com.dinstone.vertx.web.annotation.Patch;
import com.dinstone.vertx.web.annotation.Path;
import com.dinstone.vertx.web.annotation.Post;
import com.dinstone.vertx.web.annotation.Produces;
import com.dinstone.vertx.web.annotation.Put;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class AnnotationRouteResolver implements RouteResolver {

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dinstone.vertx.web.annotation.AnnotationHandler#process(io.vertx.ext.
	 * web.Router, java.lang.Object, java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	public void process(final Router router, final Object instance, final Class<?> clazz, final Method method) {
		String servicePath = getServicePath(clazz);
		if (isCompatible(method, Get.class, RoutingContext.class)) {
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			String path = servicePath + getAnnotation(method, Get.class).value();
			Route route = router.get(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Post.class, RoutingContext.class)) {
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			String path = servicePath + getAnnotation(method, Post.class).value();
			Route route = router.post(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Put.class, RoutingContext.class)) {
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			String path = servicePath + getAnnotation(method, Put.class).value();
			Route route = router.put(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Delete.class, RoutingContext.class)) {
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			String path = servicePath + getAnnotation(method, Delete.class).value();
			Route route = router.delete(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Connect.class, RoutingContext.class)) {
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			String path = servicePath + getAnnotation(method, Connect.class).value();
			Route route = router.connect(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Options.class, RoutingContext.class)) {
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			String path = servicePath + getAnnotation(method, Options.class).value();
			Route route = router.options(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Head.class, RoutingContext.class)) {
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			String path = servicePath + getAnnotation(method, Head.class).value();
			Route route = router.head(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Patch.class, RoutingContext.class)) {
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			String path = servicePath + getAnnotation(method, Patch.class).value();
			Route route = router.patch(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Path.class, RoutingContext.class)) {
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			String path = servicePath + getAnnotation(method, Path.class).value();
			Route route = router.route(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
	}

	private static void andRouteBasedContentType(Route route, final Class<?> clazz, final Method method) {
		if (route != null) {
			String[] produces = getProduces(clazz, method);
			String[] consumes = getConsumes(clazz, method);
			if (produces != null) {
				for (String contentType : produces) {
					route.produces(contentType);
				}
			}

			if (consumes != null) {
				for (String contentType : consumes) {
					route.consumes(contentType);
				}
			}
		}
	}

	private static Handler<RoutingContext> wrap(final Object instance, final MethodHandle mh) {
		return ctx -> {
			try {
				String acceptableContentType = ctx.getAcceptableContentType();
				if (acceptableContentType != null) {
					ctx.response().putHeader("Content-Type", acceptableContentType);
				}

				mh.invoke(instance, ctx);
			} catch (Throwable e) {
				ctx.fail(e);
			}
		};
	}

	private static String[] getConsumes(final Class<?> clazz, final Method method) {
		String[] consumes = null;
		Consumes defaultSetting = getAnnotation(clazz, Consumes.class);
		if (defaultSetting != null) {
			consumes = defaultSetting.value();
		}

		Consumes apiSetting = getAnnotation(method, Consumes.class);
		if (apiSetting != null) {
			consumes = apiSetting.value();
		}
		return consumes;
	}

	private static String[] getProduces(final Class<?> clazz, final Method method) {
		String[] produces = null;
		Produces defaultSetting = getAnnotation(clazz, Produces.class);
		if (defaultSetting != null) {
			produces = defaultSetting.value();
		}

		Produces apiSetting = getAnnotation(method, Produces.class);
		if (apiSetting != null) {
			produces = apiSetting.value();
		}
		return produces;
	}

	private static String getServicePath(final Class<?> clazz) {
		Path routePath = getAnnotation(clazz, Path.class);
		return routePath == null ? "" : routePath.value();
	}

	public static MethodHandle getMethodHandle(Method m, Class<?>... paramTypes) {
		try {
			Class<?>[] methodParamTypes = m.getParameterTypes();

			if (methodParamTypes != null) {
				if (methodParamTypes.length == paramTypes.length) {
					for (int i = 0; i < methodParamTypes.length; i++) {
						if (!paramTypes[i].isAssignableFrom(methodParamTypes[i])) {
							// for groovy and other languages that do not do
							// type check at compile time
							if (!methodParamTypes[i].equals(Object.class)) {
								return null;
							}
						}
					}
				} else {
					return null;
				}
			} else {
				return null;
			}

			MethodHandle methodHandle = LOOKUP.unreflect(m);
			CallSite callSite = new ConstantCallSite(methodHandle);
			return callSite.dynamicInvoker();

		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isCompatible(Method m, Class<? extends Annotation> annotation, Class<?>... paramTypes) {
		if (getAnnotation(m, annotation) != null) {
			if (getMethodHandle(m, paramTypes) != null) {
				return true;
			} else {
				throw new RuntimeException("Method signature not compatible!");
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotation(Method m, Class<T> annotation) {
		// skip static methods
		if (Modifier.isStatic(m.getModifiers())) {
			return null;
		}
		// skip non public methods
		if (!Modifier.isPublic(m.getModifiers())) {
			return null;
		}

		Annotation[] annotations = m.getAnnotations();
		// this method is not annotated
		if (annotations == null) {
			return null;
		}

		// verify if the method is annotated
		for (Annotation ann : annotations) {
			if (ann.annotationType().equals(annotation)) {
				return (T) ann;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotation(Class<?> c, Class<T> annotation) {
		// skip non public classes
		if (!Modifier.isPublic(c.getModifiers())) {
			return null;
		}

		Annotation[] annotations = c.getAnnotations();
		// this method is not annotated
		if (annotations == null) {
			return null;
		}

		// verify if the method is annotated
		for (Annotation ann : annotations) {
			if (ann.annotationType().equals(annotation)) {
				return (T) ann;
			}
		}

		return null;
	}
}
