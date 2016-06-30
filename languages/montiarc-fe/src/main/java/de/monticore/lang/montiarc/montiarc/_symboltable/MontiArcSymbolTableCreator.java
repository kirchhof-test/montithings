/*
 * Copyright (c) 2015 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package de.monticore.lang.montiarc.montiarc._symboltable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import de.monticore.ast.ASTNode;
import de.monticore.common.common._ast.ASTStereoValue;
import de.monticore.java.javadsl._ast.ASTExpression;
import de.monticore.java.prettyprint.JavaDSLPrettyPrinter;
import de.monticore.java.symboltable.JavaSymbolFactory;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.lang.expression.symboltable.ValueSymbol;
import de.monticore.lang.expression.symboltable.ValueSymbol.Kind;
import de.monticore.lang.montiarc.common._ast.ASTParameter;
import de.monticore.lang.montiarc.helper.ArcTypePrinter;
import de.monticore.lang.montiarc.helper.Timing;
import de.monticore.lang.montiarc.montiarc._ast.ASTComponent;
import de.monticore.lang.montiarc.montiarc._ast.ASTComponentHead;
import de.monticore.lang.montiarc.montiarc._ast.ASTMACompilationUnit;
import de.monticore.lang.montiarc.montiarc._ast.ASTMontiArcAutoConnect;
import de.monticore.lang.montiarc.montiarc._ast.ASTPort;
import de.monticore.lang.montiarc.montiarc._ast.ASTSimpleConnector;
import de.monticore.lang.montiarc.montiarc._ast.ASTSubComponent;
import de.monticore.lang.montiarc.montiarc._ast.ASTSubComponentInstance;
import de.monticore.lang.montiarc.trafos.AutoConnection;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.ArtifactScope;
import de.monticore.symboltable.ImportStatement;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolverConfiguration;
import de.monticore.symboltable.modifiers.BasicAccessModifier;
import de.monticore.symboltable.types.JAttributeSymbol;
import de.monticore.symboltable.types.JTypeSymbol;
import de.monticore.symboltable.types.TypeSymbol;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.symboltable.types.references.CommonJTypeReference;
import de.monticore.symboltable.types.references.JTypeReference;
import de.monticore.symboltable.types.references.TypeReference;
import de.monticore.types.TypesHelper;
import de.monticore.types.types._ast.ASTComplexArrayType;
import de.monticore.types.types._ast.ASTComplexReferenceType;
import de.monticore.types.types._ast.ASTImportStatement;
import de.monticore.types.types._ast.ASTQualifiedName;
import de.monticore.types.types._ast.ASTReferenceType;
import de.monticore.types.types._ast.ASTSimpleReferenceType;
import de.monticore.types.types._ast.ASTType;
import de.monticore.types.types._ast.ASTTypeArgument;
import de.monticore.types.types._ast.ASTWildcardType;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.StringTransformations;
import de.se_rwth.commons.logging.Log;

/**
 * Visitor that creats the symboltable of a MontiArc AST.
 *
 * @author Robert Heim
 */
public class MontiArcSymbolTableCreator extends MontiArcSymbolTableCreatorTOP {

  private String compilationUnitPackage = "";
  private MontiArcExpandedComponentInstanceSymbolCreator instanceSymbolCreator
      = new MontiArcExpandedComponentInstanceSymbolCreator();
  // extra stack of components that is used to determine which components are inner components.
  private Stack<ComponentSymbol> componentStack = new Stack<>();
  private List<ImportStatement> currentImports = new ArrayList<>();
  private AutoConnection autoConnectionTrafo = new AutoConnection();
  private JavaSymbolFactory jSymbolFactory = new JavaSymbolFactory();

  public MontiArcSymbolTableCreator(
      final ResolverConfiguration resolverConfig,
      final MutableScope enclosingScope) {
    super(resolverConfig, enclosingScope);
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

  public void endVisit(ASTMACompilationUnit node) {
    // TODO clean up component types from references to inner components
    // cleanUpReferences();

    // artifact scope
    removeCurrentScope();

    // creates all instances which are created through the top level component
    System.out.println("endVisit of " + node.getComponent().getSymbol().get().getFullName());
    //    new Error().printStackTrace();
    instanceSymbolCreator.createInstances(
        (ComponentSymbol) (Log.errorIfNull(node.getComponent().getSymbol().orElse(null)))
    );
  }

  @Override
  public void visit(ASTPort node) {
    ASTType astType = node.getType();
    String typeName = ArcTypePrinter.printTypeWithoutTypeArgumentsAndDimension(astType);

    String name = node.getName().orElse(StringTransformations.uncapitalize(typeName));
    PortSymbol sym = new PortSymbol(name);

    JTypeReference<JTypeSymbol> typeRef = new CommonJTypeReference<JTypeSymbol>(typeName, JTypeSymbol.KIND, currentScope().get());
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

  @Override
  public void visit(de.monticore.lang.montiarc.montiarc._ast.ASTConnector node) {
    String sourceName = node.getSource().toString();

    for (ASTQualifiedName target : node.getTargets()) {
      String targetName = target.toString();

      ConnectorSymbol sym = new ConnectorSymbol(targetName);
      sym.setSource(sourceName);
      sym.setTarget(targetName);

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
    String referencedCompName = ArcTypePrinter
        .printTypeWithoutTypeArgumentsAndDimension(node.getType());

    // String refCompPackage = Names.getQualifier(referencedCompName);
    String simpleCompName = Names.getSimpleName(referencedCompName);

    ComponentSymbolReference componentTypeReference = new ComponentSymbolReference(
        referencedCompName,
        currentScope().get());
    // actual type arguments
    addTypeArgumentsToTypeSymbol(componentTypeReference, node.getType());

    // ref.setPackageName(refCompPackage);

    // TODO internal representation of ValueSymbol ? that was heavily based on CommonValues
    // language and its expressions, but we use JavaDSL.
    List<ValueSymbol<TypeReference<TypeSymbol>>> configArgs = new ArrayList<>();
    for (ASTExpression arg : node.getArguments()) {
      String value = new JavaDSLPrettyPrinter(new IndentPrinter()).prettyprint(arg);
      value = value.replace("\"", "\\\"").replace("\n", "");
      configArgs.add(new ValueSymbol<>(value, Kind.Expression));
    }

    // instances

    if (!node.getInstances().isEmpty()) {
      // create instances of the referenced components.
      for (ASTSubComponentInstance i : node.getInstances()) {
        createInstance(i.getName(), i, componentTypeReference, configArgs, i.getConnectors());
      }
    }
    else {
      // auto instance because instance name is missing
      createInstance(StringTransformations.uncapitalize(simpleCompName), node,
          componentTypeReference, new ArrayList<>(), new ArrayList<>());
    }
    
    
    node.setEnclosingScope(currentScope().get());
  }

  /**
   * Creates the instance and adds it to the symTab.
   */
  private void createInstance(String name, ASTNode node,
      ComponentSymbolReference componentTypeReference,
      List<ValueSymbol<TypeReference<TypeSymbol>>> configArguments,
      List<ASTSimpleConnector> connectors) {
    ComponentInstanceSymbol instance = new ComponentInstanceSymbol(name,
        componentTypeReference);
    configArguments.forEach(v -> instance.addConfigArgument(v));
    // create a subscope for the instance
    addToScopeAndLinkWithNode(instance, node);
    for (ASTSimpleConnector c : connectors) {
      String sourceName = c.getSource().toString();
      for (ASTQualifiedName target : c.getTargets()) {
        String targetName = target.toString();
        ConnectorSymbol sym = new ConnectorSymbol(sourceName + "->" + targetName);
        sym.setSource(sourceName);
        sym.setTarget(targetName);
        addToScope(sym);
      }
    }
    // remove the created instance's scope
    removeCurrentScope();
  }

  @Override
  public void visit(ASTComponent node) {
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
    JavaHelper.addTypeParametersToType(component, node.getHead().getGenericTypeParameters(),
        currentScope().get());

    // parameters
    setParametersOfComponent(component, node.getHead());

    // super component
    if (node.getHead().getSuperComponent().isPresent()) {
      ASTReferenceType superCompRef = node.getHead().getSuperComponent().get();
      String superCompName = ArcTypePrinter.printTypeWithoutTypeArgumentsAndDimension(superCompRef);

      ComponentSymbolReference ref = new ComponentSymbolReference(superCompName,
          currentScope().get());
      ref.setAccessModifier(BasicAccessModifier.PUBLIC);
      // actual type arguments
      addTypeArgumentsToTypeSymbol(ref, superCompRef);

      component.setSuperComponent(Optional.of(ref));
    }

    // stereotype
    if (node.getStereotype().isPresent()) {
      for (ASTStereoValue st : node.getStereotype().get().getValues()) {
        component.addStereotype(st.getName(), st.getValue());
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
    autoConnectionTrafo.transformAtStart(node, component);
  }

  @Override
  public void visit(ASTMontiArcAutoConnect node) {
    autoConnectionTrafo.transform(node, componentStack.peek());
  }

  private void setParametersOfComponent(final ComponentSymbol componentSymbol,
      final ASTComponentHead astMethod) {
    for (ASTParameter astParameter : astMethod.getParameters()) {
      final String paramName = astParameter.getName();
      int dimension = TypesHelper.getArrayDimensionIfArrayOrZero(astParameter.getType());
      JTypeReference<? extends JTypeSymbol> paramTypeSymbol = new JavaTypeSymbolReference(
          ArcTypePrinter.printTypeWithoutTypeArgumentsAndDimension(astParameter
              .getType()), currentScope().get(), dimension);

      addTypeArgumentsToTypeSymbol(paramTypeSymbol, astParameter.getType());
      final JAttributeSymbol parameterSymbol = jSymbolFactory.createFormalParameterSymbol(paramName, (JavaTypeSymbolReference) paramTypeSymbol);
      componentSymbol.addConfigParameter(parameterSymbol);
    }
  }

  private boolean needsInstanceCreation(ASTComponent node, ComponentSymbol symbol) {
    boolean instanceNameGiven = node.getInstanceName().isPresent();
    boolean autoCreationPossible = symbol.getFormalTypeParameters().size() == 0;

    return instanceNameGiven || autoCreationPossible;
  }

  @Override
  public void endVisit(ASTComponent node) {
    ComponentSymbol component = componentStack.pop();
    autoConnectionTrafo.transformAtEnd(node, component);

    removeCurrentScope();

    // for inner components the symbol must be fully created to reference it. Hence, in endVisit we
    // can reference it and put the instance of the inner component into its parent scope.

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
          setActualTypeArguments(refEntry, node.getActualTypeArgument().get().getTypeArguments());
        }

        ComponentInstanceSymbol instanceSymbol = new ComponentInstanceSymbol(instanceName,
            refEntry);
        Log.debug("Created component instance " + instanceSymbol.getName()
                + " referencing component type " + referencedComponentTypeName,
            MontiArcSymbolTableCreator.class.getSimpleName());

        addToScope(instanceSymbol);
      }

      // collect inner components that do not have generic types or a
      // configuration
      if (component.getFormalTypeParameters().isEmpty()
          && component.getConfigParameters().isEmpty()
          && !node.getInstanceName().isPresent()) {
        // Pair<ComponentSymbol, ASTComponent> p = new Pair<>(owningComponent, node);
        // TODO store as inner component?
        // innerComponents.put(component, p);
      }
    }
  }

  // TODO remove after GV's refactoring of such methodology to mc4/types.
  @Deprecated
  private void addTypeArgumentsToTypeSymbol(JTypeReference<? extends JTypeSymbol> typeReference, ASTType astType) {
    if (astType instanceof ASTSimpleReferenceType) {
      ASTSimpleReferenceType astSimpleReferenceType = (ASTSimpleReferenceType) astType;
      if (!astSimpleReferenceType.getTypeArguments().isPresent()) {
        return;
      }
      List<ActualTypeArgument> actualTypeArguments = new ArrayList<>();
      for (ASTTypeArgument astTypeArgument : astSimpleReferenceType.getTypeArguments().get()
          .getTypeArguments()) {
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
                : astWildcardType
                .getUpperBound().get();

            int dimension = TypesHelper.getArrayDimensionIfArrayOrZero(typeBound);
            JTypeReference<? extends JTypeSymbol> typeBoundSymbolReference = new JavaTypeSymbolReference(
                ArcTypePrinter.printTypeWithoutTypeArgumentsAndDimension(typeBound),
                currentScope().get(), dimension);
            // TODO string representation?
            // typeBoundSymbolReference.setStringRepresentation(ArcTypePrinter
            // .printWildcardType(astWildcardType));
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
              ArcTypePrinter.printTypeWithoutTypeArgumentsAndDimension(astTypeNoBound),
              currentScope().get(), dimension);

          // TODO string representation?
          // typeArgumentSymbolReference.setStringRepresentation(TypesPrinter
          // .printType(astTypeNoBound));

          addTypeArgumentsToTypeSymbol(typeArgumentSymbolReference, astTypeNoBound);

          actualTypeArguments.add(new ActualTypeArgument(typeArgumentSymbolReference));
        }
        else {
          Log.error("0xU0401 Unknown type argument " + astTypeArgument + " of type "
              + typeReference);
        }
        typeReference.setActualTypeArguments(actualTypeArguments);
      }
    }
    else if (astType instanceof ASTComplexReferenceType) {
      ASTComplexReferenceType astComplexReferenceType = (ASTComplexReferenceType) astType;
      for (ASTSimpleReferenceType astSimpleReferenceType : astComplexReferenceType
          .getSimpleReferenceTypes()) {
        // TODO
        /* ASTComplexReferenceType represents types like class or interface types which always have
         * ASTSimpleReferenceType as qualification. For example: a.b.c<Arg>.d.e<Arg> */
      }
    }
    else if (astType instanceof ASTComplexArrayType) {
      ASTComplexArrayType astComplexArrayType = (ASTComplexArrayType) astType;
      // references to types with dimension>0, e.g., String[]
      addTypeArgumentsToTypeSymbol(typeReference, astComplexArrayType.getComponentType());
      int dimension = astComplexArrayType.getDimensions();
      typeReference.setDimension(dimension);
    }
  }

  private void setActualTypeArguments(ComponentSymbolReference typeReference,
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
              : astWildcardType
              .getUpperBound().get();
          int dimension = TypesHelper.getArrayDimensionIfArrayOrZero(typeBound);
          JTypeReference<? extends JTypeSymbol> typeBoundSymbolReference = new JavaTypeSymbolReference(
              ArcTypePrinter.printTypeWithoutTypeArgumentsAndDimension(typeBound),
              currentScope().get(), dimension);
          // TODO string representation?
          // typeBoundSymbolReference.setStringRepresentation(ArcTypePrinter
          // .printWildcardType(astWildcardType));
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
            ArcTypePrinter.printTypeWithoutTypeArgumentsAndDimension(astTypeNoBound),
            currentScope().get(), dimension);

        // TODO string representation?
        // typeArgumentSymbolReference.setStringRepresentation(TypesPrinter
        // .printType(astTypeNoBound));

        addTypeArgumentsToTypeSymbol(typeArgumentSymbolReference, astTypeNoBound);

        actualTypeArguments.add(new ActualTypeArgument(typeArgumentSymbolReference));
      }
      else {
        Log.error("0xU0401 Unknown type argument " + astTypeArgument + " of type "
            + typeReference);
      }
    }
    typeReference.setActualTypeArguments(actualTypeArguments);
  }

  // TODO references to component symbols should not differ from JavaTypeSymbolReference?
  @Deprecated
  private void addTypeArgumentsToTypeSymbol(ComponentSymbolReference typeReference,
      ASTType astType) {
    if (astType instanceof ASTSimpleReferenceType) {
      ASTSimpleReferenceType astSimpleReferenceType = (ASTSimpleReferenceType) astType;
      if (!astSimpleReferenceType.getTypeArguments().isPresent()) {
        return;
      }
      setActualTypeArguments(typeReference,
          astSimpleReferenceType.getTypeArguments().get().getTypeArguments());
    }
    else if (astType instanceof ASTComplexReferenceType) {
      ASTComplexReferenceType astComplexReferenceType = (ASTComplexReferenceType) astType;
      for (ASTSimpleReferenceType astSimpleReferenceType : astComplexReferenceType
          .getSimpleReferenceTypes()) {
        // TODO
        /* ASTComplexReferenceType represents types like class or interface types which always have
         * ASTSimpleReferenceType as qualification. For example: a.b.c<Arg>.d.e<Arg> */
      }
    }

  }
}
