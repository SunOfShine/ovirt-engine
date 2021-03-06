package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>DbUserDAO</code> defines a type for performing CRUD operations on instances of {@link DbUser}.
 *
 *
 */
public interface DbUserDAO extends DAO, SearchDAO<DbUser> {

    /**
     * Retrieves the suser with the specified id.
     *
     * @param id
     *            the id
     * @return the user, or <code>null</code> if the id was invalid
     */
    DbUser get(Guid id);

    /**
     * Retrieves a user by username.
     *
     * @param username
     *            the username
     * @return the user
     */
    DbUser getByUsername(String username);

    /**
     * Retrieves all users associated with the specified virtual machine.
     *
     * @param id
     *            the VM id
     * @return the list of users
     */
    List<DbUser> getAllForVm(Guid id);

    /**
     * Retrieves all defined used.
     *
     * @return the collection of all users
     */
    List<DbUser> getAll();

    /**
     * Saves the user.
     *
     * @param user
     *            the user
     */
    void save(DbUser user);

    /**
     * Updates the specified user in the database.
     *
     * @param user
     *            the user
     */
    void update(DbUser user);

    /**
     * Removes the user with the specified id.
     *
     * @param user
     *            the user id
     */
    void remove(Guid user);
}
