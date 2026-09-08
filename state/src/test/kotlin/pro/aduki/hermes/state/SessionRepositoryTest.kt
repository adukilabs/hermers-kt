package pro.aduki.hermes.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pro.aduki.hermes.state.repository.Identity
import pro.aduki.hermes.state.repository.SessionRepository

class SessionRepositoryTest {

    @Test
    fun testIdentityUpdatesStateFlow() {
        val repo = SessionRepository()
        assertNull(repo.identity.value)

        val id = Identity(
            user = "usr_123",
            tenant = "ten_456",
            owner = true,
            scopes = listOf("mail:read", "mail:write"),
            tier = "pro"
        )
        repo.updateIdentity(id)
        assertEquals(id, repo.identity.value)

        repo.clear()
        assertNull(repo.identity.value)
    }
}

