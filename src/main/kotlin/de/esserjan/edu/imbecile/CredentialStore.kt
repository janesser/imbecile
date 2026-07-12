package de.esserjan.edu.imbecile

/**
 * A contract for any mechanism used to retrieve or store git credentials.
 * This decouples the core Imbecile API from specific storage implementations (File, GCM, etc.).
 */
interface CredentialStore {
    /**
     * Checks if the credential store is available and configured for use.
     */
    fun isAvailable(): Boolean

    /**
     * Retrieves a stored credential for a specific git operation (e.g., token or password).
     * @param host The Git host (e.g., github.com).
     * @param username The username associated with the credential.
     * @return The stored credential string, or null if not found/available.
     */
    fun getCredential(host: String, username: String): String?

    /**
     * Stores a new credential for future use.
     */
    fun storeCredential(host: String, username: String, credential: String): Boolean

    /**
     * Deletes a credential from the store.
     */
    fun deleteCredential(host: String, username: String): Boolean
}
