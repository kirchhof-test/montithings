<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("comp", "statement", "config", "number", "isPrecondition", "existsHWC")}
<#include "/template/prepostconditions/helper/SpecificPreamble.ftl">
<#assign isLogTracingEnabled = config.getLogTracing().toString() == "ON">

${Utils.printTemplateArguments(comp)}
void
${className}${generics}::resolve (${compname}State${generics} &${Identifier.getStateName()},
${compname}Input${generics} &${Identifier.getInputName()}
<#if !isPrecondition>
    , ${compname}Result${generics} &${Identifier.getResultName()}
</#if>
    , ${compname}State${generics} &${Identifier.getStateName()}__at__pre
)
{
<#if catch.isPresent()>
    ${ComponentHelper.printJavaBlock(catch.get().handler, isLogTracingEnabled, true)}
</#if>
}