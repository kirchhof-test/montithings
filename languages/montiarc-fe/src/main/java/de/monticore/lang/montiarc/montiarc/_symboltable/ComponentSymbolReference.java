/* generated from model null*/
/* generated by template symboltable.SymbolReference*/

package de.monticore.lang.montiarc.montiarc._symboltable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.symboltable.references.CommonSymbolReference;
import de.monticore.symboltable.references.SymbolReference;
import de.monticore.symboltable.types.references.ActualTypeArgument;

/**
 * Represents a reference of {@link ComponentSymbol}.
 */
public class ComponentSymbolReference extends ComponentSymbol implements
    SymbolReference<ComponentSymbol> {

  protected final SymbolReference<ComponentSymbol> reference;
  private List<ActualTypeArgument> actualTypeArguments = new ArrayList<>();

  public ComponentSymbolReference(final String name, final Scope definingScopeOfReference) {
    super(name);
    reference = new CommonSymbolReference<>(name, ComponentSymbol.KIND, definingScopeOfReference);
    if (existsReferencedSymbol()) {
      setReferencedComponent(Optional.of(getReferencedSymbol()));
    }
  }

  public List<ActualTypeArgument> getActualTypeArguments() {
    return ImmutableList.copyOf(actualTypeArguments);
  }

  public void setActualTypeArguments(List<ActualTypeArgument> actualTypeArguments) {
    this.actualTypeArguments = new ArrayList<>(actualTypeArguments);
  }

  public boolean hasActualTypeArguments() {
    return this.actualTypeArguments.size() > 0;
  }

  // no overridden methods of ComponentSymbol as the ComponentSymbol itself checks whether it is a
  // reference or not.
  
  /* Methods of SymbolReference interface */

  @Override
  public ComponentSymbol getReferencedSymbol() {
    return reference.getReferencedSymbol();
  }

  @Override
  public boolean existsReferencedSymbol() {
    return reference.existsReferencedSymbol();
  }

  @Override
  public boolean isReferencedSymbolLoaded() {
    return reference.isReferencedSymbolLoaded();
  }
  
  /* Methods of Symbol interface */

  @Override
  public String getName() {
    return getReferencedSymbol().getName();
  }

  @Override
  public String getFullName() {
    return getReferencedSymbol().getFullName();
  }

  @Override
  public Scope getEnclosingScope() {
    return getReferencedSymbol().getEnclosingScope();
  }

  @Override
  public void setEnclosingScope(MutableScope scope) {
    getReferencedSymbol().setEnclosingScope(scope);
  }

  @Override
  public AccessModifier getAccessModifier() {
    return getReferencedSymbol().getAccessModifier();
  }

  @Override
  public void setAccessModifier(AccessModifier accessModifier) {
    getReferencedSymbol().setAccessModifier(accessModifier);
  }

}
