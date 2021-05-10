<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("comp", "className")}
<#include "/template/state/helper/GeneralPreamble.ftl">

${Utils.printTemplateArguments(comp)}
bool ${className}${generics}::restoreState (std::string content)
{
try
{
json state = json::parse (content);

// set state
<#list ComponentHelper.getFields(comp) as variable>
    ${variable.getName()} = jsonToData${"<"}${ComponentHelper.printCPPTypeName(variable.getType())}${">"}(state["${variable.getName()}"]);
</#list>
this->restoredState = true;
return true;
}
catch (nlohmann::detail::parse_error &error)
{
return false;
}
}