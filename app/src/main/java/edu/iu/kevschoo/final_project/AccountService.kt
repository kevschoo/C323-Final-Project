package edu.iu.kevschoo.final_project

import edu.iu.kevschoo.final_project.model.User
import kotlinx.coroutines.flow.Flow

interface AccountService {

    val currentUser: Flow<User?>

    val currentUserId: String

    fun hasUser(): Boolean

    suspend fun signIn(email: String, password: String)

    suspend fun signUp(name:String, email: String, password: String)

    suspend fun signOut()

}