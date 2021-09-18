// (c) https://github.com/MontiCore/monticore
package montithings._symboltable;

import arcbasis._ast.ASTComponentType;
import arcbasis._ast.ASTPort;
import arcbasis._ast.ASTPortDeclaration;
import arcbasis._symboltable.ComponentTypeSymbol;
import arcbasis._symboltable.PortSymbol;
import cd4montithings.CD4MontiThingsMill;
import cd4montithings._ast.ASTCDComponentInterface;
import cd4montithings._ast.ASTCDPort;
import cd4montithings._ast.ASTCDPortDeclaration;
import cd4montithings._ast.ASTCDPortDirection;
import cd4montithings._symboltable.CD4MontiThingsArtifactScope;
import cd4montithings._symboltable.ICD4MontiThingsArtifactScope;
import com.google.common.base.Preconditions;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import montiarc._ast.ASTMACompilationUnit;
import montithings.MontiThingsMill;
import montithings._ast.ASTMTComponentType;
import org.codehaus.commons.nullanalysis.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Symbol table creator. Does pretty much nothing right now. Only forwards calls to MontiArc
 * that MontiCore is not advanced enough to forward automatically.
 */
public class MontiThingsScopesGenitor extends MontiThingsScopesGenitorTOP {

  @Override
  public IMontiThingsArtifactScope createFromAST(@NotNull ASTMACompilationUnit rootNode) {
    Preconditions.checkArgument(rootNode != null);
    List<ImportStatement> imports = new ArrayList<>();
    for (ASTMCImportStatement importStatement : rootNode.getImportStatementList()) {
      imports.add(new ImportStatement(importStatement.getQName(), importStatement.isStar()));
    }
    IMontiThingsArtifactScope artifactScope = MontiThingsMill.artifactScope();
    artifactScope.setPackageName(rootNode.getPackage().getQName());
    artifactScope.setImportsList(imports);
    putOnStack(artifactScope);

    // for some reason the setLinkBetweenSpannedScopeAndNode doesn't accept
    // rootNode as an argument of type ASTMTComponentType.
    // Maybe a MontiCore bug? Maybe MontiCore looks for the first non-terminal
    // in our grammar instead of the "start" non-terminal? I dont know. Anyway,
    // that's why the following two lines fake the behavior that should have
    // been provided by setLinkBetweenSpannedScopeAndNode
    rootNode.setSpannedScope(artifactScope);
    artifactScope.setAstNode(rootNode);

    rootNode.accept(getTraverser());
    return artifactScope;
  }

  @Override
  public void visit(ASTMTComponentType node) {
    getTraverser().visit((ASTComponentType) node);
  }

  @Override
  public void endVisit(ASTMTComponentType node) {
    getTraverser().endVisit((ASTComponentType) node);
  }
}
