package org.ovirt.engine.core.bll.gluster;

import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;

import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.gluster.GlusterServersQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.utils.gluster.GlusterUtil;
import org.ovirt.engine.core.utils.ssh.SSHClient;

/**
 * Query to fetch list of gluster servers via ssh using the given serverName and password.
 *
 * This query will be invoked from Import Gluster Cluster dialog. In the dialog the user will provide the servername,
 * password and fingerprint of any one of the server in the cluster. This Query will validate if the given server is
 * already part of the cluster by checking with the database. If exists the query will return the error message.
 *
 * Since, the importing cluster haven't been bootstarped yet, we are running the gluster peer status command via ssh.
 *
 */
public class GetGlusterServersForImportQuery<P extends GlusterServersQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterServersForImportQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Check whether the given server is already part of the cluster
        if (getVdsStaticDao().getByHostName(getParameters().getServerName()) != null
                || getVdsStaticDao().getAllWithIpAddress(getParameters().getServerName()).size() > 0) {
            throw new RuntimeException(VdcBllMessages.SERVER_ALREADY_EXISTS_IN_ANOTHER_CLUSTER.toString());
        }

        SSHClient client = null;

        try {
            Map<String, String> serverFingerPrintMap =
                    getGlusterUtil().getPeers(getParameters().getServerName(),
                            getParameters().getPassword(),
                            getParameters().getFingerprint());

            // Check if any of the server in the map is already part of some other cluster.
            if (!validateServers(serverFingerPrintMap.keySet())) {
                throw new RuntimeException(VdcBllMessages.SERVER_ALREADY_EXISTS_IN_ANOTHER_CLUSTER.toString());
            }

            // Add the given server with it's fingerprint
            serverFingerPrintMap.put(getParameters().getServerName(), getParameters().getFingerprint());

            getQueryReturnValue().setReturnValue(serverFingerPrintMap);
        } catch (AuthenticationException ae) {
            throw new RuntimeException(VdcBllMessages.SSH_AUTHENTICATION_FAILED.toString());
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                client.disconnect();
            }
        }
    }

    protected GlusterUtil getGlusterUtil() {
        return GlusterUtil.getInstance();
    }

    /*
     * The method will return false, if the given server is already part of the existing cluster, otherwise true.
     */
    private boolean validateServers(Set<String> serverNames) {
        for (String serverName : serverNames) {
            if (getVdsStaticDao().getByHostName(serverName) != null
                    || getVdsStaticDao().getAllWithIpAddress(serverName).size() > 0) {
                return false;
            }
        }
        return true;
    }

    protected VdsStaticDAO getVdsStaticDao() {
        return DbFacade.getInstance().getVdsStaticDao();
    }
}
