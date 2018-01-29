package component.body.automaton;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import contextconditions.AbstractCoCoTest;
import contextconditions.AbstractCoCoTestExpectedErrorInfo;
import de.se_rwth.commons.logging.Log;
import montiarc._ast.ASTMontiArcNode;
import montiarc.cocos.MontiArcCoCos;

/**
 * This class checks all context conditions directly related to automata (not their sub-elements)
 *
 * @author Andreas Wortmann
 */
public class AutomatonTest extends AbstractCoCoTest {
  
  private static final String MP = "";
  private static final String PACKAGE = "component.body.automaton";
  
  @BeforeClass
  public static void setUp() {
    Log.enableFailQuick(false);
  }
  
  @Test
  public void testMutipleBehaviors() {
    ASTMontiArcNode node = getAstNode(MP, PACKAGE + "." + "MultipleAutomata");
    checkInvalid(MontiArcCoCos.createChecker(), node, new AbstractCoCoTestExpectedErrorInfo(2, "xMA050"));
  }
  
  @Test
  public void testImplementationInNonAtomicComponent() {
    ASTMontiArcNode node = getAstNode(MP, PACKAGE + "." + "AutomatonInComposedComponent");
    checkInvalid(MontiArcCoCos.createChecker(), node, new AbstractCoCoTestExpectedErrorInfo(1, "xMA051"));
  }

  @Test
  public void testLowerCaseAutomatonName() {
    ASTMontiArcNode node = getAstNode(MP, PACKAGE + "." + "AutomatonWithLowerCaseName");
    checkInvalid(MontiArcCoCos.createChecker(), node, new AbstractCoCoTestExpectedErrorInfo(1, "xMA015"));
  }

  @Test
  public void testAutomatonHasStates() {
    ASTMontiArcNode node = getAstNode(MP, PACKAGE + "." + "AutomatonWithoutState");
    checkInvalid(MontiArcCoCos.createChecker(),node, new AbstractCoCoTestExpectedErrorInfo(1, "xMA014"));
  }

  @Test
  public void testAutomatonHasNoInitialStates() {
    ASTMontiArcNode node = getAstNode(MP, PACKAGE + "." + "AutomatonWithoutInitialState");
    // automaton has states but no initial state -> exactly 1 error.
    checkInvalid(MontiArcCoCos.createChecker(),node, new AbstractCoCoTestExpectedErrorInfo(1, "xMA013"));
  }
  
  @Test
  public void testValidAutomaton() {
    checkValid(MP, PACKAGE + "." + "ValidAutomaton");
  }

}