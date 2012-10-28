package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class SaveNetworkConfigAction {

    private final SearchableListModel listModel;
    private final Model windowModel;

    public SaveNetworkConfigAction(SearchableListModel listModel, Model windowModel )
    {
        this.listModel = listModel;
        this.windowModel = windowModel;
    }

    public void execute(){
        Frontend.RunAction(VdcActionType.CommitNetworkChanges, new VdsActionParameters(((VDS)(listModel.getEntity())).getId()),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded())
                        {
                            if (windowModel != null)
                            {
                                windowModel.StopProgress();
                                listModel.setWindow(null);
                                listModel.setConfirmWindow(null);
                                listModel.Search();
                            }
                        }

                    }
                }, null);
    }

}