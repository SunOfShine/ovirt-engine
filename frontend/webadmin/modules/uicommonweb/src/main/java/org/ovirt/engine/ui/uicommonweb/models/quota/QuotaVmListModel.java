package org.ovirt.engine.ui.uicommonweb.models.quota;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetEntitiesRelatedToQuotaIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import java.util.ArrayList;
import java.util.List;

public class QuotaVmListModel extends SearchableListModel {

    public QuotaVmListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().vmsTitle());
        setHashName("vms"); //$NON-NLS-1$
        setIsTimerDisabled(true);
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null)
        {
            return;
        }

        super.syncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                QuotaVmListModel vmModel = (QuotaVmListModel) model;
                vmModel.setItems((ArrayList<VM>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                vmModel.setIsEmpty(((List) vmModel.getItems()).size() == 0);
            }
        };

        GetEntitiesRelatedToQuotaIdParameters tempVar =
                new GetEntitiesRelatedToQuotaIdParameters();
        tempVar.setQuotaId(((Quota) getEntity()).getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetVmsRelatedToQuotaId, tempVar, _asyncQuery);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected String getListName() {
        return "QuotaVmListModel"; //$NON-NLS-1$
    }

}
