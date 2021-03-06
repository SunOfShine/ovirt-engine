package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

/** A test case for {@link ImagesHandler} */
public class ImagesHandlerTest {

    /** The prefix to use for all tests */
    private static final String prefix = "PREFIX";

    /** The disks to use for testing */
    private DiskImage disk1;
    private DiskImage disk2;

    @Before
    public void setUp() {
        disk1 = new DiskImage();
        disk2 = new DiskImage();
    }

    @Test
    public void testGetSuggestedDiskAliasNullDisk() {
        assertEquals("null disk does not give the default name",
                prefix + ImagesHandler.DISK + ImagesHandler.DefaultDriveName,
                ImagesHandler.getSuggestedDiskAlias(null, prefix, 1));
    }

    @Test
    public void testGetSuggestedDiskAliasNullAliasDisk() {
        disk1.setDiskAlias(null);
        assertEquals("disk with null alias does not give the default name",
                prefix + ImagesHandler.DISK + ImagesHandler.DefaultDriveName,
                ImagesHandler.getSuggestedDiskAlias(disk1, prefix, 1));
    }

    @Test
    public void testGetSuggestedDiskAliasNotNullAliasDisk() {
        disk1.setDiskAlias("someAlias");
        assertEquals("a new alias was generated instead of returning the pre-defined one",
                disk1.getDiskAlias(),
                ImagesHandler.getSuggestedDiskAlias(disk1, prefix, 1));
    }

    @Test
    public void testGetDiskAliasWithDefaultNullAlias() {
        assertEquals("default", ImagesHandler.getDiskAliasWithDefault(disk1, "default"));
    }

    @Test
    public void testGetDiskAliasWithDefaultNotNullAlias() {
        disk1.setDiskAlias("alias");
        assertEquals("alias", ImagesHandler.getDiskAliasWithDefault(disk1, "default"));
    }

    @Test
    public void testGetAllStorageIdsForImageIds() {
        Guid sdIdShared = Guid.NewGuid();
        Guid sdId1 = Guid.NewGuid();
        Guid sdId2 = Guid.NewGuid();

        disk1.setStorageIds(new ArrayList<Guid>(Arrays.asList(sdId1, sdIdShared)));
        disk2.setStorageIds(new ArrayList<Guid>(Arrays.asList(sdId2, sdIdShared)));

        Set<Guid> result = ImagesHandler.getAllStorageIdsForImageIds(Arrays.asList(disk1, disk2));

        assertEquals("Wrong number of Guids returned", 3, result.size());
        assertTrue("Wrong Guids returned", result.containsAll(Arrays.asList(sdId1, sdId2, sdIdShared)));
    }
}
