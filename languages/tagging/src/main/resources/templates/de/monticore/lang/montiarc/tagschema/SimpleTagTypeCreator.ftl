${tc.signature("packageName", "schemaName", "tagTypeName", "importSymbols", "scopeSymbol", "nameScopeType")}

package ${packageName}.${schemaName};

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import ${importSymbols};
import de.monticore.lang.montiarc.tagging._ast.ASTNameScope;
import de.monticore.lang.montiarc.tagging._ast.ASTScope;
import de.monticore.lang.montiarc.tagging._ast.ASTTag;
import de.monticore.lang.montiarc.tagging._ast.ASTTaggingUnit;
import de.monticore.lang.montiarc.tagging._symboltable.TagSymbolCreator;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import de.monticore.symboltable.SymbolKind;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;

/**
 * created by SimpleTagTypeCreator.ftl
 */
public class ${tagTypeName}SymbolCreator implements TagSymbolCreator {

  public static Scope getGlobalScope(final Scope scope) {
    Scope s = scope;
    while (s.getEnclosingScope().isPresent()) {
      s = s.getEnclosingScope().get();
    }
    return s;
  }

  public void create(ASTTaggingUnit unit, Scope gs) {
    if (unit.getQualifiedNames().stream()
        .map(q -> q.toString())
        .filter(n -> n.endsWith("${schemaName}"))
        .count() == 0) {
      return; // the tagging model is not conform to the ${schemaName} tagging schema
    }
    final String packageName = Joiners.DOT.join(unit.getPackage());
    final String rootCmp = // if-else does not work b/c of final (required by streams)
        (unit.getTagBody().getTargetModel().isPresent()) ?
            Joiners.DOT.join(packageName, ((ASTNameScope) unit.getTagBody().getTargetModel().get())
                .getQualifiedName().toString()) :
            packageName;

     for (ASTTag element : unit.getTagBody().getTags()) {
            element.getTagElements().stream()
              .filter(t -> t.getName().equals("${tagTypeName}"))
              .filter(t -> !t.getTagValue().isPresent()) // only marker tag with no value
              .forEachOrdered(t ->
                  element.getScopes().stream()
                    .filter(this::checkScope)
                    .map(s -> (ASTNameScope) s)
                    .map(s -> getGlobalScope(gs).<Symbol>resolveDownMany(
                        Joiners.DOT.join(rootCmp, s.getQualifiedName().toString()),
                        SymbolKind.KIND))
                    .filter(s -> !s.isEmpty())
                    .map(this::checkKind)
                    .filter(s -> s != null)
                    .forEachOrdered(s -> s.addTag(new ${tagTypeName}Symbol())));
      }
    }

  protected ${scopeSymbol} checkKind(Collection<Symbol> symbols) {
    ${scopeSymbol} ret = null;
    for (Symbol symbol : symbols) {
      if (symbol.getKind().isSame(${scopeSymbol}.KIND)) {
        if (ret != null) {
          Log.error(String.format("0xA4095 Found more than one symbol: '%s' and '%s'",
              ret, symbol));
          return null;
        }
        ret = (${scopeSymbol})symbol;
      }
    }
    if (ret == null) {
      Log.error(String.format("0xT0001 Invalid symbol kinds: %s. tagTypeName expects as symbol kind '${scopeSymbol}.KIND'.",
          symbols.stream().map(s -> "'" + s.getKind().toString() + "'").collect(Collectors.joining(", "))));
      return null;
    }
    return ret;
  }

  protected boolean checkScope(ASTScope scope) {
    if (scope.getScopeKind().equals("${nameScopeType}")) {
      return true;
    }
    Log.error(String.format("0xT0005 Invalid scope kind: '%s'. ${tagTypeName} expects as scope kind '${nameScopeType}'.",
        scope.getScopeKind()), scope.get_SourcePositionStart());
    return false;
  }
}