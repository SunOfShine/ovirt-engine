package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user;

import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPanelPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class UserSubTabPanelPresenter extends AbstractSubTabPanelPresenter<UserSubTabPanelPresenter.ViewDef, UserSubTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<UserSubTabPanelPresenter> {
    }

    public interface ViewDef extends TabView, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<RequestTabsHandler>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabContent = new Type<RevealContentHandler<?>>();

    @Inject
    public UserSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs);
    }

}
