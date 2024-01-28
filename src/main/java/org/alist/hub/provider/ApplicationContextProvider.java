package org.alist.hub.provider;

import org.alist.hub.configure.HubProperties;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static HubProperties getHubProperties() {
        if (applicationContext == null) {
            throw new IllegalStateException("Application context is not set");
        }
        return applicationContext.getBean(HubProperties.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
}
