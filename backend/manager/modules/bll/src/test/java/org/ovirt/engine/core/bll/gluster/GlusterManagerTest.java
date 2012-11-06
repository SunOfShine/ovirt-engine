package org.ovirt.engine.core.bll.gluster;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.gluster.GlusterCoreUtil;
import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.InterfaceDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.VdsStatisticsDAO;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
import org.ovirt.engine.core.utils.ejb.EJBUtilsStrategy;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

@RunWith(MockitoJUnitRunner.class)
public class GlusterManagerTest {

    private static final String REPL_VOL_NAME = "repl-vol";

    private static final String DIST_VOL_NAME = "dist-vol";

    @Mock
    private EJBUtilsStrategy mockLookupStrategy;

    @Mock
    private TransactionManager mockTxnManager;

    @Mock
    private Transaction mockTxn;

    @Mock
    private ClusterUtils clusterUtils;

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.GlusterRefreshRateLight, 5),
            mockConfig(ConfigValues.GlusterRefreshRateHeavy, 300));

    private GlusterManager glusterManager;

    private static final String OPTION_AUTH_ALLOW = "auth.allow";
    private static final String OPTION_AUTH_REJECT = "auth.reject";
    private static final String OPTION_NFS_DISABLE = "nfs.disable";
    private static final String AUTH_REJECT_IP = "192.168.1.999";
    private static final String OPTION_VALUE_ON = "on";
    private static final String OPTION_VALUE_OFF = "off";
    private static final Guid SERVER_ID_1 = new Guid("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3");
    private static final Guid SERVER_ID_2 = new Guid("2001751e-549b-4e7a-aff6-32d36856c125");
    private static final Guid SERVER_ID_3 = new Guid("2001751e-549b-4e7a-aff6-32d36856c126");
    private static final String SERVER_NAME_1 = "srvr1";
    private static final String SERVER_NAME_2 = "srvr2";
    private static final String SERVER_NAME_3 = "srvr3";
    private static final String DIST_BRICK_D1 = "/export/test-vol-dist/dir1";
    private static final String DIST_BRICK_D2 = "/export/test-vol-dist/dir2";
    private static final String REPL_BRICK_R1D1 = "/export/test-vol-replicate-1/r1dir1";
    private static final String REPL_BRICK_R1D2 = "/export/test-vol-replicate-1/r1dir2";
    private static final String REPL_BRICK_R2D1 = "/export/test-vol-replicate-1/r2dir1";
    private static final String REPL_BRICK_R2D2 = "/export/test-vol-replicate-1/r2dir2";
    private static final String REPL_BRICK_R1D1_NEW = "/export/test-vol-replicate-1/r1dir1_new";
    private static final String REPL_BRICK_R2D1_NEW = "/export/test-vol-replicate-1/r2dir1_new";
    private static final Guid CLUSTER_ID = new Guid("ae956031-6be2-43d6-bb8f-5191c9253314");
    private static final Guid EXISTING_VOL_DIST_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final Guid EXISTING_VOL_REPL_ID = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private static final String NEW_VOL_NAME = "test-new-vol";

    @Mock
    private GlusterVolumeDao volumeDao;
    @Mock
    private GlusterBrickDao brickDao;
    @Mock
    private GlusterOptionDao optionDao;
    @Mock
    private VdsDAO vdsDao;
    @Mock
    private VdsStatisticsDAO vdsStatisticsDao;
    @Mock
    private VdsStaticDAO vdsStaticDao;
    @Mock
    private VdsDynamicDAO vdsDynamicDao;
    @Mock
    private VdsGroupDAO clusterDao;
    @Mock
    private InterfaceDAO interfaceDao;

    private VDSGroup existingCluster;
    private VDS existingServer1;
    private VDS existingServer2;
    private List<VDS> existingServers = new ArrayList<VDS>();
    private GlusterVolumeEntity existingDistVol;
    private GlusterVolumeEntity existingReplVol;
    private GlusterVolumeEntity newVolume;
    private List<GlusterVolumeEntity> existingVolumes = new ArrayList<GlusterVolumeEntity>();
    private List<Guid> removedBrickIds = new ArrayList<Guid>();
    private List<Guid> addedBrickIds = new ArrayList<Guid>();
    private List<GlusterBrickEntity> bricksWithChangedStatus = new ArrayList<GlusterBrickEntity>();

    @Before
    public void createObjects() {
        existingServer1 = createServer(SERVER_ID_1, SERVER_NAME_1);
        existingServer2 = createServer(SERVER_ID_2, SERVER_NAME_2);
        existingServers.add(existingServer1);
        existingServers.add(existingServer2);
        existingServers.add(createServer(SERVER_ID_3, SERVER_NAME_3));

        createCluster();

        existingDistVol = createDistVol(DIST_VOL_NAME, EXISTING_VOL_DIST_ID);
        existingReplVol = createReplVol();
    }

    private void createCluster() {
        existingCluster = new VDSGroup();
        existingCluster.setId(CLUSTER_ID);
        existingCluster.setname("cluster");
        existingCluster.setGlusterService(true);
        existingCluster.setVirtService(false);
    }

    private VDS createServer(Guid serverId, String hostname) {
        VdsStatic vdsStatic = new VdsStatic();
        vdsStatic.setId(serverId);
        vdsStatic.sethost_name(hostname);
        VdsDynamic vdsDynamic = new VdsDynamic();
        vdsDynamic.setstatus(VDSStatus.Up);
        return new VDS(vdsStatic, vdsDynamic, new VdsStatistics());
    }

    private GlusterVolumeEntity createDistVol(String volName, Guid volId) {
        GlusterVolumeEntity vol = createVolume(volName, volId);
        vol.addBrick(createBrick(volId, existingServer1, DIST_BRICK_D1));
        vol.addBrick(createBrick(volId, existingServer1, DIST_BRICK_D2));
        existingVolumes.add(vol);
        return vol;
    }

    private GlusterVolumeEntity createReplVol() {
        GlusterVolumeEntity vol = createVolume(REPL_VOL_NAME, EXISTING_VOL_REPL_ID);
        vol.addBrick(createBrick(EXISTING_VOL_REPL_ID, existingServer1, REPL_BRICK_R1D1));
        vol.addBrick(createBrick(EXISTING_VOL_REPL_ID, existingServer2, REPL_BRICK_R1D2));
        vol.addBrick(createBrick(EXISTING_VOL_REPL_ID, existingServer1, REPL_BRICK_R2D1));
        vol.addBrick(createBrick(EXISTING_VOL_REPL_ID, existingServer2, REPL_BRICK_R2D2));
        vol.setOption(OPTION_AUTH_ALLOW, "*");
        vol.setOption(OPTION_NFS_DISABLE, OPTION_VALUE_OFF);
        existingVolumes.add(vol);
        return vol;
    }

    private GlusterVolumeEntity createVolume(String volName, Guid id) {
        GlusterVolumeEntity vol = new GlusterVolumeEntity();
        vol.setId(id);
        vol.setName(volName);
        vol.setClusterId(CLUSTER_ID);
        return vol;
    }

    private GlusterBrickEntity createBrick(Guid existingVolDistId, VDS server, String brickDir) {
        return new GlusterBrickEntity(existingVolDistId, server.getStaticData(), brickDir, GlusterStatus.UP);
    }

    private void setupMocks() throws Exception {
        glusterManager = Mockito.spy(GlusterManager.getInstance());

        mockTransactionManager();
        mockDaos();

        doReturn(clusterUtils).when(glusterManager).getClusterUtils();
        doReturn(existingServer1).when(clusterUtils).getUpServer(any(Guid.class));

        doNothing().when(glusterManager).logAuditMessage(any(Guid.class),
                any(GlusterVolumeEntity.class),
                any(VDS.class),
                any(AuditLogType.class),
                any(String.class),
                any(String.class));

        doReturn(getFetchedServersList()).when(glusterManager).fetchServers(any(VDS.class));
        doReturn(getFetchedVolumesList()).when(glusterManager).fetchVolumes(any(VDS.class));
        doReturn(getVolumeAdvancedDetails(existingDistVol)).when(glusterManager)
                .getVolumeAdvancedDetails(existingServer1, CLUSTER_ID, existingDistVol.getName());
        doReturn(getVolumeAdvancedDetails(existingReplVol)).when(glusterManager)
                .getVolumeAdvancedDetails(existingServer1, CLUSTER_ID, existingReplVol.getName());
        doReturn(new VDSReturnValue()).when(glusterManager).runVdsCommand(eq(VDSCommandType.RemoveVds),
                argThat(isRemovedServer()));
    }

    private ArgumentMatcher<VDSParametersBase> isRemovedServer() {
        return new ArgumentMatcher<VDSParametersBase>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof RemoveVdsVDSCommandParameters)) {
                    return false;
                }

                return ((RemoveVdsVDSCommandParameters) argument).getVdsId().equals(SERVER_ID_3);
            }
        };
    }

    private void verifyMocksForLightWeight() {
        InOrder inOrder =
                inOrder(clusterDao,
                        vdsDao,
                        clusterUtils,
                        glusterManager,
                        vdsStatisticsDao,
                        vdsDynamicDao,
                        vdsStaticDao,
                        volumeDao,
                        brickDao,
                        optionDao);

        // all clusters fetched from db
        inOrder.verify(clusterDao, times(1)).getAll();

        // get servers of the cluster from db
        inOrder.verify(vdsDao, times(1)).getAllForVdsGroup(CLUSTER_ID);

        // get the UP server from cluster
        inOrder.verify(clusterUtils, times(1)).getUpServer(CLUSTER_ID);

        // servers are fetched from glusterfs
        inOrder.verify(glusterManager, times(1)).fetchServers(existingServer1);

        // detached server SERVER_ID_3 is deleted from DB
        inOrder.verify(vdsStatisticsDao, times(1)).remove(SERVER_ID_3);
        inOrder.verify(vdsDynamicDao, times(1)).remove(SERVER_ID_3);
        inOrder.verify(vdsStaticDao, times(1)).remove(SERVER_ID_3);

        // detached server SERVER_ID_3 is removed from resource manager
        inOrder.verify(glusterManager, times(1)).runVdsCommand(eq(VDSCommandType.RemoveVds),
                any(RemoveVdsVDSCommandParameters.class));

        // volumes are fetched from glusterfs
        inOrder.verify(glusterManager, times(1)).fetchVolumes(any(VDS.class));

        // remove detached bricks
        inOrder.verify(brickDao, times(1)).removeAll(argThat(containsRemovedBricks()));
        // add new bricks
        inOrder.verify(brickDao, times(2)).save(argThat(isAddedBrick()));
        // update modified options
        inOrder.verify(optionDao, times(1)).updateVolumeOption(argThat(isUpdatedOptionId()), eq(OPTION_VALUE_ON));
        // add new options
        inOrder.verify(optionDao, times(1)).save(argThat(isNewOption()));
        // delete removed options
        inOrder.verify(optionDao, times(1)).removeAll(argThat(areRemovedOptions()));

        // create new volume
        inOrder.verify(volumeDao, times(1)).save(newVolume);
        // get volumes by cluster id to identify those that need to be removed
        inOrder.verify(volumeDao, times(1)).getByClusterId(CLUSTER_ID);
        inOrder.verify(volumeDao, times(1)).removeAll(argThat(areRemovedVolumes()));
    }

    private void mockDaos() {
        doReturn(volumeDao).when(glusterManager).getVolumeDao();
        doReturn(brickDao).when(glusterManager).getBrickDao();
        doReturn(optionDao).when(glusterManager).getOptionDao();
        doReturn(vdsDao).when(glusterManager).getVdsDao();
        doReturn(vdsStatisticsDao).when(glusterManager).getVdsStatisticsDao();
        doReturn(vdsStaticDao).when(glusterManager).getVdsStaticDao();
        doReturn(vdsDynamicDao).when(glusterManager).getVdsDynamicDao();
        doReturn(clusterDao).when(glusterManager).getClusterDao();
        doReturn(interfaceDao).when(glusterManager).getInterfaceDao();

        doReturn(Collections.singletonList(existingCluster)).when(clusterDao).getAll();
        doReturn(existingServers).when(vdsDao).getAllForVdsGroup(CLUSTER_ID);
        doReturn(existingDistVol).when(volumeDao).getByName(CLUSTER_ID, DIST_VOL_NAME);
        doReturn(existingReplVol).when(volumeDao).getByName(CLUSTER_ID, REPL_VOL_NAME);
        doReturn(null).when(volumeDao).getByName(CLUSTER_ID, NEW_VOL_NAME);
        doNothing().when(volumeDao).save(newVolume);
        doNothing().when(brickDao).removeAll(argThat(containsRemovedBricks()));
        doNothing().when(brickDao).save(argThat(isAddedBrick()));
        doNothing().when(optionDao).updateVolumeOption(argThat(isUpdatedOptionId()), eq(OPTION_VALUE_ON));
        doNothing().when(optionDao).save(argThat(isNewOption()));
        doNothing().when(optionDao).removeAll(argThat(areRemovedOptions()));
        doReturn(existingVolumes).when(volumeDao).getByClusterId(CLUSTER_ID);
        doNothing().when(volumeDao).removeAll(argThat(areRemovedVolumes()));
        doNothing().when(brickDao).updateBrickStatuses(argThat(hasBricksWithChangedStatus()));
    }

    private ArgumentMatcher<Collection<Guid>> areRemovedVolumes() {
        return new ArgumentMatcher<Collection<Guid>>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof ArrayList)) {
                    return false;
                }
                @SuppressWarnings("unchecked")
                ArrayList<Guid> removedVolumeIds = (ArrayList<Guid>) argument;
                return removedVolumeIds.size() == 1 && removedVolumeIds.get(0).equals(EXISTING_VOL_DIST_ID);
            }
        };
    }

    private ArgumentMatcher<Collection<Guid>> areRemovedOptions() {
        return new ArgumentMatcher<Collection<Guid>>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof ArrayList)) {
                    return false;
                }
                @SuppressWarnings("unchecked")
                ArrayList<Guid> optionsToRemove = (ArrayList<Guid>) argument;
                return (optionsToRemove.size() == 1 && optionsToRemove.get(0)
                        .equals(existingReplVol.getOption(OPTION_AUTH_ALLOW).getId()));
            }
        };
    }

    private ArgumentMatcher<Guid> isUpdatedOptionId() {
        return new ArgumentMatcher<Guid>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof Guid)) {
                    return false;
                }
                return ((Guid) argument).equals(existingReplVol.getOption(OPTION_NFS_DISABLE).getId());
            }
        };
    }

    private ArgumentMatcher<Collection<Guid>> containsRemovedBricks() {
        return new ArgumentMatcher<Collection<Guid>>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof ArrayList)) {
                    return false;
                }
                @SuppressWarnings("unchecked")
                ArrayList<Guid> ids = (ArrayList<Guid>) argument;
                return (ids != null && ids.size() == removedBrickIds.size() && removedBrickIds.containsAll(ids));
            }
        };
    }

    private ArgumentMatcher<GlusterVolumeOptionEntity> isNewOption() {
        return new ArgumentMatcher<GlusterVolumeOptionEntity>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof GlusterVolumeOptionEntity)) {
                    return false;
                }
                return ((GlusterVolumeOptionEntity) argument).getKey().equals(OPTION_AUTH_REJECT);
            }
        };
    }

    private Matcher<GlusterBrickEntity> isAddedBrick() {
        return new ArgumentMatcher<GlusterBrickEntity>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof GlusterBrickEntity)) {
                    return false;
                }
                return addedBrickIds.contains(((GlusterBrickEntity) argument).getId());
            }
        };
    }

    private void mockTransactionManager() throws Exception {
        EjbUtils.setStrategy(mockLookupStrategy);
        doReturn(mockTxnManager).when(mockLookupStrategy)
                .findResource(ContainerManagedResourceType.TRANSACTION_MANAGER);
        doReturn(null).when(mockTxnManager).getTransaction();
        doNothing().when(mockTxnManager).begin();
        doReturn(mockTxn).when(mockTxnManager).getTransaction();
        doReturn(Status.STATUS_COMMITTED).when(mockTxn).getStatus();
        doNothing().when(mockTxnManager).commit();
    }

    private GlusterVolumeAdvancedDetails getVolumeAdvancedDetails(GlusterVolumeEntity volume) {
        GlusterVolumeAdvancedDetails volDetails = new GlusterVolumeAdvancedDetails();

        List<BrickDetails> brickDetailsList = new ArrayList<BrickDetails>();
        for (GlusterBrickEntity brick : volume.getBricks()) {
            BrickDetails brickDetails = new BrickDetails();
            BrickProperties properties = new BrickProperties();
            properties.setBrickId(brick.getId());
            brickDetails.setBrickProperties(properties);
            properties.setStatus(brick.getStatus());
            if (volume == existingReplVol) {
                if (brick.getServerId().equals(SERVER_ID_1)
                        && (brick.getBrickDirectory().equals(REPL_BRICK_R1D1) || brick.getBrickDirectory()
                                .equals(REPL_BRICK_R2D1))) {
                    properties.setStatus(GlusterStatus.DOWN);
                }
            }
            brickDetailsList.add(brickDetails);
            bricksWithChangedStatus.add(brick);
        }
        volDetails.setBrickDetails(brickDetailsList);

        return volDetails;
    }

    /**
     * Returns the list of volumes as if they were fetched from glusterfs. Changes from existing volumes are:<br>
     * - existingDistVol not fetched (means it was removed from gluster cli, and should be removed from db<br>
     * - option 'auth.allow' removed from the existingReplVol<br>
     * - new option 'auth.reject' added to existingReplVol<br>
     * - value of option 'nfs.disable' changed from 'off' ot 'on' in existingReplVol<br>
     * - new volume test-new-vol fetched from gluster (means it was added from gluster cli, and should be added to db<br>
     */
    private Map<String, GlusterVolumeEntity> getFetchedVolumesList() {
        Map<String, GlusterVolumeEntity> volumes = new HashMap<String, GlusterVolumeEntity>();

        GlusterVolumeEntity fetchedReplVol = createReplVol();
        fetchedReplVol.removeOption(OPTION_AUTH_ALLOW); // option removed
        fetchedReplVol.setOption(OPTION_AUTH_REJECT, AUTH_REJECT_IP); // added
        fetchedReplVol.setOption(OPTION_NFS_DISABLE, OPTION_VALUE_ON); // changed

        // brick changes
        removedBrickIds.add(GlusterCoreUtil.findBrick(existingReplVol.getBricks(), SERVER_ID_1, REPL_BRICK_R1D1)
                .getId());
        removedBrickIds.add(GlusterCoreUtil.findBrick(existingReplVol.getBricks(), SERVER_ID_1, REPL_BRICK_R2D1)
                .getId());

        GlusterBrickEntity brickToReplace =
                GlusterCoreUtil.findBrick(fetchedReplVol.getBricks(), SERVER_ID_1, REPL_BRICK_R1D1);
        replaceBrick(brickToReplace,
                SERVER_ID_1,
                REPL_BRICK_R1D1_NEW);

        brickToReplace = GlusterCoreUtil.findBrick(fetchedReplVol.getBricks(), SERVER_ID_1, REPL_BRICK_R2D1);
        replaceBrick(brickToReplace,
                SERVER_ID_1,
                REPL_BRICK_R2D1_NEW);
        volumes.put(fetchedReplVol.getName(), fetchedReplVol);

        // add a new volume
        newVolume = getNewVolume();
        volumes.put(newVolume.getName(), newVolume);

        return volumes;
    }

    private void replaceBrick(GlusterBrickEntity brick, Guid newServerId, String newBrickDir) {
        brick.setId(Guid.NewGuid());
        brick.setServerId(newServerId);
        brick.setBrickDirectory(newBrickDir);
        addedBrickIds.add(brick.getId());
    }

    private Set<GlusterServerInfo> getFetchedServersList() {
        Set<GlusterServerInfo> servers = new HashSet<GlusterServerInfo>();
        servers.add(new GlusterServerInfo(SERVER_ID_1, SERVER_NAME_1, PeerStatus.CONNECTED));
        servers.add(new GlusterServerInfo(SERVER_ID_2, SERVER_NAME_2, PeerStatus.CONNECTED));
        return servers;
    }

    @Test
    public void testRefreshLightWeight() throws Exception {
        setupMocks();

        glusterManager.refreshLightWeightData();
        verifyMocksForLightWeight();
    }

    @Test
    public void testRefreshHeavyWeight() throws Exception {
        setupMocks();
        glusterManager.refreshHeavyWeightData();
        verifyMocksForHeavyWeight();
    }

    private void verifyMocksForHeavyWeight() {
        InOrder inOrder = inOrder(clusterDao, clusterUtils, volumeDao, glusterManager, brickDao);

        // all clusters fetched from db
        inOrder.verify(clusterDao, times(1)).getAll();

        // get the UP server from cluster
        inOrder.verify(clusterUtils, times(1)).getUpServer(CLUSTER_ID);

        // get volumes of the cluster
        inOrder.verify(volumeDao, times(1)).getByClusterId(CLUSTER_ID);

        // get volume advance details
        inOrder.verify(glusterManager, times(1)).getVolumeAdvancedDetails(existingServer1,
                CLUSTER_ID,
                existingDistVol.getName());
        inOrder.verify(glusterManager, times(1)).getVolumeAdvancedDetails(existingServer1,
                CLUSTER_ID,
                existingReplVol.getName());

        // update brick status
        inOrder.verify(brickDao, times(1)).updateBrickStatuses(argThat(hasBricksWithChangedStatus()));

    }

    /**
     * Matches following properties: <br>
     * - is a list <br>
     * - has two elements (bricks) <br>
     * - both have status DOWN <br>
     * - these are the same whose status was changed <br>
     *
     * @return
     */
    private ArgumentMatcher<List<GlusterBrickEntity>> hasBricksWithChangedStatus() {
        return new ArgumentMatcher<List<GlusterBrickEntity>>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof ArrayList)) {
                    return false;
                }
                @SuppressWarnings("unchecked")
                ArrayList<GlusterBrickEntity> bricksToUpdate = (ArrayList<GlusterBrickEntity>) argument;
                if (bricksToUpdate.size() != 2) {
                    return false;
                }

                for (GlusterBrickEntity brick : bricksToUpdate) {
                    if (brick.isOnline()) {
                        return false;
                    }
                    if (!GlusterCoreUtil.containsBrick(bricksWithChangedStatus, brick)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private GlusterVolumeEntity getNewVolume() {
        Guid volumeId = Guid.NewGuid();

        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setName(NEW_VOL_NAME);
        volume.setClusterId(CLUSTER_ID);
        volume.setId(volumeId);
        volume.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volume.addTransportType(TransportType.TCP);
        volume.setReplicaCount(0);
        volume.setStripeCount(0);
        volume.setStatus(GlusterStatus.UP);
        volume.setOption("auth.allow", "*");
        volume.addAccessProtocol(AccessProtocol.GLUSTER);
        volume.addAccessProtocol(AccessProtocol.NFS);

        GlusterBrickEntity brick =
                new GlusterBrickEntity(volumeId, existingServer1.getStaticData(), "/export/testVol1", GlusterStatus.UP);
        brick.setBrickOrder(0);
        volume.addBrick(brick);

        return volume;
    }
}