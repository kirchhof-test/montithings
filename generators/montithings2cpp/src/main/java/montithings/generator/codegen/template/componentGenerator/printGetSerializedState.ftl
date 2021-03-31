<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("comp","className")}
<#assign Utils = tc.instantiate("montithings.generator.codegen.util.Utils")>
<#assign Identifier = tc.instantiate("montithings.generator.codegen.util.Identifier")>
<#assign generics = Utils.printFormalTypeParameters(comp)>

${Utils.printTemplateArguments(comp)}
json
${className}${Utils.printFormalTypeParameters(comp)}::getSerializedState ()
{
    return ${Identifier.getStateName()}.serializeState ();
}