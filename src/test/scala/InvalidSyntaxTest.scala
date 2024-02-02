import Main._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class InvalidSyntaxTest extends AnyFlatSpec {
  val folderPath = "src/test/scala/examples/invalidSyntax"

  val fileList: List[(String, String)] = new ProcessExamples(folderPath).processFolder()

  fileList.foreach { case (testName, testCode) =>
    s"Parser should parse: $testName" should "parse correctly" in {
      parseProgram(testCode) shouldBe SYNTAX_ERROR_EXIT_STATUS
    }
  }
}
