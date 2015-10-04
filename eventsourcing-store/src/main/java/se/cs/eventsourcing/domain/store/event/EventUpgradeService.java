package se.cs.eventsourcing.domain.store.event;

import org.reflections.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

public class EventUpgradeService {

    private Map<Class<DomainEvent>, Object> eventTypeToProvider = new HashMap<>();

    public EventUpgradeService(Collection<Object> upgradeProviders) {
        upgradeProviders.forEach(this::register);
    }

    private void register(Object upgradeProvider) {
        for (Class<DomainEvent> eventType
                : supportedDomainEvents(upgradeProvider.getClass())) {

            if (eventTypeToProvider.containsKey(eventType)) {
                throw new RuntimeException(
                        String.format("Domain event type %s already registered", eventType));
            }

            eventTypeToProvider.put(eventType, upgradeProvider);
        }
    }

    public DomainEvent upgrade(DomainEvent event){
        if (!eventTypeToProvider.containsKey(event.getClass())) {
            return event;
        }

        Object provider = eventTypeToProvider.get(event.getClass());
        Method method = getUpgradeMethod(event);

        method.setAccessible(true);

        try {
            DomainEvent upgraded = (DomainEvent) method.invoke(provider, event);

            return upgraded.getClass().equals(event.getClass())
                    ? upgraded
                    : upgrade(upgraded);

        } catch (Exception e) {
            throw new RuntimeException("Could not upgrade method");
        }
    }

    private Method getUpgradeMethod(DomainEvent event) {
        Class<?> providerClass = eventTypeToProvider.get(event.getClass()).getClass();

        Set<Method> methods =
                ReflectionUtils.getMethods(providerClass,
                        ReflectionUtils.withAnnotation(EventUpgrader.class),
                        ReflectionUtils.withParametersCount(1),
                        ReflectionUtils.withParameters(event.getClass()));

        if (methods.size() != 1) {
            throw new RuntimeException(
                    String.format("Duplicate upgrade methods for event type %s in class %s.",
                            event.getClass(), providerClass));
        }

        return methods.stream().findFirst().get();
    }

    Set<Class<DomainEvent>> supportedDomainEvents(Class<?> upgradeProvider) {
        Set<Method> methods =
                ReflectionUtils.getMethods(upgradeProvider,
                        ReflectionUtils.withAnnotation(EventUpgrader.class));

        if (methods.isEmpty()) {
            throw new RuntimeException(
                    String.format("No event eventTypeToProvider found in class %s", upgradeProvider));
        }

        Set<Class<DomainEvent>> result = new HashSet<>();

        for (Method method : methods) {
            if (method.getParameterCount() != 1) {
                throw new RuntimeException(
                        String.format("Parameter count for upgrade method %s must be exactly one.", method));
            }

            result.add((Class<DomainEvent>) method.getParameters()[0].getType());
        }

        return result;
    }
}
