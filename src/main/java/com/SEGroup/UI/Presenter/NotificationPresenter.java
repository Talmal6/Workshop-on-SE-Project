package com.SEGroup.UI.Presenter;

import com.SEGroup.Infrastructure.NotificationCenter.Notification;
import com.SEGroup.UI.Views.NotificationView;

public class NotificationPresenter {

    private NotificationView view;
    public NotificationPresenter(NotificationView view){
        this.view = view;
    }

    public void addNotification(Notification n){
        this.view.addNotification(n);
    }

}
