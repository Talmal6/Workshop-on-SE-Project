package com.SEGroup.UI;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;

@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET_XHR)
public class AppShellConfig implements AppShellConfigurator {
    @Override
    public void configurePage(AppShellSettings settings) {
        // Optional customization
    }
}
