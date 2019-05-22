package com.novoda.aws.v4.signer

import kotlin.test.Test
import kotlin.test.assertEquals

class PathUtilTest {

    @Test
    fun `removes double slash`() {
        val path = "some//path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "some/path", actual = normalized)
    }

    @Test
    fun `removes triple slash`() {
        val path = "some///path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "some/path", actual = normalized)
    }

    @Test
    fun `keeps ending slash`() {
        val path = "some/path/"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "some/path/", actual = normalized)
    }

    @Test
    fun `keeps starting slash`() {
        val path = "/some/path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/some/path", actual = normalized)
    }

    @Test
    fun `keeps local dot identifier at the start`() {
        val path = "./some/path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "./some/path", actual = normalized)
    }

    @Test
    fun `removed local dot identifier in the middle`() {
        val path = "/some/./path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/some/path", actual = normalized)
    }

    @Test
    fun `removed consecutive local dot identifiers in the middle`() {
        val path = "/some/././path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/some/path", actual = normalized)
    }

    @Test
    fun `removed double dot identifiers in the middle and previous segment`() {
        val path = "/some/../path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/path", actual = normalized)
    }

    @Test
    fun `removed 2 consecutive double dot identifiers in the middle and previous segment`() {
        val path = "/some/other/../../path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/path", actual = normalized)
    }

    @Test
    fun `removed 3 consecutive double dot identifiers in the middle and previous segment`() {
        val path = "/some/other/more/../../../path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/path", actual = normalized)
    }

    @Test
    fun `keep double dot at the beginning of the path`() {
        val path = "../path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "../path", actual = normalized)
    }

    @Test
    fun `keep double dot at the beginning of the path with root slash`() {
        val path = "/../path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/../path", actual = normalized)
    }

    @Test
    fun `removed combination of double dots and single dot`() {
        val path = "/some/other/.././../path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/path", actual = normalized)
    }

    @Test
    fun `removed combination of double dot and single dots`() {
        val path = "/some/./.././path"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/path", actual = normalized)
    }

    @Test
    fun `root path remains as root`() {
        val path = "/"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/", actual = normalized)
    }

    @Test
    fun `root double slash converted to roo`() {
        val path = "//"

        val normalized = PathUtil.normalize(path)

        assertEquals(expected = "/", actual = normalized)
    }

}