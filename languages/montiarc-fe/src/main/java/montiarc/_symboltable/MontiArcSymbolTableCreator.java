/*
 * Copyright (c) 2015 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package montiarc._symboltable;

import de.monticore.ast.ASTNode;
import de.monticore.java.javadsl._ast.ASTExpression;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.java.symboltable.JavaSymbolFactory;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.symboltable.*;
import de.monticore.symboltable.modifiers.BasicAccessModifier;
import de.monticore.symboltable.types.JFieldSymbol;
import de.monticore.symboltable.types.JTypeSymbol;
import de.monticore.symboltable.types.JTypeSymbolKind;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.symboltable.types.references.CommonJTypeReference;
import de.monticore.symboltable.types.references.JTypeReference;
import de.monticore.types.JTypeSymbolsHelper;
import de.monticore.types.JTypeSymbolsHelper.JTypeReferenceFactory;
import de.monticore.types.TypesHelper;
import de.monticore.types.TypesPrinter;
import de.monticore.types.types._ast.*;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.StringTransformations;
import de.se_rwth.commons.logging.Log;
import montiarc._ast.*;
import montiarc.helper.JavaHelper;
import montiarc.helper.Timing;
import montiarc.trafos.AutoConnection;
import montiarc.trafos.SimpleConnectorToQualifiedConnector;
import montiarc.visitor.AssignmentNameCompleter;

import java.util.*;

/**
 * Visitor that creates the symboltable of a MontiArc AST.
 *
 * @author Robert Heim, Andreas Wortmann
 */
public class MontiArcSymbolTableCreator extends MontiArcSymbolTableCreatorTOP {
  
  protected String compilationUnitPackage = "";
  
  // extra stack of components that is used to determine which components are
  // inner components.
  protected Stack<ComponentSymbol> componentStack = new Stack<>();
  
  protected List<ImportStatement> currentImports = new ArrayList<>();
  
  protected AutoConnection autoConnectionTrafo = new AutoConnection();
  
  protected SimpleConnectorToQualifiedConnector simpleConnectorTrafo = new SimpleConnectorToQualifiedConnector();
  
  private ASTComponent currentComponent;
  
  private final static JavaSymbolFactory jSymbolFactory = new JavaSymbolFactory();
  
  private final static JTypeReferenceFactory<JavaTypeSymbolReference> jTypeRefFactory = (name,
      scope, dim) -> new JavaTypeSymbolReference(name, scope, dim);
  
  public MontiArcSymbolTableCreator(
      final ResolvingConfiguration resolverConfig,
      final MutableScope enclosingScope) {
    super(resolverConfig, enclosingScope);
  }
  
  public MontiArcSymbolTableCreator(
      final ResolvingConfiguration resolverConfig,
      final Deque<MutableScope> scopeStack) {
    super(resolverConfig, scopeStack);
  }
  
  @Override
  public void visit(ASTMACompilationUnit compilationUnit) {
    Log.debug("Building Symboltable for Component: " + compilationUnit.getComponent().getName(),
        MontiArcSymbolTableCreator.class.getSimpleName());
    compilationUnitPackage = Names.getQualifiedName(compilationUnit.getPackage());
    
    // imports
    List<ImportStatement> imports = new ArrayList<>();
    for (ASTImportStatement astImportStatement : compilationUnit.getImportStatements()) {
      String qualifiedImport = Names.getQualifiedName(astImportStatement.getImportList());
      ImportStatement importStatement = new ImportStatement(qualifiedImport,
          astImportStatement.isStar());
      imports.add(importStatement);
    }
    JavaHelper.addJavaDefaultImports(imports);
    
    ArtifactScope artifactScope = new MontiArcArtifactScope(
        Optional.empty(),
        compilationUnitPackage,
        imports);
    this.currentImports = imports;
    putOnStack(artifactScope);
  }
  
  @Override
  public void endVisit(ASTMACompilationUnit node) {
    // artifact scope
    removeCurrentScope();
  }
  
  @Override
  public void visit(ASTPort node) {
    ASTType astType = node.getType();
    String typeName = TypesPrinter.printTypeWithoutTypeArgumentsAndDimension(astType);
    
    List<String> names = node.getNames();
    
    if (names.isEmpty()) {
      names.add(StringTransformations.uncapitalize(typeName));
    }
    
    for (String name : names) {
      PortSymbol sym = new PortSymbol(name);
      
      JTypeReference<JTypeSymbol> typeRef = new MAJTypeReference(typeName, JTypeSymbol.KIND,
          currentScope().get());
      
      typeRef.setDimension(TypesHelper.getArrayDimensionIfArrayOrZero(astType));
      
      addTypeArgumentsToTypeSymbol(typeRef, astType);
      
      sym.setTypeReference(typeRef);
      sym.setDirection(node.isIncoming());
      
      // stereotype
      if (node.getStereotype().isPresent()) {
        for (ASTStereoValue st : node.getStereotype().get().getValues()) {
          sym.addStereotype(st.getName(), st.getValue());
        }
      }
      
      addToScopeAndLinkWithNode(sym, node);
    }
  }
  
  @Override
  public void visit(ASTVariableDeclaration node) {
    ASTType astType = node.getType();
    String typeName = TypesPrinter.printTypeWithoutTypeArgumentsAndDimension(astType);
    
    List<String> names = node.getNames();
    
    if (names.isEmpty()) {
      names.add(StringTransformations.uncapitalize(typeName));
    }
    for (String name : names) {
      VariableSymbol sym = new VariableSymbol(name);
      
      JTypeReference<JTypeSymbol> typeRef = new MAJTypeReference(typeName, JTypeSymbol.KIND,
          currentScope().get());
      addTypeArgumentsToTypeSymbol(typeRef, astType);
      
      typeRef.setDimension(TypesHelper.getArrayDimensionIfArrayOrZero(astType));
      
      sym.setTypeReference(typeRef);
      
      addToScopeAndLinkWithNode(sym, node);
    }
  }
  
  private void addTypeArgumentsToTypeSymbol(JTypeReference<? extends JTypeSymbol> typeRef,
      ASTType astType) {
    JTypeSymbolsHelper.addTypeArgumentsToTypeSymbol(typeRef, astType, currentScope().get(),
        new MAJTypeReferenceFactory());
  }
  
  @Override
  public void visit(ASTConnector node) {
    String sourceName = node.getSource().toString();
    
    for (ASTQualifiedName target : node.getTargets()) {
      String targetName = target.toString();
      
      ConnectorSymbol sym = new ConnectorSymbol(sourceName, targetName);
      
      // stereotype
      if (node.getStereotype().isPresent()) {
        for (ASTStereoValue st : node.getStereotype().get().getValues()) {
          sym.addStereotype(st.getName(), st.getValue());
        }
      }
      
      addToScopeAndLinkWithNode(sym, node);
    }
  }
  
  @Override
  public void visit(ASTSubComponent node) {
    String referencedCompName = TypesPrinter
        .printTypeWithoutTypeArgumentsAndDimension(node.getType());
    
    // String refCompPackage = Names.getQualifier(referencedCompName);
    String simpleCompName = Names.getSimpleName(referencedCompName);
    
    ComponentSymbolReference componentTypeReference = new ComponentSymbolReference(
        referencedCompName,
        currentScope().get());
    // actual type arguments
    addTypeArgumentsToComponent(componentTypeReference, node.getType());
    
    // ref.setPackageName(refCompPackage);
    
    List<ASTExpression> configArgs = new ArrayList<>();
    for (ASTExpression arg : node.getArguments()) {
      arg.setEnclosingScope(currentScope().get());
      setEnclosingScopeOfNodes(arg);
      configArgs.add(arg);
    }
    
    // instances
    if (!node.getInstances().isEmpty()) {
      // create instances of the referenced components.
      for (ASTSubComponentInstance i : node.getInstances()) {
        createInstance(i.getName(), i, componentTypeReference, configArgs);
      }
    }
    else {
      // auto instance because instance name is missing
      createInstance(StringTransformations.uncapitalize(simpleCompName), node,
          componentTypeReference, configArgs);
    }
    
    node.setEnclosingScope(currentScope().get());
  }
  
  /**
   * Creates the instance and adds it to the symTab.
   */
  private void createInstance(String name, ASTNode node,
      ComponentSymbolReference componentTypeReference,
      List<ASTExpression> configArguments) {
    ComponentInstanceSymbol instance = new ComponentInstanceSymbol(name,
        componentTypeReference);
    configArguments.forEach(v -> instance.addConfigArgument(v));
    // create a subscope for the instance
//    setLinkBetweenSymbolAndNode(instance, node);
    addToScopeAndLinkWithNode(instance, node);
    // remove the created instance's scope
//    removeCurrentScope();
  }
  
  @Override
  public void visit(ASTComponent node) {
    this.currentComponent = node;
    String componentName = node.getName();
    
    String componentPackageName = "";
    if (componentStack.isEmpty()) {
      // root component (most outer component of the diagram)
      componentPackageName = compilationUnitPackage;
    }
    else {
      // inner component uses its parents component full name as package
      componentPackageName = componentStack.peek().getFullName();
    }
    ComponentSymbol component = new ComponentSymbol(componentName);
    component.setImports(currentImports);
    component.setPackageName(componentPackageName);
    
    // generic type parameters
    addTypeParametersToComponent(component, node.getHead().getGenericTypeParameters(),
        currentScope().get());
    
    // parameters
    setParametersOfComponent(component, node.getHead());
    
    // stereotype
    if (node.getStereotype().isPresent()) {
      for (ASTStereoValue st : node.getStereotype().get().getValues()) {
        component.addStereotype(st.getName(), Optional.of(st.getValue()));
      }
    }
    
    // check if this component is an inner component
    if (!componentStack.isEmpty()) {
      component.setIsInnerComponent(true);
    }
    
    // timing
    component.setBehaviorKind(Timing.getBehaviorKind(node));
    
    componentStack.push(component);
    addToScopeAndLinkWithNode(component, node);
    
    // Transform SimpleConncetors to normal qaualified connectors
    for (ASTSubComponent astSubComponent : node.getSubComponents()) {
      for (ASTSubComponentInstance astSubComponentInstance : astSubComponent.getInstances()) {
        simpleConnectorTrafo.transform(astSubComponentInstance, component);
      }
    }
    
    autoConnectionTrafo.transformAtStart(node, component);
  }
  
  @Override
  public void visit(ASTMontiArcAutoConnect node) {
    autoConnectionTrafo.transform(node, componentStack.peek());
  }
  
  protected void setParametersOfComponent(final ComponentSymbol componentSymbol,
      final ASTComponentHead astMethod) {
    for (ASTParameter astParameter : astMethod.getParameters()) {
      final String paramName = astParameter.getName();
      astParameter.setEnclosingScope(currentScope().get());
      setEnclosingScopeOfNodes(astParameter);
      int dimension = TypesHelper.getArrayDimensionIfArrayOrZero(astParameter.getType());
      JTypeReference<? extends JTypeSymbol> paramTypeSymbol = new JavaTypeSymbolReference(
          TypesPrinter.printTypeWithoutTypeArgumentsAndDimension(astParameter
              .getType()),
          currentScope().get(), dimension);
      
      addTypeArgumentsToTypeSymbol(paramTypeSymbol, astParameter.getType());
      final JFieldSymbol parameterSymbol = jSymbolFactory.createFormalParameterSymbol(paramName,
          (JavaTypeSymbolReference) paramTypeSymbol);
      componentSymbol.addConfigParameter(parameterSymbol);
    }
  }

  /**
   * Determine whether the component should be instantiated.
   * @param node ASTComponent node to analyze
   * @param symbol Symbol of the {@param node}
   * @return true, iff an instance should be created.
   */
  private boolean needsInstanceCreation(ASTComponent node, ComponentSymbol symbol) {
    boolean instanceNameGiven = node.getInstanceName().isPresent();
    boolean autoCreationPossible = symbol.getFormalTypeParameters().size() == 0;
    boolean noParametersRequired = symbol.getConfigParameters().size() == 0;
    //TODO: Check if the component only contains default parameters

    return instanceNameGiven || (autoCreationPossible && noParametersRequired);
  }
  
  @Override
  public void endVisit(ASTComponent node) {
    ComponentSymbol component = componentStack.pop();
    autoConnectionTrafo.transformAtEnd(node, component);
    
    // super component
    if (node.getHead().getSuperComponent().isPresent()) {
      ASTReferenceType superCompRef = node.getHead().getSuperComponent().get();
      String superCompName = TypesPrinter.printTypeWithoutTypeArgumentsAndDimension(superCompRef);
      
      ComponentSymbolReference ref = new ComponentSymbolReference(superCompName,
          currentScope().get());
      ref.setAccessModifier(BasicAccessModifier.PUBLIC);
      // actual type arguments
      addTypeArgumentsToComponent(ref, superCompRef);
      
      component.setSuperComponent(Optional.of(ref));
    }
    
    removeCurrentScope();

    // for inner components the symbol must be fully created to reference it.
    // Hence, in endVisit we
    // can reference it and put the instance of the inner component into its
    // parent scope.
    
    if (component.isInnerComponent()) {
      String referencedComponentTypeName = component.getFullName();
      ComponentSymbolReference refEntry = new ComponentSymbolReference(
          referencedComponentTypeName, component.getSpannedScope());
      refEntry.setReferencedComponent(Optional.of(component));
      
      if (needsInstanceCreation(node, component)) {
        // create instance
        String instanceName = node.getInstanceName()
            .orElse(StringTransformations.uncapitalize(component.getName()));
        
        if (node.getActualTypeArgument().isPresent()) {
          setActualTypeArgumentsOfCompRef(refEntry,
              node.getActualTypeArgument().get().getTypeArguments());
        }
        
        ComponentInstanceSymbol instanceSymbol = new ComponentInstanceSymbol(instanceName,
            refEntry);
        Log.debug("Created component instance " + instanceSymbol.getName()
            + " referencing component type " + referencedComponentTypeName,
            MontiArcSymbolTableCreator.class.getSimpleName());
        
        addToScope(instanceSymbol);
      }

      // check whether there are already instances of the inner component type
      // defined in the component type. We then have to set the referenced
      // component.
      Collection<ComponentInstanceSymbol> instances = component.getEnclosingScope()
          .resolveLocally(ComponentInstanceSymbol.KIND);
      for (ComponentInstanceSymbol instance : instances) {
        if (instance.getComponentType().getName().equals(component.getName())) {
          instance.getComponentType().setReferencedComponent(Optional.of(component));
        }
      }
    }
  }
  
  private void setActualTypeArgumentsOfCompRef(ComponentSymbolReference typeReference,
      List<ASTTypeArgument> astTypeArguments) {
    List<ActualTypeArgument> actualTypeArguments = new ArrayList<>();
    for (ASTTypeArgument astTypeArgument : astTypeArguments) {
      if (astTypeArgument instanceof ASTWildcardType) {
        ASTWildcardType astWildcardType = (ASTWildcardType) astTypeArgument;
        
        // Three cases can occur here: lower bound, upper bound, no bound
        if (astWildcardType.lowerBoundIsPresent() || astWildcardType.upperBoundIsPresent()) {
          // We have a bound.
          // Examples: Set<? extends Number>, Set<? super Integer>
          
          // new bound
          boolean lowerBound = astWildcardType.lowerBoundIsPresent();
          ASTType typeBound = lowerBound
              ? astWildcardType.getLowerBound().get()
              : astWildcardType.getUpperBound().get();
          int dimension = TypesHelper.getArrayDimensionIfArrayOrZero(typeBound);
          JTypeReference<? extends JTypeSymbol> typeBoundSymbolReference = new JavaTypeSymbolReference(
              TypesPrinter.printTypeWithoutTypeArgumentsAndDimension(typeBound),
              currentScope().get(), dimension);
          ActualTypeArgument actualTypeArgument = new ActualTypeArgument(lowerBound, !lowerBound,
              typeBoundSymbolReference);
          
          // init bound
          addTypeArgumentsToTypeSymbol(typeBoundSymbolReference, typeBound);
          
          actualTypeArguments.add(actualTypeArgument);
        }
        else {
          // No bound. Example: Set<?>
          actualTypeArguments.add(new ActualTypeArgument(false, false, null));
        }
      }
      else if (astTypeArgument instanceof ASTType) {
        // Examples: Set<Integer>, Set<Set<?>>, Set<java.lang.String>
        ASTType astTypeNoBound = (ASTType) astTypeArgument;
        int dimension = TypesHelper.getArrayDimensionIfArrayOrZero(astTypeNoBound);
        JTypeReference<? extends JTypeSymbol> typeArgumentSymbolReference = new JavaTypeSymbolReference(
            TypesPrinter.printTypeWithoutTypeArgumentsAndDimension(astTypeNoBound),
            currentScope().get(), dimension);
        
        addTypeArgumentsToTypeSymbol(typeArgumentSymbolReference, astTypeNoBound);
        
        actualTypeArguments.add(new ActualTypeArgument(typeArgumentSymbolReference));
      }
      else {
        Log.error("0xMA073 Unknown type argument " + astTypeArgument + " of type "
            + typeReference);
      }
    }
    typeReference.setActualTypeArguments(actualTypeArguments);
  }
  
  protected JTypeReferenceFactory<JavaTypeSymbolReference> typeRefFactory = (name, scope,
      dim) -> new JavaTypeSymbolReference(name, scope, dim);
  
  protected void addTypeArgumentsToComponent(ComponentSymbolReference typeReference,
      ASTType astType) {
    if (astType instanceof ASTSimpleReferenceType) {
      ASTSimpleReferenceType astSimpleReferenceType = (ASTSimpleReferenceType) astType;
      if (!astSimpleReferenceType.getTypeArguments().isPresent()) {
        return;
      }
      setActualTypeArgumentsOfCompRef(typeReference,
          astSimpleReferenceType.getTypeArguments().get().getTypeArguments());
    }
    else if (astType instanceof ASTComplexReferenceType) {
      ASTComplexReferenceType astComplexReferenceType = (ASTComplexReferenceType) astType;
      for (ASTSimpleReferenceType astSimpleReferenceType : astComplexReferenceType
          .getSimpleReferenceTypes()) {
        /* ASTComplexReferenceType represents types like class or interface
         * types which always have ASTSimpleReferenceType as qualification. For
         * example: a.b.c<Arg>.d.e<Arg> */
        setActualTypeArgumentsOfCompRef(typeReference,
            astSimpleReferenceType.getTypeArguments().get().getTypeArguments());
      }
    }
    
  }
  
  /**
   * Adds the TypeParameters to the ComponentSymbol if it declares
   * TypeVariables. Since the restrictions on TypeParameters may base on the
   * JavaDSL its the actual recursive definition of bounds is respected and its
   * implementation within the JavaDSL is reused. Example:
   * <p>
   * component Bla<T, S extends SomeClass<T> & SomeInterface>
   * </p>
   * T and S are added to Bla.
   *
   * @param componentSymbol
   * @param optionalTypeParameters
   * @return currentScope
   * @see JTypeSymbolsHelper
   */
  protected static List<JTypeSymbol> addTypeParametersToComponent(
      ComponentSymbol componentSymbol, Optional<ASTTypeParameters> optionalTypeParameters,
      Scope currentScope) {
    if (optionalTypeParameters.isPresent()) {
      // component has type parameters -> translate AST to Java Symbols and add
      // these to the
      // componentSymbol.
      ASTTypeParameters astTypeParameters = optionalTypeParameters.get();
      for (ASTTypeVariableDeclaration astTypeParameter : astTypeParameters
          .getTypeVariableDeclarations()) {
        // TypeParameters/TypeVariables are seen as type declarations.
        JavaTypeSymbol javaTypeVariableSymbol = jSymbolFactory
            .createTypeVariable(astTypeParameter.getName());
        
        List<ASTType> types = new ArrayList<ASTType>(astTypeParameter.getUpperBounds());
        // reuse JavaDSL
        JTypeSymbolsHelper.addInterfacesToType(javaTypeVariableSymbol, types, currentScope,
            jTypeRefFactory);
        
        componentSymbol.addFormalTypeParameter(javaTypeVariableSymbol);
      }
    }
    return componentSymbol.getFormalTypeParameters();
  }
  
  /***************************************
   * Java/P integration
   ***************************************/
  
  @Override
  public void visit(ASTJavaPBehavior node) {
    MutableScope javaPScope = new CommonScope(true);
    javaPScope.setResolvingFilters(currentScope().get().getResolvingFilters());
    javaPScope.setEnclosingScope(currentScope().get());
    node.setEnclosingScope(currentScope().get());
    this.scopeStack.addLast(javaPScope);
  }
  
  @Override
  public void endVisit(ASTJavaPBehavior node) {
    setEnclosingScopeOfNodes(node);
    removeCurrentScope();
  }
  
  public void visit(ASTValueInitialization init) {
    Scope enclosingScope = currentScope().get();
    String qualifiedName = init.getQualifiedName().toString();
    Optional<VariableSymbol> var = enclosingScope
        .<VariableSymbol> resolve(qualifiedName, VariableSymbol.KIND);
    if (var.isPresent()) {
      var.get().setValuation(Optional.of(init.getValuation()));
    }
    
  }
  
  /**
   * Visits ASTLocalVariableDeclaration nodes that occur in AJava compute
   * blocks. A new VariableSymbol is created for declared variables and added to
   * the scope
   *
   * @param variableDeclaration ASTNode that is a variable declaration.
   */
  public void visit(ASTLocalVariableDeclaration variableDeclaration) {
    final ASTType type = variableDeclaration.getType();
    String typeName = TypesPrinter.printTypeWithoutTypeArgumentsAndDimension(type);
    
    List<String> names = new ArrayList<>();
    
    for (ASTVariableDeclarator astVariableDeclarator : variableDeclaration
        .getVariableDeclarators()) {
      final String variableName = astVariableDeclarator.getDeclaratorId().getName();
      names.add(variableName);
    }
    
    if (names.isEmpty()) {
      names.add(StringTransformations.uncapitalize(typeName));
    }
    
    for (String name : names) {
      VariableSymbol variableSymbol = new VariableSymbol(name);
      
      JTypeReference<JTypeSymbol> typeRef = new MAJTypeReference(typeName, JTypeSymbol.KIND,
          currentScope().get());
      addTypeArgumentsToTypeSymbol(typeRef, type);
      
      typeRef.setDimension(TypesHelper.getArrayDimensionIfArrayOrZero(type));
      
      variableSymbol.setTypeReference(typeRef);
      
      addToScopeAndLinkWithNode(variableSymbol, variableDeclaration);
    }
    
  }
  
  
  /**
   * Checks whether the passed name references to a configuration parameter
   * 
   * @param name Name of the parameter to look up
   * @return true, iff the currently processed node has a parameter of the
   * passed name
   */
  private boolean isConfigurationArgument(String name) {
    for (ASTParameter param : this.currentComponent.getHead().getParameters()) {
      if (name.equals(param.getName())) {
        return true;
      }
    }
    return false;
  }
  
  /***************************************
   * I/O Automaton integration
   ***************************************/
  
  /**
   * @see de.monticore.lang.montiarc.montiarc._visitor.MontiArcVisitor#visit(de.monticore.lang.montiarc.montiarc._ast.ASTAutomatonBehavior)
   */
  @Override
  public void visit(ASTAutomatonBehavior node) {
    MutableScope automatonScope = new CommonScope(true);
    automatonScope.setResolvingFilters(currentScope().get().getResolvingFilters());
    automatonScope.setEnclosingScope(currentScope().get());
    node.setEnclosingScope(currentScope().get());
    this.scopeStack.addLast(automatonScope);
  }
  
  /**
   * @see de.monticore.lang.montiarc.montiarc._visitor.MontiArcVisitor#endVisit(de.monticore.lang.montiarc.montiarc._ast.ASTAutomatonBehavior)
   */
  @Override
  public void endVisit(ASTAutomatonBehavior node) {
    removeCurrentScope();
  }
  
  /**
   * @see de.monticore.lang.montiarc.montiarc._visitor.MontiArcVisitor#visit(de.monticore.lang.montiarc.montiarc._ast.ASTAutomaton)
   */
  @Override
  public void visit(ASTAutomaton node) {
    node.setEnclosingScope(currentScope().get());
  }
  
  @Override
  public void endVisit(ASTAutomaton node) {
    setEnclosingScopeOfNodes(node);
    // automaton core loaded & all enclosing scopes set, so we can reconstruct
    // the missing assignment names
    node.accept(new AssignmentNameCompleter(currentScope().get()));
  }
  
  /**
   * @see de.monticore.lang.montiarc.montiarc._visitor.MontiArcVisitor#visit(de.monticore.lang.montiarc.montiarc._ast.ASTState)
   */
  @Override
  public void visit(ASTState node) {
    StateSymbol state = new StateSymbol(node.getName());
    if (node.getStereotype().isPresent()) {
      for (ASTStereoValue value : node.getStereotype().get().getValues()) {
        state.addStereoValue(value.getName());
      }
    }
    addToScopeAndLinkWithNode(state, node);
  }
  
  /**
   * @see montiarc._visitor.MontiArcVisitor#endVisit(montiarc._ast.ASTState)
   */
  @Override
  public void endVisit(ASTState node) {
    super.endVisit(node);
  }
  
  /**
   * @see de.monticore.lang.montiarc.montiarc._visitor.MontiArcVisitor#visit(de.monticore.lang.montiarc.montiarc._ast.ASTInitialStateDeclaration)
   */
  @Override
  public void visit(ASTInitialStateDeclaration node) {
    MutableScope scope = currentScope().get();
    for (String name : node.getNames()) {
      scope.<StateSymbol> resolveMany(name, StateSymbol.KIND).forEach(c -> {
        c.setInitial(true);
        c.setInitialReactionAST(node.getBlock());
        if (node.getBlock().isPresent()) {
          for (ASTIOAssignment assign : node.getBlock().get().getIOAssignments()) {
              if (assign.getName().isPresent()) {
                Optional<VariableSymbol> var = currentScope().get()
                    .<VariableSymbol> resolve(assign.getName().get(), VariableSymbol.KIND);
                if (var.isPresent()) {
                  if (assign.getValueList().isPresent()
                      && !assign.getValueList().get().getAllValuations().isEmpty()) {
                    // This only covers the case "var i = somevalue"
                    ASTValuation v = assign.getValueList().get().getAllValuations().get(0);
                    var.get().setValuation(Optional.of(v));
                  }
                }
            }
          }
        }
      });
    }
  }
  
  @Override
  public void visit(ASTTransition node) {
    // get target name, if there is no get source name (loop to itself)
    // TODO what about same transitions with other stimulus? -> name clash
    String targetName = node.getTarget().orElse(node.getSource());
    
    StateSymbolReference source = new StateSymbolReference(node.getSource(), currentScope().get());
    StateSymbolReference target = new StateSymbolReference(targetName, currentScope().get());
    
    TransitionSymbol transition = new TransitionSymbol(node.getSource() + " -> " + targetName);
    transition.setSource(source);
    transition.setTarget(target);
    
    transition.setGuardAST(node.getGuard());
    transition.setReactionAST(node.getReaction());
    
    addToScopeAndLinkWithNode(transition, node); // introduces new scope
  }
  
  @Override
  public void endVisit(ASTTransition node) {
    removeCurrentScope();
  }
  
  @Override
  public void visit(ASTIOAssignment node) {
    node.setEnclosingScope(currentScope().get());
  }
  
  /**
   * @see montiarc._symboltable.MontiArcSymbolTableCreatorTOP#visit(montiarc._ast.ASTStateDeclaration)
   */
  @Override
  public void visit(ASTStateDeclaration ast) {
    // to prevent creation of unnecessary scope we override with nothing
  }
  
  /**
   * @see montiarc._symboltable.MontiArcSymbolTableCreatorTOP#endVisit(montiarc._ast.ASTStateDeclaration)
   */
  @Override
  public void endVisit(ASTStateDeclaration ast) {
    // to prevent deletion of not existing scope we override with nothing
  }
  
  /**
   * Default implementation to create {@link CommonJTypeReference}s using the
   * {@link JTypeSymbolKind}.
   *
   * @author Robert Heim
   */
  public static class MAJTypeReferenceFactory
      implements JTypeReferenceFactory<CommonJTypeReference<JTypeSymbol>> {
    @Override
    public CommonJTypeReference<JTypeSymbol> create(String referencedSymbolName,
        Scope definingScopeOfReference,
        int arrayDimension) {
      CommonJTypeReference<JTypeSymbol> tref = new MAJTypeReference(referencedSymbolName,
          JTypeSymbol.KIND,
          definingScopeOfReference);
      tref.setDimension(arrayDimension);
      return tref;
    }
  }
  
}
