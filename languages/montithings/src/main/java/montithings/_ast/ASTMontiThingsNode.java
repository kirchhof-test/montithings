// (c) https://github.com/MontiCore/monticore

package montithings._ast;

import montiarc._ast.ASTMontiArcNode;
import montithings._visitor.MontiThingsVisitor;

/**
 * Interface for all AST nodes of the MontiThings language.
 */
public interface ASTMontiThingsNode extends ASTMontiArcNode {
  void accept(MontiThingsVisitor visitor);
}


