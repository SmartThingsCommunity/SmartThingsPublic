extra["arctic"] = true
tasks.named("hello") {
    doLast {
        println("- The weight of my species in summer is twice as heavy as all human beings.")
    }
}

tasks.register("distanceToIceberg") {
    doLast {
        println("5 nautical miles")
    }
}
