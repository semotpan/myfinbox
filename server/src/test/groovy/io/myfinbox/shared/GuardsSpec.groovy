package io.myfinbox.shared

import org.javamoney.moneta.Money
import spock.lang.Specification
import spock.lang.Tag

import javax.money.MonetaryAmount
import java.math.BigDecimal

@Tag("unit")
class GuardsSpec extends Specification {

    def "notNull should throw NullPointerException when object is null"() {
        when: 'object is null'
        Guards.notNull(null, 'object cannot be null')

        then: 'exception is thrown'
        def e = thrown(NullPointerException)

        and: 'exception message'
        e.message == 'object cannot be null'
    }

    def "notNull should return object when not null"() {
        expect: 'object returned when not null'
        Guards.notNull('value', 'object cannot be null') == 'value'
    }

    def "notBlank should throw IllegalArgumentException when text is blank or null"() {
        when: 'text is blank or null'
        Guards.notBlank(text, 'text cannot be blank')

        then: 'exception is thrown'
        def e = thrown(IllegalArgumentException)

        and: 'exception message'
        e.message == 'text cannot be blank'

        where:
        text << ['   ', null]
    }

    def "notBlank should return text when not blank"() {
        expect: 'text returned when not blank'
        Guards.notBlank('abc', 'text cannot be blank') == 'abc'
    }

    def "greaterThanZero BigDecimal should throw NullPointerException when value is null"() {
        when: 'value is null'
        Guards.greaterThanZero((BigDecimal) null, 'invalid')

        then: 'exception is thrown'
        def e = thrown(NullPointerException)

        and: 'exception message'
        e.message == 'value cannot be null.'
    }

    def "greaterThanZero BigDecimal should throw IllegalArgumentException when value not greater than zero"() {
        when: 'value is not greater than zero'
        Guards.greaterThanZero(new BigDecimal('-1'), 'invalid')

        then: 'exception is thrown'
        def e = thrown(IllegalArgumentException)

        and: 'exception message'
        e.message == 'invalid'
    }

    def "greaterThanZero BigDecimal should return value when greater than zero"() {
        given: 'a positive value'
        def value = new BigDecimal('1')

        expect: 'value returned when greater than zero'
        Guards.greaterThanZero(value, 'invalid') == value
    }

    def "greaterThanZero MonetaryAmount should throw NullPointerException when amount is null"() {
        when: 'amount is null'
        Guards.greaterThanZero((MonetaryAmount) null, 'invalid')

        then: 'exception is thrown'
        def e = thrown(NullPointerException)

        and: 'exception message'
        e.message == 'amount cannot be null.'
    }

    def "greaterThanZero MonetaryAmount should throw IllegalArgumentException when amount not greater than zero"() {
        when: 'amount is zero or negative'
        Guards.greaterThanZero(Money.of(BigDecimal.ZERO, 'USD'), 'invalid')

        then: 'exception is thrown'
        def e = thrown(IllegalArgumentException)

        and: 'exception message'
        e.message == 'invalid'
    }

    def "greaterThanZero MonetaryAmount should return amount when greater than zero"() {
        given: 'a positive amount'
        def amount = Money.of(new BigDecimal('1'), 'USD')

        expect: 'amount returned when greater than zero'
        Guards.greaterThanZero(amount, 'invalid') == amount
    }

    def "doesNotOverflow should throw IllegalArgumentException when text overflows"() {
        when: 'text exceeds max length'
        Guards.doesNotOverflow('abcd', 3, 'overflow')

        then: 'exception is thrown'
        def e = thrown(IllegalArgumentException)

        and: 'exception message'
        e.message == 'overflow'
    }

    def "doesNotOverflow should return text when within max length"() {
        expect: 'text returned when within max length'
        Guards.doesNotOverflow('abc', 3, 'overflow') == 'abc'
    }

    def "doesNotOverflow should return null when text is null"() {
        expect: 'null returned when text is null'
        Guards.doesNotOverflow(null, 3, 'overflow') == null
    }

    def "matches should throw NullPointerException when value is null"() {
        given: 'a digit pattern'
        def pattern = ~/\d+/

        when: 'value is null'
        Guards.matches(null, pattern, 'invalid')

        then: 'exception is thrown'
        def e = thrown(NullPointerException)

        and: 'exception message'
        e.message == 'value cannot be null.'
    }

    def "matches should throw NullPointerException when pattern is null"() {
        when: 'pattern is null'
        Guards.matches("value", null, "invalid")

        then: 'exception is thrown'
        def e = thrown(NullPointerException)

        and: 'exception message'
        e.message == 'pattern cannot be null.'
    }

    def "matches should throw IllegalArgumentException when value does not match pattern"() {
        given: 'a digit pattern'
        def pattern = ~/\d+/

        when: 'value does not match pattern'
        Guards.matches('abc', pattern, 'invalid')

        then: 'exception is thrown'
        def e = thrown(IllegalArgumentException)

        and: 'exception message'
        e.message == 'invalid'
    }

    def "matches should return value when it matches pattern"() {
        given: 'a digit pattern'
        def pattern = ~/\d+/

        expect: 'value returned when matches pattern'
        Guards.matches('123', pattern, 'invalid') == '123'
    }

    def "nonEmpty should throw IllegalArgumentException when list is null"() {
        when: 'list is null'
        Guards.nonEmpty(null, 'list cannot be empty')

        then: 'exception is thrown'
        def e = thrown(IllegalArgumentException)

        and: 'exception message'
        e.message == 'list cannot be empty'
    }

    def "nonEmpty should throw IllegalArgumentException when list is empty"() {
        when: 'list is empty'
        Guards.nonEmpty([], 'list cannot be empty')

        then: 'exception is thrown'
        def e = thrown(IllegalArgumentException)

        and: 'exception message'
        e.message == 'list cannot be empty'
    }

    def "nonEmpty should return list when not empty"() {
        given: 'a non empty list'
        def values = ['value']

        expect: 'list returned when not empty'
        Guards.nonEmpty(values, 'list cannot be empty') == values
    }
}
