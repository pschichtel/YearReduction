package tel.schich.yearreducation

import java.time.LocalDate

case class Blocker(name: String, start: LocalDate, end: LocalDate) {
    def isWithin(date: LocalDate): Boolean = (date.isAfter(start) || date.isEqual(start)) && date.isBefore(end)
}
