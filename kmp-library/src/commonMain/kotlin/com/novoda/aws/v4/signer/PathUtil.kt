package com.novoda.aws.v4.signer

private const val IGNORED = -1

internal object PathUtil {

    fun normalize(path: String): String {
        val segmentCount = path.getSegmentCount()
        if (segmentCount < 0) {
            return path
        }
        val segments = IntArray(segmentCount)

        return CharArray(path.length) { path[it] }
                .apply {
                    split(segments)
                    removeDots(segments)
                    maybeAddLeadingDot(segments)
                }
                .let { String(it, 0, it.join(segments)) }
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

    private fun String.findNextSegmentStart(index: Int): Pair<Int, Boolean> {
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

    private fun CharArray.split(segments: IntArray) {
        var currentSegmentIndex = 0
        var characterIndex = skipSlashes(0)

        while (characterIndex < size) {
            segments[currentSegmentIndex++] = characterIndex++
            characterIndex = findBeginningOfNextSegment(characterIndex)
        }

        check(currentSegmentIndex == segments.size) //ASSERT
    }

    private fun CharArray.findBeginningOfNextSegment(startIndex: Int): Int {
        for (index in startIndex until size) {
            if (this[index] == '/') {
                return skipSlashes(index)
            }
        }
        return size
    }

    private fun CharArray.skipSlashes(startIndex: Int): Int {
        for (index in startIndex until size) {
            if (this[index] == '/') {
                this[index] = '\u0000'
            } else {
                return index
            }
        }
        return size
    }

    // Join the segments in the given path according to the given segment-index
    // array, ignoring those segments whose index entries have been set to -1,
    // and inserting slashes as needed.  Return the length of the resulting
    // path.
    //
    // Preconditions:
    //   segments[i] == -1 implies segment i is to be ignored
    //   path computed by split, as above, with '\0' having replaced '/'
    //
    // Postconditions:
    //   path[0] .. path[return value] == Resulting path
    //
    private fun CharArray.join(segments: IntArray): Int {
        var writeIndex = 0

        if (isNullAt(writeIndex)) {
            // Restore initial slash for absolute paths
            writeIndex = restoreSlashAt(writeIndex)
        }

        segments.asSequence()
                .filter(PathUtil::isSegmentNotIgnored)
                .forEach { segmentStart ->
                    writeIndex = when {
                        writeIndex == segmentStart -> skipToSegmentEnd(writeIndex)
                        writeIndex < segmentStart -> copyDownFromSegmentStart(segmentStart, writeIndex)
                        else -> error("Unexpected character write index") // ASSERT false
                    }
                }

        return writeIndex
    }

    private fun isSegmentNotIgnored(segmentIndex: Int) = segmentIndex != IGNORED

    private fun CharArray.skipToSegmentEnd(index: Int): Int {
        for (writeIndex in index until size) {
            if (isNullAt(writeIndex)) {
                return restoreSlashAt(writeIndex)
            }
        }
        return size
    }

    private fun CharArray.copyDownFromSegmentStart(segmentStart: Int, index: Int): Int {
        var writeIndex = index
        for (segmentIndex in segmentStart until size) {
            if (isNullAt(segmentIndex)) {
                return restoreSlashAt(writeIndex)
            }
            this[writeIndex] = this[segmentIndex]
            writeIndex++
        }
        return writeIndex
    }

    private fun CharArray.restoreSlashAt(index: Int): Int {
        this[index] = '/'
        return index + 1
    }

    private fun CharArray.isNullAt(index: Int) = this[index] == '\u0000'


    // Remove "." segments from the given path, and remove segment pairs
    // consisting of a non-".." segment followed by a ".." segment.
    //
    private fun CharArray.removeDots(segments: IntArray) {
        var segmentIndex = 0
        while (segmentIndex < segments.size) {
            segmentIndex = findNextSegmentWithDots(segmentIndex, segments)
                    .run { ignoreSegmentWithDots(first, second, segments) }
        }
    }

    private fun CharArray.findNextSegmentWithDots(index: Int, segments: IntArray): Pair<Dots, Int> {
        // Find next occurrence of "." or ".."
        for (segmentIndex in index until segments.size) {
            val segmentStartIndex = segments[segmentIndex]
            if (isDotAt(segmentStartIndex)) {
                if (isEndAt(segmentStartIndex) || isNullAt(segmentStartIndex + 1)) {
                    return Dots.ONE to segmentIndex
                } else if (isDotAt(segmentStartIndex + 1) && (isEndAt(segmentStartIndex + 1) || isNullAt(segmentStartIndex + 2))) {
                    return Dots.TWO to segmentIndex
                }
            }
        }
        return Dots.NONE to segments.size
    }

    private fun CharArray.ignoreSegmentWithDots(dots: Dots, segmentIndex: Int, segments: IntArray): Int {
        return when {
            segmentIndex >= segments.size || dots == Dots.NONE -> segments.size
            dots == Dots.ONE -> {// Remove this occurrence of "."
                segments[segmentIndex] = IGNORED
                segmentIndex + 1
            }
            else -> {// If there is a preceding non-".." segment, remove both that
                // segment and this occurrence of ".."; otherwise, leave this
                // ".." segment as-is.
                segments.findPreviousNotIgnoredSegmentIndex(segmentIndex)
                        .let { if (it >= 0 && isNotSegmentWithOnlyDotsAt(segments[it])) it else null }
                        ?.also {
                            segments[segmentIndex] = IGNORED
                            segments[it] = IGNORED
                        }
                segmentIndex + 1
            }
        }
    }

    private fun CharArray.isDotAt(index: Int) = this[index] == '.'

    private fun CharArray.isNotSegmentWithOnlyDotsAt(index: Int) = !(isDotAt(index) && isDotAt(index + 1) && isNullAt(index + 2))

    private fun IntArray.findPreviousNotIgnoredSegmentIndex(index: Int): Int {
        for (previousSegmentIndex in index - 1 downTo 0) {
            if (isNotIgnoredAt(previousSegmentIndex)) {
                return previousSegmentIndex
            }
        }
        return -1
    }

    private fun IntArray.isNotIgnoredAt(index: Int) = this[index] != IGNORED

    private fun CharArray.isEndAt(index: Int) = index == size - 1


    // DEVIATION: If the normalized path is relative, and if the first
    // segment could be parsed as a scheme name, then prepend a "." segment
    //
    // Prevent scheme-name confusion
    private fun CharArray.maybeAddLeadingDot(segments: IntArray) {
        if (noNeedForLeadingDot(segments)) return

        // At this point we know that the first segment is unused,
        // hence we can insert a "." segment at that position
        this[0] = '.'
        this[1] = '\u0000'
        segments[0] = 0
    }

    private fun CharArray.noNeedForLeadingDot(segments: IntArray): Boolean {
        if (isNullAt(0)) {
            return true
        }
        return segments.findFirstValidSegmentIndex()
                .let { it >= segments.size || it == 0 || doesNotContainColonInSegment(segments[it]) }
    }

    private fun CharArray.doesNotContainColonInSegment(index: Int): Boolean {
        for (pathIndex in index until size) {
            if (this[pathIndex] == ':' || isNullAt(pathIndex)) {
                return false
            }
        }
        return true
    }

    private fun IntArray.findFirstValidSegmentIndex(): Int {
        for (segmentIndex in 0 until size) {
            if (this[segmentIndex] >= 0) {
                return segmentIndex
            }
        }
        return size
    }

}

private enum class Dots {
    NONE, ONE, TWO
}
