// (c) https://github.com/MontiCore/monticore
package montithings.generator.prettyprinter;

import arcbasis._ast.ASTPortAccess;
import arcbasis._symboltable.PortSymbol;
import behavior._ast.*;
import behavior._visitor.BehaviorHandler;
import behavior._visitor.BehaviorTraverser;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.siunitliterals._ast.ASTSIUnitLiteral;
import de.monticore.siunits.prettyprint.SIUnitsPrettyPrinter;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.se_rwth.commons.StringTransformations;
import montithings._auxiliary.ExpressionsBasisMillForMontiThings;
import montithings.generator.codegen.util.Identifier;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static montithings.generator.prettyprinter.CppPrettyPrinterUtils.capitalize;
import static montithings.util.IdentifierUtils.getPortForName;

public class CppBehaviorPrettyPrinter
        implements BehaviorHandler {

  protected BehaviorTraverser traverser;
  protected IndentPrinter printer;
  protected int afterStatementIndex;

  public CppBehaviorPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
    this.afterStatementIndex = 0;
  }

  @Override
  public void handle(ASTAfterStatement node) {
    getPrinter().print("std::future<bool> fut");
    getPrinter().print(afterStatementIndex);
    getPrinter().print(" = std::async(std::launch::async, [&] () -> bool {");
    getPrinter().print("std::this_thread::sleep_for( std::chrono::");
    printTime(node.getSIUnitLiteral());
    getPrinter().print(");");

    node.getMCJavaBlock().accept(getTraverser());

    getPrinter().print("return true;");
    getPrinter().print("} );");

    this.afterStatementIndex++;
  }

  @Override
  public void handle(ASTLogStatement node) {
    getPrinter().print("LOG(INFO) <<");
    printLogString(node);
    getPrinter().print(";");
  }

  @Override
  public void handle(ASTAgoQualification node) {
    handle(node, false);
  }

  @Override
  public void handle(ASTConnectStatement node) {
    boolean sourceIsComponentInstance = isStaticComponentInstance(node.getConnector().getSource());
    for (ASTPortAccess target : node.getConnector().getTargetList()) {
      boolean targetIsComponentInstance = isStaticComponentInstance(target);
      getPrinter().print("component.getMqttClientInstance()->publish (replaceDotsBySlashes (\"/connectors/\" + ");
      if (targetIsComponentInstance) {
        getPrinter().print("instanceName");
        getPrinter().print(" + \"/" + target.getQName() + "\"");
      }
      else {
        printGetExternalPortAccessFQN(target);
      }
      getPrinter().print("), replaceDotsBySlashes (");
      if (sourceIsComponentInstance) {
        getPrinter().print("instanceName");
        getPrinter().print(" + \"/" + node.getConnector().getSource().getQName() + "\"");
      }
      else {
        printGetExternalPortAccessFQN(node.getConnector().getSource());
      }
      getPrinter().print("));");
    }
  }

  @Override
  public void handle(ASTDisconnectStatement node) {
    boolean sourceIsComponentInstance = isStaticComponentInstance(node.getSource());
    for (ASTPortAccess target : node.getTargetList()) {
      boolean targetIsComponentInstance = isStaticComponentInstance(target);
      getPrinter().print("component.getMqttClientInstance()->publish (replaceDotsBySlashes (\"/disconnect/\" + ");
      if (targetIsComponentInstance) {
        getPrinter().print("instanceName");
        getPrinter().print(" + \"/" + target.getQName() + "\"");
      }
      else {
        printGetExternalPortAccessFQN(target);
      }
      getPrinter().print("), replaceDotsBySlashes (");
      if (sourceIsComponentInstance) {
        getPrinter().print("instanceName");
        getPrinter().print(" + \"/" + node.getSource().getQName() + "\"");
      }
      else {
        printGetExternalPortAccessFQN(node.getSource());
      }
      getPrinter().print("));");
    }
  }

  protected void printGetExternalPortAccessFQN(ASTPortAccess portAccess) {
    getPrinter().print(
      "input.get" + StringTransformations.capitalize(portAccess.getComponent()) + " ().value ()" +
        ".get" + StringTransformations.capitalize(portAccess.getPort()) + " ()" +
        ".getFullyQualifiedName ()"
    );
  }

  /**
   * With dynamic component instances, some port accesses may not actually refer to ports we can
   * directly access because they are our subcomponents. This method will return true if we have
   * a port we instantiated as part of a subcomponent we statically instantiated ourselves. The
   * method will return false if we have a port that we only know how to connect to because we
   * received its communication information via a port.
   *
   * @param portAccess the port access to check
   * @return if port access refers to port from a statically instantiated component
   */
  protected boolean isStaticComponentInstance(ASTPortAccess portAccess) {
    return portAccess.isPresentComponentSymbol();
  }

  public void handle(ASTAgoQualification node, boolean isComparedToNoData) {
    if (node.getExpression() instanceof ASTNameExpression) {
      ASTNameExpression name = (ASTNameExpression) node.getExpression();
      Optional<PortSymbol> port = getPortForName(name);
      if (port.isPresent()) {
        if (port.get().isIncoming()) {
          getPrinter().print(Identifier.getInputName());
        } else {
          getPrinter().print(Identifier.getResultName());
        }
        getPrinter().print(".agoGet");
        getPrinter().print(capitalize(name.getName()) + "(std::chrono::");
        printTime(node.getSIUnitLiteral());
        getPrinter().print(")");
        if (!isComparedToNoData) {
          getPrinter().print(".value()");
        }
      } else {
        Optional<VariableSymbol> symbol = node.getEnclosingScope().resolveVariable(name.getName());
        if (symbol.isPresent()) {
          getPrinter().print(Identifier.getStateName() + ".agoGet");
          getPrinter().print(capitalize(name.getName()) + "(std::chrono::");
          printTime(node.getSIUnitLiteral());
          getPrinter().print(")");
        } else {
          getPrinter().print(name.getName());
        }
      }
    } else {
      node.getExpression().accept(getTraverser());
    }
  }

  protected void printTime(ASTSIUnitLiteral lit) {
    if (SIUnitsPrettyPrinter.prettyprint(lit.getSIUnit()).equals("ns")) {
      getPrinter().print("nanoseconds");
    } else if (SIUnitsPrettyPrinter.prettyprint(lit.getSIUnit()).equals("μs")) {
      getPrinter().print("microseconds");
    } else if (SIUnitsPrettyPrinter.prettyprint(lit.getSIUnit()).equals("ms")) {
      getPrinter().print("milliseconds");
    } else if (SIUnitsPrettyPrinter.prettyprint(lit.getSIUnit()).equals("s")) {
      getPrinter().print("seconds");
    } else if (SIUnitsPrettyPrinter.prettyprint(lit.getSIUnit()).equals("min")) {
      getPrinter().print("minutes");
    }
    getPrinter().print("{");
    lit.getNumericLiteral().accept(getTraverser());
    getPrinter().print("}");
  }

  protected void printLogString(ASTLogStatement node) {
    String input = node.getStringLiteral().getSource();
    Matcher m = Pattern.compile("\\$\\w+").matcher(input);
    int currentPosition = 0;
    while (m.find()) {
      getPrinter().print("\"");
      getPrinter().print(input.substring(currentPosition, m.start()));
      getPrinter().print("\"");

      getPrinter().print(" << ");

      String subString = input.substring(m.start() + 1, m.end());
      ASTNameExpression name = ExpressionsBasisMillForMontiThings
              .nameExpressionBuilder()
              .setName(subString)
              .build();
      name.setEnclosingScope(node.getEnclosingScope());
      name.accept(getTraverser());

      getPrinter().print(" << ");

      currentPosition = m.end();
    }

    getPrinter().print("\"");
    getPrinter().print(input.substring(currentPosition));
    getPrinter().print("\"");
  }

  /* ============================================================ */
  /* ====================== GENERATED CODE ====================== */
  /* ============================================================ */

  protected IndentPrinter getPrinter() {
    return printer;
  }

  @Override
  public BehaviorTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(BehaviorTraverser traverser) {
    this.traverser = traverser;
  }
}
