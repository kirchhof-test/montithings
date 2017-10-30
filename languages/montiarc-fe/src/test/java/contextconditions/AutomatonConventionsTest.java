package contextconditions;

import de.monticore.symboltable.Scope;
import de.monticore.umlcd4a.symboltable.CDFieldSymbol;
import de.se_rwth.commons.logging.Log;
import montiarc._ast.ASTMontiArcNode;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class AutomatonConventionsTest extends AutomatonAbstractCocoTest {
  @BeforeClass
  public static void setUp() {
    Log.enableFailQuick(false);
  }
  
  @Test
  public void testMutipleBehaviors() {
    ASTMontiArcNode node = getAstNode(MODEL_PATH, "contextconditions.invalid.MultipleBehaviors");
    checkInvalid(node, new ExpectedErrorInfo(2, "xMA050"));
  }

  @Test
  public void testLowerCaseAutomatonName() {
    ASTMontiArcNode node = getAstNode(MODEL_PATH, "contextconditions.invalid.LowerCaseAutomaton");
    checkInvalid(node, new ExpectedErrorInfo(1, "xMA049"));
  }

  @Test
  @Ignore("MontiCore error code that is not compatible with the new MontiArc error code pattern " +
      "MAXXX")
  public void testLowerCaseEnumeration() {
    Log.getFindings().clear();
    Scope symTab = createSymTab(MODEL_PATH);
    symTab.<CDFieldSymbol> resolve("contextconditions.invalid.LowerCaseEnumeration.lowerCaseEnumeration.A", CDFieldSymbol.KIND).orElse(null);
    new ExpectedErrorInfo(1, "xC4A05").checkFindings(Log.getFindings());
  }


  @Test
  public void testImplementationInNonAtomicComponent() {
    checkInvalid(getAstNode("src/test/resources", "contextconditions.invalid.ImplementationInNonAtomicComponent"), new ExpectedErrorInfo(1, "xMA051"));
  }
  
  @Test
  public void testInvalidImports() {
    //Todo: Star imports?
    ASTMontiArcNode node = getAstNode(MODEL_PATH,"contextconditions.invalid.InvalidCD");
    checkInvalid(node, new ExpectedErrorInfo(1,"xMA076"));

    checkValid(MODEL_PATH,"contextconditions.valid.BumpControl");
  }
}