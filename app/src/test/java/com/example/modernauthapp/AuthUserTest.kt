package com.example.modernauthapp

import com.example.modernauthapp.data.model.AuthUser
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthUserTest {

    @Test
    fun `parseNames extracts first and last names from two word string`() {
        val (first, last) = AuthUser.parseNames("John Doe", null, null)
        assertEquals("John", first)
        assertEquals("Doe", last)
    }

    @Test
    fun `parseNames extracts first and compound last names`() {
        val (first, last) = AuthUser.parseNames("Mary Jane Watson", null, null)
        assertEquals("Mary", first)
        assertEquals("Jane Watson", last)
    }

    @Test
    fun `parseNames handles single name properly`() {
        val (first, last) = AuthUser.parseNames("Cher", null, null)
        assertEquals("Cher", first)
        assertEquals("", last)
    }

    @Test
    fun `parseNames prioritizes Google ID token given and family names`() {
        val (first, last) = AuthUser.parseNames("Dr. Jane Doe", "Jane", "Doe")
        assertEquals("Jane", first)
        assertEquals("Doe", last)
    }

    @Test
    fun `welcomeGreeting formats exact greeting correctly`() {
        val userWithSurname = AuthUser(
            uid = "123",
            displayName = "Bruce Wayne",
            firstName = "Bruce",
            surname = "Wayne"
        )
        assertEquals("Hello Bruce Wayne", userWithSurname.welcomeGreeting)

        val userWithoutSurname = AuthUser(
            uid = "456",
            displayName = "Hulk",
            firstName = "Hulk",
            surname = ""
        )
        assertEquals("Hello Hulk", userWithoutSurname.welcomeGreeting)
    }
}
