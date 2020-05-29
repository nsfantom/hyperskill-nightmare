package phonebook

import java.io.File
import kotlin.math.floor
import kotlin.math.sqrt

fun main() {
    val find = File("/media/fantom/Soft/linux/temp/find.txt").readLines()
    val dict = File("/media/fantom/Soft/linux/temp/directory.txt").readLines()
    val dictM: MutableList<SortElement> = File("/media/fantom/Soft/linux/temp/directory.txt")
            .readLines()
            .map { it.partition { i -> i.isDigit() } }
            .toMutableList()
    var dictQ: MutableList<SortElement> = File("/media/fantom/Soft/linux/temp/directory.txt")
            .readLines()
            .map { it.partition { i -> i.isDigit() } }
            .toMutableList()
    val t = find.size
    var c: Int
    val timer = SearchTimer
    println("Start searching (linear search)...")
    timer.start()
    c = linearSearch(find, dict)
    timer.stop()
    val linearTime = timer.getTimeInMillis()
    println("Found $c / $t entries. Time taken: ${timer.getTime()}")

    println()
    println("Start searching (bubble sort + jump search)...")

    val bubbleTime = dictM.bubbleSort(linearTime)

    if (bubbleTime > linearTime * 10) {
        timer.start()
        c = linearSearch(find, dict)
        timer.stop()
        val linearTime2 = timer.getTimeInMillis()

        println("Found $c / $t entries. Time taken: ${timer.getTimeText(bubbleTime + linearTime2)}")
        println("Sorting time: ${timer.getTimeText(bubbleTime)} - STOPPED, moved to linear search")
        println("Searching time: ${timer.getTimeText(linearTime2)}")
    } else {
        timer.start()
        c = jumpSearch(find, dictM)
        timer.stop()
        val jumpTime = timer.getTimeInMillis()

        println("Found $c / $t entries. Time taken: ${timer.getTimeText(bubbleTime + jumpTime)}")
        println("Sorting time: ${timer.getTimeText(bubbleTime)}")
        println("Searching time: ${timer.getTimeText(jumpTime)}")
    }
    println()
    println("Start searching (quick sort + binary search)...")
    val quickTime = dictQ.quickSort()
    timer.start()
    c = binarySearch(find, dictQ)
    timer.stop()
    val binaryTime = timer.getTimeInMillis()

    println("Found $c / $t entries. Time taken: ${timer.getTimeText(quickTime + binaryTime)}")
    println("Sorting time: ${timer.getTimeText(quickTime)}")
    println("Searching time: ${timer.getTimeText(binaryTime)}")

    println()
    println("Start searching (hash table)...")
    timer.start()
    val hash = dictQ.createHashMap()
    timer.stop()
    val hashTime = timer.getTimeInMillis()
    timer.start()
    c = hash.searchAll(find)
    timer.stop()
    val hashSearchTime = timer.getTimeInMillis()

    println("Found $c / $t entries. Time taken: ${timer.getTimeText(hashTime + hashSearchTime)}")
    println("Creating time: ${timer.getTimeText(hashTime)}")
    println("Searching time: ${timer.getTimeText(hashSearchTime)}")
}

fun MutableList<SortElement>.createHashMap(): HashMap<Int, SortElement> {
    return map { Pair(it.second.trim().hashCode(), it) }.toMap() as HashMap<Int, SortElement>
}

fun HashMap<Int, SortElement>.searchAll(find: List<String>): Int {
    var found = 0
    for (s in find) {
        if (this[s.hashCode()] != null) found++
    }
    return found
}

fun <R, S> MutableList<Pair<R, S>>.bubbleSort(delta: Long): Long {
    val timer = SearchTimer
    var takes: Long
    timer.start()
    var temp: Pair<R, S>
    for (i in this.indices) {
        for (j in this.indices - i) {
            if (this[j].second as String > this[i].second as String) {
                temp = this[j]
                this[j] = this[i]
                this[i] = temp
            }
            takes = timer.checkTime()
            if (takes > delta * 10) {
                return takes
            }
        }
    }
    timer.stop()
    return timer.getTimeInMillis()
}

typealias SortElement = Pair<String, String>

fun MutableList<SortElement>.quickSort(): Long {
    val timer = SearchTimer
    timer.start()
    val temp = quickIterate(this)
    for (i in 0 until temp.size) {
        this[i] = temp[i]
    }
    timer.stop()
    return timer.getTimeInMillis()
}

fun quickIterate(arr: List<SortElement>): MutableList<SortElement> {

    if (arr.size >= 2) {
//        val f = arr.first().second
//        val l = arr.first().second
//        val m = arr[arr.size / 2].second
//        val pivot = when {
//            f in l..m || f in m..l -> f
//            m in l..f || m in f..l -> m
//            l in m..f || l in f..m -> l
//            else -> m
//        }
        val pivot = arr.last().second
        val result = quickIterate(arr.filter { (it.second) < pivot })
        result.addAll(arr.filter { it.second == pivot })
        result.addAll(quickIterate(arr.filter { it.second > pivot }))
        return result
    }

    return arr as MutableList<SortElement>
}

fun binarySearch(find: List<String>, list: MutableList<SortElement>): Int {
    var found = 0
    for (s in find) {
        var l = 0
        var r = list.lastIndex
        var c = (l + r) / 2

        loop@ do {
            when {
                list[c].second.trim() == s -> {
                    found++
                    break@loop
                }
                list[c].second.trim() > s -> {
                    r = c
                }
                list[c].second.trim() < s -> {
                    l = c
                }
            }
            c = (l + r) / 2
        } while (c in l..r)

//        if (binaryIterate(s, list) != null) found++
    }
    return found
}

fun binaryIterate(who: String, list: MutableList<SortElement>): SortElement? {

    when {
        list.size > 2 -> {
            val middle = list.size / 2
            val m = list[middle]
            return when {
                m.second.trim() == who -> m
                m.second.trim() > who -> binaryIterate(who, list.subList(0, middle - 1))
                else -> binaryIterate(who, list.subList(middle + 1, list.lastIndex))
            }
        }
        list.size == 1 -> {
            return if (list[0].second.trim() == who) list[0] else null
        }
        list.size == 2 -> {
            return when (who) {
                list[0].second.trim() -> list[0]
                list[1].second.trim() -> list[1]
                else -> null
            }
        }
        else -> return null
    }
}

fun jumpSearch(find: List<String>, list: MutableList<SortElement>): Int {
    var found = 0
    val jump = floor(sqrt(list.size.toDouble())).toInt()
    loopF@ for (li in find) {
        loop@ for (i in list.indices step jump) {
            when {
                list[i].second.trim() < li -> continue@loop
                list[i].second.trim() == li -> found++
                i == 0 -> continue@loopF
                else -> {
                    loopBack@ for (b in i downTo i - jump) {
                        when {
                            list[b].second.trim() < li -> continue@loopBack
                            list[b].second.trim() == li -> {
                                found++
                                continue@loopF
                            }
                            else -> continue@loopF
                        }
                    }
                }
            }
            if (i > list.size - jump) {
                loopPro@ for (pro in i..list.size) {
                    when {
                        list[i].second.trim() < li -> continue@loopPro
                        list[pro].second.trim() == li -> {
                            found++
                            continue@loopF
                        }
                    }
                }
            }
        }
    }
    return found
}

fun linearSearch(find: List<String>, dict: List<String>): Int {
    var c = 0
    for (li in find) {
        for (di in dict) {
            if (di.contains(li)) {
                c++
                break
            }
        }
    }
    return c
}

object SearchTimer {
    private var start: Long = 0
    private var end: Long = 0

    fun start() {
        start = System.currentTimeMillis()
    }

    fun stop() {
        end = System.currentTimeMillis()
    }

    fun checkTime(): Long {
        return System.currentTimeMillis() - start
    }

    fun getTimeInMillis(): Long {
        return end - start
    }

    fun getTimeText(delta: Long): String {
        val m = delta / (1000 * 60)
        val s = (delta - m * 1000 * 60) / 1000
        val ms = (delta - m * 1000 * 60 - s * 1000)
        return "$m min. $s sec. $ms ms."
    }

    fun getTime(): String {
        val delta = end - start
        val m = delta / (1000 * 60)
        val s = (delta - m * 1000 * 60) / 1000
        val ms = (delta - m * 1000 * 60 - s * 1000)
        return "$m min. $s sec. $ms ms."
    }
}
