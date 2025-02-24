<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("behavior", "comp", "config", "existsHWC")}
<#include "/template/impl/helper/GeneralPreamble.ftl">
<#assign isLogTracingEnabled = config.getLogTracing().toString() == "ON">

${Utils.printTemplateArguments(comp)}
${compname}Result${generics}
${className}${generics}::init${ComponentHelper.getPortSpecificInitBehaviorName(comp, behavior)}
(${compname}Input${generics} ${Identifier.getInputName()})
{
${compname}Result${generics} ${Identifier.getResultName()};
${compname}State${generics} state__at__pre = ${Identifier.getStateName()};
${tc.includeArgs("template.impl.helper.RecorderComputationMeasurementStart", [comp, config])}
${ComponentHelper.printJavaBlock(behavior.getMCJavaBlock(), isLogTracingEnabled)}
${tc.includeArgs("template.impl.helper.RecorderComputationMeasurementEnd", [comp, config])}
<#list ComponentHelper.getPublishedPorts(comp, behavior.getMCJavaBlock()) as port>
    ${Identifier.getResultName()}.set${port.getName()?capitalize}(tl::nullopt);
</#list>
return ${Identifier.getResultName()};
}