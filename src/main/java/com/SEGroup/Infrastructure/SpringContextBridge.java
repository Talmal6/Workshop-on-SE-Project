// src/main/java/com/SEGroup/Infrastructure/SpringContextBridge.java
package com.SEGroup.Infrastructure;

import com.SEGroup.UI.ServiceLocator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Registers the running Spring ApplicationContext inside ServiceLocator
 * as early as possible, so all get*() helpers can delegate to real beans.
 */
@Component
public class SpringContextBridge implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        ServiceLocator.setApplicationContext(ctx);
    }
}
