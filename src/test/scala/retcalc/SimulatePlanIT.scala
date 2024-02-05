package retcalc

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{Matchers, fixture}

class SimulatePlanIT extends fixture.WordSpec with Matchers with TypeCheckedTripleEquals {
  "SimulatePlanApp.strMain" should {
    "simulate a retirement plan using market returns" in {
      val actualResult = SimulatePlanIT.strMain(
        Array("1997.09,2017.09", // The period
              "25",              // The number of years of savings
              "40",              // The number of years in retirement
              "3000",            // Income
              "2000",            // Expenses
              "10000")           // Initial capital
      )

      val expectedResult =
        s"""
           |Capital after 25 years of savings:    499923
           |Capital after 40 years in retirement: 586435
           """.stripMargin

      actualResult should === (expectedResult)
    }
  }
}
