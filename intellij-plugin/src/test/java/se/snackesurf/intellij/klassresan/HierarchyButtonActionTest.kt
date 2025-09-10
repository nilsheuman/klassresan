package se.snackesurf.intellij.klassresan

import org.junit.jupiter.api.Test
import se.snackesurf.intellij.klassresan.extractors.HierarchyExtractor.parseHierarchyLine
import kotlin.test.assertEquals

class HierarchyButtonActionTest {
    @Test
    fun testStrings() {
        testStrings.forEachIndexed { index, input ->
            val result = parseHierarchyLine(input)
            val expected = expectedStrings[index]
            println("$input -> $result == $expected")
            assertEquals(result.toString(), expected.toString())
        }
    }
}

val testStrings = listOf(
    "MainResource.post() (se.snackesurf.kotlin)",
    "ForrestService.getKoalas() (se.snackesurf.kotlin)",
    "KoalaDao.getKoalas() (se.snackesurf.kotlin)",
    "Db.select(String) (se.snackesurf.kotlin)",
    "main() (se.snackesurf.kotlin)",
    "MainResource.get() (se.snackesurf.kotlin)",
    "ForrestService.getAnimals() (se.snackesurf.kotlin)",
    "ForrestService.subAnimals() (se.snackesurf.kotlin)",
)

val expectedStrings = listOf(
    listOf("MainResource", "post", "", "se.snackesurf.kotlin"),
    listOf("ForrestService", "getKoalas", "", "se.snackesurf.kotlin"),
    listOf("KoalaDao", "getKoalas", "", "se.snackesurf.kotlin"),
    listOf("Db", "select", "String", "se.snackesurf.kotlin"),
    listOf("", "main", "", "se.snackesurf.kotlin"),
    listOf("MainResource", "get", "", "se.snackesurf.kotlin"),
    listOf("ForrestService", "getAnimals", "", "se.snackesurf.kotlin"),
    listOf("ForrestService", "subAnimals", "", "se.snackesurf.kotlin"),
)