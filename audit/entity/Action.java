package com.thinkon.common.audit.entity;

/**
 * Enumeration representing the types of actions that can be performed
 * on an auditable entity.
 */
public enum Action {
    /**
     * Represents the creation of a new entity.
     */
    CREATE,

    /**
     * Represents the update of an existing entity.
     */
    UPDATE,

    /**
     * Represents the deletion of an existing entity.
     */
    DELETE;
}
