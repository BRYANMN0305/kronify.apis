package co.com.kronifyapis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KronifyApisApplication

fun main(args: Array<String>) {
    println("DATABASE_URL = " + System.getenv("DATABASE_URL"))
    runApplication<KronifyApisApplication>(*args)
}
