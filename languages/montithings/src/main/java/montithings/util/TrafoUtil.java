package montithings.util;

import arcbasis._ast.*;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedNameBuilder;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import genericarc._ast.ASTArcTypeParameter;
import genericarc._ast.ASTGenericComponentHead;
import montiarc._ast.ASTMACompilationUnit;
import montithings.MontiThingsMill;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static montithings.util.GenericBindingUtil.printSimpleType;

public abstract class TrafoUtil {

    /**
     * Returns the port type of a given port instance
     *
     * @param comp     AST of component which is modified
     * @param portName Qualified name of the port
     * @return ASTMCType or throws an exception if port is not found
     */
    public static ASTMCType getPortTypeByName(ASTMACompilationUnit comp, String portName) throws Exception {
        List<ASTPortDeclaration> sourcePorts = comp.getComponentType().getBody().streamArcElements()
                .filter(el -> el instanceof ASTComponentInterface)
                .map(el -> ((ASTComponentInterface) el).getPortDeclarationList())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Optional<ASTMCType> portType = sourcePorts.stream()
                //TODO: code smell
                .filter(p -> p.getPort(0).getName().equals(portName))
                .map(ASTPortDeclarationTOP::getMCType).findFirst();

        if (!portType.isPresent()) {
            throw new Exception("Port named " + portName + " not found, could not get type.");
        }

        return portType.get();
    }

    /**
     * Returns the component type which declared the given port (part of a connection).
     * If the port is declared locally the name of the given component is returned
     * Otherwise the type is searched in the sub-component instantiations.
     * <p>
     * E.g. v -> sink.value
     * Left hand side is declared locally, right hand side is declared in instance sink.
     * When searching through all component instantiations, sink may resolve to type Sink.
     * <p>
     * This method may return null.
     *
     * @param comp AST of component which contains the connection with the port
     * @param port AST of port which is part of a connection in comp
     * @return String of component type or null
     */
    public static String getPortOwningComponentType(ASTMACompilationUnit comp, ASTPortAccess port) {
        if (!port.getQName().contains(".")) {
            // port is declared locally
            return comp.getComponentType().getName();
        }

        // Searches for the port declaration which is either in the current comp or in one of its subcomponents
        List<ASTArcElement> arcElementList = comp.getComponentType().getBody().getArcElementList();
        for (ASTArcElement element :
                arcElementList) {
            if (element instanceof ASTComponentInstantiation) {
                ASTComponentInstantiation inst = (ASTComponentInstantiation) element;
                for (String name : inst.getInstancesNames()) {
                    if (name.equals(port.getComponent())) {
                        return printSimpleType(inst.getMCType());
                    }
                }
            }
        }

        // TODO avoid returning null
        return null;
    }

    /**
     * Searches in the collection of models for the given name
     *
     * @param models      Collection of AST components where is searched in
     * @param qNameSearch Qualified component name
     * @return AST of searched component
     */
    public static ASTMACompilationUnit getComponentByName(Collection<ASTMACompilationUnit> models, String qNameSearch) {
        for (ASTMACompilationUnit model : models) {
            String qName = model.getPackage().getQName() + "." + model.getComponentType().getName();
            if (qName.equals(qNameSearch)) {
                return model;
            }
        }
        throw new NoSuchElementException("There is no such model named " + qNameSearch);
    }

    /**
     * Capitalizes first character of the given string
     *
     * @param str string to capitalized
     * @return capitalized string
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Replaces dots and applies camelCase
     *
     * @param str string input
     */
    public static String replaceDotsWithCamelCase(String str) {
        if (!str.contains(".")) {
            return str;
        }

        StringBuilder res = new StringBuilder();
        for (String part : str.split("\\.")) {
            res.append(capitalize(part));
        }
        return res.toString();
    }

    /**
     * @param models Collection of AST models where is searched in
     * @param child  AST child component
     * @return collection of parent component names
     */
    public static Collection<String> findParents(Collection<ASTMACompilationUnit> models, ASTMACompilationUnit child) {
        Collection<String> res = new ArrayList<>();
        String name = child.getComponentType().getName();

        for (ASTMACompilationUnit model : models) {
            boolean isParent = model.getComponentType().getSubComponentInstantiations().stream()
                    .map(s -> printSimpleType(s.getMCType()))
                    .filter(Objects::nonNull)
                    .anyMatch(t -> t.equals(name));
            if (isParent) {
                String qName = model.getPackage() + "." + model.getComponentType().getName();
                res.add(qName);
            }
        }

        return res;
    }

    /**
     * Clones ASTMCQualifiedName object which can be used to avoid pass-by-ref situations
     *
     * @param qName ASTMCQualifiedName
     */
    public static ASTMCQualifiedName copyASTMCQualifiedName(ASTMCQualifiedName qName) {
        ASTMCQualifiedNameBuilder qualifiedNameBuilder = MontiThingsMill.mCQualifiedNameBuilder();
        for (String part : qName.getPartsList()) {
            qualifiedNameBuilder.addParts(part);
        }

        return qualifiedNameBuilder.build();
    }

    public static ASTMCQualifiedName getFullyQNameFromImports(File modelPath, ASTMACompilationUnit comp, String typeName) throws Exception {
        ASTMCQualifiedName qNameComp = TrafoUtil.copyASTMCQualifiedName(comp.getPackage());
        qNameComp.addParts(typeName);

        // case 1: it equals comp
        if (comp.getComponentType().getName().equals(typeName)) {
            return qNameComp;
        }

        // case 2: its found within the list of imports
        for (ASTMCImportStatement importStatement : comp.getImportStatementList()) {
            ASTMCQualifiedName mcQualifiedName = importStatement.getMCQualifiedName();

            if (importStatement.isStar()) {
                // check if it is a class diagram
                boolean isCDImport = Files.walk(modelPath.toPath())
                        .filter(Files::isRegularFile)
                        .map(f -> f.getFileName().toString())
                        .filter(f -> f.endsWith(".cd"))
                        .map(f -> f.split("\\.")[0])
                        .anyMatch(f -> f.equals(mcQualifiedName.getBaseName()));

                if (isCDImport) {
                    // the content of a class diagram was imported, skip...
                    continue;
                }

                // e.g. smartHome.openings.*, then mcQualifiedName = smartHome.openings
                // modelPath = ..resources/models/smartHome
                File importStarDir = new File(modelPath, mcQualifiedName.getQName().replace(".", File.separator));
                List<Path> models = Files.walk(importStarDir.toPath())
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());

                for (Path model : models) {
                    String modelName = model.getFileName().toString();
                    if (modelName.contains(".")) {
                        modelName = modelName.split("\\.")[0];

                        if (modelName.equals(typeName)) {
                            ASTMCQualifiedName qName = TrafoUtil.copyASTMCQualifiedName(mcQualifiedName);
                            qName.addParts(typeName);
                            return qName;
                        }
                    }
                }

            } else {
                String baseName = mcQualifiedName.getBaseName();

                if (typeName.equals(baseName)) {
                    return mcQualifiedName;
                }
            }
        }

        // case 3: its located in the same package
        String compDirPath = comp.getPackage().getQName().replace(".", File.separator);
        List<Path> models = Files.walk(modelPath.toPath())
                .filter(Files::isRegularFile)
                .filter(dir -> dir.getParent().toString().endsWith(compDirPath))
                .collect(Collectors.toList());

        for (Path model : models) {
            String modelName = model.getFileName().toString();
            if (modelName.contains(".")) {
                modelName = modelName.split("\\.")[0];

                if (modelName.equals(typeName)) {
                    return qNameComp;
                }
            }
        }

        throw new ClassNotFoundException("Package for " + typeName + " not found.");

    }

    /**
     * Returns instantiations equalling the given typeName
     *
     * @param comp     Component where instantiations are searched in
     * @param typeName String of type
     */
    public static List<ASTComponentInstantiation> getInstantiationsByType(ASTMACompilationUnit comp, String typeName) {
        List<ASTComponentInstantiation> res = new ArrayList<>();

        for (ASTComponentInstantiation subComponentInstantiation : comp.getComponentType().getSubComponentInstantiations()) {
            String subCompType = printSimpleType(subComponentInstantiation.getMCType());
            if (subCompType.equals(typeName)) {
                res.add(subComponentInstantiation);
            }
        }
        return res;
    }

    /**
     * @param comp     AST of model where the instantiation is declared
     * @param compName String of instantiated type
     * @return whether compName is a generic type in comp or not
     */
    public static boolean isGeneric(ASTMACompilationUnit comp, String compName) {
        if (comp.getComponentType().getHead() instanceof ASTGenericComponentHead) {
            List<ASTArcTypeParameter> typeParameters =
                    ((ASTGenericComponentHead) comp.getComponentType().getHead()).getArcTypeParameterList();

            return typeParameters.stream().anyMatch(p -> p.getName().equals(compName));
        }
        return false;
    }

    public static List<String> getInterfaces(ASTMACompilationUnit comp, String compName) {
        List<ASTArcTypeParameter> typeParameters =
                ((ASTGenericComponentHead) comp.getComponentType().getHead()).getArcTypeParameterList();

        return typeParameters.stream()
                .filter(p -> p.getName().equals(compName))
                .map(ASTArcTypeParameter::getUpperBoundList)
                .flatMap(Collection::stream)
                .map(GenericBindingUtil::printSimpleType)
                .collect(Collectors.toList());
    }
}
