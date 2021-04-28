import kotlin.test.Test
import kotlin.test.assertFalse

class LinkStoreTest {

    @Test
    fun emptyStore() {
        val store = LinkStore()
        assertFalse(store.hasNextToVisit())
    }
}
