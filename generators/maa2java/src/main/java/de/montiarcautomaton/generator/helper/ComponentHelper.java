package de.montiarcautomaton.generator.helper;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import de.monticore.ast.ASTNode;
import de.monticore.java.prettyprint.JavaDSLPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.types.JFieldSymbol;
import de.monticore.symboltable.types.JTypeSymbol;
import de.monticore.symboltable.types.references.JTypeReference;
import de.se_rwth.commons.Names;
import montiarc._ast.ASTComponent;
import montiarc._ast.ASTElement;
import montiarc._ast.ASTJavaPInitializer;
import montiarc._ast.ASTValueInitialization;
import montiarc._symboltable.ComponentInstanceSymbol;
import montiarc._symboltable.ComponentSymbol;
import montiarc._symboltable.ConnectorSymbol;
import montiarc._symboltable.PortSymbol;
import montiarc._symboltable.VariableSymbol;
import montiarc.helper.SymbolPrinter;

/**
 * Helper class used in the template to generate target code of atomic or
 * composed components.
 * 
 * @author Gerrit Leonhardt
 */
public class ComponentHelper {
  public static String DEPLOY_STEREOTYPE = "deploy";
  
  private final ComponentSymbol component;
  
  public ComponentHelper(ComponentSymbol component) {
    this.component = component;
  }
  
  public String getPortTypeName(PortSymbol port) {
    return printFqnTypeName(port.getTypeReference());
  }
  
  public String printVariableTypeName(VariableSymbol var) {
    return printFqnTypeName(var.getTypeReference());
  }
  
  public String printInit(ASTValueInitialization init) {
    String ret = "";
    JavaDSLPrettyPrinter printer = new JavaDSLPrettyPrinter(new IndentPrinter());
    String name = Names.getQualifiedName(init.getQualifiedName().getParts());
    ret += name;
    ret+= " = ";
    ret+= printer.prettyprint(init.getValuation().getExpression());
    ret+= ";";
    
    return ret;
    
    
  }
  
  public String getParamTypeName(JFieldSymbol param) {
    return printFqnTypeName(param.getType());
  }
  
  public Collection<String> getParamValues(ComponentInstanceSymbol param) {
    return param.getConfigArguments().stream().map(symbol -> SymbolPrinter.printConfigArgument(symbol)).collect(Collectors.toList());
  }
  
  public String getSubComponentTypeName(ComponentInstanceSymbol instance) {
    return instance.getComponentType().getName();
  }
  
  public boolean isIncomingPort(ComponentSymbol cmp, ConnectorSymbol conn, boolean isSource, String portName) {
    String subCompName = getConnectorComponentName(conn, isSource);
    String portNameUnqualified = getConnectorPortName(conn, isSource);
    Optional<PortSymbol> port = Optional.empty();
    // port is of subcomponent
    if(portName.contains(".")) {
      Optional<ComponentInstanceSymbol> subCompInstance = cmp.getSpannedScope().<ComponentInstanceSymbol> resolve(subCompName, ComponentInstanceSymbol.KIND);
      Optional<ComponentSymbol> subComp = subCompInstance.get().getComponentType().getReferencedComponent();
      port =  subComp.get().getSpannedScope().<PortSymbol> resolve(portNameUnqualified, PortSymbol.KIND);  
    }
    else {
      port = cmp.getSpannedScope().<PortSymbol> resolve(portName, PortSymbol.KIND);  
    }
    
    
    if(port.isPresent()) {
      return port.get().isIncoming();
    }
    
    return false;
  }

  /**
   * Returns the component name of a connection.
   * 
   * @param conn the connection
   * @param isSource <tt>true</tt> for siurce component, else <tt>false>tt>
   * @return
   */
  public String getConnectorComponentName(ConnectorSymbol conn, boolean isSource) {
    final String name;
    if (isSource) {
      name = conn.getSource();
    }
    else {
      name = conn.getTarget();
    }
    if(name.contains(".")) {
      return name.split("\\.")[0];
    }
    return "this";
    
  }
  
  /**
   * Returns the port name of a connection.
   * 
   * @param conn the connection
   * @param isSource <tt>true</tt> for siurce component, else <tt>false>tt>
   * @return
   */
  public String getConnectorPortName(ConnectorSymbol conn, boolean isSource) {
    final String name;
    if (isSource) {
      name = conn.getSource();
    }
    else {
      name = conn.getTarget();
    }
    
    if(name.contains(".")) {
      return name.split("\\.")[1];
    }
    return name;
  }
  
  /**
   * Returns <tt>true</tt> if the component is deployable.
   * 
   * @return
   */
  public boolean isDeploy() {
    if (component.getStereotype().containsKey(DEPLOY_STEREOTYPE)) {
      if (!component.getConfigParameters().isEmpty()) {
        throw new RuntimeException("Config parameters are not allowed for a depolyable component.");
      }
      return true;
    }
    return false;
  }
  
  /**
   * Prints the type of the reference including dimensions.
   * 
   * @param ref
   * @return
   */
  protected String printFqnTypeName(JTypeReference<? extends JTypeSymbol> ref) {
    String name = ref.getName();
    Collection<JTypeSymbol> sym = ref.getEnclosingScope().<JTypeSymbol>resolveMany(ref.getName(), JTypeSymbol.KIND);
    if(!sym.isEmpty()){
      name = sym.iterator().next().getFullName();
    }
    for (int i = 0; i < ref.getDimension(); ++i) {
      name += "[]";
    }
    return name;
  }
  
  public static Optional<ASTJavaPInitializer> getComponentInitialization(ComponentSymbol comp) {
    Optional<ASTJavaPInitializer> ret = Optional.empty();
    Optional<ASTNode> ast = comp.getAstNode();
    if(ast.isPresent()) {
      ASTComponent compAST = (ASTComponent) ast.get();
      for(ASTElement e : compAST.getBody().getElements()) {
        if(e instanceof ASTJavaPInitializer) {
          ret = Optional.of((ASTJavaPInitializer) e);
          
        }
      }
    }    
    return ret;
  }
}
