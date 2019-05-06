package com.novoda.aws.v4.signer

internal object PathUtil {

    fun normalize(path: String): String {
        val segmentCount = path.getSegmentCount()
        if (segmentCount < 0) {
            return path
        }

        val pathArray = CharArray(path.length) { path[it] }

        val segments = IntArray(segmentCount)
        split(pathArray, segments)
        removeDots(pathArray, segments)
        maybeAddLeadingDot(pathArray, segments)

        return String(pathArray, 0, join(pathArray, segments))
    }

    private fun String.getSegmentCount(): Int {
        val startIndex = 0
        return skipSlashes(startIndex)
                .run { scanSegments(first, second) }
    }

    private fun String.scanSegments(index: Int, isNormalInput: Boolean): Int {
        var nextIndex = index
        var isNormal = isNormalInput
        var segmentCount = 0

        while (nextIndex < length) {
            segmentCount++

            isNormal = isNormal && !hasDotOrTwoDotsAt(nextIndex)

            findNextSegmentStart(nextIndex).apply {
                nextIndex = first
                isNormal = isNormal && second
            }
        }
        return when {
            isNormal -> -1
            else -> segmentCount
        }
    }

    private fun String.findNextSegmentStart(index: Int): Pair< Int, Boolean> {
        var nextIndex = index
        var isNormal = true
        while (nextIndex < length) {
            if (this[nextIndex++] != '/') {
                continue
            }

            skipSlashes(nextIndex).apply {
                nextIndex = first
                isNormal = isNormal && second
            }

            break
        }
        return Pair(nextIndex, isNormal)
    }

    private fun String.skipSlashes(index: Int): Pair<Int, Boolean> {
        var nextIndex = index
        var isNormal = true
        while (nextIndex < length) {
            if (this[nextIndex] != '/') break
            isNormal = false
            nextIndex++
        }
        return Pair(nextIndex, isNormal)
    }

    // Looking at "." or ".." ?
    private fun String.hasDotOrTwoDotsAt(index: Int): Boolean {
        return isDotAt(index) && (isLastOrFollowedByPathSeparator(index)
                || isDotAt(index + 1) && isLastOrFollowedByPathSeparator(index + 1))
    }

    private fun String.isLastOrFollowedByPathSeparator(index: Int) = index == length - 1 || this[index + 1] == '/'

    private fun String.isDotAt(index: Int) = this[index] == '.'

    private fun split(path: CharArray, segs: IntArray) {
        val end = path.size - 1      // Index of last char in path
        var p = 0                      // Index of next char in path
        var i = 0                      // Index of current segment

        // Skip initial slashes
        while (p <= end) {
            if (path[p] != '/') break
            path[p] = '\u0000'
            p++
        }

        while (p <= end) {

            // Note start of segment
            segs[i++] = p++

            // Find beginning of next segment
            while (p <= end) {
                if (path[p++] != '/')
                    continue
                path[p - 1] = '\u0000'

                // Skip redundant slashes
                while (p <= end) {
                    if (path[p] != '/') break
                    path[p++] = '\u0000'
                }
                break
            }
        }

        check(i == segs.size) //ASSERT
    }

    // Join the segments in the given path according to the given segment-index
    // array, ignoring those segments whose index entries have been set to -1,
    // and inserting slashes as needed.  Return the length of the resulting
    // path.
    //
    // Preconditions:
    //   segs[i] == -1 implies segment i is to be ignored
    //   path computed by split, as above, with '\0' having replaced '/'
    //
    // Postconditions:
    //   path[0] .. path[return value] == Resulting path
    //
    private fun join(path: CharArray, segs: IntArray): Int {
        val ns = segs.size           // Number of segments
        val end = path.size - 1      // Index of last char in path
        var p = 0                      // Index of next path char to write

        if (path[p] == '\u0000') {
            // Restore initial slash for absolute paths
            path[p++] = '/'
        }

        for (i in 0 until ns) {
            var q = segs[i]            // Current segment
            if (q == -1)
            // Ignore this segment
                continue

            if (p == q) {
                // We're already at this segment, so just skip to its end
                while (p <= end && path[p] != '\u0000')
                    p++
                if (p <= end) {
                    // Preserve trailing slash
                    path[p++] = '/'
                }
            } else if (p < q) {
                // Copy q down to p
                while (q <= end && path[q] != '\u0000')
                    path[p++] = path[q++]
                if (q <= end) {
                    // Preserve trailing slash
                    path[p++] = '/'
                }
            } else
                error("") // ASSERT false
        }

        return p
    }


    // Remove "." segments from the given path, and remove segment pairs
    // consisting of a non-".." segment followed by a ".." segment.
    //
    private fun removeDots(path: CharArray, segs: IntArray) {
        val ns = segs.size
        val end = path.size - 1

        var i = 0
        while (i < ns) {
            var dots = 0               // Number of dots found (0, 1, or 2)

            // Find next occurrence of "." or ".."
            do {
                val p = segs[i]
                if (path[p] == '.') {
                    if (p == end) {
                        dots = 1
                        break
                    } else if (path[p + 1] == '\u0000') {
                        dots = 1
                        break
                    } else if (path[p + 1] == '.' && (p + 1 == end || path[p + 2] == '\u0000')) {
                        dots = 2
                        break
                    }
                }
                i++
            } while (i < ns)
            if (i > ns || dots == 0)
                break

            if (dots == 1) {
                // Remove this occurrence of "."
                segs[i] = -1
            } else {
                // If there is a preceding non-".." segment, remove both that
                // segment and this occurrence of ".."; otherwise, leave this
                // ".." segment as-is.
                var j: Int
                j = i - 1
                while (j >= 0) {
                    if (segs[j] != -1) break
                    j--
                }
                if (j >= 0) {
                    val q = segs[j]
                    if (!(path[q] == '.'
                                    && path[q + 1] == '.'
                                    && path[q + 2] == '\u0000')) {
                        segs[i] = -1
                        segs[j] = -1
                    }
                }
            }
            i++
        }
    }


    // DEVIATION: If the normalized path is relative, and if the first
    // segment could be parsed as a scheme name, then prepend a "." segment
    //
    // Prevent scheme-name confusion
    private fun maybeAddLeadingDot(path: CharArray, segs: IntArray) {

        if (path[0] == '\u0000')
        // The path is absolute
            return

        val ns = segs.size
        var f = 0                      // Index of first segment
        while (f < ns) {
            if (segs[f] >= 0)
                break
            f++
        }
        if (f >= ns || f == 0)
        // The path is empty, or else the original first segment survived,
        // in which case we already know that no leading "." is needed
            return

        var p = segs[f]
        while (p < path.size && path[p] != ':' && path[p] != '\u0000') p++
        if (p >= path.size || path[p] == '\u0000')
        // No colon in first segment, so no "." needed
            return

        // At this point we know that the first segment is unused,
        // hence we can insert a "." segment at that position
        path[0] = '.'
        path[1] = '\u0000'
        segs[0] = 0
    }

}
