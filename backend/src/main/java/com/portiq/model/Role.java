package com.portiq.model;

/**
 * Distinguishes a regular customer (OWNER) account from a fund manager account that can
 * administer multiple customers.
 */
public enum Role {
    OWNER,
    FUND_MANAGER
}
