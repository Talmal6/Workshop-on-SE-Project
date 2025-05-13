// ui/NotificationCenter.java  (NEW)
package com.SEGroup.UI;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

@Component("uiNotificationCenter")
public class NotificationCenter {

    private final Set<UI> listeners = new CopyOnWriteArraySet<>();

    /* called once in MainLayout */
    public void register(UI ui) { listeners.add(ui); }
    public void unregister(UI ui){ listeners.remove(ui); }

    /* the Domain team will inject this bean and call notify(..) */
    public void notify(String msg){
        listeners.forEach(ui -> ui.access(
                () -> com.vaadin.flow.component.notification.Notification
                        .show(msg, 4000, com.vaadin.flow.component.notification.Notification.Position.TOP_END)
        ));
    }

    /* utility for presenters that need a callback */
    public Consumer<String> asConsumer(){ return this::notify; }
}
