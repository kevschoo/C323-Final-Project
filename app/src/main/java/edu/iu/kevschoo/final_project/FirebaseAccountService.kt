package edu.iu.kevschoo.final_project

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import edu.iu.kevschoo.final_project.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseAccountService : AccountService
{

    private val auth: FirebaseAuth = Firebase.auth

    /** Flow of the current user, providing real-time updates on the authentication state */
    override val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                val firebaseUser = auth.currentUser
                if (firebaseUser != null)
                {
                    FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val user = documentSnapshot.toObject(User::class.java)
                            trySend(user).isSuccess
                        }
                }
                else { trySend(null).isSuccess }
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    /** The current user's unique identifier*/
    override val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** Returns true if a user is currently signed in */
    override fun hasUser(): Boolean {return auth.currentUser != null }

    /** Signs in a user with the specified email and password */
    override suspend fun signIn(email: String, password: String) { auth.signInWithEmailAndPassword(email, password).await() }

    /** Creates a new user account with the specified name, email, and password */
    override suspend fun signUp(name: String, email: String, password: String)
    {
        val userCredential = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = userCredential.user
        firebaseUser?.let { user ->
            val newUser = User(
                id = user.uid,
                name = name,
                email = user.email ?: "",
                signUpDate = Date(user.metadata?.creationTimestamp ?: 0)
            )
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .set(newUser)
                .addOnSuccessListener { Log.d("FirebaseAccountService", "User profile created") }
                .addOnFailureListener { e -> Log.e("FirebaseAccountService", "Error creating user profile", e) }
        }
    }

    /** Signs out the currently signed-in user */
    override suspend fun signOut() { auth.signOut() }
}