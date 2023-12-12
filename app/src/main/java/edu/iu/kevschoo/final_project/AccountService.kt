package edu.iu.kevschoo.final_project

import edu.iu.kevschoo.final_project.model.User
import kotlinx.coroutines.flow.Flow

interface AccountService {
    /** A flow representing the currently signed-in user, if any */
    val currentUser: Flow<User?>
    /** The unique ID of the currently signed-in user, if any */
    val currentUserId: String

    /** Checks if a user is currently signed in */
    fun hasUser(): Boolean

    /** Signs in a user with the given email and password */
    suspend fun signIn(email: String, password: String)

    /** Registers a new user with the given name, email, and password */
    suspend fun signUp(name:String, email: String, password: String)

    /** Signs out the current user */
    suspend fun signOut()

}