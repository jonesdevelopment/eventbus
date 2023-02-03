/*
 *  Copyright (c) 2022, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.eventbus;

import jones.eventbus.api.Event;
import jones.eventbus.api.Listener;
import jones.eventbus.api.Subscribe;
import jones.eventbus.internal.Since;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @author jonesdev.xyz
 *
 * © <a href="https://github.com/jonesdevelopment/">jonesdev.xyz</a> - Do not distribute
 */

@Since("0.0.1-dev")
@RequiredArgsConstructor
public final class EventBus {
    private final Map<Class<?>, Set<SubscribedMethod>> registered = new HashMap<>();

    public <T extends Event> void fire(final T event) {
        requireNonNull(event);

        final Set<SubscribedMethod> subscribedMethods = registered.get(event.getClass());

        if (subscribedMethods == null || subscribedMethods.isEmpty()) {
            // Save some performance
            return;
        }

        subscribedMethods.forEach(subscribed -> {
            try {
                subscribed.invoke(event);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void register(final Object subscriber) {
        requireNonNull(subscriber);

        if (!Modifier.isPublic(subscriber.getClass().getModifiers())) {
            System.err.println("Error registering event subscriber -> " + subscriber);
            System.err.println("Class is missing the public modifier (class not accessible)");
            return;
        }

        Arrays.stream(subscriber.getClass().getDeclaredMethods())
                .filter(obj -> Objects.nonNull(obj) &&
                        obj.isAnnotationPresent(Subscribe.class) &&
                        obj.getParameterTypes().length == 1 &&
                        !Modifier.isStatic(obj.getModifiers())
                        && Modifier.isPublic(obj.getModifiers())
                )
                .distinct()
                .forEach(method -> registerInternally(method.getParameterTypes()[0], subscriber.getClass(), (Listener) subscriber, method));
    }

    private void registerInternally(final Class<?> parameters,
                                    final Class<?> subscriber,
                                    final Listener listener,
                                    final Method method) {
        registered.putIfAbsent(requireNonNull(parameters), new HashSet<>());

        final SubscribedMethod subscribed = new SubscribedMethod(
                subscriber,
                listener,
                requireNonNull(method));

        registered.get(parameters).add(subscribed);
    }

    public void unregister(final Object subscriber) {
        requireNonNull(subscriber);

        final Class<?> subscriberClass = subscriber.getClass();
        registered.values().forEach(e -> {
            e.stream()
                    .filter(subscribedMethod -> subscribedMethod.subscriber == subscriberClass)
                    .toList()
                    .forEach(e::remove);
        });
    }

    record SubscribedMethod(Class<?> subscriber, Listener listener, Method method) {
        public <T extends Event> void invoke(final T event) throws Throwable {
            method.invoke(listener, event); // legacy
        }
    }
}
