package org.gradle.sample

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PersonSpec extends FunSpec {
  describe("create a person") {
    it("should succeed for scala") {
      val person: Named = new Person("bob smith")
      person
    }

    it("should succeed for java") {
      val person: Named = new JavaPerson("alice smith")
      person
    }
  }
}
