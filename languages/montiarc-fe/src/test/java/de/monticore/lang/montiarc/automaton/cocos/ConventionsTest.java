package de.monticore.lang.montiarc.automaton.cocos;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import contextconditions.AutomatonAbstractCocoTest;
import de.monticore.lang.montiarc.montiarc._ast.ASTMontiArcNode;
import de.monticore.symboltable.Scope;
import de.monticore.umlcd4a.symboltable.CDFieldSymbol;
import de.se_rwth.commons.logging.Log;

public class ConventionsTest extends AutomatonAbstractCocoTest {
  @BeforeClass
  public static void setUp() {
    Log.enableFailQuick(false);
  }
  
  @Test
  public void testMutipleBehaviors() {
    ASTMontiArcNode node = getAstNode(MODEL_PATH, "automaton.invalid.MutipleBehaviors");
    checkInvalid(node, new ExpectedErrorInfo(3, "xAB140", "xAB130"));
  }
  
  @Test
  public void testLowerCaseEnumeration() {
    Log.getFindings().clear();
    // loads the CD explicitly and checks its cocos
    Scope symTab = createSymTab(MODEL_PATH);
    symTab.<CDFieldSymbol> resolve("automaton.invalid.LowerCaseEnumeration.lowerCaseEnumeration.A", CDFieldSymbol.KIND).orElse(null);
    new ExpectedErrorInfo(1, "xC4A05").checkFindings(Log.getFindings());
    // @JP: Was sollte xC4A05 tun und wieso tut es das nicht?
  }
  
  @Test
  public void testImplementationInNonAtomicComponent() {
    checkInvalid(getAstNode("src/test/resources", "automaton.invalid.ImplementationInNonAtomicComponent"), new ExpectedErrorInfo(1, "xAB141"));
  }
  
  @Ignore
  @Test
  public void testInvalidCDImplicit() {
    // TODO symbols are only loaded if they are resolved. Therefore it is not
    // easily possible to perform CoCo checks to detect errors in CDs that are
    // imported in the given MAA model.
    ASTMontiArcNode node = getAstNode(MODEL_PATH, "automaton.invalid.InvalidCD");
    checkInvalid(node, new ExpectedErrorInfo(1, "xC4A05"));
    // @JP: Fehlercodes zu Konstantes machen
  }
}
